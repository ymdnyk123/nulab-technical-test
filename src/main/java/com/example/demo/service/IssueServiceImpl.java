
package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.constant.Time;
import com.example.demo.common.util.ConversionUtil;
import com.example.demo.mybatis.mapper.WorkHoursMapper;
import com.example.demo.mybatis.model.WorkHours;
import com.example.demo.mybatis.model.WorkHoursExample;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IssueServiceImpl implements IssueService {
	
	private final WorkHoursMapper workHoursMapper;
	
	@Autowired
	public IssueServiceImpl(WorkHoursMapper workHoursMapper) {
		
		this.workHoursMapper = workHoursMapper;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<WorkHours> select(Long userId, Long issueId) {
		
		// 課題実績リスト取得
		return this.selectWorkHoursList(userId, issueId);
	}
	
	/**
	 * 作業実績リスト取得
	 * 
	 * @param userId
	 * @param issueId
	 * @return 作業実績リスト
	 */
	private List<WorkHours> selectWorkHoursList(Long userId, Long issueId) {
		
		WorkHoursExample example = new WorkHoursExample();
		
		WorkHoursExample.Criteria criteria = example.createCriteria();
		
		// ユーザID
		criteria.andUserIdEqualTo(userId);
		
		// 課題ID
		criteria.andIssueIdEqualTo(issueId);
		
		return this.workHoursMapper.selectByExample(example);
	}

	/**
	 * 作業実績登録
	 * 
	 * @param userId
	 * @param issueId
	 * @param isWorking
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void register(Long userId, Long issueId, Boolean isWorking) {
		
		
		if (isWorking) {
			
			// 作業開始
			this.insertWorkHours(userId, issueId);
			
		} else {
			
			// 作業終了
			this.updateWorkHours(userId, issueId);
		}
	}
	
	/**
	 * 作業実績登録（作業開始）
	 * 
	 * @param userId
	 * @param issueId
	 */
	private void insertWorkHours(Long userId, Long issueId) {
		
		// 登録データ
		WorkHours workHours = new WorkHours();
		
		// 現在日時
		Date nowDate = new Date();
		
		// ユーザID
		workHours.setUserId(userId);
		
		// 課題ID
		workHours.setIssueId(issueId);
		
		// シーケンス
		workHours.setSequence(this.getSequence(userId, issueId) + 1L);
		
		// 開始日時
		workHours.setStartTime(nowDate);
		
		// 登録日時
		workHours.setInsertDatetime(nowDate);
		
		// 登録
		int cnt = this.workHoursMapper.insertSelective(workHours);
		
		log.info("作業開始 登録[{}] uid[{}] iid[{}] seq[{}]",
				cnt, workHours.getUserId(), workHours.getIssueId(), workHours.getSequence());
	}
	
	/**
	 * 作業実績更新（作業終了）
	 * 
	 * @param userId
	 * @param issueId
	 */
	private void updateWorkHours(Long userId, Long issueId) {
		
		// 更新データ
		WorkHours workHours = new WorkHours();
		
		// 現在日時
		Date nowDate = new Date();
		
		// ユーザID
		workHours.setUserId(userId);
		
		// 課題ID
		workHours.setIssueId(issueId);
		
		// シーケンス
		workHours.setSequence(this.getSequence(userId, issueId));
		
		// 終了日時
		workHours.setEndTime(nowDate);
		
		// 実績時間
		workHours.setActualHours(this.getActualHours(userId, issueId, nowDate));
		
		// 更新日時
		workHours.setUpdateDatetime(nowDate);
		
		// 更新
		int cnt = this.workHoursMapper.updateByPrimaryKeySelective(workHours);
		
		log.info("作業終了 更新[{}] uid[{}] iid[{}] seq[{}]",
				cnt, workHours.getUserId(), workHours.getIssueId(), workHours.getSequence());
	}
	
	/**
	 * シーケンス取得
	 * 
	 * @param userId
	 * @param issueId
	 * @return シーケンス
	 */
	private long getSequence(Long userId, Long issueId) {
		
		// 最新作業実績取得
		WorkHours latest = this.selectLatestWorkHoursList(userId, issueId);
		
		if (Objects.isNull(latest)) {
			
			return 0L;
		}
		
		return latest.getSequence();
	}
	
	/**
	 * 作業実績取得（最新）
	 * @param userId
	 * @param issueId
	 * @return
	 */
	private WorkHours selectLatestWorkHoursList(Long userId, Long issueId) {
		
		// 作業実績リスト取得
		List<WorkHours> workHoursList = this.selectWorkHoursList(userId, issueId);
		
		if (ObjectUtils.isEmpty(workHoursList)) {
			
			return null;
			
		} else {
			
			// シーケンスが最大の作業実績取得
			return workHoursList.stream().max(Comparator.comparing(WorkHours::getSequence)).get();
		}
	}
	
	/**
	 * 実績時間取得
	 * 
	 * @param userId
	 * @param issueId
	 * @param nowDate
	 * @return
	 */
	private BigDecimal getActualHours(Long userId, Long issueId, Date nowDate) {
		
		// 最新作業実績取得
		WorkHours workHours = this.selectLatestWorkHoursList(userId, issueId);
		
		if (ObjectUtils.isEmpty(workHours)) {
			
			return null;
		}
		
		// 実績時間算出
		return this.calcHours(workHours.getStartTime(), nowDate);
	}
	
	/**
	 * 実績時間算出
	 * 
	 * @param paramStart
	 * @param paramEnd
	 * @return
	 */
	private BigDecimal calcHours(Date paramStart, Date paramEnd) {
		
		// 開始時間
		LocalDateTime start = ConversionUtil.toLocalDateTime(paramStart);
		
		// 終了時間
		LocalDateTime end = ConversionUtil.toLocalDateTime(paramEnd);
		
		Duration duration = Duration.between(start, end);
		
		if (duration.isZero()) {
			
			return BigDecimal.valueOf(0);
		}
		
		// 分取得
		BigDecimal min = new BigDecimal(duration.toMinutes());
		
		// 変換（時）
		return min.divide(new BigDecimal(Time.MINUTES_PER_HOUR), 3, RoundingMode.HALF_UP);
	}
	
	/**
	 * 作業実績削除
	 * 
	 * @param userId
	 * @param issueId
	 */
	@Transactional
	@Override
	public void delete(Long userId, Long issueId) {
		
		WorkHoursExample example = new WorkHoursExample();
		
		WorkHoursExample.Criteria criteria = example.createCriteria();
		
		// ユーザID
		criteria.andUserIdEqualTo(userId);
		
		// 課題ID
		criteria.andIssueIdEqualTo(issueId);
		
		// 削除
		int cnt = this.workHoursMapper.deleteByExample(example);
		
		log.info("実績削除 削除[{}] uid[{}] iid[{}]", cnt, userId, issueId);
	}
}