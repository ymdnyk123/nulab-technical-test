package com.example.demo.controller;

import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.demo.dto.CommonDto;
import com.example.demo.dto.PageHeader;
import com.example.demo.service.AuthorizationService;

@Controller
public class OauthController{
	
	@Autowired
	private CommonDto commonDto;
	
	@Autowired
	private AuthorizationService service;
	
	@RequestMapping(value="/", method = RequestMethod.GET)
	public String init(Model model) {
		
		commonDto.setSpaceKey("nulab-exam");
		
		commonDto.setHeader(new PageHeader());
		
		// OAuth認可要求
		try {
			
			return "redirect:" + service.getOAuthAuthorizationURL("nulab-exam");
			
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
			
			return "error";
		}
	}
}