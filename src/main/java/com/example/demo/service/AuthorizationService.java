package com.example.demo.service;

import java.net.MalformedURLException;

public interface  AuthorizationService {
	
	String getOAuthAuthorizationURL(String spaceKey) throws MalformedURLException;
	
	void getAccessToken(String spaceKey, String code) throws MalformedURLException;
	
	void refreshAccessToken(String spaceKey) throws MalformedURLException;
}