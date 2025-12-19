package com.holin.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holin.dto.SqlQueryReq;
import com.holin.dto.TableSchemaReq;
import com.holin.schema.SchemaHelper;
import com.holin.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author holin
 * @date 2025/12/17
 */
@Component
public class TableSchemaTool implements McpTool{

    @Autowired
    private SchemaHelper schemaHelper;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DatabaseService databaseService;

    @Override
    public String getName() {
        return "table_schema";
    }

    @Override
    public String getDescription() {
        return "根据数据表名称查询该表的结构、备注等信息";
    }

    @Override
    public JsonNode getInputSchema() {
        return schemaHelper.generate(TableSchemaReq.class);
    }

    @Override
    public Object execute(JsonNode arguments) {
        TableSchemaReq req = mapper.convertValue(arguments, TableSchemaReq.class);
        return databaseService.getTableSchema(req.tableName());
    }
}
