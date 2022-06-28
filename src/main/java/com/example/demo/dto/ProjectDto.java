package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

public class ProjectDto {
	
	@Getter
	@Setter
	private Long projectId;
	
	@Getter
	@Setter
	private String projectKey;
	
	@Getter
	@Setter
	private String projectName;
	
	@Getter
	@Setter
	private Integer rowStatus;
}
