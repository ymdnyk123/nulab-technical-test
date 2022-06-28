package com.example.demo.service;

import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.dto.BacklogApiAuthoriaztionInfo;
import com.example.demo.dto.CommonDto;
import com.nulabinc.backlog4j.BacklogClientFactory;
import com.nulabinc.backlog4j.auth.BacklogOAuthSupport;
import com.nulabinc.backlog4j.auth.OnAccessTokenRefreshListener;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogJpConfigure;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthorizationServiceImpl implements AuthorizationService {
	
	@Autowired
	BacklogApiAuthoriaztionInfo backlogApiAuthoriaztionInfo;

	@Autowired
	CommonDto commonDto;
	
	/**
	 * OAUth認証URL取得
	 * 
	 * @param spaceKey
	 */
	@Override
	public String getOAuthAuthorizationURL(String spaceKey) throws MalformedURLException {
		
		// OAuth2設定
		BacklogConfigure configure = new BacklogJpConfigure(spaceKey);
		
		BacklogOAuthSupport backlogOAuthSuport =  new BacklogOAuthSupport(configure);
		
		// クライアント情報登録
		backlogOAuthSuport.setOAuthClientId(
				backlogApiAuthoriaztionInfo.getClientId(),
				backlogApiAuthoriaztionInfo.getClientSecret());
		
		backlogOAuthSuport.setOAuthRedirectUrl(backlogApiAuthoriaztionInfo.getRedirectUri());
		
		return backlogOAuthSuport.getOAuthAuthorizationURL();
	}
	
	/**
	 * アクセストークン取得
	 * 
	 * @param spaceKey
	 * @param code
	 * @throws MalformedURLException
	 */
	@Override
	public void getAccessToken(String spaceKey, String code) throws MalformedURLException {
		
		// 設定
		BacklogConfigure configure = new BacklogJpConfigure(spaceKey);
		
		// サポート
		BacklogOAuthSupport support = new BacklogOAuthSupport(configure);
		
		// クライアント
		support.setOAuthClientId(
				backlogApiAuthoriaztionInfo.getClientId(),
				backlogApiAuthoriaztionInfo.getClientSecret());
		
		// リダイレクトURL
		support.setOAuthRedirectUrl(backlogApiAuthoriaztionInfo.getRedirectUri());
		
		// アクセストークン取得
		this.commonDto.setAccessToken(support.getOAuthAccessToken(code));
		
		// クライアント作成用
		configure.accessToken(this.commonDto.getAccessToken());
		
		// クライアント再作成
		this.commonDto.setBacklogClient(new BacklogClientFactory(configure).newClient());
	} 
	
	/**
	 * アクセストークンリフレッシュ
	 * 
	 * @param spaceKey
	 * @throws MalformedURLException
	 */
	@Override
	@Async
	public void refreshAccessToken(String spaceKey) throws MalformedURLException {
		
		while(true) {
			
			if (ObjectUtils.isEmpty(this.commonDto.getAccessToken())) {
				
				return;
			}
			
			long sleep = (this.commonDto.getAccessToken().getExpires() - 10L) * 1000L;
			
			if (sleep < 0) {
				
				sleep = 0;
			}
			
			try{
				
				log.info("access token refresh sleep [{}] msec", sleep);
				
				Thread.sleep(sleep);
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
			// リフレッシュ
			this.getRefreshtAccessToken(spaceKey);
			
			log.info("access token refresh success. expires [{}] sec", this.commonDto.getAccessToken().getExpires());
		}
	}
	
	/**
	 * リフレッシュトークン取得
	 * 
	 * @param spaceKey
	 * @throws MalformedURLException
	 */
	private void getRefreshtAccessToken(String spaceKey) throws MalformedURLException {
		
		// 設定
		BacklogConfigure configure = new BacklogJpConfigure(spaceKey);
		
		// 既存アクセストークン
		configure.accessToken(this.commonDto.getAccessToken());
		
		BacklogOAuthSupport support = new BacklogOAuthSupport(configure);
		
		// クライアント
		support.setOAuthClientId(
				backlogApiAuthoriaztionInfo.getClientId(), 
				backlogApiAuthoriaztionInfo.getClientSecret());
		
		// リスナー
		OnAccessTokenRefreshListener listener = new OnAccessTokenRefreshListener();

		listener.onAccessTokenRefresh(this.commonDto.getAccessToken());

		support.setOnAccessTokenRefreshListener(listener);
		
		// アクセストークンリフレッシュ
		this.commonDto.setAccessToken(support.refreshOAuthAccessToken());
		
		configure.accessToken(this.commonDto.getAccessToken());
		
		// クライアント再作成
		this.commonDto.setBacklogClient(new BacklogClientFactory(configure).newClient());
	}
}