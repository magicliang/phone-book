#!/bin/bash

echo "开始运行电话簿应用性能测试..."

# 设置测试环境
export SPRING_PROFILES_ACTIVE=test

# 运行性能测试
mvn clean test -Dtest="*PerformanceTest" -Dspring.profiles.active=test

# 检查测试结果
if [ $? -eq 0 ]; then
    echo "✅ 所有性能测试通过！"
    echo "📊 性能测试报告位置: target/surefire-reports/"
else
    echo "❌ 性能测试失败，请检查日志"
    exit 1
fi

echo "性能测试完成"