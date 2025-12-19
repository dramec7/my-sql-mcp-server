package com.holin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holin.dto.JsonRpcRequest;
import com.holin.dto.JsonRpcResponse;
import com.holin.exception.McpErrorCode;
import com.holin.exception.McpException;
import com.holin.tool.McpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author holin
 * @date 2025/12/18
 */
@Service
public class McpService {

    private static final Logger log = LoggerFactory.getLogger(McpService.class);

    private final Map<String, McpTool> toolRegistry;
    private final ObjectMapper objectMapper;

    // 构造器注入 List<McpTool>
    // Spring 会自动把所有实现了 McpTool 接口的 Bean 放到这个 List 里
    public McpService(List<McpTool> tools, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // 【核心修复】
        // 手动构建 Map，强制使用 tool.getName() (即 "query_database") 作为 Key
        // 而不是使用 Spring 的 Bean Name ("sqlQueryTool")
        this.toolRegistry = tools.stream()
                .collect(Collectors.toMap(
                        McpTool::getName,  // Key: 工具内部定义的名称
                        Function.identity() // Value: 工具实例本身
                ));

        log.info("Loaded {} tools: {}", toolRegistry.size(), toolRegistry.keySet());
    }


    /**
     * 核心处理入口：统一异常捕获与分发
     */
    public JsonRpcResponse process(JsonRpcRequest request) {

        log.info("Processing request method: {}", request.method());
        try {
            return switch (request.method()) {
                case "initialize" -> handleInitialize(request);
                case "tools/list" -> handleListTools(request);
                case "tools/call" -> handleToolCall(request);

                // Notification: 握手确认，返回 null (Controller 会转为 204 No Content)
                case "notifications/initialized" -> null;

                // 未知方法：直接抛出异常，进入下方 catch 块
                default ->
                        throw new McpException(McpErrorCode.METHOD_NOT_FOUND, "Method not found: " + request.method());
            };

        } catch (McpException e) {
            // 捕获业务层明确抛出的已知异常 (如: SQL语法错, 安全拦截, 表不存在)
            // 这种错误 Agent 通常可以自我修正
            log.warn("MCP Business Error [id={}]: Code={}, Msg={}", request.id(), e.getCode(), e.getMessage());
            return JsonRpcResponse.error(request.id(), e.getCode(), e.getMessage());

        } catch (Exception e) {
            // 捕获未知的系统级异常 (如: 空指针, OOM, 序列化失败)
            // 这种错误需要掩盖堆栈细节，返回 Internal Error
            log.error("MCP System Error [id={}]", request.id(), e);
            return JsonRpcResponse.error(request.id(),
                    McpErrorCode.INTERNAL_ERROR.getCode(),
                    "Internal Server Error: " + e.getMessage());
        }
    }

    private JsonRpcResponse handleInitialize(JsonRpcRequest request) {
        log.info("Client initializing...");

        var capabilities = Map.of("tools", Map.of());

        var serverInfo = Map.of(
                "name", "my-sql-mcp-server",
                "version", "1.0.0"
        );

        var result = Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", capabilities,
                "serverInfo", serverInfo
        );

        return JsonRpcResponse.success(request.id(), result);
    }

    private JsonRpcResponse handleListTools(JsonRpcRequest request) {
        log.info("Listing tools...");

        List<Map<String, Object>> toolsList = toolRegistry.values().stream()
                .map(tool -> Map.<String, Object>of(
                        "name", tool.getName(),
                        "description", tool.getDescription(),
                        "inputSchema", tool.getInputSchema()
                ))
                .toList();

        return JsonRpcResponse.success(request.id(), Map.of("tools", toolsList));
    }

    /**
     * 执行工具调用
     * 注意：这里不再进行 try-catch，让底层异常直接冒泡到 process 方法
     */
    private JsonRpcResponse handleToolCall(JsonRpcRequest request) {
        JsonNode params = request.params();
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        // 查找工具
        McpTool tool = toolRegistry.get(toolName);
        if (tool == null) {
            throw new McpException(McpErrorCode.TOOL_NOT_FOUND, "Tool not found: " + toolName);
        }

        // 执行工具
        Object resultData = tool.execute(arguments);

        try {
            String resultString = objectMapper.writeValueAsString(resultData);

            var contentItem = Map.of(
                    "type", "text",
                    "text", resultString
            );
            return JsonRpcResponse.success(request.id(), Map.of("content", List.of(contentItem)));

        } catch (Exception e) {
            throw new McpException(McpErrorCode.INTERNAL_ERROR, "Failed to serialize tool result: " + e.getMessage());
        }
    }
}