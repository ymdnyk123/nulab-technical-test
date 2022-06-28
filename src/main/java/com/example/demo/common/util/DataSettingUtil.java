package com.example.demo.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

import com.example.demo.common.constant.Time;

public class DataSettingUtil {
	
	public static Long getUpdatedData(Long lastValue, Long thisValue) {
		
		if (lastValue == null) {
			
			if (thisValue == null) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		} else {
			
			if (lastValue.equals(thisValue)) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		}
	}
	
	public static String getUpdatedData(String lastValue, String thisValue) {
		
		if (lastValue == null) {
			
			if (thisValue == null) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		} else {
			
			if (lastValue.equals(thisValue)) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		}
	}
	
	public static Integer getUpdatedData(Integer lastValue, Integer thisValue) {
		
		if (lastValue == null) {
			
			if (thisValue == null) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		} else {
			
			if (lastValue.equals(thisValue)) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		}
	}
	
	public static Date getUpdatedData(Date lastValue, Date thisValue) {
		
		if (lastValue == null) {
			
			if (thisValue == null) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		} else {
			
			if (lastValue.equals(thisValue)) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		}
	}
	
	public static Short getUpdatedData(Short lastValue, Short thisValue) {
		
		if (lastValue == null) {
			
			if (thisValue == null) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		} else {
			
			if (lastValue.equals(thisValue)) {
				
				return null;
				
			} else {
				
				return thisValue;
			}
		}
	}
	
	
	public static BigDecimal getActualHours(LocalDateTime start, LocalDateTime end) {
		
		Duration duration = Duration.between(start, end);
		
		if (duration.isZero()) {
			
			return BigDecimal.valueOf(0);
		}
		
		BigDecimal min = new BigDecimal(duration.toMinutes());
		
		return  min.divide(new BigDecimal(Time.MINUTES_PER_HOUR), 3, RoundingMode.HALF_UP);
	}
}
