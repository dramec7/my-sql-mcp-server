package com.holin.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holin.dto.SqlQueryReq;
import com.holin.dto.TableListReq;
import com.holin.schema.SchemaHelper;
import com.holin.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author holin
 * @date 2025/12/17
 */
@Component
public class TableListTool implements McpTool {

    @Autowired
    private SchemaHelper schemaHelper;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DatabaseService databaseService;

    @Override
    public String getName() {
        return "table_list";
    }

    @Override
    public String getDescription() {
        return "获取并显示当前数据库中所有可用数据表的名称列表，用于了解数据库结构和可用的表资源";
    }

    @Override
    public JsonNode getInputSchema() {
        return schemaHelper.generate(TableListReq.class);
    }

    @Override
    public Object execute(JsonNode arguments) {
        return databaseService.listTables();
    }
}
