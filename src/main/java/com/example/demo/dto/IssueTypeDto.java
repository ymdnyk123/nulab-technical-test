package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

public class IssueTypeDto {

	@Getter
	@Setter
	private Long typeId;
	
	@Getter
	@Setter
	private String typeName;
	
	@Getter
	@Setter
	private String typeColor;
	
	@Getter
	@Setter
	private Integer displayOrder;
}
