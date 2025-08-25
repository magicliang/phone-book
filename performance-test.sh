#!/bin/bash

# 性能测试脚本
# 用于测试Phonebook应用的性能表现

set -e

# 配置
BASE_URL="http://localhost:8080"
CONCURRENT_USERS=50
TEST_DURATION=60
RAMP_UP_TIME=10

echo "=== Phonebook 应用性能测试 ==="
echo "基础URL: $BASE_URL"
echo "并发用户数: $CONCURRENT_USERS"
echo "测试持续时间: ${TEST_DURATION}秒"
echo "启动时间: ${RAMP_UP_TIME}秒"
echo

# 检查应用是否运行
echo "检查应用状态..."
if ! curl -f "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "错误: 应用未运行或健康检查失败"
    exit 1
fi
echo "✓ 应用运行正常"

# 创建测试数据
echo "创建测试数据..."
for i in {1..100}; do
    curl -s -X POST "$BASE_URL/api/contacts" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"测试用户$i\",
            \"phoneNumber\": \"1380000$(printf "%04d" $i)\",
            \"email\": \"test$i@example.com\",
            \"category\": \"test\",
            \"address\": \"测试地址$i\"
        }" > /dev/null
done
echo "✓ 创建了100个测试联系人"

# 性能测试函数
run_performance_test() {
    local test_name=$1
    local url=$2
    local method=${3:-GET}
    
    echo "运行测试: $test_name"
    
    # 使用Apache Bench进行性能测试
    if command -v ab > /dev/null; then
        echo "使用Apache Bench进行测试..."
        ab -n 1000 -c 10 -g "${test_name}_results.tsv" "$url" | grep -E "(Requests per second|Time per request|Transfer rate)"
    fi
    
    # 使用curl进行简单的响应时间测试
    echo "测试响应时间..."
    total_time=0
    for i in {1..10}; do
        response_time=$(curl -o /dev/null -s -w "%{time_total}" "$url")
        total_time=$(echo "$total_time + $response_time" | bc -l)
        echo "请求 $i: ${response_time}s"
    done
    avg_time=$(echo "scale=3; $total_time / 10" | bc -l)
    echo "平均响应时间: ${avg_time}s"
    echo
}

# 运行各种性能测试
echo "=== 开始性能测试 ==="

# 1. 获取联系人列表
run_performance_test "获取联系人列表" "$BASE_URL/api/contacts?page=0&size=10"

# 2. 搜索联系人
run_performance_test "搜索联系人" "$BASE_URL/api/contacts/search?keyword=测试&page=0&size=10"

# 3. 获取分类统计
run_performance_test "获取分类统计" "$BASE_URL/api/contacts/statistics"

# 4. 获取单个联系人
run_performance_test "获取单个联系人" "$BASE_URL/api/contacts/1"

# 5. 按分类查询
run_performance_test "按分类查询" "$BASE_URL/api/contacts/category/test?page=0&size=10"

# 内存和CPU使用情况
echo "=== 系统资源使用情况 ==="
if command -v docker > /dev/null; then
    echo "Docker容器资源使用:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
fi

# JVM内存使用情况
echo "JVM内存使用情况:"
curl -s "$BASE_URL/actuator/metrics/jvm.memory.used" | grep -o '"value":[0-9]*' | cut -d':' -f2

# 数据库连接池状态
echo "数据库连接池状态:"
curl -s "$BASE_URL/actuator/metrics/hikaricp.connections.active" | grep -o '"value":[0-9]*' | cut -d':' -f2

# 缓存命中率
echo "缓存统计:"
curl -s "$BASE_URL/actuator/metrics" | grep -i cache

echo "=== 性能测试完成 ==="
echo "详细结果已保存到 *_results.tsv 文件中"

# 清理测试数据
echo "清理测试数据..."
for i in {1..100}; do
    curl -s -X DELETE "$BASE_URL/api/contacts/$i" > /dev/null 2>&1 || true
done
echo "✓ 测试数据清理完成"