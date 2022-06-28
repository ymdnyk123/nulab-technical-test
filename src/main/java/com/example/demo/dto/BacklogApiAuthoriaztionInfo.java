package com.example.demo.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "backlog.api")
public class BacklogApiAuthoriaztionInfo {

	@Getter
	@Setter
	private String authServer;

	@Getter
	@Setter
	private String authEndpoint;

	@Getter
	@Setter
	private String accessTokenRequest;
	
	@Getter
	@Setter
	private String responseType;

	@Getter
	@Setter
	private String clientId;
	
	@Getter
	@Setter
	private String clientSecret;
	
	@Getter
	@Setter
	private String redirectUri;
	
	@Getter
	@Setter
	private String state;
	
	@Getter
	@Setter
	private String grantType;
}