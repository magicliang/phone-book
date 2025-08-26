-- 开发环境测试数据初始化脚本
-- 这些数据将在开发环境启动时自动加载

-- 清理现有数据
DELETE FROM contact;

-- 插入测试联系人数据
INSERT INTO contact (id, name, phone, email, address, created_at, updated_at) VALUES 
(1, '张三', '13800138001', 'zhangsan@example.com', '北京市朝阳区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '李四', '13800138002', 'lisi@example.com', '上海市浦东新区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '王五', '13800138003', 'wangwu@example.com', '广州市天河区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '赵六', '13800138004', 'zhaoliu@example.com', '深圳市南山区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '钱七', '13800138005', 'qianqi@example.com', '杭州市西湖区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '孙八', '13800138006', 'sunba@example.com', '成都市锦江区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '周九', '13800138007', 'zhoujiu@example.com', '武汉市武昌区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, '吴十', '13800138008', 'wushi@example.com', '南京市鼓楼区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, '郑十一', '13800138009', 'zhengshiyi@example.com', '西安市雁塔区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, '王十二', '13800138010', 'wangshier@example.com', '重庆市渝中区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 重置序列
ALTER SEQUENCE contact_seq RESTART WITH 11;