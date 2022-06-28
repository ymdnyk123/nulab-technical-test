
package com.example.demo.service;

import java.util.Date;
import java.util.List;

import com.example.demo.mybatis.model.ProjectT01;

public interface ProjectService {
	
	void registerProjects(Date nowDate);
	
	List<ProjectT01> selectProjectListBySpaceKey(String spaceKey);
}