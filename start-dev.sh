#!/bin/bash

# 开发环境启动脚本 - 使用H2数据库进行调试

echo "=== 电话号码簿系统 - 开发环境启动 ==="
echo "使用H2内存数据库进行开发调试"
echo ""

# 设置开发环境变量
export SPRING_PROFILES_ACTIVE=dev
export JAVA_OPTS="-Xms256m -Xmx512m -Dspring.profiles.active=dev"

# 显示配置信息
echo "环境配置："
echo "- Profile: dev"
echo "- 数据库: H2 (内存数据库)"
echo "- H2控制台: http://localhost:8080/h2-console"
echo "- 应用地址: http://localhost:8080"
echo "- 监控端点: http://localhost:8080/actuator"
echo ""

# 检查Java版本
java -version
echo ""

# 启动应用
echo "正在启动应用..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo ""
echo "=== 开发环境信息 ==="
echo "H2数据库连接信息："
echo "- JDBC URL: jdbc:h2:mem:phonebook_dev"
echo "- 用户名: sa"
echo "- 密码: (空)"
echo "- 驱动类: org.h2.Driver"
echo ""
echo "访问H2控制台进行数据库调试："
echo "http://localhost:8080/h2-console"