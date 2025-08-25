#!/bin/bash

# 电话号码簿系统 - Kubernetes 构建和部署脚本
# 作者: With AI Assistant
# 版本: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
APP_NAME="phonebook"
IMAGE_NAME="phonebook"
IMAGE_TAG="latest"
NAMESPACE="phonebook"
REGISTRY_URL=""  # 如果使用私有镜像仓库，请设置此变量

# 函数：打印带颜色的消息
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}[$(date '+%Y-%m-%d %H:%M:%S')] ${message}${NC}"
}

# 函数：检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_message $RED "错误: $1 命令未找到，请先安装 $1"
        exit 1
    fi
}

# 函数：检查必要的工具
check_prerequisites() {
    print_message $BLUE "检查必要的工具..."
    check_command "docker"
    check_command "kubectl"
    check_command "mvn"
    
    # 可选工具检查（用于性能测试）
    if command -v ab &> /dev/null; then
        print_message $GREEN "✓ Apache Bench (ab) 已安装，可进行性能测试"
    else
        print_message $YELLOW "⚠ Apache Bench (ab) 未安装，性能测试功能受限"
    fi
    
    # 检查Docker是否运行
    if ! docker info &> /dev/null; then
        print_message $RED "错误: Docker 未运行，请启动 Docker"
        exit 1
    fi
    
    # 检查kubectl是否能连接到集群
    if ! kubectl cluster-info &> /dev/null; then
        print_message $RED "错误: 无法连接到 Kubernetes 集群"
        exit 1
    fi
    
    print_message $GREEN "所有必要工具检查通过"
}

# 函数：构建应用
build_application() {
    print_message $BLUE "开始构建 Spring Boot 应用..."
    
    # 清理之前的构建
    mvn clean
    
    # 编译和打包
    mvn package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "Spring Boot 应用构建成功"
    else
        print_message $RED "Spring Boot 应用构建失败"
        exit 1
    fi
}

# 函数：构建Docker镜像
build_docker_image() {
    print_message $BLUE "开始构建 Docker 镜像..."
    
    # 构建镜像
    if [ -n "$REGISTRY_URL" ]; then
        FULL_IMAGE_NAME="${REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG}"
    else
        FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"
    fi
    
    docker build -t $FULL_IMAGE_NAME .
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "Docker 镜像构建成功: $FULL_IMAGE_NAME"
    else
        print_message $RED "Docker 镜像构建失败"
        exit 1
    fi
    
    # 如果设置了镜像仓库，推送镜像
    if [ -n "$REGISTRY_URL" ]; then
        print_message $BLUE "推送镜像到仓库..."
        docker push $FULL_IMAGE_NAME
        if [ $? -eq 0 ]; then
            print_message $GREEN "镜像推送成功"
        else
            print_message $RED "镜像推送失败"
            exit 1
        fi
    fi
}

# 函数：部署到Kubernetes
deploy_to_kubernetes() {
    print_message $BLUE "开始部署到 Kubernetes..."
    
    # 创建命名空间（如果不存在）
    kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
    
    # 应用所有配置文件
    kubectl apply -f k8s/
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "Kubernetes 资源部署成功"
    else
        print_message $RED "Kubernetes 资源部署失败"
        exit 1
    fi
    
    # 等待部署完成
    print_message $BLUE "等待部署完成..."
    kubectl rollout status deployment/$APP_NAME-app -n $NAMESPACE --timeout=300s
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "部署完成"
    else
        print_message $RED "部署超时或失败"
        exit 1
    fi
}

# 函数：显示部署状态
show_deployment_status() {
    print_message $BLUE "显示部署状态..."
    
    echo ""
    print_message $YELLOW "=== Pods 状态 ==="
    kubectl get pods -n $NAMESPACE -l app=$APP_NAME
    
    echo ""
    print_message $YELLOW "=== Services 状态 ==="
    kubectl get services -n $NAMESPACE
    
    echo ""
    print_message $YELLOW "=== Ingress 状态 ==="
    kubectl get ingress -n $NAMESPACE
    
    echo ""
    print_message $YELLOW "=== HPA 状态 ==="
    kubectl get hpa -n $NAMESPACE
    
    # 获取访问地址
    NODEPORT=$(kubectl get service $APP_NAME-nodeport -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}')
    NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
    if [ -z "$NODE_IP" ]; then
        NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
    fi
    
    echo ""
    print_message $GREEN "=== 访问地址 ==="
    print_message $GREEN "NodePort 访问: http://$NODE_IP:$NODEPORT"
    print_message $GREEN "Ingress 访问: http://phonebook.local (需要配置 hosts 文件)"
    print_message $GREEN "集群内访问: http://$APP_NAME-service.$NAMESPACE.svc.cluster.local"
}

# 函数：清理部署
cleanup_deployment() {
    print_message $YELLOW "清理 Kubernetes 部署..."
    kubectl delete -f k8s/ --ignore-not-found=true
    print_message $GREEN "清理完成"
}

# 主函数
main() {
    print_message $GREEN "=== 电话号码簿系统 Kubernetes 部署脚本 ==="
    
    case "${1:-deploy}" in
        "build")
            check_prerequisites
            build_application
            build_docker_image
            ;;
        "deploy")
            check_prerequisites
            build_application
            build_docker_image
            deploy_to_kubernetes
            show_deployment_status
            ;;
        "status")
            show_deployment_status
            ;;
        "cleanup")
            cleanup_deployment
            ;;
        "help")
            echo "用法: $0 [build|deploy|status|cleanup|help]"
            echo "  build   - 只构建应用和Docker镜像"
            echo "  deploy  - 构建并部署到Kubernetes (默认)"
            echo "  status  - 显示部署状态"
            echo "  cleanup - 清理Kubernetes部署"
            echo "  help    - 显示此帮助信息"
            ;;
        *)
            print_message $RED "未知命令: $1"
            print_message $YELLOW "使用 '$0 help' 查看可用命令"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"