package com.holin.tool;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * @author holin
 * @date 2025/12/17
 */
@Component
public interface McpTool {
    // 工具名称
    String getName();

    // 工具描述
    String getDescription();

    // 参数定义的 Schema
    JsonNode getInputSchema();

    // 具体的执行逻辑
    Object execute(JsonNode arguments);
}
