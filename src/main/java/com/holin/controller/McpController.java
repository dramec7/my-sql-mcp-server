package com.holin.controller;

import com.holin.dto.JsonRpcRequest;
import com.holin.dto.JsonRpcResponse;
import com.holin.manager.SseSessionManager;
import com.holin.service.McpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * @author holin
 * @date 2025/12/17
 */
@RestController
@CrossOrigin(origins = "*")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    @Autowired
    private McpService mcpService;

    @Autowired
    private SseSessionManager sessionManager;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        return sessionManager.createConnection();
    }

    @PostMapping("/messages")
    public void handleMessage(
            @RequestBody JsonRpcRequest request,
            @RequestParam String sessionId // 必须从 URL 参数获取 SessionID
    ) {
        // 1. 调用 Service 计算结果 (同步执行)
        JsonRpcResponse response = mcpService.process(request);

        // 2. 如果是 Notification (response 为 null)，不需要回复
        if (response == null) {
            return;
        }
        sessionManager.send(sessionId, response);
    }

}
