
package com.example.demo.service;

import java.util.Date;
import java.util.List;

import com.example.demo.mybatis.model.StatusT01;

public interface StatusService {
	
	void registerStatus(Date nowDate);
	
	List<StatusT01> selectStatusT01ListByProjectId(long projectId);
	
	List<StatusT01> selectStatusT01List();
}