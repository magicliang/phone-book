# Phonebook 应用性能优化指南

## 概述

本文档详细介绍了对 Phonebook 应用实施的全面性能优化措施，包括数据库优化、缓存策略、JVM调优、容器化优化等多个方面。

## 🚀 性能优化措施

### 1. 数据库层面优化

#### 1.1 索引优化
- **主要索引**：
  - `idx_phone_number`: 电话号码唯一索引
  - `idx_email`: 邮箱索引
  - `idx_name`: 姓名索引
  - `idx_category`: 分类索引
  - `idx_created_at`: 创建时间索引

- **复合索引**：
  - `idx_name_phone`: 姓名+电话复合索引
  - `idx_category_name`: 分类+姓名复合索引

- **全文搜索索引**：
  - 对 `name` 和 `email` 字段创建全文索引，提升搜索性能

#### 1.2 连接池优化 (HikariCP)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # 最大连接数
      minimum-idle: 5            # 最小空闲连接
      connection-timeout: 30000  # 连接超时
      idle-timeout: 600000       # 空闲超时
      max-lifetime: 1800000      # 连接最大生命周期
      leak-detection-threshold: 60000  # 连接泄漏检测
```

#### 1.3 JPA/Hibernate 优化
- **批量操作**：启用批量插入和更新
- **二级缓存**：启用 Hibernate 二级缓存
- **查询优化**：使用 `@QueryHints` 优化查询性能
- **懒加载**：合理使用懒加载策略

### 2. 缓存策略

#### 2.1 Redis 缓存配置
- **联系人列表缓存**：5分钟 TTL
- **联系人详情缓存**：15分钟 TTL
- **搜索结果缓存**：3分钟 TTL
- **分类统计缓存**：30分钟 TTL

#### 2.2 缓存注解使用
```java
@Cacheable(value = "contacts", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
@CacheEvict(value = {"contacts", "searchResults"}, allEntries = true)
@CachePut(value = "contact", key = "#contactDTO.id")
```

### 3. JVM 性能调优

#### 3.1 内存配置
```bash
JAVA_OPTS="-server \
    -Xmx1g \
    -Xms512m \
    -XX:NewRatio=2"
```

#### 3.2 垃圾收集器优化
```bash
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+UseStringDeduplication
```

#### 3.3 其他JVM优化
```bash
-XX:+OptimizeStringConcat \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-Djava.security.egd=file:/dev/./urandom
```

### 4. 应用层面优化

#### 4.1 异步处理
- 配置自定义线程池处理异步任务
- 使用 `@Async` 注解处理耗时操作
- 批量处理和缓存预热

#### 4.2 查询优化
- 使用 `EXISTS` 替代 `COUNT > 0`
- 优化搜索查询，按相关性排序
- 分页查询避免深度分页问题

#### 4.3 数据转换优化
- 使用 `BeanUtils.copyProperties()` 快速属性复制
- 减少不必要的对象创建
- 优化 DTO 转换逻辑

### 5. 容器化优化

#### 5.1 Docker 镜像优化
- 使用多阶段构建减少镜像大小
- 使用 `openjdk:8-jre-slim` 基础镜像
- 非 root 用户运行应用

#### 5.2 容器运行时优化
```dockerfile
# 使用 dumb-init 作为 PID 1 进程
ENTRYPOINT ["dumb-init", "--"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3
```

### 6. Kubernetes 优化

#### 6.1 资源配置
```yaml
resources:
  requests:
    memory: "768Mi"
    cpu: "500m"
  limits:
    memory: "1.5Gi"
    cpu: "1000m"
```

#### 6.2 水平扩展配置
- HPA 基于 CPU 和内存使用率自动扩缩容
- 最小副本数：3
- 最大副本数：10

#### 6.3 Redis 集成
- 部署 Redis 作为缓存服务
- 配置适当的内存限制和 LRU 策略

### 7. 监控和指标

#### 7.1 应用指标
- JVM 内存使用情况
- 垃圾收集统计
- 线程池状态
- 数据库连接池监控

#### 7.2 业务指标
- 联系人创建/更新/删除计数
- 搜索请求计数
- 响应时间分布

#### 7.3 Prometheus 集成
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## 📊 性能测试

### 测试工具
- Apache Bench (ab)
- curl 响应时间测试
- 自定义性能测试脚本

### 运行性能测试
```bash
chmod +x performance-test.sh
./performance-test.sh
```

### 预期性能指标
- **响应时间**：平均 < 100ms
- **吞吐量**：> 1000 requests/second
- **并发用户**：支持 100+ 并发用户
- **内存使用**：< 1GB 堆内存

## 🔧 配置文件说明

### application.yml 关键配置
```yaml
# 数据库连接池
spring.datasource.hikari.*

# JPA 优化
spring.jpa.properties.hibernate.*

# Redis 缓存
spring.redis.*
spring.cache.*

# 监控
management.endpoints.*
```

### Dockerfile 优化要点
- 多阶段构建
- JVM 参数优化
- 健康检查配置
- 安全性配置

## 📈 性能监控

### 关键监控指标
1. **应用性能**
   - 响应时间
   - 吞吐量
   - 错误率

2. **系统资源**
   - CPU 使用率
   - 内存使用率
   - 磁盘 I/O

3. **数据库性能**
   - 连接池状态
   - 查询执行时间
   - 慢查询日志

4. **缓存性能**
   - 缓存命中率
   - 缓存大小
   - 过期策略效果

### 监控工具集成
- Prometheus + Grafana
- Spring Boot Actuator
- Micrometer 指标收集

## 🚨 性能调优建议

### 1. 定期监控
- 设置性能基线
- 监控关键指标趋势
- 及时发现性能瓶颈

### 2. 容量规划
- 根据业务增长预测资源需求
- 合理设置 HPA 阈值
- 定期评估数据库性能

### 3. 缓存策略
- 根据数据访问模式调整 TTL
- 监控缓存命中率
- 避免缓存雪崩和穿透

### 4. 数据库优化
- 定期分析慢查询
- 优化索引策略
- 考虑读写分离

## 📝 性能优化检查清单

- [ ] 数据库索引已创建并优化
- [ ] 连接池配置合理
- [ ] Redis 缓存正常工作
- [ ] JVM 参数已调优
- [ ] 容器资源配置适当
- [ ] 监控指标正常收集
- [ ] 性能测试通过
- [ ] 文档已更新

## 🔗 相关文档

- [部署指南](./k8s-README.md)
- [Kubernetes 配置](./KUBERNETES-DEPLOYMENT.md)
- [应用配置](./README.md)

---

**注意**：性能优化是一个持续的过程，需要根据实际使用情况和监控数据不断调整和优化。