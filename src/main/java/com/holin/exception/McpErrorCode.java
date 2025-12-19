package com.holin.exception;

import lombok.Getter;

@Getter
public enum McpErrorCode {

    // === JSON-RPC 2.0 标准错误码 ===
    PARSE_ERROR(-32700, "Parse error"),
    INVALID_REQUEST(-32600, "Invalid Request"),
    METHOD_NOT_FOUND(-32601, "Method not found"),
    INVALID_PARAMS(-32602, "Invalid params"),
    INTERNAL_ERROR(-32603, "Internal error"),

    // === 自定义业务错误码 (-32000 to -32099) ===
    // 安全类错误
    SECURITY_VIOLATION(-32001, "Security violation"),

    // 数据库/执行类错误
    TOOL_NOT_FOUND(-32002, "Tool not found"),
    TOOL_EXECUTION_FAILED(-32003, "Tool execution failed"),
    DATABASE_ERROR(-32004, "Database error");

    private final int code;
    private final String message;

    McpErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}