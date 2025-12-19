package com.holin.service;

import com.holin.exception.McpErrorCode;
import com.holin.exception.McpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author holin
 * @date 2025/12/17
 */
@Service
public class DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);

    @Autowired
    private DataSource dataSource;

    // 获取所有表名
    public List<String> listTables() {
        log.info("Fetching database table list...");
        List<String> tables = new ArrayList<>();

        // 使用 try-with-resources 自动关闭连接，防止连接泄漏
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 这里的 catalog 通常对应 MySQL 的数据库名
            // 获取类型为 "TABLE" 的对象 (排除 VIEW, SYSTEM TABLE 等)
            try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            log.info("Successfully found {} tables.", tables.size());
            return tables;

        } catch (SQLException e) {
            // 捕获 SQL 异常 (如连接超时、Access Denied)
            log.error("Failed to fetch table meta data", e);

            // 抛出标准 MCP 异常
            // e.getMessage() 会包含如 "Access denied for user..." 或 "Communications link failure" 等有用信息
            throw new McpException(McpErrorCode.DATABASE_ERROR,
                    "Failed to retrieve table list from database: " + e.getMessage());
        }
    }

    // 获取表结构详细信息
    public List<Map<String, Object>> getTableSchema(String tableName) {
        log.info("Fetching schema for table: {}", tableName);
        List<Map<String, Object>> columns = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 获取列信息
            // 注意：MySQL 通常忽略 schemaPattern (第二个参数)，主要依赖 catalog (第一个参数) 或 tableName
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, null)) {
                while (rs.next()) {
                    // 使用 LinkedHashMap 保证 JSON 输出顺序：name -> type -> comment
                    // 这样 Agent 阅读起来更顺畅
                    Map<String, Object> col = new LinkedHashMap<>();

                    col.put("name", rs.getString("COLUMN_NAME"));
                    col.put("type", rs.getString("TYPE_NAME")); // 如 VARCHAR, BIGINT
                    col.put("size", rs.getInt("COLUMN_SIZE"));

                    // 转换 boolean 更直观
                    boolean isNullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                    col.put("nullable", isNullable);

                    // 获取注释 (前提：JDBC URL 已配置 remarks=true)
                    String remarks = rs.getString("REMARKS");
                    col.put("comment", (remarks != null && !remarks.isBlank()) ? remarks : "No description");

                    columns.add(col);
                }
            }

            // 关键检查：如果循环结束后 list 依然为空，说明表名可能错了
            if (columns.isEmpty()) {
                log.warn("Table '{}' not found or empty schema.", tableName);
                // 抛出 INVALID_PARAMS，提示 Agent 检查表名
                throw new McpException(McpErrorCode.INVALID_PARAMS,
                        "Table '" + tableName + "' not found. Please use 'list_tables' tool to verify the table name.");
            }

            log.info("Successfully fetched schema for table '{}'. Columns count: {}", tableName, columns.size());
            return columns;

        } catch (SQLException e) {
            log.error("Failed to fetch schema for table: {}", tableName, e);
            throw new McpException(McpErrorCode.DATABASE_ERROR,
                    "Database error while inspecting table '" + tableName + "': " + e.getMessage());
        }
    }
}
