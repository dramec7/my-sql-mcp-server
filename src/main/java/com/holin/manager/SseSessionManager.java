package com.holin.manager;

import com.holin.dto.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 会话管理器
 * 负责管理 Client 的长连接，以及通过长连接推送数据
 */
@Component
public class SseSessionManager {

    private static final Logger log = LoggerFactory.getLogger(SseSessionManager.class);

    // 存储会话 ID -> Emitter 的映射
    private final Map<String, SseEmitter> sessions = new ConcurrentHashMap<>();

    // 如果你的服务端口或 IP 变了，记得修改这里，或者改为从配置文件读取
    private static final String BASE_URL = "http://localhost:8080";

    /**
     * 创建一个新的 SSE 连接
     */
    public SseEmitter createConnection() {
        SseEmitter emitter = new SseEmitter(3600_000L);

        String sessionId = UUID.randomUUID().toString();

        emitter.onCompletion(() -> removeSession(sessionId, "Completed"));
        emitter.onTimeout(() -> removeSession(sessionId, "Timeout"));
        emitter.onError((e) -> removeSession(sessionId, "Error: " + e.getMessage()));

        sessions.put(sessionId, emitter);

        try {
            String endpointUrl = BASE_URL + "/messages?sessionId=" + sessionId;

            emitter.send(SseEmitter.event().name("endpoint").data(endpointUrl));

            log.info("Client connected. SessionID: {}", sessionId);
        } catch (IOException e) {
            log.error("Failed to send initial endpoint event", e);
            emitter.completeWithError(e);
            sessions.remove(sessionId);
        }

        return emitter;
    }

    /**
     * 向指定会话发送 JSON-RPC 响应
     */
    public void send(String sessionId, JsonRpcResponse response) {
        SseEmitter emitter = sessions.get(sessionId);
        if (emitter == null) {
            log.warn("Session not found or expired: {}", sessionId);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name("message").data(response));

            log.info("Sent response to [{}]. ID: {}", sessionId, response.id());
        } catch (IOException e) {
            log.error("Failed to send message to session: {}", sessionId, e);
            // 发送失败通常意味着连接已断开，主动清理
            removeSession(sessionId, "Send Failed");
        }
    }

    /**
     * 内部清理方法
     */
    private void removeSession(String sessionId, String reason) {
        // remove 返回被删除的值，如果不为 null 说明确实移除了一个存在的连接
        if (sessions.remove(sessionId) != null) {
            log.info("Session removed: {} (Reason: {})", sessionId, reason);
        }
    }
}