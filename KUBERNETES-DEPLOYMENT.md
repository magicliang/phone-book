# 🚀 电话号码簿系统 - Kubernetes 完整部署方案

## 📋 项目概述

本项目已完成从传统Spring Boot应用到云原生Kubernetes部署的完整转换，提供了企业级的容器化部署方案。

## 🏗️ 技术栈

- **后端框架**: Spring Boot 2.7.14 (Java 8兼容)
- **数据库**: MySQL 8.0 (外部托管)
- **容器化**: Docker
- **编排平台**: Kubernetes
- **服务网格**: Ingress Controller
- **监控**: Prometheus + Actuator
- **构建工具**: Maven

## 📁 项目结构

```
phonebook/
├── src/                          # Spring Boot 源代码
├── k8s/                          # Kubernetes 配置文件
│   ├── namespace.yaml            # 命名空间
│   ├── configmap.yaml            # 应用配置
│   ├── secret.yaml               # 敏感信息
│   ├── deployment.yaml           # 应用部署
│   ├── service.yaml              # 服务发现
│   ├── ingress.yaml              # 外部访问
│   ├── hpa.yaml                  # 自动扩缩容
│   ├── pdb.yaml                  # Pod中断预算
│   ├── networkpolicy.yaml        # 网络安全策略
│   ├── rbac.yaml                 # 权限管理
│   ├── kustomization.yaml        # 资源管理
│   └── monitoring/
│       └── servicemonitor.yaml   # Prometheus监控
├── build-and-deploy.sh           # 一键部署脚本
├── k8s-README.md                 # 详细部署文档
├── Dockerfile                    # 容器镜像构建
└── pom.xml                       # Maven配置
```

## 🎯 核心特性

### 1. 高可用性
- **多副本部署**: 默认3个Pod副本
- **滚动更新**: 零停机时间部署
- **健康检查**: 完整的存活、就绪和启动探针
- **Pod中断预算**: 保证最小可用实例数

### 2. 自动扩缩容
- **HPA配置**: 基于CPU和内存使用率自动扩缩容
- **资源限制**: 合理的CPU和内存配额
- **扩容策略**: 智能的扩容和缩容策略

### 3. 安全性
- **网络策略**: 限制Pod间通信
- **RBAC**: 基于角色的访问控制
- **Secret管理**: 敏感信息加密存储
- **最小权限原则**: 应用只获得必要权限

### 4. 监控和可观测性
- **Actuator端点**: 健康检查、指标和信息端点
- **Prometheus集成**: 自动服务发现和指标收集
- **日志管理**: 结构化日志输出
- **分布式追踪**: 支持链路追踪

## 🚀 快速部署

### 方式一：一键部署（推荐）

```bash
# 1. 赋予执行权限
chmod +x build-and-deploy.sh

# 2. 执行部署
./build-and-deploy.sh deploy

# 3. 查看部署状态
./build-and-deploy.sh status
```

### 方式二：手动部署

```bash
# 1. 构建应用
mvn clean package -DskipTests

# 2. 构建Docker镜像
docker build -t phonebook:latest .

# 3. 部署到Kubernetes
kubectl apply -f k8s/

# 4. 检查部署状态
kubectl get all -n phonebook
```

## 🌐 访问方式

### 1. NodePort访问
```bash
# 获取访问地址
kubectl get service phonebook-nodeport -n phonebook
# 访问: http://<NODE_IP>:30080
```

### 2. Ingress访问
```bash
# 配置hosts文件
echo "<INGRESS_IP> phonebook.local" >> /etc/hosts
# 访问: http://phonebook.local
```

### 3. 端口转发
```bash
kubectl port-forward service/phonebook-service 8080:80 -n phonebook
# 访问: http://localhost:8080
```

## 📊 监控端点

| 端点 | 描述 | URL |
|------|------|-----|
| 健康检查 | 应用健康状态 | `/actuator/health` |
| 应用信息 | 应用基本信息 | `/actuator/info` |
| 指标数据 | Prometheus指标 | `/actuator/metrics` |
| 环境信息 | 环境变量和配置 | `/actuator/env` |

## 🔧 配置管理

### 数据库配置
- **主机**: 11.142.154.110
- **端口**: 3306
- **数据库**: fuaq8xj3
- **连接池**: HikariCP (最大20个连接)

### 资源配置
- **CPU请求**: 250m
- **CPU限制**: 500m
- **内存请求**: 512Mi
- **内存限制**: 1Gi

### 扩缩容配置
- **最小副本**: 2
- **最大副本**: 10
- **CPU阈值**: 70%
- **内存阈值**: 80%

## 🛠️ 运维操作

### 扩缩容
```bash
# 手动扩容
kubectl scale deployment phonebook-app --replicas=5 -n phonebook

# 查看HPA状态
kubectl get hpa -n phonebook
```

### 更新部署
```bash
# 更新镜像
kubectl set image deployment/phonebook-app phonebook=phonebook:v2.0.0 -n phonebook

# 查看更新状态
kubectl rollout status deployment/phonebook-app -n phonebook
```

### 日志查看
```bash
# 查看应用日志
kubectl logs -f deployment/phonebook-app -n phonebook

# 查看特定Pod日志
kubectl logs -f <pod-name> -n phonebook
```

## 🔍 故障排查

### 常见问题

1. **Pod启动失败**
   ```bash
   kubectl describe pod <pod-name> -n phonebook
   kubectl logs <pod-name> -n phonebook
   ```

2. **数据库连接问题**
   ```bash
   kubectl get secret phonebook-secret -n phonebook -o yaml
   ```

3. **服务访问问题**
   ```bash
   kubectl get endpoints -n phonebook
   kubectl get ingress -n phonebook
   ```

## 📈 性能优化

### JVM调优
- 使用G1垃圾收集器
- 合理设置堆内存大小
- 启用JVM监控参数

### 数据库优化
- 连接池参数调优
- 查询性能优化
- 索引优化

### Kubernetes优化
- 资源请求和限制调优
- 节点亲和性配置
- 存储优化

## 🔒 安全最佳实践

1. **网络安全**
   - 使用NetworkPolicy限制流量
   - 启用TLS加密
   - 定期更新镜像

2. **访问控制**
   - 使用RBAC管理权限
   - 最小权限原则
   - 定期审计权限

3. **数据安全**
   - 敏感信息使用Secret
   - 数据库连接加密
   - 定期备份数据

## 📚 相关文档

- [详细部署指南](k8s-README.md)
- [Spring Boot文档](README.md)
- [Kubernetes官方文档](https://kubernetes.io/docs/)

## 🎉 部署成功验证

部署完成后，你可以通过以下方式验证系统正常运行：

1. **健康检查**: 访问 `/actuator/health` 端点
2. **功能测试**: 访问主页面进行CRUD操作
3. **性能测试**: 使用压力测试工具验证性能
4. **监控验证**: 检查Prometheus指标收集

## 🤝 技术支持

如有问题或需要技术支持，请参考：
- 详细部署文档: `k8s-README.md`
- 故障排查指南
- 性能调优建议

---

**🎯 总结**: 本Kubernetes部署方案提供了完整的企业级容器化解决方案，包括高可用性、自动扩缩容、监控告警、安全防护等特性，可直接用于生产环境部署。