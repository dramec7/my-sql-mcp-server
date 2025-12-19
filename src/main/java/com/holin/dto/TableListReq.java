package com.holin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author holin
 * @date 2025/12/17
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 允许客户端传参但我们忽略，防止报错
public record TableListReq() {
}