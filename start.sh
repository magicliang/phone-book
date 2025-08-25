#!/bin/bash

# 电话号码簿项目启动脚本

echo "==================================="
echo "  电话号码簿管理系统启动脚本"
echo "==================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装Java 8或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请安装Maven 3.6或更高版本"
    exit 1
fi

# 设置环境变量（如果未设置）
export DB_HOST=${DB_HOST:-"11.142.154.110"}
export DB_PORT=${DB_PORT:-"3306"}
export DB_NAME=${DB_NAME:-"fuaq8xj3"}
export DB_USERNAME=${DB_USERNAME:-"with_meszuizkjkswzzpl"}
export DB_PASSWORD=${DB_PASSWORD:-"G50laKOk@s\$lDg"}

echo "数据库配置:"
echo "  主机: $DB_HOST"
echo "  端口: $DB_PORT"
echo "  数据库: $DB_NAME"
echo "  用户名: $DB_USERNAME"
echo ""

# 检查数据库连接
echo "正在检查数据库连接..."
if command -v mysql &> /dev/null; then
    if mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USERNAME" -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null; then
        echo "✓ 数据库连接成功"
    else
        echo "⚠ 数据库连接失败，但程序仍会尝试启动"
    fi
else
    echo "⚠ 未安装MySQL客户端，跳过数据库连接检查"
fi

echo ""
echo "正在启动应用..."
echo "访问地址: http://localhost:8080"
echo "API地址: http://localhost:8080/api/contacts"
echo ""
echo "按 Ctrl+C 停止应用"
echo ""

# 启动Spring Boot应用
mvn spring-boot:run