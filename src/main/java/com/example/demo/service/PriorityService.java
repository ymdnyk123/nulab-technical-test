
package com.example.demo.service;

import java.util.Date;
import java.util.List;

import com.example.demo.mybatis.model.PriorityT01;

public interface PriorityService {
	
	void registerPriority(Date nowDate);
	
	List<PriorityT01> selectPriorityT01();
}