package com.holin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * @author holin
 * @date 2025/12/17
 */
public record TableSchemaReq(
        @JsonProperty(required = true)
        @JsonPropertyDescription("数据表的名称，根据数据表名称查询该表的结构、备注等信息")
        String tableName
) {}
