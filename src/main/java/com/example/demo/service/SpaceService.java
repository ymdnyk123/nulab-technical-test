
package com.example.demo.service;

import java.util.Date;

import com.example.demo.mybatis.model.SpaceT01;

public interface SpaceService {
	
	void insertSpace(Date nowDate);
	
	SpaceT01 selectSpaceBySpaceKey(String spaceKey);
}