package com.holin.service;

import com.holin.exception.McpErrorCode;
import com.holin.exception.McpException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author holin
 * @date 2025/12/17
 */
@Service
public class SqlService {

    private static final Logger log = LoggerFactory.getLogger(SqlService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // sql安全校验
    public void validate(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new McpException(McpErrorCode.INVALID_PARAMS, "SQL content cannot be empty");
        }

        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sql);

            if (statements.isEmpty()) {
                throw new McpException(McpErrorCode.INVALID_PARAMS, "SQL content is empty or invalid.");
            }

            if (statements.size() > 1) {
                log.warn("Security Alert: Multi-statement transaction blocked. SQL: {}", sql);
                throw new McpException(McpErrorCode.SECURITY_VIOLATION,
                        "Batch execution is not allowed. Please execute one SQL at a time.");
            }

            Statement statement = statements.getFirst();

            if (!(statement instanceof Select)) {
                log.warn("Security Alert: Dangerous operation blocked. Type: {}, SQL: {}",
                        statement.getClass().getSimpleName(), sql);
                throw new McpException(McpErrorCode.SECURITY_VIOLATION,
                        "Operation forbidden: Only SELECT statements are allowed.");
            }

            log.debug("SQL validation passed.");

        } catch (JSQLParserException e) {
            log.error("SQL parsing failed: {}", e.getMessage());
            throw new McpException(McpErrorCode.DATABASE_ERROR,
                    "SQL Syntax Error: " + e.getCause().getMessage());
        }
    }

    // 执行sql
    @Transactional(readOnly = true) // 标记为只读事务，优化数据库性能
    public List<Map<String, Object>> executeQuery(String sql) {
        log.info("Processing SQL query request...");

        validate(sql);

        long startTime = System.currentTimeMillis();
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

            long duration = System.currentTimeMillis() - startTime;
            log.info("SQL executed successfully in {}ms. Rows returned: {}", duration, result.size());

            if (result.size() > 1000) {
                log.warn("Query result too large ({} rows), truncating to 1000 rows. SQL: {}", result.size(), sql);
                return result.subList(0, 1000);
            }

            return result;

        } catch (BadSqlGrammarException e) {
            // sql语法
            String syntaxMessage = e.getSQLException().getMessage();

            log.warn("SQL Syntax Error: {}", syntaxMessage);
            throw new McpException(McpErrorCode.DATABASE_ERROR, "Database Error: " + syntaxMessage);

        } catch (DataAccessException e) {
            // 其他异常（如连接超时、数据库宕机）
            log.error("Database connectivity or execution error", e);
            throw new McpException(McpErrorCode.DATABASE_ERROR,
                    "Database Error: " + e.getMostSpecificCause().getMessage());

        } catch (Exception e) {
            // 未知异常（如 OOM、序列化失败等）
            log.error("Unexpected error during SQL execution", e);
            throw new McpException(McpErrorCode.INTERNAL_ERROR, "Internal Execution Error: " + e.getMessage());
        }
    }
}