# LightCatch（拾光）— AI 自媒体内容工坊

面向自媒体创作者的 AI 内容生产平台。上传你的历史文章和素材库，AI 学习你的写作风格，帮你写文案、配图、排版。支持搭建自动化内容流水线——从选题、检索素材、生成初稿到发布。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.5 |
| 语言 | Java | 17+ |
| 构建工具 | Maven | 3.9+ |
| ORM | MyBatis-Plus | 3.5.9 |
| AI 框架 | LangChain4j | 1.12.2 |
| 向量存储 | 纯 Java（JSON 文件持久化） | — |
| 数据库 | PostgreSQL | 16 |
| 缓存 | Redis | 7+ |
| 认证 | JWT（jjwt） | 0.12.6 |
| 前端框架 | Vue 3 + TypeScript + Vite | — |
| UI 组件库 | Ant Design Vue | 4.2.6 |
| 工作流引擎 | LiteFlow | 2.15.0 |

## 项目结构

```
lightcatch/
├── lightcatch-api/            # 应用入口
├── lightcatch-common/         # 公共模块
├── lightcatch-system/         # 用户与认证
├── lightcatch-ai/             # AI 核心模块
├── lightcatch-workflow/       # 工作流引擎
├── lightcatch-frontend/       # Vue 3 前端
├── db/init.sql                # 数据库脚本
├── docker-compose.yml         # Redis
├── CLAUDE.md                  # AI 项目规则
└── logs/                      # 运行日志
```

## 功能状态

### ✅ 已完成
- **用户系统**：登录、注册、JWT 认证
- **AI 对话**：SSE 流式聊天，支持多模型切换
- **AI 写作**：生成、改写、起标题、列大纲、优化、扩写、缩写
- **模型管理**：5 类模型（对话/向量/图片/语音/视频）配置 + 类型 tab 筛选
- **自然语言生成工作流**：用户写描述 → LLM 解析 → 结构化节点 → 存库
- **工作流 LLM 节点**：调 DeepSeek 生成内容
- **工作流知识库节点**：调 VectorStore 检索素材
- **草稿箱**：工作流执行结果自动保存 + 列表查看
- **手动触发执行**：点"运行"按钮执行工作流

### 🔄 进行中/待完善
- **工作流 userId 传递**：从 JWT 提取 → 草稿箱归属正确用户
- **向量模型兼容**：非 default 的模型也能被工作流识别

### 📋 未开始
- **图片生成节点** — 需要接通义万相/DALL-E API
- **联网搜索节点** — 需要接 Brave Search API
- **条件分支/路由节点** — LLM 判断走哪个功能节点
- **定时触发** — cron 表达式自动执行
- **参数化复用** — 描述中的 {变量} 自动提取为参数
- **RRF 融合召回** — 双路结果融合排序
- **三级记忆** — 会话级 + 用户级 + 摘要级
- **对接社交平台发布** — 小红书/公众号/抖音 API
- **草稿箱编辑** — 前端编辑草稿内容

## 快速启动

```bash
# 1. 启动 PostgreSQL
"D:\software\postgresql-16\pgsql\bin\pg_ctl" -D "D:\software\postgresql-16\data" start

# 2. 初始化数据库
psql -U postgres -d lightcatch -f db/init.sql

# 3. 启动后端（IDEA 运行 LightCatchApplication）
#    或命令行：mvn spring-boot:run -pl lightcatch-api

# 4. 启动前端
cd lightcatch-frontend && npm run dev

# 5. 打开 http://localhost:3000  →  账号 admin / admin123
```
