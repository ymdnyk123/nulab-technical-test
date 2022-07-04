
package com.example.demo.service;

import java.util.List;

import com.example.demo.mybatis.model.WorkHours;

public interface IssueService {
	
	List<WorkHours> select(Long userId, Long isssueId);
	
	void register(Long userId, Long issueId, Boolean isWorking);
	
	void delete(Long userId, Long issueId);
}