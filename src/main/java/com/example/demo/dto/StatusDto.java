package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

public class StatusDto {
	
	@Getter
	@Setter
	private Integer statusId;
	
	@Getter
	@Setter
    private String statusName;
	
	@Getter
	@Setter
	private String statusColor;

	@Getter
	@Setter
    private Integer displayOrder;

}
