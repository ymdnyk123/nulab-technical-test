
package com.example.demo.controller;



import java.net.MalformedURLException;
import java.util.Date;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.CommonDto;
import com.example.demo.dto.SpaceDto;
import com.example.demo.mybatis.model.SpaceT01;
import com.example.demo.service.AuthorizationService;
import com.example.demo.service.IssueTypeService;
import com.example.demo.service.PriorityService;
import com.example.demo.service.ProjectService;
import com.example.demo.service.SpaceService;
import com.example.demo.service.StatusService;
import com.example.demo.service.UserServiceImpl;

@Controller
@RequestMapping(value="redirect")
public class RedirectUrlController {

	@Autowired
	AuthorizationService authorizationService;
	
	@Autowired
	SpaceService spaceService;
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	UserServiceImpl userService;
	
	@Autowired
	IssueTypeService issueTypeService;
	
	@Autowired
	StatusService statusService;
	
	@Autowired
	PriorityService priorityService;
	
	@Autowired
	CommonDto commonDto;
	
	@Autowired
	ModelMapper modelMapper;

	/**
	 * リダイレクトコントローラ
	 * 
	 * 認可コード取得後に呼ばれる。
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value="auth")
	public String requestAuthorization(Model model, @RequestParam("code") String code) {
		
		try {
			
			// アクセストークン取得
			this.authorizationService.getAccessToken("nulab-exam", code);
			
			// アクセストークンリフレッシュ
			this.authorizationService.refreshAccessToken("nulab-exam");
			
			// Backlog同期
			this.synchronizeBacklog();
			
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
			
			return "error";
		}
		
		// ヘッダー設定
		this.setPageHeader();
		
		return "forward:/issues/init";
	}
	
	/**
	 * Backlog同期
	 * 
	 * @param backlogClient
	 */
	private void synchronizeBacklog() {
		
		Date nowDate = new Date();
		
		// スペース情報登録
		this.spaceService.insertSpace(nowDate);
		
		// プロジェクト情報登録
		this.projectService.registerProjects(nowDate);
		
		// 認証ユーザ情報登録
		this.userService.registerMySelf(nowDate);
		
		// 種別情報登録
		this.issueTypeService.registerIssueType(nowDate);
		
		// ステータス情報登録
		this.statusService.registerStatus(nowDate);
		
		// 優先度情報登録
		this.priorityService.registerPriority(nowDate);
	}
	
	/**
	 * ヘッダ設定
	 * 
	 * @param spaceKey
	 */
	private void setPageHeader() {

		// スペース
		this.setSpaceToPageHeader();
		
		// 認証ユーザ
		this.commonDto.getHeader().setMyUserId(this.commonDto.getMySelf().getId());
	}
	
	/**
	 * スペース設定（ヘッダ）
	 * 
	 * @param spaceKey
	 */
	private void setSpaceToPageHeader() {
		
		SpaceT01 spaceT01 = spaceService.selectSpaceBySpaceKey(commonDto.getSpaceKey());
		
		if (ObjectUtils.isEmpty(spaceT01)) {
			
			return;
		}
		
		this.commonDto.getHeader().setSpaceDto(modelMapper.map(spaceT01, SpaceDto.class));
	}
}