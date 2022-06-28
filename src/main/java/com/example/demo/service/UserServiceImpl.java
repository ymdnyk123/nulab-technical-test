
package com.example.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.common.util.DataSettingUtil;
import com.example.demo.dto.CommonDto;
import com.example.demo.mybatis.mapper.UserT01Mapper;
import com.example.demo.mybatis.model.UserT01;
import com.nulabinc.backlog4j.User;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	UserT01Mapper userT01Mapper;
	
	@Autowired
	CommonDto commonDto;
	
	/**
	 * 認証ユーザ登録（更新）
	 * 
	 * @param Date nowDate
	 */
	public void registerMySelf(Date nowDate) {
		
		// 認証ユーザ情報取得
		User user = this.commonDto.getBacklogClient().getMyself();
		
		// 共通情報設定
		this.commonDto.setMySelf(user);
		
		// ユーザ登録（更新）
		this.registerUser(user, nowDate);
	}
	
	/**
	 * ユーザ登録（更新）
	 * 
	 * @param Date nowDate
	 */
	@Transactional
	@Override
	public void registerUser(User user, Date nowDate) {
		
		UserT01 userT01 = userT01Mapper.selectByPrimaryKey(user.getId());
		
		if (ObjectUtils.isEmpty(userT01)) {
					
			// 登録
			this.insertUserT01(user, nowDate);
			
		} else {
			
			// 更新
			this.updateUserT01(user, nowDate, userT01);
		}
	}
	
	/**
	 * ユーザ登録
	 * 
	 * @param user
	 */
	private void insertUserT01(User user, Date nowDate) {
		
		UserT01 target = new UserT01();
		
		target.setUserNumberId(user.getId());
		
		target.setUserCharId(user.getUserId());
		
		target.setUserName(user.getName());
		
		target.setUserLang(user.getLang());
		
		target.setRoleType(user.getRoleType().getIntValue());
		
		target.setMailAddress(user.getMailAddress());
		
		target.setInsertDatetime(nowDate);
		
		userT01Mapper.insertSelective(target);
	}
	
	/**
	 * ユーザ登録
	 * 
	 * @param user
	 */
	private void updateUserT01(User user, Date nowDate, UserT01 userT01) {
		
		UserT01 target = new UserT01();
		
		// ID（主キー）
		target.setUserNumberId(userT01.getUserNumberId());
		
		// ユーザID
		target.setUserCharId(DataSettingUtil.getUpdatedData(userT01.getUserCharId(), user.getUserId()));
		
		// ユーザ名
		target.setUserName(DataSettingUtil.getUpdatedData(userT01.getUserName(), user.getName()));
		
		// 言語
		target.setUserLang(DataSettingUtil.getUpdatedData(userT01.getUserLang(), user.getLang()));
		
		// ロール
		target.setRoleType(DataSettingUtil.getUpdatedData(userT01.getRoleType(), user.getRoleType().getIntValue()));
		
		// メールアドレス
		target.setMailAddress(DataSettingUtil.getUpdatedData(userT01.getMailAddress(), user.getMailAddress()));
		
		// 更新日時
		target.setUpdateDatetime(nowDate);
		
		if ( Objects.nonNull(target.getUserCharId())
				|| Objects.nonNull(target.getUserName())
				|| Objects.nonNull(target.getUserLang())
				|| Objects.nonNull(target.getRoleType())
				|| Objects.nonNull(target.getMailAddress())) {
			
			userT01Mapper.updateByPrimaryKeySelective(target);
		}
	}
	
	/**
	 *  ユーザ取得（ユーザID（数値））
	 * 
	 * @param userNumberId
	 * @return	ユーザ
	 */
	@Override
	public UserT01 selectUserByUserNumberId(long userNumberId) {
		
		return userT01Mapper.selectByPrimaryKey(userNumberId);
	}
	
	@Override
	public List<UserT01> selectUserT01List() {
		
		return userT01Mapper.selectByExample(null);
	}
}
