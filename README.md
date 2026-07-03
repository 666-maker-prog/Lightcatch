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
- **AI 对话**：SSE 流式聊天，支持 DeepSeek/通义千问/OpenAI 等多模型
- **素材库（RAG）**：文档上传 → 文本提取 → 自动向量化 → 语义检索
- **AI 写作**：生成、改写、起标题、列大纲、优化、扩写、缩写
- **模型管理**：对话/向量嵌入/图片/语音/视频 5 类模型配置
- **工作流**：可视化拖拽编排 + LiteFlow 执行引擎
- **系统人设**：AI 身份为「拾光创作助手」

### 🔄 进行中
- 自然语言生成工作流（用户说人话，AI 自动搭流程）
- 图片生成（文生图/图生图）
- 定时触发工作流
- 联网搜索（Brave Search）

### 📋 规划中
- 条件分支、数据抓取、一键发布到平台

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
