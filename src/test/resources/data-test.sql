-- 测试环境数据初始化脚本
-- 用于集成测试和自动化测试

-- 清理现有数据
DELETE FROM contact;

-- 插入测试数据
INSERT INTO contact (id, name, phone, email, address, created_at, updated_at) VALUES 
(1, '测试用户1', '13900000001', 'test1@test.com', '测试地址1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '测试用户2', '13900000002', 'test2@test.com', '测试地址2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '测试用户3', '13900000003', 'test3@test.com', '测试地址3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 重置序列
ALTER SEQUENCE contact_seq RESTART WITH 4;