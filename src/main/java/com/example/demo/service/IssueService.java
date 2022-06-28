
package com.example.demo.service;

import java.util.Date;
import java.util.List;

import com.example.demo.mybatis.model.IssueT01;
import com.example.demo.mybatis.model.IssueT02ActualHours;
import com.nulabinc.backlog4j.Issue;

public interface IssueService {
	
	void registerWatchingIssues();
	
	List<IssueT02ActualHours> getIssueIssueT02ActualHours(long asigneeUserId, long issueId);
	
	IssueT02ActualHours getLatestIssueIssueT02ActualHours(long asigneeUserId, long issueId);
	
	boolean registerIssue(long watchUserId, Issue issue, Date nowDate);
	
	List<IssueT01> selectIssueT01List(long userId);
	
	void switchAutoSetting(long watchUserId, long issueId, boolean isAutoSetting, Date nowDate);
	
	void switchRecording(long watchUserId, long issueId, boolean isRecording, Date nowDate);
}