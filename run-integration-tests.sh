#!/bin/bash

echo "开始运行电话簿应用集成测试..."

# 设置测试环境
export SPRING_PROFILES_ACTIVE=test

# 运行集成测试
mvn clean test -Dtest="*IntegrationTest" -Dspring.profiles.active=test

# 检查测试结果
if [ $? -eq 0 ]; then
    echo "✅ 所有集成测试通过！"
    echo "📊 测试报告位置: target/surefire-reports/"
else
    echo "❌ 集成测试失败，请检查日志"
    exit 1
fi

echo "集成测试完成"