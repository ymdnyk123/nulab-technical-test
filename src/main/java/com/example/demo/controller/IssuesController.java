package com.example.demo.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.common.constant.AutoSetting;
import com.example.demo.common.constant.Format;
import com.example.demo.common.constant.ModelAttribute;
import com.example.demo.common.constant.PageId;
import com.example.demo.common.constant.Recording;
import com.example.demo.common.constant.Symbol;
import com.example.demo.common.util.ConversionUtil;
import com.example.demo.dto.CommonDto;
import com.example.demo.dto.IssueDto;
import com.example.demo.dto.IssueTypeDto;
import com.example.demo.dto.IssuesView;
import com.example.demo.dto.PriorityDto;
import com.example.demo.dto.ProjectDto;
import com.example.demo.dto.StatusDto;
import com.example.demo.dto.UserDto;
import com.example.demo.mybatis.model.IssueT01;
import com.example.demo.mybatis.model.IssueT02ActualHours;
import com.example.demo.mybatis.model.IssueTypeT01;
import com.example.demo.mybatis.model.PriorityT01;
import com.example.demo.mybatis.model.ProjectT01;
import com.example.demo.mybatis.model.StatusT01;
import com.example.demo.mybatis.model.UserT01;
import com.example.demo.service.IssueService;
import com.example.demo.service.IssueTypeService;
import com.example.demo.service.PriorityService;
import com.example.demo.service.ProjectService;
import com.example.demo.service.StatusService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping(value="issues")
public class IssuesController {
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	IssueTypeService issueTypeService;

	@Autowired
	StatusService statusService;
	
	@Autowired
	PriorityService priorityService;
	
	@Autowired
	IssueService issueService;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	CommonDto commonDto;
	
	/**
	 * 課題一覧画面初期表示
	 * 
	 * @param model
	 * @param projectId
	 * @return 画面名
	 */
	@RequestMapping(value="init")
	public String init(Model model) {
		
		// ウォッチ中課題リスト取得
		this.issueService.registerWatchingIssues();
		
		// ヘッダ
		model.addAttribute(ModelAttribute.PAGE_HEADER, commonDto.getHeader());
		
		// 本文
		IssuesView issuesView = new IssuesView();
		
		issuesView.setIssueDtoList(this.getIssueList());
		
		// 本文
		model.addAttribute(ModelAttribute.ISSUES_VIEW, issuesView);
		
		return PageId.ISSUES;
	}

	/**
	 * 課題リスト取得
	 * 
	 * @param projectId
	 * @return 課題リスト
	 */
	private List<IssueDto> getIssueList() {
		
		// 課題リスト取得
		List<IssueT01> issueT01List = this.issueService.selectIssueT01List(
				this.commonDto.getMySelf().getId());
		
		if (ObjectUtils.isEmpty(issueT01List)) {
			
			return null;
		}
		
		List<IssueDto> issueDtoList = new ArrayList<IssueDto>();
		
		issueT01List.forEach(item -> issueDtoList.add(modelMapper.map(item, IssueDto.class)));
		
		// プロジェクトリスト取得
		List<ProjectT01> projectT01List = this.projectService.selectProjectListBySpaceKey(
				this.commonDto.getSpaceKey());
		
		// ユーザリスト取得
		List<UserT01> userT01List = this.userService.selectUserT01List();
		
		// 課題タイプリスト取得
		List<IssueTypeT01> issueTypeT01List = this.issueTypeService.selectIssueTypeList();
		
		// 優先度一覧取得
		List<PriorityT01> priorityT01List = this.priorityService.selectPriorityT01();
		
		// ステータス一覧取得
		List<StatusT01> statusT01List = this.statusService.selectStatusT01List();
		
		for (IssueDto issue : issueDtoList) {
			
			// プロジェクト
			if (Objects.nonNull(projectT01List)) {
				
				issue.setProjectDto(
						modelMapper.map(
								projectT01List.stream().filter(
										item -> item.getProjectId().equals(issue.getProjectId())
											).findFirst().orElse(new ProjectT01()),
								ProjectDto.class));
			}
			
			// 課題タイプ
			if (Objects.nonNull(issueTypeT01List)) {
				
				issue.setIssueTypeDto(
					modelMapper.map(
							issueTypeT01List.stream().filter(item ->
								item.getProjectId().equals(issue.getProjectId())
								&& item.getTypeId().equals(issue.getIssueType()))
									.findFirst().orElse(new IssueTypeT01()),
							IssueTypeDto.class));
			}
			
			// 優先度
			if (Objects.nonNull(priorityT01List)) {
				
				issue.setPriorityDto(
						modelMapper.map(
								priorityT01List.stream().filter(
										item -> item.getPriorityId().equals(issue.getPriority()))
											.findFirst().orElse(new PriorityT01())
						, PriorityDto.class));
			}
			
			// ステータス
			if (Objects.nonNull(statusT01List)) {
				
				issue.setStatusDto(
						modelMapper.map(
								statusT01List.stream().filter(item -> 
									item.getProjectId().equals(issue.getProjectId())
									&& item.getStatusId().equals(issue.getStatus()))
									.findFirst().orElse(new StatusT01()),
						StatusDto.class));
			}
			
			// 担当者
			if (Objects.nonNull(userT01List)) {
				
				issue.setAsigneeDto(
						modelMapper.map(
								userT01List.stream().filter(
										item -> item.getUserNumberId().equals(issue.getAsigneeUserNumberId()))
											.findFirst().orElse(new UserT01()),
						UserDto.class));
			}
			
			// 期限
			if (Objects.nonNull(issue.getDueDate())) {
				
				issue.setDueDateDisp(new SimpleDateFormat(Format.DATE).format(issue.getDueDate()));
			}
			
			// 予定時間
			if (Objects.isNull(issue.getEstimatedHours())) {
				
				issue.setEstimatedHoursDisp(Symbol.HYPHEN);
				
			} else {
				
				issue.setEstimatedHoursDisp(new DecimalFormat(Format.HOURS).format(issue.getEstimatedHours()));
			}
			
			// 実績時間（Backlog）
			if (Objects.isNull(issue.getActualHours())) {
				
				issue.setActualHoursDisp(Symbol.HYPHEN);
				
			} else {
				
				issue.setActualHoursDisp(new DecimalFormat(Format.HOURS).format(issue.getActualHours()));
			}
			
			// 実績時間（測定結果）
			List<IssueT02ActualHours> issueT02ActualHoursList =
					this.issueService.getIssueIssueT02ActualHours(issue.getAsigneeUserNumberId(), issue.getIssueId());
			
			if (ObjectUtils.isEmpty(issueT02ActualHoursList)) {	
				
				issue.setActualHoursDispOwn(Symbol.HYPHEN);
				
			} else {
				
				// 実績時間合計
				BigDecimal actualHours = new BigDecimal(0);
				
				for (IssueT02ActualHours issueT02ActualHours : issueT02ActualHoursList) {
					
					actualHours = actualHours.add(issueT02ActualHours.getWorkActualHours());
				}
				
				issue.setActualHoursDispOwn(new DecimalFormat(Format.HOURS).format(actualHours));
			}
		}
		
		return issueDtoList;
	}
	
	/**
	 * 実績自動設定切替え
	 * 
	 * @param model
	 * @param projectId
	 * @return 画面名
	 */
	@RequestMapping(value="switchAutoSetting")
	@ResponseBody
	public String switchAutoSetting(
			Model model,
			@RequestParam(required = true)String issueId,
			@RequestParam(required = true)String isAutoSetting) {
		
		boolean isAutoSettingFlg = false;
		
		if (AutoSetting.ENABLED.equals(isAutoSetting)) {
			
			isAutoSettingFlg = true;
		}

		if (Objects.isNull(ConversionUtil.toLong(issueId))) {
			
			return "";
		}
		
		this.issueService.switchAutoSetting(
				this.commonDto.getMySelf().getId(),
				ConversionUtil.toLong(issueId),
				isAutoSettingFlg,
				new Date());
		
		return "";
	}
	
	/**
	 * 記録切替え
	 * 
	 * @param model
	 * @param projectId
	 * @return 画面名
	 */
	@RequestMapping(value="switchRecording")
	@ResponseBody
	public String switchRecording(
			Model model,
			@RequestParam(required = true)String issueId,
			@RequestParam(required = true)String isRecording) {
		
		boolean isRecordingFlg = false;
		
		if (Recording.ENABLED.equals(isRecording)) {
			
			isRecordingFlg = true;
		}

		if (Objects.isNull(ConversionUtil.toLong(issueId))) {
			
			return "";
		}
		
		// 現在日時
		Date nowDate = new Date();
		
		// 記録状態更新
		this.issueService.switchRecording(
				this.commonDto.getMySelf().getId(),
				ConversionUtil.toLong(issueId),
				isRecordingFlg,
				nowDate);
		
		return "";
	}
}