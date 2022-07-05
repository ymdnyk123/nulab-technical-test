package com.example.demo.service;

import java.net.MalformedURLException;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import com.nulabinc.backlog4j.BacklogClient;

public interface  BacklogClientService {
	
	BacklogClient getClient(OAuth2AuthorizedClient authorizedClient) throws MalformedURLException;
}