package com.example.demo.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


public class IssuesView {
	
	@Getter
	@Setter
	private List<IssueDto> issueDtoList;
}
