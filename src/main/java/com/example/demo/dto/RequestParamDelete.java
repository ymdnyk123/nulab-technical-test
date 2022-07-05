package com.example.demo.dto;

import javax.validation.constraints.NotNull;

import org.checkerframework.checker.index.qual.Positive;

import lombok.Data;

/**
 * 実績削除リクエストパラメータ
 */
@Data
public class RequestParamDelete {
	
	@NotNull
	@Positive
    private Long issueId;
}
