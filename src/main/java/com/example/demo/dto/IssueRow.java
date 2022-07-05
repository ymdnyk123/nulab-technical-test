package com.example.demo.dto;

import lombok.Data;

/**
 * 課題
 */
@Data
public class IssueRow {
	
	/**
	 * 課題ID
	 */
	private Long issueId;
	
	/**
	 * 課題キー
	 */
	private String issueKey;
	
	/**
	 * プロジェクト
	 */
	private ProjectDisp project;
	
	/**
	 * 種別
	 */
	private IssueTypeDisp issueType;
	
	/**
	 * 件名
	 */
	private String summary;
	
	/**
	 * 優先度
	 */
	private PriorityDisp priority;
	
	/**
	 * ステータス
	 */
	private StatusDisp status;
	
	/**
	 * 予定時間
	 */
	private String estimatedHours;
	
	/**
	 * 実績時間（Backlog）
	 */
	private String backlogActualHours;
	
	/**
	 * 実績時間（本アプリ）
	 */
	private String thisActualHours;
	
	/**
	 * 期限
	 */
	private String dueDate;
	
	/**
	 * 担当者ユーザ名
	 */
	private UserDisp asignee;
	
	/**
	 * 作業中フラグ
	 */
	private Boolean isWorking;
	
	/**
	 * 操作可否
	 */
	private Boolean canOperate;
}
