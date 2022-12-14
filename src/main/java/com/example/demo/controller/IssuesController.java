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
 * ????????????????????????
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
	 * ??????????????????
	 * 
	 * @param authentication
	 * @param model
	 * @return
	 */
	@GetMapping("/")
	public String init(OAuth2AuthenticationToken authentication, Model model) throws MalformedURLException {
		
		// ?????????????????????????????????OAuth2AuthorizedClientService????????????????????????????????????????????????????????????Model?????????
		model.addAttribute(AttributeName.AUTH_CLIENT, this.getAuthorizedClient(authentication));
		
		OAuth2AuthorizedClient authorizedClient = this.getAuthorizedClient(authentication);
		
		if (Objects.isNull(authorizedClient)) {
			
			return PageId.LOGIN;
		}
		
		// BacklogClient??????
		BacklogClient backlogClient = this.backlogClientService.getClient(authorizedClient);
		
		// ????????????????????????
		Space space = backlogClient.getSpace();
		
		model.addAttribute(AttributeName.SPACE_NAME, space.getName());
		
		// ?????????????????????????????????
		ResponseList<Project> projectList = backlogClient.getProjects();
		
		// ???????????????ID
		Long myUserId = this.getMyUserId(authentication);
		
		// ??????????????????
		ResponseList<Watch> userWatches = backlogClient.getUserWatches(myUserId);
		
		// ?????????????????????????????????
		List<IssueRow> issueRowList = this.getIssueRowList(userWatches, projectList, myUserId);
		
		model.addAttribute(AttributeName.ISSUE_ROW_LIST, issueRowList);
		
		
		return PageId.ISSUES;
	}
	
	/**
	 * ????????????????????????
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
		
		// ???????????????ID
		Long myUserId = this.getMyUserId(authentication);
		
		// ??????????????????
		int cnt = this.workHoursService.register(
				myUserId, requestParamRegister.getIssueId(), requestParamRegister.getIsStart());
		
		if (cnt <= 0) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		// ??????????????????
		List<WorkHours> workHoursList = this.workHoursService.select(myUserId, requestParamRegister.getIssueId());
		
		String actualHours = this.getThisActualHours(workHoursList);
		
		return actualHours;
	}
	
	/**
	 * ????????????????????????
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
		
		// ???????????????ID
		Long myUserId = this.getMyUserId(authentication);
		
		// ??????????????????
		int cnt = this.workHoursService.delete(myUserId, requestParamDelete.getIssueId());
		
		if (cnt <= 0) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * ??????????????????
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
		
		
		// ?????????????????????
		UpdateIssueParams params = new UpdateIssueParams(requestParamSend.getIssueId())
				.actualHours(requestParamSend.getActualHours());
		
		// ???????????????ID
		Long myUserId = this.getMyUserId(authentication);
		
		BacklogClient backlogClient = this.backlogClientService.getClient(authorizedClient);
		
		if (!this.checkSendData(backlogClient, requestParamSend.getIssueId(), myUserId)) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		// ????????????
		backlogClient.updateIssue(params);
	}
	
	/**
	 * ?????????????????????
	 * 
	 * @param backlogClient
	 * @param issueId
	 * @param myUserId
	 * @return ?????????????????????true???OK???false???NG???
	 */
	private boolean checkSendData(BacklogClient backlogClient, Long issueId, Long myUserId) {
		
		// ????????????????????????
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
	 * ????????????????????????????????????
	 * 
	 * @param authentication
	 * @return ??????????????????????????????
	 */
	private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
		
		return this.authorizedClientService.loadAuthorizedClient(
				authentication.getAuthorizedClientRegistrationId(),
				authentication.getName());
	}
	
	/**
	 * ?????????????????????
	 * 
	 * @param userWatches
	 * @param projects
	 * @param myUserId
	 * @return ???????????????
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
			
			// ???????????????????????????
			List<WorkHours> workHoursList = this.workHoursService.select(myUserId, watch.getIssue().getId());
			
			IssueRow issueRow = new IssueRow();
			
			// ??????ID
			issueRow.setIssueId(watch.getIssue().getId());
			
			// ????????????
			issueRow.setIssueKey(watch.getIssue().getIssueKey());
			
			// ??????????????????
			issueRow.setProject(
					this.getProject(projects, watch.getIssue().getProjectId()));
			
			// ??????
			issueRow.setIssueType(
					this.getIssueType(watch.getIssue().getIssueType()));
			
			// ??????
			issueRow.setSummary(watch.getIssue().getSummary());
			
			// ?????????
			issueRow.setPriority(
					this.getPriority(watch.getIssue().getPriority()));
			
			// ???????????????
			issueRow.setStatus(
					this.getStatus(watch.getIssue().getStatus()));
			
			// ??????
			issueRow.setEstimatedHours(
					this.getHours(watch.getIssue().getEstimatedHours()));
			
			// ?????????Backlog???
			issueRow.setBacklogActualHours(
					this.getHours(watch.getIssue().getActualHours()));
			
			// ????????????????????????
			issueRow.setThisActualHours(
					this.getThisActualHours(workHoursList));
			
			// ????????????
			issueRow.setAsignee(
					this.getUser(watch.getIssue().getAssignee()));
			
			// ??????
			issueRow.setDueDate(
					this.getDate(watch.getIssue().getDueDate()));
			
			// ????????????
			issueRow.setCanOperate(
					this.getCanOperation(watch.getIssue().getAssignee(), myUserId));
			
			// ?????????
			issueRow.setIsWorking(
					this.getIsWorking(workHoursList));
			
			issueRowList.add(issueRow);
		}
		
		return issueRowList;
	}
	
	/**
	 * ???????????????????????????
	 * 
	 * @param projects
	 * @param projectId
	 * @return ?????????????????????
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
	 * ????????????
	 * 
	 * @param issueType
	 * @return ??????
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
	 * ???????????????
	 * 
	 * @param priority
	 * @return ?????????
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
	 * ?????????????????????
	 * 
	 * @param status
	 * @return ???????????????
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
	 * ????????????
	 * 
	 * @param hours
	 * @return ??????
	 */
	private String getHours(BigDecimal hours) {
		
		if (Objects.isNull(hours)) {
			
			return Symbol.HYPHEN;
		}
		
		return new DecimalFormat(Format.HOURS).format(hours);
	}
	
	/**
	 * ????????????
	 * 
	 * @param date
	 * @return ??????
	 */
	private String getDate(Date date) {
		
		// ??????
		if (Objects.isNull(date)) {
			
			return Symbol.HYPHEN;
		}
		
		return new SimpleDateFormat(Format.DATE).format(date);
	}
	
	/**
	 * ???????????????
	 * 
	 * @param asignee
	 * @return ?????????
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
	 * ??????????????????
	 * 
	 * @param asignee
	 * @param myUserId
	 * @return ????????????
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
	 * ????????????????????????????????????
	 * 
	 * @param workHoursList
	 * @return ??????????????????????????????
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
	 * ????????????????????????
	 * 
	 * @param workHoursList
	 * @return ??????????????????
	 */
	private boolean getIsWorking(List<WorkHours> workHoursList) {
		
		if (ObjectUtils.isEmpty(workHoursList)) {
			
			return false;
		}
		
		// ??????????????????
		WorkHours workHours = workHoursList.stream().max(Comparator.comparing(WorkHours::getSequence)).get();
		
		if (Objects.isNull(workHours.getEndTime())) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * ???????????????ID??????
	 * @param authentication
	 * @return
	 */
	private Long getMyUserId(OAuth2AuthenticationToken authentication) throws ResponseStatusException {
		
		// ???????????????ID
		Long myUserId = ConversionUtil.toLong(
				String.valueOf((Object)authentication.getPrincipal().getAttribute(AttributeName.AUTH_ID)));
		
		if (Objects.isNull(myUserId)) {
			
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		return myUserId;
	}
}
