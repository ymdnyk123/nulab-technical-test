
package com.example.demo.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.common.constant.Format;
import com.example.demo.common.util.ConversionUtil;
import com.example.demo.common.util.DataSettingUtil;
import com.example.demo.dto.CommonDto;
import com.example.demo.mybatis.mapper.IssueT01Mapper;
import com.example.demo.mybatis.mapper.IssueT02ActualHoursMapper;
import com.example.demo.mybatis.model.IssueT01;
import com.example.demo.mybatis.model.IssueT01Example;
import com.example.demo.mybatis.model.IssueT01Key;
import com.example.demo.mybatis.model.IssueT02ActualHours;
import com.example.demo.mybatis.model.IssueT02ActualHoursExample;
import com.nulabinc.backlog4j.Issue;
import com.nulabinc.backlog4j.ResponseList;
import com.nulabinc.backlog4j.Watch;
import com.nulabinc.backlog4j.api.option.UpdateIssueParams;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IssueServiceImpl implements IssueService {

	@Autowired
	CommonDto commonDto;
	
	@Autowired
	IssueT01Mapper issueT01Mapper;
	
	@Autowired
	IssueT02ActualHoursMapper issueT02ActualHoursMapper;
	
	/**
	 * 課題登録（更新）（ウォッチ、課題、ユーザ）
	 * 
	 * <br>
	 * 10秒周期実行
	 */
	@Scheduled(fixedDelay=10000)
	@Override
	public void registerWatchingIssues() {
		
		if (ObjectUtils.isEmpty(this.commonDto.getMySelf())) {
			
			return;
		}
		
		// ウォッチ一覧の取得
		ResponseList<Watch> watches = this.commonDto.getBacklogClient().getUserWatches(
				this.commonDto.getMySelf().getId());
		
		if (ObjectUtils.isEmpty(watches)) {
			
			return;
		}
		
		// 現在日時取得
		Date nowDate = new Date();
		
		// 課題状態監視
		this.monitorIssues(watches, nowDate);
		
		watches.stream().forEach(watch -> {
			
			if (ObjectUtils.isEmpty(watch.getIssue())) {
				
				return;
			}
			
			// 課題登録
			boolean isInsert = this.registerIssue(this.commonDto.getMySelf().getId(), watch.getIssue(), nowDate);
			
			if (isInsert) {
				
				if (watch.getIssue().getStatus().getId() == Issue.StatusType.InProgress.getIntValue()) {
					
					// 記録開始
					this.startRecording(watch.getIssue(), nowDate);
				}
			}
		});
	}
	
	/**
	 * 課題監視
	 * 
	 * @param lastIssue
	 * @param watches
	 * @param nowDate
	 */
	private void monitorIssues(ResponseList<Watch> watches, Date nowDate) {
		
		// ウォッチ中課題リスト取得（前回）
		List<IssueT01> lastIssueT01List = this.selectIssueT01ListByAsignee(
				this.commonDto.getMySelf().getId());
		
		if (ObjectUtils.isEmpty(lastIssueT01List)) {
			
			return;
		}
		
		lastIssueT01List.stream().forEach(lastIssueT01 -> {
			
			// 課題ID、担当者でフィルター
			Optional<Watch> watch = watches.stream()
					.filter(item -> lastIssueT01.getIssueId().equals(item.getIssue().getId()))
					.filter(item -> lastIssueT01.getAsigneeUserNumberId().equals(item.getIssue().getAssignee().getId()))
					.findFirst();
			
			if (!watch.isPresent()) {
				
				// 記録停止
				this.stopRecording(lastIssueT01.getAsigneeUserNumberId(), lastIssueT01.getIssueId(), nowDate);
				
				// 課題更新(API)
				this.patchIssue(this.commonDto.getMySelf().getId(), lastIssueT01);
				
				return;
			}
			
			Issue thisIssue = watch.get().getIssue();
			
			// ステータス変化チェック
			if (!lastIssueT01.getStatus().equals(thisIssue.getStatus().getId())) {
				
				if (Issue.StatusType.InProgress.getIntValue() == thisIssue.getStatus().getId()) {
					
					// 記録開始
					this.startRecording(thisIssue, nowDate);
					
				} else {
					
					// 記録停止
					this.stopRecording(thisIssue, nowDate);
					
					// 課題更新（API）
					this.patchIssue(this.commonDto.getMySelf().getId(), lastIssueT01);
				}
			}
		});
	}
	
	/**
	 * 課題実績登録（記録開始）
	 * 
	 * @param issue
	 * @param nowDate
	 */
	private void startRecording(Issue issue, Date nowDate) {
		
		IssueT02ActualHours target = new IssueT02ActualHours();
		
		// 担当ユーザID
		target.setAssigneeUserId(issue.getAssignee().getId());
		
		// 課題ID（主キー）
		target.setIssueId(issue.getId());
		
		// 枝番（主キー）
		IssueT02ActualHours latest = this.getLatestIssueIssueT02ActualHours(issue.getAssignee().getId(), issue.getId());
		
		if (ObjectUtils.isEmpty(latest)) {
			
			target.setBranchNo(1L);
			
		} else {
			
			target.setBranchNo(latest.getBranchNo() + 1);
		}
		
		// 開始日時
		target.setWorkStartTime(issue.getUpdated());
		
		// 登録日時
		target.setInsertDatetime(nowDate);
		
		this.issueT02ActualHoursMapper.insertSelective(target);
	}
	
	/**
	 * 課題実績更新（記録停止）
	 * 
	 * @param issue
	 * @param nowDate
	 */
	private void stopRecording(Issue issue, Date nowDate) {
		
		IssueT02ActualHours latest = this.getLatestIssueIssueT02ActualHours(issue.getAssignee().getId(), issue.getId());
		
		if (ObjectUtils.isEmpty(latest)) {
			
			return;
		}
		
		if (Objects.nonNull(latest.getWorkEndTime())) {
			
			return;
		}
		
		IssueT02ActualHours target = new IssueT02ActualHours();
		
		// 担当ユーザID
		target.setAssigneeUserId(latest.getAssigneeUserId());
		
		// 課題ID
		target.setIssueId(latest.getIssueId());
		
		// 枝番
		target.setBranchNo(latest.getBranchNo());
		
		if (ObjectUtils.isEmpty(issue.getUpdated())) {
			
			target.setWorkEndTime(nowDate);
			
		} else {
			
			target.setWorkEndTime(issue.getUpdated());
		}
		
		// 開始時間
		LocalDateTime startDate = ConversionUtil.toLocalDateTime(latest.getWorkStartTime());
		
		// 終了時間
		LocalDateTime endDate = ConversionUtil.toLocalDateTime(target.getWorkEndTime());
		
		// 作業実績
		target.setWorkActualHours(DataSettingUtil.getActualHours(startDate, endDate));
		
		this.issueT02ActualHoursMapper.updateByPrimaryKeySelective(target);
	}
	
	/**
	 * 課題更新（API）
	 * 
	 * @param userId
	 * @param issueId
	 * @param issueT01
	 */
	private void patchIssue(long userId, IssueT01 issueT01) {
		
		if (issueT01.getAsigneeUserNumberId() != userId) {
			
			return;
		}
		
		if (!issueT01.getIsAutoSetting()) {
			
			return;
		}
		
		// 実績時間取得
		BigDecimal actualHours = this.getActualHours(issueT01.getAsigneeUserNumberId(), issueT01.getIssueId());
		
		if (ObjectUtils.isEmpty(actualHours)) {
			
			return;
		}
		
		// 実績時間
		UpdateIssueParams params = new UpdateIssueParams(issueT01.getIssueId()).actualHours(actualHours);
		
		// 課題更新
		this.commonDto.getBacklogClient().updateIssue(params);
		
		log.info("update issue[{}] actual hours[{}]", issueT01.getIssueId(), actualHours);
	}
	
	/**
	 * 実績時間取得
	 * 
	 * @param asigneeUserId
	 * @param issueId
	 * @return 実績時間（null：更新なし）
	 */
	private BigDecimal getActualHours(long asigneeUserId, long issueId) {
		
		// 実績取得
		List<IssueT02ActualHours> issueT02ActualHoursList = this.getIssueIssueT02ActualHours(asigneeUserId, issueId);
		
		// 実績時間合計
		BigDecimal actualHours = new BigDecimal(0);
		
		for (IssueT02ActualHours issueT02ActualHours : issueT02ActualHoursList) {
			
			actualHours = actualHours.add(issueT02ActualHours.getWorkActualHours());
		}
		
		// 更新対象課題取得
		Issue issue = this.commonDto.getBacklogClient().getIssue(issueId);
		
		if (ObjectUtils.isEmpty(issue)) {
			
			return null;
		}
		
		if (ObjectUtils.isEmpty(issue.getActualHours())) {
			
			return null;
		}
		
		String text1 = new DecimalFormat(Format.HOURS).format(actualHours);
		
		String text2 = new DecimalFormat(Format.HOURS).format(issue.getActualHours());
		
		// 変化有無
		if (text1.equals(text2)) {
			
			return null;
		}
		
		return actualHours;
	}
	
	/**
	 * 記録終了
	 * 
	 * @param asigneeUserId
	 * @param issue
	 * @param nowDate
	 */
	private void stopRecording(long asigneeUserId, long issueId, Date nowDate) {
		
		// 最新作業実績取得
		IssueT02ActualHours latest = this.getLatestIssueIssueT02ActualHours(asigneeUserId, issueId);
		
		if (ObjectUtils.isEmpty(latest)) {
			
			return;
		}
		
		if (Objects.nonNull(latest.getWorkEndTime())) {
			
			return;
		}
		
		IssueT02ActualHours target = new IssueT02ActualHours();
		
		// 担当ユーザ
		target.setAssigneeUserId(asigneeUserId);
		
		// 課題ID
		target.setIssueId(latest.getIssueId());
		
		// 枝番
		target.setBranchNo(latest.getBranchNo());
		
		// 終了日時
		target.setWorkEndTime(nowDate);
		
		// 開始時間
		LocalDateTime startDate = ConversionUtil.toLocalDateTime(latest.getWorkStartTime());
		
		// 終了時間
		LocalDateTime endDate = ConversionUtil.toLocalDateTime(target.getWorkEndTime());
		
		// 作業実績
		target.setWorkActualHours(DataSettingUtil.getActualHours(startDate, endDate));
		
		this.issueT02ActualHoursMapper.updateByPrimaryKeySelective(target);
	}
	
	/**
	 * 課題実績登録（記録開始）
	 * 
	 * @param issue
	 * @param nowDate
	 */
	private void startRecording(long asigneeUserId, long issueId, Date nowDate) {
		
		// 最新作業実績取得
		IssueT02ActualHours latest = this.getLatestIssueIssueT02ActualHours(asigneeUserId, issueId);
		
		IssueT02ActualHours target = new IssueT02ActualHours();
		
		// 担当ユーザ
		target.setAssigneeUserId(asigneeUserId);
		
		// 課題ID
		target.setIssueId(issueId);
		
		// 枝番
		if (ObjectUtils.isEmpty(latest)) {
			
			target.setBranchNo(1L);
			
		} else {
			
			target.setBranchNo(latest.getBranchNo() + 1);
		}
		
		// 開始日時
		target.setWorkStartTime(nowDate);
		
		// 登録日時
		target.setInsertDatetime(nowDate);
		
		this.issueT02ActualHoursMapper.insertSelective(target);
	}
	
	/**
	 * 最新課題実績取得
	 * 
	 * @param asigneeUserId
	 * @param issueId
	 * @return 最新課題実績
	 */
	@Override
	public IssueT02ActualHours getLatestIssueIssueT02ActualHours(long asigneeUserId, long issueId) {
		
		List<IssueT02ActualHours> issueT02ActualHoursList = this.getIssueIssueT02ActualHours(asigneeUserId, issueId);
		
		if (ObjectUtils.isEmpty(issueT02ActualHoursList)) {
			
			return null;
			
		} else {
			
			return issueT02ActualHoursList.stream().max(
					Comparator.comparing(IssueT02ActualHours::getBranchNo)).get();
		}
	}
	
	/**
	 * 課題枝番リスト取得
	 * 
	 * @param asigneeUserId
	 * @param issueId
	 * @return 課題枝番リスト
	 */
	@Override
	public List<IssueT02ActualHours> getIssueIssueT02ActualHours(long asigneeUserId, long issueId) {
		
		IssueT02ActualHoursExample example = new IssueT02ActualHoursExample();
		
		IssueT02ActualHoursExample.Criteria criteria = example.createCriteria();
		
		// 担当者
		criteria.andAssigneeUserIdEqualTo(asigneeUserId);
		
		// 課題ID
		criteria.andIssueIdEqualTo(issueId);
		
		return this.issueT02ActualHoursMapper.selectByExample(example);
	}
	
	/**
	 * 課題登録（更新）
	 * 
	 * @param watchUserId
	 * @param issue
	 * @param nowDate
	 */
	@Override
	@Transactional
	public boolean registerIssue(long watchUserId, Issue issue, Date nowDate) {
		
		IssueT01Key key = new IssueT01Key();
		
		key.setWatchUserId(watchUserId);
		
		key.setIssueId(issue.getId());
		
		IssueT01 issueT01 = issueT01Mapper.selectByPrimaryKey(key);
		
		if (ObjectUtils.isEmpty(issueT01)) {
			
			// 登録
			this.insertIssueT01(watchUserId, issue, nowDate);
			
			return true;
			
		} else {
			
			// 更新
			this.updateIssueT01(watchUserId, issue, nowDate, issueT01);
			
			return false;
		}
	}

	/**
	 * 課題登録
	 * 
	 * @param watchUserId
	 * @param issue
	 * @param nowDate
	 */
	private void insertIssueT01(long watchUserId, Issue issue, Date nowDate) {
		
		IssueT01 target = new IssueT01();
		
		// ウォッチユーザID（主キー）
		target.setWatchUserId(watchUserId);
		
		// 課題ID（主キー）
		target.setIssueId(issue.getId());
		
		// 課題キー
		target.setIssueKey(issue.getIssueKey());
		
		// プロジェクトID
		target.setProjectId(issue.getProjectId());
		
		// 親課題ID
		target.setParentIssueId(issue.getParentIssueId());
		
		// タイプ
		target.setIssueType(issue.getIssueType().getId());
		
		// サマリ
		target.setSummary(issue.getSummary());
		
		// 優先度
		target.setPriority(issue.getPriority().getId());
		
		// ステータス
		target.setStatus(issue.getStatus().getStatusType().getIntValue());
		
		// 予定時間
		if (!ObjectUtils.isEmpty(issue.getEstimatedHours())) {
			target.setEstimatedHours(issue.getEstimatedHours().shortValue());
		}
		
		// 実績時間
		if (!ObjectUtils.isEmpty(issue.getActualHours())) {
			target.setActualHours(issue.getActualHours().shortValue());
		}
		
		// 期限
		target.setDueDate(issue.getDueDate());
		
		// 担当者ID
		if (!ObjectUtils.isEmpty(issue.getAssignee())) {
			
			target.setAsigneeUserNumberId(issue.getAssignee().getId());
		}
		
		// 自動設定
		target.setIsAutoSetting(false);
		
		// 記録
		if (target.getStatus().equals(Issue.StatusType.InProgress.getIntValue())) {
			
			target.setIsRecording(true);
			
		} else {
			
			target.setIsRecording(false);
		}
		
		// 登録日時
		target.setInsertDatetime(nowDate);
		
		issueT01Mapper.insertSelective(target);
	}
	
	/**
	 * 課題更新
	 * 
	 * @param watchUserId
	 * @param issue
	 * @param nowDate
	 * @param issueT01
	 */
	private void updateIssueT01(long watchUserId, Issue issue, Date nowDate, IssueT01 issueT01) {
		
		IssueT01 target = new IssueT01();
		
		// ウォッチユーザID（主キー）
		target.setWatchUserId(watchUserId);
		
		// 課題ID（主キー）
		target.setIssueId(issueT01.getIssueId());
		
		// 親課題ID
		target.setParentIssueId(DataSettingUtil.getUpdatedData(issueT01.getParentIssueId(), issue.getParentIssueId()));
		
		// タイプ
		target.setIssueType(DataSettingUtil.getUpdatedData(issueT01.getParentIssueId(), issue.getParentIssueId()));
		
		// サマリ
		target.setSummary(DataSettingUtil.getUpdatedData(issueT01.getSummary(), issue.getSummary()));
		
		// 優先度
		target.setPriority(DataSettingUtil.getUpdatedData(issueT01.getPriority(), issue.getPriority().getId()));
		
		// 状態
		target.setStatus(DataSettingUtil.getUpdatedData(issueT01.getStatus(), issue.getStatus().getId()));
		
		// 予定時間
		if (issue.getEstimatedHours() == null) {
			
			target.setEstimatedHours(DataSettingUtil.getUpdatedData(issueT01.getEstimatedHours(), null));
			
		} else {
			
			target.setEstimatedHours(
					DataSettingUtil.getUpdatedData(
							issueT01.getEstimatedHours(), issue.getEstimatedHours().shortValue()));
		}
		
		// 実績時間
		if (issue.getActualHours() == null) {
			
			target.setActualHours(DataSettingUtil.getUpdatedData(issueT01.getActualHours(), null));
			
		} else {
			
			target.setActualHours(
					DataSettingUtil.getUpdatedData(issueT01.getActualHours(), issue.getActualHours().shortValue()));
		}
		
		// 期限
		target.setDueDate(DataSettingUtil.getUpdatedData(issueT01.getDueDate(), issue.getDueDate()));
		
		// 担当者ID
		if (!ObjectUtils.isEmpty(issue.getAssignee())) {
			
			target.setAsigneeUserNumberId(
					DataSettingUtil.getUpdatedData(issueT01.getParentIssueId(), issue.getParentIssueId()));
		}
		
		// 更新日時
		target.setUpdateDatetime(nowDate);
		
		// 記録
		if (Objects.nonNull(target.getStatus())) {
			
			if (target.getStatus().equals(Issue.StatusType.InProgress.getIntValue())) {
				
				target.setIsRecording(true);
				
			} else {
				
				target.setIsRecording(false);
			}
		} else {
			
			if (issueT01.getStatus().equals(Issue.StatusType.InProgress.getIntValue())) {
				
				target.setIsRecording(true);
				
			} else {
				
				target.setIsRecording(false);
			}
		}
		
		if (Objects.nonNull(target.getParentIssueId())
				|| Objects.nonNull(target.getIssueType())
				|| Objects.nonNull(target.getSummary())
				|| Objects.nonNull(target.getPriority())
				|| Objects.nonNull(target.getStatus())
				|| Objects.nonNull(target.getEstimatedHours())
				|| Objects.nonNull(target.getActualHours())
				|| Objects.nonNull(target.getDueDate())
				|| Objects.nonNull(target.getAsigneeUserNumberId())) {
			
			this.issueT01Mapper.updateByPrimaryKeySelective(target);
		}
	}
	
	/**
	 * 課題リスト取得（担当者）
	 * 
	 * @param userId
	 */
	private List<IssueT01> selectIssueT01ListByAsignee(long userId) {
		
		IssueT01Example example = new IssueT01Example();
		
		IssueT01Example.Criteria criteria = example.createCriteria();
		
		// ウォッチユーザID
		criteria.andWatchUserIdEqualTo(userId);
		
		// 担当者
		criteria.andAsigneeUserNumberIdEqualTo(userId);
		
		return this.issueT01Mapper.selectByExample(example);
	}
	
	/**
	 * ウォッチ課題リスト取得表示
	 * 
	 * @param userId
	 */
	@Override
	public List<IssueT01> selectIssueT01List(long userId) {
		
		IssueT01Example example = new IssueT01Example();
		
		IssueT01Example.Criteria criteria = example.createCriteria();
		
		// ウォッチユーザID
		criteria.andWatchUserIdEqualTo(userId);
		
		return this.issueT01Mapper.selectByExample(example);
	}
	
	/**
	 * 課題（自動設定）切り替え
	 * 
	 * @param watchUserId
	 * @param issueId
	 * @param isAutoSetting
	 * @param nowDate
	 */
	@Override
	public void switchAutoSetting(long watchUserId, long issueId, boolean isAutoSetting, Date nowDate) {
		
		// 課題（自動設定）更新
		this.updateIsAutoSetting(watchUserId, issueId, isAutoSetting, nowDate);
	}
	
	/**
	 * 課題（自動設定）更新
	 * 
	 * @param watchUserId
	 * @param issueId
	 * @param isAutoSetting
	 * @param nowDate
	 */
	private void updateIsAutoSetting(long watchUserId, long issueId, boolean isAutoSetting, Date nowDate) {
		
		IssueT01 target = new IssueT01();
		
		// ウォッチユーザID（主キー）
		target.setWatchUserId(watchUserId);
		
		// 課題ID（主キー）
		target.setIssueId(issueId);
		
		// 自動設定
		target.setIsAutoSetting(isAutoSetting);
		
		// 更新日時
		target.setUpdateDatetime(nowDate);
		
		this.issueT01Mapper.updateByPrimaryKeySelective(target);
	}
	
	/**
	 * 課題（記録状態）切り替え
	 * 
	 * @param watchUserId
	 * @param issueId
	 * @param isRecording
	 * @param nowDate
	 */
	@Override
	public void switchRecording(long watchUserId, long issueId, boolean isRecording, Date nowDate) {
		
		// 課題（自動設定）更新
		this.updateIsRecording(watchUserId, issueId, isRecording, nowDate);
		
		if (isRecording) {
			
			// 記録開始
			this.startRecording(watchUserId, issueId, nowDate);
			
		} else {
			
			// 記録停止
			this.stopRecording(watchUserId, issueId, nowDate);
			
			IssueT01 issueT01 = this.selectIssueT01ByPrimaryKey(watchUserId, issueId);
			
			if (ObjectUtils.isEmpty(issueT01)) {
				
				return;
			}
			
			// 課題更新
			this.patchIssue(watchUserId, issueT01);
		}
	}
	
	/**
	 * 課題取得（主キー）
	 * 
	 * @param watchUserId
	 * @param issueId
	 * @return
	 */
	private IssueT01 selectIssueT01ByPrimaryKey(long watchUserId, long issueId) {
		
		IssueT01Key key = new IssueT01Key();
		
		key.setWatchUserId(watchUserId);
		
		key.setIssueId(issueId);
		
		return this.issueT01Mapper.selectByPrimaryKey(key);
	}
	
	/**
	 * 課題（記録中）更新
	 * 
	 * @param watchUserId
	 * @param issueId
	 * @param isRecording
	 * @param nowDate
	 */
	private void updateIsRecording(long watchUserId, long issueId, boolean isRecording, Date nowDate) {
		
		IssueT01 target = new IssueT01();
		
		// ウォッチユーザID（主キー）
		target.setWatchUserId(watchUserId);
		
		// 課題ID（主キー）
		target.setIssueId(issueId);
		
		// 記録中
		target.setIsRecording(isRecording);
		
		// 更新日時
		target.setUpdateDatetime(nowDate);
		
		this.issueT01Mapper.updateByPrimaryKeySelective(target);
	}
}