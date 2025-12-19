# MySQL MCP Server (Java Implementation)

基于 **Model Context Protocol (MCP)** 标准实现的 MySQL 数据库服务连接器。该项目允许 AI Agent（如 Claude Desktop, Cline 等）通过标准化协议安全地连接、查询和分析 MySQL 数据库中的数据。

## 🛠 技术栈

Language: Java 21

Framework: Spring Boot 3.2+

Protocol: MCP (Model Context Protocol) via HTTP SSE (Server-Sent Events)

Database: MySQL

## ✨ 核心功能 (Tools)

本项目向 AI Agent 暴露了以下三个核心工具：

1. table_list

    功能: 获取当前数据库中所有可用数据表的名称列表。

    用途: 资产发现与结构概览。


2. table_schema

    参数: tableName (String)

    功能: 获取指定表的详细结构（字段名、数据类型、列注释等）。

    用途: 帮助 Agent 理解业务字段含义，构建正确的 SQL。


3. query_database

    参数: sql (String)

    功能: 执行标准的 SQL SELECT 查询语句。

    安全机制: 仅支持只读查询（SELECT），拦截 DELETE/UPDATE/DROP 等高危操作。
