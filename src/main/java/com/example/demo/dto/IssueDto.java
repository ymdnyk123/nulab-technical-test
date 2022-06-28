package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class IssueDto {
	
	@Getter
	@Setter
	private Long issueId;
	
	@Getter
	@Setter
	private String issueKey;
	
	@Getter
	@Setter
	private Long projectId;
	
	@Getter
	@Setter
	private ProjectDto projectDto;
	
	@Getter
	@Setter
	private Long parentIssueId;
	
	@Getter
	@Setter
	private Long issueType;
	
	@Getter
	@Setter
	private IssueTypeDto issueTypeDto;
	
	@Getter
	@Setter
	private String summary;
	
	@Getter
	@Setter
	private String description;
	
	@Getter
	@Setter
	private Long priority;
	
	@Getter
	@Setter
	private PriorityDto priorityDto;
	
	@Getter
	@Setter
	private Integer status;
	
	@Getter
	@Setter
	private StatusDto statusDto;
	
	@Getter
	@Setter
	private BigDecimal estimatedHours;
	
	@Getter
	@Setter
	private String estimatedHoursDisp;
	
	@Getter
	@Setter
	private BigDecimal actualHours;
	
	@Getter
	@Setter
	private String actualHoursDisp;
	
	@Getter
	@Setter
	private String actualHoursDispOwn;

	@Getter
	@Setter
	private Date dueDate;
	
	@Getter
	@Setter
	private String dueDateDisp;
	
	@Getter
	@Setter
	private Long asigneeUserNumberId;
	
	@Getter
	@Setter
	private UserDto asigneeDto;
	
	@Getter
	@Setter
	private Integer rowStatus;
	
	@Getter
	@Setter
	private Boolean isAutoSetting;
	
	@Getter
	@Setter
	private Boolean isRecording;
}
