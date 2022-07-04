package com.example.demo.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.checkerframework.checker.index.qual.Positive;

import lombok.Data;

/**
 * 実績登録リクエストパラメータ
 */
@Data
public class RequestParamSend {
	
	@NotNull
	@Positive
    private Long issueId;
	
	@NotNull
	@Positive
	@Digits(integer = 3, fraction = 2)
	private BigDecimal actualHours;
}
