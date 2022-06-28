package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

public class PageHeader {
	
	@Getter
	@Setter
	private SpaceDto spaceDto;
	
	@Getter
	@Setter
	private long myUserId;
}
