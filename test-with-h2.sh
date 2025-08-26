#!/bin/bash

# H2数据库测试自动化脚本

echo "=== 电话号码簿系统 - H2数据库测试自动化 ==="
echo ""

# 设置测试环境
export SPRING_PROFILES_ACTIVE=test
export MAVEN_OPTS="-Xms256m -Xmx512m"

# 清理之前的测试结果
echo "清理之前的测试结果..."
mvn clean
echo ""

# 编译项目
echo "编译项目..."
mvn compile test-compile
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✅ 编译成功"
echo ""

# 运行单元测试
echo "运行单元测试 (使用H2数据库)..."
mvn test -Dspring.profiles.active=test
UNIT_TEST_RESULT=$?
echo ""

# 运行集成测试
echo "运行集成测试 (使用H2数据库)..."
mvn integration-test -Dspring.profiles.active=test
INTEGRATION_TEST_RESULT=$?
echo ""

# 生成测试覆盖率报告
echo "生成测试覆盖率报告..."
mvn jacoco:report
echo ""

# 显示测试结果
echo "=== 测试结果汇总 ==="
if [ $UNIT_TEST_RESULT -eq 0 ]; then
    echo "✅ 单元测试: 通过"
else
    echo "❌ 单元测试: 失败"
fi

if [ $INTEGRATION_TEST_RESULT -eq 0 ]; then
    echo "✅ 集成测试: 通过"
else
    echo "❌ 集成测试: 失败"
fi

echo ""
echo "=== H2数据库测试配置 ==="
echo "- 测试数据库: H2 (内存数据库)"
echo "- 测试Profile: test"
echo "- 数据初始化: src/test/resources/data-test.sql"
echo "- 测试配置: src/test/resources/application-test.yml"
echo ""

# 显示报告位置
echo "=== 测试报告 ==="
echo "- 单元测试报告: target/surefire-reports/"
echo "- 集成测试报告: target/failsafe-reports/"
echo "- 覆盖率报告: target/site/jacoco/index.html"
echo ""

# 检查覆盖率
if [ -f "target/site/jacoco/index.html" ]; then
    echo "📊 测试覆盖率报告已生成，可以在浏览器中打开查看："
    echo "file://$(pwd)/target/site/jacoco/index.html"
fi

# 返回测试结果
if [ $UNIT_TEST_RESULT -eq 0 ] && [ $INTEGRATION_TEST_RESULT -eq 0 ]; then
    echo ""
    echo "🎉 所有测试通过！H2数据库测试自动化成功完成。"
    exit 0
else
    echo ""
    echo "⚠️  部分测试失败，请检查测试日志。"
    exit 1
fi