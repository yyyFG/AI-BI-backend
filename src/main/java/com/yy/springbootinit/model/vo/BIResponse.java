package com.yy.springbootinit.model.vo;


import lombok.Data;

/**
 * BI 的返回结果
 */
@Data
public class BIResponse {

    private String genChart;

    private String genResult;

    private Long charId;
}
