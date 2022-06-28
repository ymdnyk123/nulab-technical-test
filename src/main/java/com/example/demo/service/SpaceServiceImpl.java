
package com.example.demo.service;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.dto.CommonDto;
import com.example.demo.mybatis.mapper.SpaceT01Mapper;
import com.example.demo.mybatis.model.SpaceT01;
import com.nulabinc.backlog4j.Space;

@Service
public class SpaceServiceImpl implements SpaceService {
	
	@Autowired
	SpaceT01Mapper spaceT01Mapper;
	
	@Autowired
	CommonDto commonDto;
	
	/**
	 * スペース登録
	 * 
	 * @param backlogClient
	 * @param nowDate
	 */
	@Transactional
	@Override
	public void insertSpace(Date nowDate) {
		
		Space space = commonDto.getBacklogClient().getSpace();
		
		if (ObjectUtils.isEmpty(space)) {
			
			// TODO システムエラー
		}
		
		SpaceT01 spaceT01 = spaceT01Mapper.selectByPrimaryKey(space.getSpaceKey());
		
		if (!ObjectUtils.isEmpty(spaceT01)) {
			
			return;
		}
		
		// 登録
		this.insertSpaceT01(space, nowDate);
	}
	
	/**
	 * スペース登録
	 * 
	 * @param space
	 * @param nowDate
	 */
	private void insertSpaceT01(Space space, Date nowDate) {
		
		SpaceT01 target = new SpaceT01();
		
		// スペースキー（主キー）
		target.setSpaceKey(space.getSpaceKey());
		
		// スペース名
		target.setSpaceName(space.getName());
		
		// 登録日時
		target.setInsertDatetime(nowDate);
		
		spaceT01Mapper.insertSelective(target);
	}
	
	/**
	 * スペース取得(スペースキー)
	 * 
	 * @param spaceKey
	 */
	@Override
	public SpaceT01 selectSpaceBySpaceKey(String spaceKey) {
		
		return spaceT01Mapper.selectByPrimaryKey(spaceKey);
	}
}
