
package com.example.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.demo.common.util.DataSettingUtil;
import com.example.demo.dto.CommonDto;
import com.example.demo.mybatis.mapper.PriorityT01Mapper;
import com.example.demo.mybatis.model.PriorityT01;
import com.nulabinc.backlog4j.Priority;
import com.nulabinc.backlog4j.ResponseList;

@Service
public class PriorityServiceImpl implements PriorityService {

	@Autowired
	PriorityT01Mapper priorityT01Mapper;
	
	@Autowired
	CommonDto commonDto;
	
	/**
	 * 優先度登録（更新）
	 * 
	 * @param priority
	 * @param owDate
	 */
	@Override
	public void registerPriority(Date nowDate) {
		
		// 優先度一覧の取得
		ResponseList<Priority> priorities = this.commonDto.getBacklogClient().getPriorities();
		
		if (ObjectUtils.isEmpty(priorities)) {
			
			return;
		}
		
		priorities.stream().forEach(priority ->{
			
			PriorityT01 priorityT01 = priorityT01Mapper.selectByPrimaryKey(priority.getId());
			
			if (ObjectUtils.isEmpty(priorityT01)) {
				
				// 登録
				this.insertPriorityT01(priority, nowDate);
				
			} else {
				
				// 更新
				this.updatePriorityT01(priority, nowDate, priorityT01);
			}
		});
	}
	
	/**
	 * 優先度登録
	 * 
	 * @param priority
	 * @param nowDate
	 */
	private void insertPriorityT01(Priority priority, Date nowDate) {
		
		PriorityT01 target = new PriorityT01();
		
		// 優先度ID（主キー）
		target.setPriorityId(priority.getId());
		
		// 優先度名
		target.setPriorityName(priority.getName());
		
		// 登録日時
		target.setInsertDatetime(nowDate);
		
		priorityT01Mapper.insertSelective(target);
	}
	
	/**
	 * 優先度更新
	 * 
	 * @param priority
	 * @param nowDate
	 * @param priorityT01
	 */
	private void updatePriorityT01(Priority priority, Date nowDate, PriorityT01 priorityT01) {
		
		PriorityT01 target = new PriorityT01();
		
		// 優先度ID（主キー）
		target.setPriorityId(priorityT01.getPriorityId());
		
		// 優先度名
		target.setPriorityName(DataSettingUtil.getUpdatedData(priorityT01.getPriorityName(), priority.getName()));
		
		// 登録日時
		target.setUpdateDatetime(nowDate);
		
		if (Objects.nonNull(target.getPriorityName())) {
			
			priorityT01Mapper.insertSelective(target);
		}
	}

	/**
	 * 優先度リスト取得（全件）
	 * 
	 * @return 優先度リスト
	 */
	@Override
	public List<PriorityT01> selectPriorityT01() {
		
		return priorityT01Mapper.selectByExample(null);
	}
}
