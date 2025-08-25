-- 数据库性能优化脚本

-- 创建联系人表（如果不存在）
CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    category VARCHAR(50) DEFAULT 'personal',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建性能优化索引
CREATE INDEX IF NOT EXISTS idx_phone_number ON contacts(phone_number);
CREATE INDEX IF NOT EXISTS idx_email ON contacts(email);
CREATE INDEX IF NOT EXISTS idx_name ON contacts(name);
CREATE INDEX IF NOT EXISTS idx_category ON contacts(category);
CREATE INDEX IF NOT EXISTS idx_created_at ON contacts(created_at);
CREATE INDEX IF NOT EXISTS idx_name_phone ON contacts(name, phone_number);
CREATE INDEX IF NOT EXISTS idx_category_name ON contacts(category, name);

-- 创建全文搜索索引（MySQL 5.7+）
ALTER TABLE contacts ADD FULLTEXT(name, email);

-- 优化表结构
ALTER TABLE contacts 
    ENGINE=InnoDB 
    ROW_FORMAT=DYNAMIC 
    CHARSET=utf8mb4 
    COLLATE=utf8mb4_unicode_ci;

-- 分析表以更新统计信息
ANALYZE TABLE contacts;

-- 创建分类统计视图
CREATE OR REPLACE VIEW contact_category_stats AS
SELECT 
    COALESCE(category, '未分类') as category,
    COUNT(*) as contact_count,
    MAX(created_at) as last_created
FROM contacts 
GROUP BY category
ORDER BY contact_count DESC;

-- 创建最近联系人视图
CREATE OR REPLACE VIEW recent_contacts AS
SELECT 
    id, name, phone_number, email, category, created_at
FROM contacts 
ORDER BY created_at DESC 
LIMIT 50;