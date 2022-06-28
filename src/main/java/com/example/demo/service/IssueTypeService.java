
package com.example.demo.service;

import java.util.Date;
import java.util.List;

import com.example.demo.mybatis.model.IssueTypeT01;

public interface IssueTypeService {
	
	void registerIssueType(Date nowDate);
	
	List<IssueTypeT01> selectIssueTypeListByProjectId(long projectId);
	
	List<IssueTypeT01> selectIssueTypeList();
}