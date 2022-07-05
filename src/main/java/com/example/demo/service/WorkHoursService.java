
package com.example.demo.service;

import java.util.List;

import com.example.demo.mybatis.model.WorkHours;

public interface WorkHoursService {
	
	List<WorkHours> select(Long userId, Long isssueId);
	
	int register(Long userId, Long issueId, Boolean isWorking);
	
	int delete(Long userId, Long issueId);
}