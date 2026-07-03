-- LightCatch Database Initialization Script
-- Requires PostgreSQL 16+ with PGVector extension

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. User & Tenant
-- ============================================================
CREATE TABLE sys_tenant (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    status INTEGER DEFAULT 1,
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    real_name VARCHAR(100),
    avatar VARCHAR(500),
    password VARCHAR(200) NOT NULL,
    salt VARCHAR(50),
    status INTEGER DEFAULT 1,
    phone VARCHAR(20),
    email VARCHAR(100),
    tenant_id VARCHAR(32) DEFAULT '0',
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(32),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_tenant ON sys_user(tenant_id);
CREATE INDEX idx_user_username ON sys_user(username);

-- ============================================================
-- 2. AI Model Configuration
-- ============================================================
CREATE TABLE ai_model (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    model_name VARCHAR(200) NOT NULL,
    api_key TEXT,
    base_url VARCHAR(500),
    model_type VARCHAR(20) NOT NULL DEFAULT 'chat',
    model_params TEXT,
    description VARCHAR(500),
    sort_order INTEGER DEFAULT 0,
    is_default BOOLEAN DEFAULT FALSE,
    status INTEGER DEFAULT 1,
    tenant_id VARCHAR(32) DEFAULT '0',
    user_id VARCHAR(32),
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ai_model_tenant ON ai_model(tenant_id);
CREATE INDEX idx_ai_model_type ON ai_model(model_type);

-- ============================================================
-- 3. Knowledge Base
-- ============================================================
CREATE TABLE ai_knowledge (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(20) DEFAULT 'knowledge',
    embed_model_id VARCHAR(32),
    doc_count INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    tenant_id VARCHAR(32) DEFAULT '0',
    user_id VARCHAR(32),
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ai_knowledge_tenant ON ai_knowledge(tenant_id);

-- ============================================================
-- 4. Knowledge Documents
-- ============================================================
CREATE TABLE ai_knowledge_doc (
    id VARCHAR(32) PRIMARY KEY,
    knowledge_id VARCHAR(32) NOT NULL,
    title VARCHAR(500) NOT NULL,
    file_type VARCHAR(20),
    content TEXT,
    metadata TEXT,
    word_count INTEGER DEFAULT 0,
    chunk_count INTEGER DEFAULT 0,
    status INTEGER DEFAULT 0,
    error_msg TEXT,
    tenant_id VARCHAR(32) DEFAULT '0',
    user_id VARCHAR(32),
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_doc_knowledge ON ai_knowledge_doc(knowledge_id);

-- ============================================================
-- 5. Document Chunks (Vectorized)
-- ============================================================
CREATE TABLE ai_doc_chunk (
    id VARCHAR(32) PRIMARY KEY,
    doc_id VARCHAR(32) NOT NULL,
    knowledge_id VARCHAR(32),
    content TEXT NOT NULL,
    embedding VECTOR(1536),
    chunk_index INTEGER NOT NULL,
    metadata TEXT,
    tenant_id VARCHAR(32) DEFAULT '0',
    username VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_chunk_doc ON ai_doc_chunk(doc_id);
CREATE INDEX idx_chunk_knowledge ON ai_doc_chunk(knowledge_id);
CREATE INDEX idx_chunk_username ON ai_doc_chunk(username);
CREATE INDEX idx_chunk_tenant ON ai_doc_chunk(tenant_id);
CREATE INDEX idx_chunk_embedding ON ai_doc_chunk USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- ============================================================
-- 6. Conversations
-- ============================================================
CREATE TABLE ai_conversation (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    title VARCHAR(500) DEFAULT '新对话',
    model_id VARCHAR(32),
    knowledge_ids TEXT,
    type VARCHAR(20) DEFAULT 'chat',
    flow_id VARCHAR(32),
    status INTEGER DEFAULT 1,
    tenant_id VARCHAR(32) DEFAULT '0',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_conv_user ON ai_conversation(user_id);
CREATE INDEX idx_conv_tenant ON ai_conversation(tenant_id);

-- ============================================================
-- 7. Chat Messages
-- ============================================================
CREATE TABLE ai_chat_message (
    id VARCHAR(32) PRIMARY KEY,
    conversation_id VARCHAR(32) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    tool_execution_requests TEXT,
    tool_execution_result TEXT,
    tokens INTEGER DEFAULT 0,
    model_name VARCHAR(200),
    thinking TEXT,
    tenant_id VARCHAR(32) DEFAULT '0',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_msg_conversation ON ai_chat_message(conversation_id, create_time);

-- ============================================================
-- 8. AI App (Writing Assistant Templates)
-- ============================================================
CREATE TABLE ai_app (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) DEFAULT 'chat',
    description TEXT,
    prompt TEXT,
    model_id VARCHAR(32),
    knowledge_ids TEXT,
    flow_id VARCHAR(32),
    plugins TEXT,
    memory_id VARCHAR(32),
    msg_num INTEGER DEFAULT 10,
    metadata TEXT,
    status INTEGER DEFAULT 1,
    tenant_id VARCHAR(32) DEFAULT '0',
    user_id VARCHAR(32),
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 9. AI Workflows
-- ============================================================
CREATE TABLE ai_flow (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) DEFAULT 'chat',
    chain TEXT,
    design TEXT,
    metadata TEXT,
    status INTEGER DEFAULT 1,
    tenant_id VARCHAR(32) DEFAULT '0',
    user_id VARCHAR(32),
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 10. MCP Plugin Config
-- ============================================================
CREATE TABLE ai_mcp (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) DEFAULT 'http',
    endpoint VARCHAR(500),
    headers TEXT,
    category VARCHAR(20) DEFAULT 'mcp',
    description TEXT,
    status INTEGER DEFAULT 1,
    tenant_id VARCHAR(32) DEFAULT '0',
    user_id VARCHAR(32),
    create_by VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 11. Memory (Long-term)
-- ============================================================
CREATE TABLE ai_memory (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'memory',
    metadata TEXT,
    tenant_id VARCHAR(32) DEFAULT '0',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_memory_user ON ai_memory(user_id);

-- ============================================================
-- Seed Data
-- ============================================================
INSERT INTO sys_tenant (id, name) VALUES ('0', '默认租户');

INSERT INTO sys_user (id, username, real_name, password, salt, status, tenant_id)
VALUES ('admin', 'admin', '管理员',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        '', 1, '0');
-- Default password: admin123

INSERT INTO sys_user (id, username, real_name, password, salt, status, tenant_id)
VALUES ('demo', 'demo', '演示用户',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        '', 1, '0');
-- Default password: admin123
