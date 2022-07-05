package com.example.demo.controller;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.common.constant.AttributeName;
import com.example.demo.common.constant.Format;
import com.example.demo.common.constant.PageId;
import com.example.demo.common.constant.Symbol;
import com.example.demo.common.util.ConversionUtil;
import com.example.demo.dto.IssueRow;
import com.example.demo.dto.IssueTypeDisp;
import com.example.demo.dto.PriorityDisp;
import com.example.demo.dto.ProjectDisp;
import com.example.demo.dto.RequestParamDelete;
import com.example.demo.dto.RequestParamRegister;
import com.example.demo.dto.RequestParamSend;
import com.example.demo.dto.StatusDisp;
import com.example.demo.dto.UserDisp;
import com.example.demo.mybatis.model.WorkHours;
import com.example.demo.service.BacklogClientService;
import com.example.demo.service.WorkHoursService;
import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.Issue;
import com.nulabinc.backlog4j.IssueType;
import com.nulabinc.backlog4j.Priority;
import com.nulabinc.backlog4j.Project;
import com.nulabinc.backlog4j.ResponseList;
import com.nulabinc.backlog4j.Space;
import com.nulabinc.backlog4j.Status;
import com.nulabinc.backlog4j.User;
import com.nulabinc.backlog4j.Watch;
import com.nulabinc.backlog4j.api.option.UpdateIssueParams;

/**
 * 課題コントローラ
 */
@Controller
public class IssuesController {

	private final OAuth2AuthorizedClientService authorizedClientService;
	
	private final BacklogClientService backlogClientService;
	
	private final WorkHoursService workHoursService;
	
	@Autowired
	public IssuesController(
			OAuth2AuthorizedClientService authorizedClientService, 
			BacklogClientService backlogClientService,
			WorkHoursService workHoursService) {
		
		this.authorizedClientService = authorizedClientService;
		this.backlogClientService = backlogClientService;
		this.workHoursService = workHoursService;
	}
	
	/**
	 * 初期表示処理
	 * 
	 * @param authentication
	 * @param model
	 * @return
	 */
	@GetMapping("/")
	public String init(OAuth2AuthenticationToken authentication, Model model) throws MalformedURLException {
		
		// 画面に表示するために、OAuth2AuthorizedClientService経由で認可済みのクライアント情報を取得しModelに格納
		model.addAttribute(AttributeName.AUTH_CLIENT, this.getAuthorizedClient(authentication));
		
		OAuth2AuthorizedClient authorizedClient = this.getAuthorizedClient(authentication);
		
		if (Objects.isNull(authorizedClient)) {
			
			return PageId.LOGIN;
		}
		
		// BacklogClient取得
		BacklogClient backlogClient = this.backlogClientService.getClient(authorizedClient);
		
		// スペース情報取得
		Space space = backlogClient.getSpace();
		
		model.addAttribute(AttributeName.SPACE_NAME, space.getName());
		
		// プロジェクトリスト取得
		ResponseList<Project> projectList = backlogClient.getProjects();
		
		// 認証ユーザID
		Long myUserId = this.getMyUserId(authentication);
		
		// ウォッチ取得
		ResponseList<Watch> userWatches = backlogClient.getUserWatches(myUserId);
		
		// ウォッチ課題リスト取得
		List<IssueRow> issueRowList = this.getIssueRowList(userWatches, projectList, myUserId);
		
		model.addAttribute(AttributeName.ISSUE_ROW_LIST, issueRowList);
		
		return PageId.ISSUES;
	}
	
	/**
	 * 登録（作業開始）
	 * 
	 * @param authentication
	 * @param issueId
	 * @param isStart
	 * @return
	 */
	@PostMapping("/register")
	@ResponseBody
	public String register(
			OAuth2AuthenticationToken authentication, 
			@Validated @RequestBody RequestParamRegister requestParamRegister,
			BindingResult bindingResult) {
		
		if (bindingResult.hasErrors()) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		// 認証ユーザID
		Long myUserId = this.getMyUserId(authentication);
		
		// 作業実績登録
		int cnt = this.workHoursService.register(
				myUserId, requestParamRegister.getIssueId(), requestParamRegister.getIsStart());
		
		if (cnt <= 0) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		// 作業実績取得
		List<WorkHours> workHoursList = this.workHoursService.select(myUserId, requestParamRegister.getIssueId());
		
		String actualHours = this.getThisActualHours(workHoursList);
		
		return actualHours;
	}
	
	/**
	 * 削除（作業開始）
	 * 
	 * @param authentication
	 * @param issueId
	 * @return
	 */
	@PostMapping("/delete")
	@ResponseBody
	public void delete(
			OAuth2AuthenticationToken authentication, 
			@Validated @RequestBody RequestParamDelete requestParamDelete,
			BindingResult bindingResult) {
		
		if (bindingResult.hasErrors()) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		// 認証ユーザID
		Long myUserId = this.getMyUserId(authentication);
		
		// 作業実績削除
		int cnt = this.workHoursService.delete(myUserId, requestParamDelete.getIssueId());
		
		if (cnt <= 0) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 作業実績送信
	 * 
	 * @param authentication
	 * @param issueId
	 * @return
	 * @throws MalformedURLException 
	 */
	@PostMapping("/send")
	@ResponseBody
	public void send(
			OAuth2AuthenticationToken authentication, 
			@Validated @RequestBody RequestParamSend requestParamSend,
			BindingResult bindingResult) throws MalformedURLException {
		
		if (bindingResult.hasErrors()) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		OAuth2AuthorizedClient authorizedClient = this.getAuthorizedClient(authentication);
		
		if (Objects.isNull(authorizedClient)) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		
		// 課題パラメータ
		UpdateIssueParams params = new UpdateIssueParams(requestParamSend.getIssueId())
				.actualHours(requestParamSend.getActualHours());
		
		// 認証ユーザID
		Long myUserId = this.getMyUserId(authentication);
		
		BacklogClient backlogClient = this.backlogClientService.getClient(authorizedClient);
		
		if (!this.checkSendData(backlogClient, requestParamSend.getIssueId(), myUserId)) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		// 課題更新
		backlogClient.updateIssue(params);
	}
	
	/**
	 * 送信前チェック
	 * 
	 * @param backlogClient
	 * @param issueId
	 * @param myUserId
	 * @return チェック結果（true：OK、false：NG）
	 */
	private boolean checkSendData(BacklogClient backlogClient, Long issueId, Long myUserId) {
		
		// 更新対象課題取得
		Issue issue = backlogClient.getIssue(issueId);
		
		if (Objects.isNull(issue.getAssignee())) {
			
			return true;
		}
		
		if (myUserId.equals(issue.getAssignee().getId())) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 認可済みクライアント取得
	 * 
	 * @param authentication
	 * @return 認可済みクライアント
	 */
	private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
		
		return this.authorizedClientService.loadAuthorizedClient(
				authentication.getAuthorizedClientRegistrationId(),
				authentication.getName());
	}
	
	/**
	 * 課題リスト取得
	 * 
	 * @param userWatches
	 * @param projects
	 * @param myUserId
	 * @return 課題リスト
	 */
	private List<IssueRow> getIssueRowList(
			ResponseList<Watch> userWatches, 
			ResponseList<Project> projects, 
			long myUserId) {
		
		List<IssueRow> issueRowList = new ArrayList<IssueRow>();
		
		for (Watch watch : userWatches) {
			
			if (Objects.isNull(watch.getIssue())) {
				
				continue;
			}
			
			// 作業実績リスト取得
			List<WorkHours> workHoursList = this.workHoursService.select(myUserId, watch.getIssue().getId());
			
			IssueRow issueRow = new IssueRow();
			
			// 課題ID
			issueRow.setIssueId(watch.getIssue().getId());
			
			// 課題キー
			issueRow.setIssueKey(watch.getIssue().getIssueKey());
			
			// プロジェクト
			issueRow.setProject(
					this.getProject(projects, watch.getIssue().getProjectId()));
			
			// 種別
			issueRow.setIssueType(
					this.getIssueType(watch.getIssue().getIssueType()));
			
			// 件名
			issueRow.setSummary(watch.getIssue().getSummary());
			
			// 優先度
			issueRow.setPriority(
					this.getPriority(watch.getIssue().getPriority()));
			
			// ステータス
			issueRow.setStatus(
					this.getStatus(watch.getIssue().getStatus()));
			
			// 予定
			issueRow.setEstimatedHours(
					this.getHours(watch.getIssue().getEstimatedHours()));
			
			// 実績（Backlog）
			issueRow.setBacklogActualHours(
					this.getHours(watch.getIssue().getActualHours()));
			
			// 実績（本アプリ）
			issueRow.setThisActualHours(
					this.getThisActualHours(workHoursList));
			
			// 担当者名
			issueRow.setAsignee(
					this.getUser(watch.getIssue().getAssignee()));
			
			// 期限
			issueRow.setDueDate(
					this.getDate(watch.getIssue().getDueDate()));
			
			// 操作可否
			issueRow.setCanOperate(
					this.getCanOperation(watch.getIssue().getAssignee(), myUserId));
			
			// 作業中
			issueRow.setIsWorking(
					this.getIsWorking(workHoursList));
			
			issueRowList.add(issueRow);
		}
		
		return issueRowList;
	}
	
	/**
	 * プロジェクト名取得
	 * 
	 * @param projects
	 * @param projectId
	 * @return プロジェクト名
	 */
	private ProjectDisp getProject(ResponseList<Project> projects, long projectId) {
		
		ProjectDisp projectDisp = new ProjectDisp();
		
		Optional<Project> project = projects.stream().filter(prj -> prj.getId() == projectId).findFirst();
		
		if(project.isPresent()) {
			
			projectDisp.setName(project.get().getName());
			
			projectDisp.setProjectKey(project.get().getProjectKey());
		}
		
		return projectDisp;
	}
	
	/**
	 * 種別取得
	 * 
	 * @param issueType
	 * @return 種別
	 */
	private IssueTypeDisp getIssueType(IssueType issueType) {
		
		IssueTypeDisp issueTypeDisp = new IssueTypeDisp();
		
		if (Objects.isNull(issueType)) {
			
			return issueTypeDisp;
		}
		
		issueTypeDisp.setName(issueType.getName());
		
		if (Objects.isNull(issueType.getColor())) {
		
			return issueTypeDisp;
		}
		
		issueTypeDisp.setColor(issueType.getColor().getStrValue());
		
		return issueTypeDisp;
	}
	
	/**
	 * 優先度取得
	 * 
	 * @param priority
	 * @return 優先度
	 */
	private PriorityDisp getPriority(Priority priority) {
		
		PriorityDisp priorityDisp = new PriorityDisp();
		
		if (Objects.isNull(priority)) {
			
			return priorityDisp;
		}
		
		priorityDisp.setName(priority.getName());
		
		return priorityDisp;
	}
	
	/**
	 * ステータス取得
	 * 
	 * @param status
	 * @return ステータス
	 */
	private StatusDisp getStatus(Status status) {
		
		StatusDisp statusDisp = new StatusDisp();
		
		if (Objects.isNull(status)) {
			
			return statusDisp;
		}
		
		statusDisp.setName(status.getName());
		
		if (Objects.isNull(status.getColor())) {
			
			return statusDisp;
		}
		
		statusDisp.setColor(status.getColor().getStrValue());
		
		return statusDisp;
	}
	
	/**
	 * 時間取得
	 * 
	 * @param hours
	 * @return 時間
	 */
	private String getHours(BigDecimal hours) {
		
		if (Objects.isNull(hours)) {
			
			return Symbol.HYPHEN;
		}
		
		return new DecimalFormat(Format.HOURS).format(hours);
	}
	
	/**
	 * 日付取得
	 * 
	 * @param date
	 * @return 日付
	 */
	private String getDate(Date date) {
		
		// 期限
		if (Objects.isNull(date)) {
			
			return Symbol.HYPHEN;
		}
		
		return new SimpleDateFormat(Format.DATE).format(date);
	}
	
	/**
	 * ユーザ取得
	 * 
	 * @param asignee
	 * @return ユーザ
	 */
	private UserDisp getUser(User user) {
		
		UserDisp userDisp = new UserDisp();
		
		if (Objects.isNull(user)) {
			
			return userDisp;
		}
		
		userDisp.setName(user.getName());
		
		return userDisp;
	}
	
	/**
	 * 操作可否取得
	 * 
	 * @param asignee
	 * @param myUserId
	 * @return 操作可否
	 */
	private boolean getCanOperation(User asignee, long myUserId) {
		
		if (Objects.isNull(asignee)) {
			
			return true;
		}
		
		if (myUserId == asignee.getId()) {
			
			return true;
			
		} else {
			
			return false;
		}
	}
	
	/**
	 * 実績時間（本アプリ）取得
	 * 
	 * @param workHoursList
	 * @return 実績時間（記録結果）
	 */
	private String getThisActualHours(List<WorkHours> workHoursList) {
		
		if (ObjectUtils.isEmpty(workHoursList)) {
			
			return this.getHours(null);
		}
		
		BigDecimal sum = new BigDecimal(0);
		
		for (WorkHours workHours : workHoursList) {
			
			sum = sum.add(workHours.getActualHours());
		}
		
		return this.getHours(sum);
	}
	
	/**
	 * 作業中フラグ取得
	 * 
	 * @param workHoursList
	 * @return 作業中フラグ
	 */
	private boolean getIsWorking(List<WorkHours> workHoursList) {
		
		if (ObjectUtils.isEmpty(workHoursList)) {
			
			return false;
		}
		
		// 最新作業実績
		WorkHours workHours = workHoursList.stream().max(Comparator.comparing(WorkHours::getSequence)).get();
		
		if (Objects.isNull(workHours.getEndTime())) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 認証ユーザID取得
	 * @param authentication
	 * @return
	 */
	private Long getMyUserId(OAuth2AuthenticationToken authentication) throws ResponseStatusException {
		
		// 認証ユーザID
		Long myUserId = ConversionUtil.toLong(
				String.valueOf((Object)authentication.getPrincipal().getAttribute(AttributeName.AUTH_ID)));
		
		if (Objects.isNull(myUserId)) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		return myUserId;
	}
}
