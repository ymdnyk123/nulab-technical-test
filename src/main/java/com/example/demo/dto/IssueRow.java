package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 課題
 */
public class IssueRow {
	
	/**
	 * 課題ID
	 */
	@Getter
	@Setter
	private Long issueId;
	
	/**
	 * 課題キー
	 */
	@Getter
	@Setter
	private String issueKey;
	
	/**
	 * プロジェクト
	 */
	@Getter
	@Setter
	private ProjectDisp project;
	
	/**
	 * 種別
	 */
	@Getter
	@Setter
	private IssueTypeDisp issueType;
	
	/**
	 * 件名
	 */
	@Getter
	@Setter
	private String summary;
	
	/**
	 * 優先度
	 */
	@Getter
	@Setter
	private PriorityDisp priority;
	
	/**
	 * ステータス
	 */
	@Getter
	@Setter
	private StatusDisp status;
	
	/**
	 * 予定時間
	 */
	@Getter
	@Setter
	private String estimatedHours;
	
	/**
	 * 実績時間（Backlog）
	 */
	@Getter
	@Setter
	private String backlogActualHours;
	
	/**
	 * 実績時間（本アプリ）
	 */
	@Getter
	@Setter
	private String thisActualHours;
	
	/**
	 * 期限
	 */
	@Getter
	@Setter
	private String dueDate;
	
	/**
	 * 担当者ユーザ名
	 */
	@Getter
	@Setter
	private UserDisp asignee;
	
	/**
	 * 作業中フラグ
	 */
	@Getter
	@Setter
	private Boolean isWorking;
	
	/**
	 * 操作可否
	 */
	@Getter
	@Setter
	private Boolean canOperate;
}
