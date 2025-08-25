# 电话号码簿系统 - Kubernetes 部署指南

## 📋 概述

本文档提供了电话号码簿系统在 Kubernetes 集群中的完整部署方案，包括高可用性、自动扩缩容、监控和安全配置。

## 🏗️ 架构设计

### 系统架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Ingress       │    │   LoadBalancer  │    │   NodePort      │
│   Controller    │────│   Service       │────│   Service       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   Deployment    │
                       │   (3 Replicas)  │
                       └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   ConfigMap     │
                       │   Secret        │
                       └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   External      │
                       │   MySQL DB      │
                       └─────────────────┘
```

### 组件说明

| 组件 | 描述 | 配置文件 |
|------|------|----------|
| **Namespace** | 资源隔离 | `namespace.yaml` |
| **ConfigMap** | 应用配置 | `configmap.yaml` |
| **Secret** | 敏感信息 | `secret.yaml` |
| **Deployment** | 应用部署 | `deployment.yaml` |
| **Service** | 服务发现 | `service.yaml` |
| **Ingress** | 外部访问 | `ingress.yaml` |
| **HPA** | 自动扩缩容 | `hpa.yaml` |
| **PDB** | 中断预算 | `pdb.yaml` |
| **NetworkPolicy** | 网络安全 | `networkpolicy.yaml` |

## 🚀 快速部署

### 前置条件

1. **Kubernetes 集群** (版本 >= 1.20)
2. **kubectl** 命令行工具
3. **Docker** 容器运行时
4. **Maven** 构建工具
5. **Ingress Controller** (推荐 NGINX)

### 一键部署

```bash
# 赋予执行权限
chmod +x build-and-deploy.sh

# 执行部署
./build-and-deploy.sh deploy
```

### 手动部署步骤

#### 1. 构建应用

```bash
# 编译 Spring Boot 应用
mvn clean package -DskipTests

# 构建 Docker 镜像
docker build -t phonebook:latest .
```

#### 2. 部署到 Kubernetes

```bash
# 应用所有配置
kubectl apply -f k8s/

# 检查部署状态
kubectl get all -n phonebook
```

#### 3. 验证部署

```bash
# 检查 Pod 状态
kubectl get pods -n phonebook

# 检查服务状态
kubectl get services -n phonebook

# 查看应用日志
kubectl logs -f deployment/phonebook-app -n phonebook
```

## 🔧 配置说明

### 环境变量配置

应用通过以下环境变量连接数据库：

| 变量名 | 描述 | 来源 |
|--------|------|------|
| `DB_HOST` | 数据库主机 | Secret |
| `DB_PORT` | 数据库端口 | Secret |
| `DB_NAME` | 数据库名称 | Secret |
| `DB_USERNAME` | 数据库用户名 | Secret |
| `DB_PASSWORD` | 数据库密码 | Secret |

### 资源配置

#### CPU 和内存限制
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

#### 自动扩缩容配置
- **最小副本数**: 2
- **最大副本数**: 10
- **CPU 阈值**: 70%
- **内存阈值**: 80%

### 健康检查配置

#### 存活探针 (Liveness Probe)
- **路径**: `/actuator/health`
- **初始延迟**: 60秒
- **检查间隔**: 30秒

#### 就绪探针 (Readiness Probe)
- **路径**: `/actuator/health`
- **初始延迟**: 30秒
- **检查间隔**: 10秒

#### 启动探针 (Startup Probe)
- **路径**: `/actuator/health`
- **初始延迟**: 30秒
- **失败阈值**: 10次

## 🌐 访问方式

### 1. NodePort 访问

```bash
# 获取 NodePort 端口
kubectl get service phonebook-nodeport -n phonebook

# 访问地址
http://<NODE_IP>:30080
```

### 2. Ingress 访问

```bash
# 配置 hosts 文件
echo "<INGRESS_IP> phonebook.local" >> /etc/hosts

# 访问地址
http://phonebook.local
```

### 3. 端口转发访问

```bash
# 创建端口转发
kubectl port-forward service/phonebook-service 8080:80 -n phonebook

# 访问地址
http://localhost:8080
```

## 📊 监控和日志

### 应用监控

应用暴露了以下监控端点：

- **健康检查**: `/actuator/health`
- **应用信息**: `/actuator/info`
- **指标数据**: `/actuator/metrics`

### 日志查看

```bash
# 查看所有 Pod 日志
kubectl logs -f deployment/phonebook-app -n phonebook

# 查看特定 Pod 日志
kubectl logs -f <pod-name> -n phonebook

# 查看前 100 行日志
kubectl logs --tail=100 deployment/phonebook-app -n phonebook
```

### Prometheus 集成

应用已配置 Prometheus 注解，支持自动服务发现：

```yaml
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"
```

## 🔒 安全配置

### 网络策略

NetworkPolicy 配置了以下安全规则：

- **入站流量**: 仅允许来自 Ingress Controller 和同命名空间的流量
- **出站流量**: 允许访问数据库、DNS 和 HTTP/HTTPS 服务

### Secret 管理

敏感信息通过 Kubernetes Secret 管理：

```bash
# 查看 Secret
kubectl get secret phonebook-secret -n phonebook -o yaml

# 解码 Secret 值
kubectl get secret phonebook-secret -n phonebook -o jsonpath='{.data.db-password}' | base64 -d
```

## 🛠️ 运维操作

### 扩缩容操作

```bash
# 手动扩容到 5 个副本
kubectl scale deployment phonebook-app --replicas=5 -n phonebook

# 查看 HPA 状态
kubectl get hpa -n phonebook
```

### 滚动更新

```bash
# 更新镜像
kubectl set image deployment/phonebook-app phonebook=phonebook:v2.0.0 -n phonebook

# 查看更新状态
kubectl rollout status deployment/phonebook-app -n phonebook

# 回滚到上一版本
kubectl rollout undo deployment/phonebook-app -n phonebook
```

### 配置更新

```bash
# 更新 ConfigMap
kubectl apply -f k8s/configmap.yaml

# 重启 Deployment 以应用新配置
kubectl rollout restart deployment/phonebook-app -n phonebook
```

## 🧪 测试验证

### 功能测试

```bash
# 获取服务地址
SERVICE_URL=$(kubectl get service phonebook-nodeport -n phonebook -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# 测试健康检查
curl http://$SERVICE_URL:30080/actuator/health

# 测试主页面
curl http://$SERVICE_URL:30080/
```

### 压力测试

```bash
# 使用 Apache Bench 进行压力测试
ab -n 1000 -c 10 http://$SERVICE_URL:30080/

# 观察 HPA 自动扩容
watch kubectl get hpa -n phonebook
```

## 🔧 故障排查

### 常见问题

#### 1. Pod 启动失败

```bash
# 查看 Pod 状态
kubectl describe pod <pod-name> -n phonebook

# 查看 Pod 日志
kubectl logs <pod-name> -n phonebook
```

#### 2. 数据库连接失败

```bash
# 检查 Secret 配置
kubectl get secret phonebook-secret -n phonebook -o yaml

# 测试数据库连接
kubectl run mysql-client --image=mysql:8.0 --rm -it --restart=Never -- mysql -h<DB_HOST> -u<DB_USER> -p
```

#### 3. 服务无法访问

```bash
# 检查服务状态
kubectl get endpoints -n phonebook

# 检查网络策略
kubectl get networkpolicy -n phonebook
```

### 日志级别调整

```bash
# 临时调整日志级别
kubectl set env deployment/phonebook-app LOGGING_LEVEL_COM_EXAMPLE_PHONEBOOK=DEBUG -n phonebook
```

## 📈 性能优化

### JVM 参数优化

在 Deployment 中配置 JVM 参数：

```yaml
env:
- name: JAVA_OPTS
  value: "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### 数据库连接池优化

在 ConfigMap 中调整连接池配置：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

## 🚀 高级配置

### 多环境部署

使用 Kustomize 管理多环境配置：

```bash
# 开发环境
kubectl apply -k k8s/overlays/dev

# 生产环境
kubectl apply -k k8s/overlays/prod
```

### 蓝绿部署

```bash
# 创建新版本部署
kubectl apply -f k8s/blue-green/

# 切换流量
kubectl patch service phonebook-service -p '{"spec":{"selector":{"version":"green"}}}'
```

### 金丝雀发布

```bash
# 部署金丝雀版本
kubectl apply -f k8s/canary/

# 逐步增加流量权重
kubectl patch ingress phonebook-ingress --type='json' -p='[{"op": "replace", "path": "/spec/rules/0/http/paths/0/backend/service/name", "value": "phonebook-canary"}]'
```

## 📚 参考资料

- [Kubernetes 官方文档](https://kubernetes.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Prometheus Operator](https://prometheus-operator.dev/)

## 🤝 支持

如有问题或建议，请联系开发团队或提交 Issue。

---

**版本**: 1.0.0  
**更新时间**: 2025-08-22  
**维护者**: With AI Assistant