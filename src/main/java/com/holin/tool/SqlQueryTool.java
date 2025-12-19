package com.holin.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holin.dto.SqlQueryReq;
import com.holin.schema.SchemaHelper;
import com.holin.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author holin
 * @date 2025/12/17
 */
@Component
public class SqlQueryTool implements McpTool {

    @Autowired
    private SchemaHelper schemaHelper;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SqlService sqlService;

    @Override
    public String getName() {
        return "query_database";
    }

    @Override
    public String getDescription() {
        return "在数据库中执行一条 SELECT 语句";
    }

    @Override
    public JsonNode getInputSchema() {
        return schemaHelper.generate(SqlQueryReq.class);
    }

    @Override
    public Object execute(JsonNode arguments) {
        SqlQueryReq req = mapper.convertValue(arguments, SqlQueryReq.class);
        return sqlService.executeQuery(req.sql());
    }
}
