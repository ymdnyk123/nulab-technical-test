package com.example.demo.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


public class ProjectsView {
	
	@Getter
	@Setter
	private SpaceDto spaceDto;
	
	@Getter
	@Setter
	private List<ProjectDto> projectDto;
}
