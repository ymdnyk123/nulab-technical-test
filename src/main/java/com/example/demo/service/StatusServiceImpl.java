
package com.example.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.common.util.DataSettingUtil;
import com.example.demo.dto.CommonDto;
import com.example.demo.mybatis.mapper.StatusT01Mapper;
import com.example.demo.mybatis.model.StatusT01;
import com.example.demo.mybatis.model.StatusT01Example;
import com.example.demo.mybatis.model.StatusT01Key;
import com.nulabinc.backlog4j.Project;
import com.nulabinc.backlog4j.ResponseList;
import com.nulabinc.backlog4j.Status;

@Service
public class StatusServiceImpl implements StatusService {

	@Autowired
	StatusT01Mapper statusT01Mapper;
	
	@Autowired
	CommonDto commonDto;
	
	/**
	 * ステータス登録（更新）
	 * 
	 *  @param status
	 *  @param nowDate
	 */
	@Override
	public void registerStatus(Date nowDate) {
		
		// プロジェクト一覧取得
		ResponseList<Project> projects = this.commonDto.getBacklogClient().getProjects();
		
		if (ObjectUtils.isEmpty(projects)) {
			
			return;
		}

		projects.stream().forEach(project -> {
			
			// プロジェクトの状態一覧の取得
			ResponseList<Status> statuses = this.commonDto.getBacklogClient().getStatuses(project.getId());
			
			if (Objects.nonNull(statuses)) {
				
				statuses.stream().forEach(status -> {
					
					StatusT01Key key = new StatusT01Key();
					
					key.setProjectId(status.getProjectId());
					
					key.setStatusId(status.getId());
					
					StatusT01 statusT01 = statusT01Mapper.selectByPrimaryKey(key);
					
					if (ObjectUtils.isEmpty(statusT01)) {
						
						// 登録
						this.insertStatusT01(status, nowDate);
						
					} else {
						
						// 更新
						this.updateStatusT01(status, nowDate, statusT01);
					}
				});
			}
		});
	}
	
	/**
	 * ステータス登録
	 * @param status
	 * @param nowDate
	 */
	private void insertStatusT01(Status status, Date nowDate) {
		
		StatusT01 target = new StatusT01();
		
		// プロジェクトID（主キー）
		target.setProjectId(status.getProjectId());
		
		// ステータスID（主キー）
		target.setStatusId(status.getId());
		
		// ステータス名
		target.setStatusName(status.getName());
		
		// カラー
		if (!ObjectUtils.isEmpty(status.getColor())) {
			
			target.setStatusColor(status.getColor().getStrValue());
		}
		
		// 表示順
		target.setDisplayOrder(status.getDisplayOrder());
		
		// 登録日時
		target.setInsertDatetime(nowDate);
		
		statusT01Mapper.insertSelective(target);
	}
	
	/**
	 * ステータス更新
	 * 
	 * @param status
	 * @param nowDate
	 * @param statusT01
	 */
	private void updateStatusT01(Status status, Date nowDate, StatusT01 statusT01) {
		
		StatusT01 target = new StatusT01();
		
		// プロジェクトID（主キー）
		target.setProjectId(statusT01.getProjectId());
		
		// ステータスID（主キー）
		target.setStatusId(statusT01.getStatusId());
		
		// ステータス名
		target.setStatusName(DataSettingUtil.getUpdatedData(statusT01.getStatusName(), status.getName()));
		
		// カラー
		if (ObjectUtils.isEmpty(status.getColor())) {
			
			target.setStatusColor(
					DataSettingUtil.getUpdatedData(statusT01.getStatusColor(), null));
			
		} else {
			
			target.setStatusColor(
					DataSettingUtil.getUpdatedData(statusT01.getStatusColor(), status.getColor().getStrValue()));
		}
		
		// 表示順
		target.setDisplayOrder(DataSettingUtil.getUpdatedData(statusT01.getDisplayOrder(), status.getDisplayOrder()));
		
		// 更新日時
		target.setUpdateDatetime(nowDate);
		
		if (Objects.nonNull(target.getStatusName()) 
				|| Objects.nonNull(target.getStatusColor())
				|| Objects.nonNull(target.getDisplayOrder())) {
			
			statusT01Mapper.updateByPrimaryKeySelective(target);
		}
	}
	
	/**
	 * ステータスリスト取得（プロジェクトID）
	 * 
	 * @return ステータスリスト
	 */
	@Override
	public List<StatusT01> selectStatusT01ListByProjectId(long projectId) {
		
		StatusT01Example example = new StatusT01Example();
		
		StatusT01Example.Criteria criteria = example.createCriteria();
		
		// プロジェクトID
		criteria.andProjectIdEqualTo(projectId);
		
		return statusT01Mapper.selectByExample(example);
	}
	
	/**
	 * ステータスリスト取得
	 * 
	 * @return ステータスリスト
	 */
	@Override
	public List<StatusT01> selectStatusT01List() {
		
		return statusT01Mapper.selectByExample(null);
	}
}
