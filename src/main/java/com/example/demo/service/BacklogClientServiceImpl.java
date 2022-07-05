package com.example.demo.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import com.example.demo.common.constant.Symbol;
import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.BacklogClientFactory;
import com.nulabinc.backlog4j.auth.AccessToken;
import com.nulabinc.backlog4j.auth.BacklogOAuthSupport;
import com.nulabinc.backlog4j.auth.OnAccessTokenRefreshListener;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogJpConfigure;

/**
 * BacklogClientサービス
 * 
 * @author user
 */
@Service
public class BacklogClientServiceImpl implements BacklogClientService {
	
	/**
	 * BacklogClient取得
	 * 
	 * @param authorizedClient
	 * @throws MalformedURLException
	 */
	@Override
	public BacklogClient getClient(OAuth2AuthorizedClient authorizedClient) throws MalformedURLException {
		
		if (Objects.isNull(authorizedClient)) {
			
			return null;
		}
		
		// 設定
		BacklogConfigure configure = new BacklogJpConfigure(
				this.getSpaceKey(
						authorizedClient.getClientRegistration().getProviderDetails().getAuthorizationUri()));
		
		// アクセストークン有効期限
		Long expires = this.getAccessTokenExpires(authorizedClient.getAccessToken().getExpiresAt());
		
		// アクセストークン設定
		AccessToken accessToken = new AccessToken(
				authorizedClient.getAccessToken().getTokenValue(),
				expires,
				authorizedClient.getRefreshToken().getTokenValue());
		
		configure.accessToken(accessToken);
		
		// サポート
		BacklogOAuthSupport support = new BacklogOAuthSupport(configure);
		
		// クライアント情報
		support.setOAuthClientId(
				authorizedClient.getClientRegistration().getClientId(), 
				authorizedClient.getClientRegistration().getClientSecret());
		
		// リスナー
		support.setOnAccessTokenRefreshListener(
				this.getListener(accessToken));
		
		// BacklogClient作成
		BacklogClient backlogClient = new BacklogClientFactory(configure).newClient();
		
		backlogClient.setOAuthSupport(support);
		
		return backlogClient;
	}
	
	/**
	 * スペースキー取得
	 * 
	 * @param authorizationUri
	 * @return スペースキー
	 */
	private String getSpaceKey(String authorizationUri) {
		
		try {
			
			URI uri = new URI(authorizationUri);
			
			return uri.getHost().split(Pattern.quote(Symbol.PERIOD))[0];
			
		} catch (URISyntaxException e) {
			
			e.printStackTrace();
			
			return null;
			
		} catch (NullPointerException e) {
			
			e.printStackTrace();
			
			return null;
			
		} catch (ArrayIndexOutOfBoundsException e) {
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * アクセストークン有効期限取得
	 * 
	 * @param expiresAt
	 * @return アクセストークン有効期限
	 */
	private Long getAccessTokenExpires(Instant expiresAt) {
		
		if (Objects.isNull(expiresAt)) {
			
			return null;
		}
		
		Long expires = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
		
		if (expires <= 0L) {
			
			return null;
		}
		
		return expires;
	}
	
	/**
	 * リスナー取得
	 * 
	 * @param accessToken
	 * @return リスナー
	 */
	private OnAccessTokenRefreshListener getListener(AccessToken accessToken) {
		
		OnAccessTokenRefreshListener listener = new OnAccessTokenRefreshListener();
		
		listener.onAccessTokenRefresh(accessToken);
		
		return listener;
	}
}