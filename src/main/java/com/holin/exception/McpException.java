package com.holin.exception;


public class McpException extends RuntimeException {

    private final McpErrorCode errorCode;

    public McpException(McpErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public McpException(McpErrorCode errorCode, String details) {
        super(details);
        this.errorCode = errorCode;
    }

    public McpException(McpErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }
}