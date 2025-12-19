package com.holin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * @author holin
 * @date 2025/12/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcResponse(
        String jsonrpc,
        Object result,
        Object error,
        Object id
) {
    public static JsonRpcResponse success(Object id, Object result) {
        return new JsonRpcResponse("2.0", result, null, id);
    }

    public static JsonRpcResponse error(Object id, int code, String message) {
        var errorBody = Map.of(
                "code", code,
                "message", message
        );
        return new JsonRpcResponse("2.0", null, errorBody, id);
    }

    record ErrorBody(int code, String message) {}
}
