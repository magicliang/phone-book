# H2数据库调试指南

## 概述

本项目已配置为在非生产环境使用H2内存数据库，支持开发调试和测试自动化。

## 环境配置

### 1. 开发环境 (dev profile)
- **配置文件**: `src/main/resources/application-dev.yml`
- **数据库**: H2内存数据库
- **测试数据**: `src/main/resources/data-dev.sql`
- **H2控制台**: 启用，支持外部访问

### 2. 测试环境 (test profile)
- **配置文件**: `src/test/resources/application-test.yml`
- **数据库**: H2内存数据库
- **测试数据**: `src/test/resources/data-test.sql`
- **H2控制台**: 启用，仅本地访问

## 启动方式

### 开发环境启动
```bash
# 使用启动脚本
./start-dev.sh

# 或者直接使用Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 测试自动化
```bash
# 运行完整的测试套件
./test-with-h2.sh

# 或者分别运行
mvn test -Dspring.profiles.active=test
mvn integration-test -Dspring.profiles.active=test
```

## H2数据库访问

### 1. H2控制台访问
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:phonebook_dev` (开发环境)
- **JDBC URL**: `jdbc:h2:mem:testdb` (测试环境)
- **用户名**: `sa`
- **密码**: (空)
- **驱动**: `org.h2.Driver`

### 2. 连接配置
```yaml
# 开发环境
spring:
  datasource:
    url: jdbc:h2:mem:phonebook_dev;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
```

## 调试功能

### 1. SQL日志
- 开发环境和测试环境都启用了SQL日志
- 可以在控制台看到执行的SQL语句
- 包含参数绑定信息

### 2. 连接池监控
- 启用了HikariCP连接池泄漏检测
- 开发环境: 30秒检测
- 测试环境: 10秒检测

### 3. 性能监控
- 启用了Hibernate统计信息
- 慢查询日志 (>100ms)
- 事务监控

## 测试数据

### 开发环境测试数据
文件: `src/main/resources/data-dev.sql`
- 包含10个示例联系人
- 自动在应用启动时加载

### 测试环境测试数据
文件: `src/test/resources/data-test.sql`
- 包含3个测试联系人
- 用于自动化测试

## 常用调试SQL

### 查看所有联系人
```sql
SELECT * FROM contact ORDER BY id;
```

### 查看表结构
```sql
SHOW COLUMNS FROM contact;
```

### 查看索引
```sql
SHOW INDEX FROM contact;
```

### 查看连接信息
```sql
SELECT * FROM INFORMATION_SCHEMA.SESSIONS;
```

## 故障排除

### 1. H2控制台无法访问
- 检查应用是否启动成功
- 确认使用了正确的profile (dev或test)
- 检查端口8080是否被占用

### 2. 数据库连接失败
- 确认JDBC URL正确
- 检查用户名密码 (sa/空密码)
- 确认驱动类名正确

### 3. 测试数据未加载
- 检查SQL文件路径是否正确
- 确认SQL语法正确
- 查看应用启动日志

## 最佳实践

### 1. 开发调试
- 使用H2控制台查看数据状态
- 利用SQL日志分析性能问题
- 监控连接池使用情况

### 2. 测试自动化
- 每个测试方法使用独立的事务
- 测试后自动回滚数据
- 使用TestDataFactory创建测试数据

### 3. 性能优化
- 监控慢查询日志
- 检查连接池配置
- 分析Hibernate统计信息

## 配置参数说明

### H2数据库URL参数
- `DB_CLOSE_DELAY=-1`: 防止数据库过早关闭
- `DB_CLOSE_ON_EXIT=FALSE`: JVM退出时不关闭数据库
- `MODE=MySQL`: 兼容MySQL语法
- `DATABASE_TO_LOWER=TRUE`: 数据库名转小写
- `CASE_INSENSITIVE_IDENTIFIERS=TRUE`: 标识符不区分大小写

### 连接池配置
- `maximum-pool-size`: 最大连接数
- `minimum-idle`: 最小空闲连接数
- `leak-detection-threshold`: 连接泄漏检测阈值
- `connection-timeout`: 连接超时时间

通过以上配置，项目在非生产环境完全使用H2数据库，提供了完整的调试和测试自动化支持。