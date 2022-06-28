
package com.example.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.common.util.DataSettingUtil;
import com.example.demo.dto.CommonDto;
import com.example.demo.mybatis.mapper.ProjectT01Mapper;
import com.example.demo.mybatis.model.ProjectT01;
import com.example.demo.mybatis.model.ProjectT01Example;
import com.example.demo.mybatis.model.ProjectT01Key;
import com.nulabinc.backlog4j.Project;
import com.nulabinc.backlog4j.ResponseList;

@Service
public class ProjectServiceImpl implements ProjectService {
	
	@Autowired
	ProjectT01Mapper projectT01Mapper;
	
	@Autowired
	CommonDto commonDto;
	
	/**
	 * プロジェクト登録（更新）
	 * 
	 * @param backlogClient
	 * @param nowDate
	 * @param spaceKey
	 */
	@Transactional
	@Override
	public void registerProjects(Date nowDate) {
		
		// プロジェクト取得API
		ResponseList<Project> projects = commonDto.getBacklogClient().getProjects();
		
		if (ObjectUtils.isEmpty(projects)) {
			
			// TODO システムエラー
		}
		
		for (Project project : projects) {
			
			ProjectT01Key key = new ProjectT01Key();
			
			key.setSpaceKey(commonDto.getSpaceKey());
			
			key.setProjectId(project.getId());
			
			ProjectT01 projectT01 = this.projectT01Mapper.selectByPrimaryKey(key);
			
			if (ObjectUtils.isEmpty(projectT01)) {
				
				// 登録
				this.insertProjectT01(project, commonDto.getSpaceKey(), nowDate);
				
			} else {
				
				// 更新
				this.updateProjectT01(project, nowDate, projectT01);
			}
		}
	}

	/**
	 * プロジェクト登録
	 * 
	 * @param project
	 * @param spaceKey
	 * @param nowDate
	 */
	private void insertProjectT01(Project project, String spaceKey, Date nowDate) {
		
		ProjectT01 target = new ProjectT01();
		
		target.setSpaceKey(spaceKey);
		
		target.setProjectId(project.getId());
		
		target.setProjectKey(project.getProjectKey());
		
		target.setProjectName(project.getName());
		
		target.setInsertDatetime(nowDate);
		
		projectT01Mapper.insertSelective(target);
	}
	
	/**
	 * プロジェクト更新
	 * 
	 * @param project
	 * @param nowDate
	 * @param projectT01
	 */
	private void updateProjectT01(Project project, Date nowDate, ProjectT01 projectT01) {
		
		ProjectT01 target = new ProjectT01();
		
		// スペースキー（主キー）
		target.setSpaceKey(projectT01.getSpaceKey());
		
		// プロジェクトID（主キー）
		target.setProjectId(projectT01.getProjectId());
		
		// プロジェクトキー
		target.setProjectKey(DataSettingUtil.getUpdatedData(projectT01.getProjectKey(),project.getProjectKey()));
		
		// プロジェクト名
		target.setProjectKey(DataSettingUtil.getUpdatedData(projectT01.getProjectName(), project.getName()));
		
		// 更新日時
		target.setUpdateDatetime(nowDate);
		
		if (Objects.nonNull(target.getProjectKey())
				|| Objects.nonNull(target.getProjectKey())) {
			
			projectT01Mapper.updateByPrimaryKeySelective(target);
		}
	}
	
	/**
	 * プロジェクトリスト取得（スペースキー）
	 * 
	 * @param String spaceKey
	 */
	@Override
	public List<ProjectT01> selectProjectListBySpaceKey(String spaceKey) {
		
		ProjectT01Example example = new ProjectT01Example();
		ProjectT01Example.Criteria criteria = example.createCriteria();
		
		criteria.andSpaceKeyEqualTo(spaceKey);
		
		return projectT01Mapper.selectByExample(example);
	}
}
