# 使用Maven构建阶段
FROM maven:3.8.6-openjdk-8-slim AS build

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .

# 下载依赖（利用Docker缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# 运行阶段 - 使用更轻量的基础镜像
FROM openjdk:8-jre-slim

# 安装必要的工具和优化
RUN apt-get update && apt-get install -y \
    curl \
    dumb-init \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# 创建非root用户
RUN groupadd -r phonebook && useradd -r -g phonebook phonebook

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build /app/target/phonebook-1.0.0.jar app.jar

# 更改文件所有者
RUN chown -R phonebook:phonebook /app

# 切换到非root用户
USER phonebook

# 暴露端口
EXPOSE 8080

# 设置JVM优化参数
ENV JAVA_OPTS="-server \
    -Xmx1g \
    -Xms512m \
    -XX:NewRatio=2 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -XX:+UseCompressedOops \
    -XX:+UseCompressedClassPointers \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 使用dumb-init作为PID 1进程
ENTRYPOINT ["dumb-init", "--"]

# 运行应用
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]