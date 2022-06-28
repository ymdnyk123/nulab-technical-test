package com.example.demo.dto;

import org.springframework.stereotype.Component;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.User;
import com.nulabinc.backlog4j.auth.AccessToken;

import lombok.Getter;
import lombok.Setter;

@Component
public class CommonDto {
	
	@Getter
	@Setter
	String spaceKey;
	
	@Getter
	@Setter
	AccessToken accessToken;
	
	@Getter
	@Setter
	BacklogClient backlogClient;
	
	@Getter
	@Setter
	PageHeader header;
	
	@Getter
	@Setter
	User mySelf;
}
