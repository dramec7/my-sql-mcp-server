package com.holin.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author holin
 * @date 2025/12/18
 */
public record JsonRpcRequest(
        String jsonrpc, // 必须是 "2.0"
        String method,  // 例如 "tools/list", "tools/call"
        JsonNode params,
        Object id       // 请求 ID，必须原样返回
) {}