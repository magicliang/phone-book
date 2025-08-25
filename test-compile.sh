#!/bin/bash

echo "测试Java 8兼容性编译..."

# 清理并编译项目
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ 编译成功！项目已成功适配Java 8"
else
    echo "❌ 编译失败，请检查错误信息"
    exit 1
fi

echo "测试完成！"