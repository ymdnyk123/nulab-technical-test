
package com.example.demo.service;

import java.util.Date;
import java.util.List;

import com.example.demo.mybatis.model.UserT01;
import com.nulabinc.backlog4j.User;

public interface UserService {
	
	void registerMySelf(Date nowDate);
	
	void registerUser(User user, Date nowDate);
	
	UserT01 selectUserByUserNumberId(long userNumberId);
	
	List<UserT01> selectUserT01List();
}