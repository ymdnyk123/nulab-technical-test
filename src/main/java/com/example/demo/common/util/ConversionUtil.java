package com.example.demo.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ConversionUtil {
	
	public static LocalDateTime toLocalDateTime(Date date) {
		
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	public static Long toLong(String val) {
		
		try {
			
			return Long.parseLong(val);
			
		} catch (NumberFormatException e) {
			
			return null;
		}
	}
}
