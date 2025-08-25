# 电话号码簿管理系统

一个基于Spring Boot和MySQL的现代化电话号码簿管理系统，提供完整的联系人管理功能。

## 功能特性

### 核心功能
- ✅ 联系人的增删改查操作
- ✅ 支持姓名、电话号码、邮箱、地址等完整信息
- ✅ 联系人分类管理（个人、商务、家庭、朋友、其他）
- ✅ 智能搜索功能（支持姓名、电话、邮箱模糊搜索）
- ✅ 分页显示和排序功能
- ✅ 数据统计和分析

### 技术特性
- ✅ RESTful API设计
- ✅ 数据验证和错误处理
- ✅ 响应式前端界面
- ✅ MySQL数据库持久化
- ✅ Docker容器化部署
- ✅ 环境变量配置

## 技术栈

### 后端
- **Spring Boot 2.7.14** - 主框架
- **Spring Data JPA** - 数据访问层
- **MySQL 8.0** - 数据库
- **Maven** - 项目管理
- **Java 8** - 编程语言

### 前端
- **HTML5/CSS3** - 页面结构和样式
- **JavaScript (ES6+)** - 交互逻辑
- **Tailwind CSS** - UI框架
- **Font Awesome** - 图标库

## 项目结构

```
phonebook/
├── src/
│   ├── main/
│   │   ├── java/com/example/phonebook/
│   │   │   ├── PhonebookApplication.java      # 启动类
│   │   │   ├── entity/Contact.java            # 联系人实体
│   │   │   ├── dto/ContactDTO.java            # 数据传输对象
│   │   │   ├── repository/ContactRepository.java # 数据访问层
│   │   │   ├── service/ContactService.java    # 服务接口
│   │   │   ├── service/impl/ContactServiceImpl.java # 服务实现
│   │   │   ├── controller/ContactController.java # 控制器
│   │   │   └── config/WebConfig.java          # Web配置
│   │   └── resources/
│   │       ├── application.yml                # 应用配置
│   │       └── static/                        # 静态资源
│   │           ├── index.html                 # 主页面
│   │           └── app.js                     # 前端脚本
├── pom.xml                                    # Maven配置
├── Dockerfile                                 # Docker配置
└── README.md                                  # 项目说明
```

## 数据库设计

### contacts 表结构
```sql
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    category VARCHAR(50) DEFAULT 'personal',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_phone (phone_number),
    INDEX idx_category (category)
);
```

## API接口文档

### 联系人管理

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/contacts` | 分页获取联系人列表 |
| GET | `/api/contacts/all` | 获取所有联系人 |
| GET | `/api/contacts/{id}` | 根据ID获取联系人 |
| POST | `/api/contacts` | 创建新联系人 |
| PUT | `/api/contacts/{id}` | 更新联系人信息 |
| DELETE | `/api/contacts/{id}` | 删除联系人 |
| DELETE | `/api/contacts/batch` | 批量删除联系人 |

### 搜索和筛选

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/contacts/search?keyword={keyword}` | 模糊搜索联系人 |
| GET | `/api/contacts/category/{category}` | 按分类获取联系人 |
| GET | `/api/contacts/phone/{phoneNumber}` | 根据电话号码查找 |

### 统计和验证

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/contacts/statistics` | 获取统计信息 |
| GET | `/api/contacts/check-phone?phoneNumber={phone}` | 检查电话号码是否存在 |
| GET | `/api/contacts/check-email?email={email}` | 检查邮箱是否存在 |

## 快速开始

### 环境要求
- Java 8+
- Maven 3.6+
- MySQL 8.0+
- Docker (可选)

### 本地开发

1. **克隆项目**
```bash
git clone <repository-url>
cd phonebook
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE phonebook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **配置环境变量**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=phonebook
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

4. **运行应用**
```bash
mvn spring-boot:run
```

5. **访问应用**
- 前端界面: http://localhost:8080
- API文档: http://localhost:8080/api/contacts

### Docker部署

1. **构建镜像**
```bash
docker build -t phonebook:latest .
```

2. **运行容器**
```bash
docker run -d \
  --name phonebook \
  -p 8080:8080 \
  -e DB_HOST=your_mysql_host \
  -e DB_PORT=3306 \
  -e DB_NAME=phonebook \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  phonebook:latest
```

## 使用说明

### 主要功能

1. **添加联系人**
   - 点击"添加联系人"按钮
   - 填写必要信息（姓名和电话号码为必填）
   - 选择合适的分类
   - 保存联系人

2. **搜索联系人**
   - 在搜索框中输入关键词
   - 支持按姓名、电话号码、邮箱搜索
   - 实时显示搜索结果

3. **分类筛选**
   - 使用分类下拉框筛选特定类型的联系人
   - 支持个人、商务、家庭、朋友、其他分类

4. **编辑和删除**
   - 点击联系人卡片上的编辑按钮修改信息
   - 点击删除按钮删除联系人（需确认）

5. **统计信息**
   - 点击"统计信息"按钮查看数据统计
   - 显示总数和各分类的联系人数量

### 界面特性

- **响应式设计**: 适配桌面和移动设备
- **现代化UI**: 使用Tailwind CSS构建美观界面
- **流畅动画**: 提供平滑的交互体验
- **实时反馈**: 操作结果即时显示

## 系统设计亮点

### 1. 分层架构
- **Controller层**: 处理HTTP请求和响应
- **Service层**: 实现业务逻辑
- **Repository层**: 数据访问抽象
- **Entity层**: 数据模型定义

### 2. 数据验证
- 前端表单验证
- 后端Bean Validation
- 数据库约束检查

### 3. 错误处理
- 统一异常处理
- 友好的错误提示
- 详细的日志记录

### 4. 性能优化
- 数据库索引优化
- 分页查询减少内存占用
- 前端防抖处理减少请求

### 5. 安全考虑
- SQL注入防护
- XSS攻击防护
- CORS跨域配置

## 扩展功能建议

- [ ] 用户认证和授权
- [ ] 联系人头像上传
- [ ] 数据导入导出功能
- [ ] 联系人分组管理
- [ ] 通话记录集成
- [ ] 短信发送功能
- [ ] 联系人备份和同步

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 项目Issues: [GitHub Issues](https://github.com/your-repo/phonebook/issues)
- 邮箱: your-email@example.com

---

**注意**: 这是一个演示项目，用于展示Spring Boot全栈开发的最佳实践。在生产环境中使用前，请确保进行充分的安全性评估和性能测试。