#!/bin/bash

echo "=== 电话号码簿项目单元测试执行脚本 ==="
echo "开始执行所有单元测试..."

# 清理之前的构建
echo "1. 清理项目..."
mvn clean

# 编译项目
echo "2. 编译项目..."
mvn compile test-compile

# 运行所有测试
echo "3. 运行单元测试..."
mvn test

# 生成测试报告
echo "4. 生成测试覆盖率报告..."
mvn jacoco:report

echo "=== 测试执行完成 ==="
echo "测试报告位置: target/site/jacoco/index.html"
echo "Surefire测试报告: target/surefire-reports/"