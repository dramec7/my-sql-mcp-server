package com.holin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * @author holin
 * @date 2025/12/17
 */
public record SqlQueryReq(
        @JsonProperty(required = true)
        @JsonPropertyDescription("SQL 查询语句，支持标准 SQL 语法")
        String sql
) {}
