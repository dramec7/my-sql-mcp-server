package com.holin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author holin
 * @date 2025/12/17
 */
public class SqlSecurityTest {

    private final SqlService service = new SqlService();

    @Test
    void shouldAllowSimpleSelect() {
        assertDoesNotThrow(() -> service.validate("SELECT * FROM users WHERE id = 1"));
//        service.validate("SELECT * FROM users WHERE id = 1");
    }

    @Test
    void shouldBlockDelete() {
        assertThrows(SecurityException.class, () -> service.validate("DELETE FROM users"));
    }

    @Test
    void shouldBlockDrop() {
        assertThrows(SecurityException.class, () -> service.validate("DROP TABLE users"));
    }

    @Test
    void shouldBlockMultiStatementInjection() {
        String dangerousSql = "SELECT * FROM users; DROP TABLE users";
        assertThrows(SecurityException.class, () -> service.validate(dangerousSql));
    }

    @Test
    void shouldBlockAlter() {
        assertThrows(SecurityException.class, () -> service.validate("ALTER TABLE users ADD COLUMN age INT"));
    }
}