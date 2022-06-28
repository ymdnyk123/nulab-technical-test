package com.example.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.common.util.DataSettingUtil;
import com.example.demo.dto.CommonDto;
import com.example.demo.mybatis.mapper.IssueTypeT01Mapper;
import com.example.demo.mybatis.model.IssueTypeT01;
import com.example.demo.mybatis.model.IssueTypeT01Example;
import com.example.demo.mybatis.model.IssueTypeT01Key;
import com.nulabinc.backlog4j.IssueType;
import com.nulabinc.backlog4j.Project;
import com.nulabinc.backlog4j.ResponseList;

@Service
public class IssueTypeServiceImpl implements IssueTypeService {

	@Autowired
	IssueTypeT01Mapper issueTypeT01Mapper;
	
	@Autowired
	CommonDto commonDto;
	
	/**
	 * タイプ登録（更新）
	 * 
	 * @param ssuType
	 * @param nowDate
	 */
	@Override
	public void registerIssueType(Date nowDate) {
		
		// プロジェクト一覧の取得
		ResponseList<Project> projects = this.commonDto.getBacklogClient().getProjects();
		
		if (ObjectUtils.isEmpty(projects)) {
			
			return;
		}
		
		projects.stream().forEach(project -> {
			
			// 種別一覧の取得
			ResponseList<IssueType> issueTypes = this.commonDto.getBacklogClient().getIssueTypes(project.getId());
			
			if (Objects.nonNull(issueTypes)) {
				
				issueTypes.stream().forEach(issueType -> {
					
					IssueTypeT01Key key = new IssueTypeT01Key();
					
					key.setProjectId(issueType.getProjectId());
					
					key.setTypeId(issueType.getId());
					
					IssueTypeT01 issueTypeT01 = issueTypeT01Mapper.selectByPrimaryKey(key);
					
					if (ObjectUtils.isEmpty(issueTypeT01)) {
						
						// 登録
						this.insertIssueTypeT01(issueType, nowDate);
						
					} else {
						
						// 更新
						this.updateIssueTypeT01(issueType, nowDate, issueTypeT01);
					}
				});
			}
		});
	}
	
	/**
	 * タイプ登録
	 * 
	 * @param issueType
	 * @param nowDate
	 */
	private void insertIssueTypeT01(IssueType issueType, Date nowDate) {
		
		IssueTypeT01 target = new IssueTypeT01();
		
		// プロジェクトID（主キー）
		target.setProjectId(issueType.getProjectId());
		
		// タイプID（主キー）
		target.setTypeId(issueType.getId());
		
		// タイプ名
		target.setTypeName(issueType.getName());
		
		// カラー
		if (!ObjectUtils.isEmpty(issueType.getColor())) {
			
			target.setTypeColor(issueType.getColor().getStrValue());
		}
		
		// 登録日時
		target.setInsertDatetime(nowDate);
		
		issueTypeT01Mapper.insertSelective(target);
	}
	
	/**
	 * タイプ更新
	 * 
	 * @param issueType
	 * @param nowDate
	 * @param issueTypeT01
	 */
	private void updateIssueTypeT01(IssueType issueType, Date nowDate, IssueTypeT01 issueTypeT01) {
		
		IssueTypeT01 target = new IssueTypeT01();
		
		// プロジェクトID（主キー）
		target.setProjectId(issueTypeT01.getProjectId());
		
		// タイプID（主キー）
		target.setTypeId(issueTypeT01.getTypeId());
		
		// タイプ名
		target.setTypeName(DataSettingUtil.getUpdatedData(issueTypeT01.getTypeName(), issueType.getName()));
		
		// カラー
		if (ObjectUtils.isEmpty(issueType.getColor())) {
			
			target.setTypeColor(
					DataSettingUtil.getUpdatedData(issueTypeT01.getTypeColor(), null));
			
		} else {
			
			target.setTypeColor(
					DataSettingUtil.getUpdatedData(issueTypeT01.getTypeColor(), issueType.getColor().getStrValue()));
		}
		
		// 更新日時
		target.setUpdateDatetime(nowDate);
		
		if (Objects.nonNull(target.getTypeName())
				|| Objects.nonNull(target.getTypeColor())) {
		}
		
		issueTypeT01Mapper.updateByPrimaryKeySelective(target);
	}
	
	/**
	 * タイプ一覧取得（プロジェクトID）
	 * 
	 * @param projectId
	 * @return タイプ一覧
	 */
	@Override
	public List<IssueTypeT01> selectIssueTypeListByProjectId(long projectId) {
		
		IssueTypeT01Example example = new IssueTypeT01Example();
		
		IssueTypeT01Example.Criteria criteria = example.createCriteria();
		
		// プロジェクトID
		criteria.andProjectIdEqualTo(projectId);
		
		return issueTypeT01Mapper.selectByExample(example);
	}
	
	/**
	 * タイプ一覧取得
	 * 
	 * @return タイプ一覧
	 */
	@Override
	public List<IssueTypeT01> selectIssueTypeList() {
		
		return issueTypeT01Mapper.selectByExample(null);
	}
	

}
