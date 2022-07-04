package com.example.demo.dto;

import javax.validation.constraints.NotNull;

import org.checkerframework.checker.index.qual.Positive;

import lombok.Data;

/**
 * 実績登録リクエストパラメータ
 */
@Data
public class RequestParamRegister {
	
	@NotNull
	@Positive
    private Long issueId;
	
	@NotNull
	private Boolean isStart;
}
