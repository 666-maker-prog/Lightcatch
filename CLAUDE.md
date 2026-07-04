# LightCatch（拾光）项目规则

## 技术栈
- 后端：Spring Boot 3.5.5 + JDK 17 + Maven
- 前端：Vue 3 + TypeScript + Vite + Ant Design Vue 4.2.6 + Pinia
- ORM：MyBatis-Plus 3.5.9
- AI 框架：LangChain4j 1.12.2
- 向量存储：纯 Java VectorStore（JSON 文件持久化，重启不丢）
- 数据库：PostgreSQL 16
- 工作流引擎：自定义节点执行（绕过 LiteFlow 动态 EL 限制）
- 缓存：Redis
- 认证：JWT（jjwt 0.12.6）

## 架构约束

### 后端
- 所有 API 返回统一格式：`Result<T>`（success, message, data）
- Controller 只做参数校验和路由，业务逻辑在 Service 层
- AI 相关代码在 `lightcatch-ai` 模块
- 工作流相关代码在 `lightcatch-workflow` 模块
- 草稿箱相关在 `lightcatch-workflow` 模块

### 前端
- 所有 API 调用通过 `@/utils/request.ts`（axios 封装）
- 状态管理用 Pinia store
- SSE 流式聊天在 `stores/chat.ts` 中处理
- 前端调试日志通过 `utils/debug.ts` 发送到后端 `logs/frontend.log`

### API 规范
- 认证方式：`X-Access-Token` Header（axios 自动注入）
- EventSource（SSE）不支持自定义 Header，token 通过 query param `?token=` 传递
- 所有 SSE 事件使用 `SseEmitter.send(jsonString)`，不要手动加 `data: ` 前缀
- 所有 GET 请求加 `_t` 时间戳防缓存
- `/api/ai/flow/run` 必须接收并传递 userId（从 JWT 提取）

## 核心功能设计

### 1. 文档切分策略（ContentChunker）
- **默认策略**：递归字符分割，按 `\n\n` → `\n` → `. ` → 空格 → 字符 逐级降级
- **默认参数**：500 字一段，50 字重叠
- **HTML 表格保护**：正则识别 `<table>...</table>` 完整块，表格外文本正常分段，表格内整块保留不分段
- **PDF 特殊格式**：通过 Apache Tika 提取纯文本后统一走递归切分，不保留 PDF 原始排版
- **多级切分**：上层段落级索引（检索用）+ 下层句子级索引（精排用），后续可扩展
- **含图片文档**：图片本身嵌入语义由多模态模型提取描述文本，描述文本参与向量化

### 2. 自然语言 → 工作流（FlowController + FlowServiceImpl）
- **规划者 LLM**：用户口语化描述 → LLM 解析为结构化节点列表（`design` JSON）
- **并行分析**：LLM 判断节点间依赖关系，`depends_on` 为空的可并行执行
- **节点路由**：按 type 分发到对应的功能组件（`LLM_Node`、`Knowledge_Node` 等）
- **链式输出**：前一个节点的输出自动作为下一个节点的输入
- **不支持的节点**：跳过时记日志，不中断流程
- **参数化复用**：描述中的 `{变量}` 自动提取为参数，用户运行时可填

### 3. 记忆机制

#### 三层记忆
| 层级 | 存储 | 内容 | 生命周期 |
|------|------|------|---------|
| 会话级 | Redis / 上下文窗口 | 当前对话历史 | 会话结束过期 |
| 用户级 | VectorStore（memory 类型） | 跨对话的用户偏好、关注点 | 持久化 |
| 摘要级 | VectorStore | LLM 定期压缩的关键信息 | 持久化 |

#### 结构化提取
- 每 N 轮对话后，LLM 自动提取关键信息（偏好、决策、重要结论）
- 提取结果去口语化，按结构化格式存储
- 减少原始上下文占用，避免 Context Window 膨胀

### 4. 检索与召回

#### 双路召回
| 路徑 | 来源 | 用途 |
|------|------|------|
| 素材库检 索 | VectorStore | 私域知识、历史文章、品牌素材 |
| 联网搜索 | Brave Search API | 实时热点、最新信息 |

#### RRF 融合
- 双路结果通过 RRF（倒数排名融合）算法排序
- 不依赖原始分数，只需排名即可融合

#### 检索质量门控
- 相似度阈值 0.75 以下的片段丢弃，不进入 LLM 上下文
- 避免低质量内容污染生成结果

#### 可追溯性
- 每个检索结果标注来源文档名 + 段落编号
- LLM 生成内容标注"AI 生成"，检索内容标注来源
- 用户可追溯到每一条信息的出处

### 5. 草稿箱
- 工作流执行结果自动保存到 `ai_flow_output` 表
- `content` 为文字内容，`media` 为媒体附件 JSON
- 预留 `platforms` 字段记录多平台发布状态
- 按 `user_id` 隔离

## 验证要求
- 改后端代码后必须 `mvn compile` 或 `mvn install -DskipTests`
- 改前端代码后必须 `npx vue-tsc --noEmit`
- 改 API 后必须 curl 测试并确认返回 200
- 改模型配置相关代码后必须检查 status 默认值
- 改工作流代码后必须测试 generate → run 完整链路

## 常见错误记录
1. SSE 使用 SseEmitter.send() 时不要手动加 `data: ` 前缀，SseEmitter 自动处理
2. Ant Design Vue 4 的 Modal 使用 `v-model:open`（不是 `:open`），如果弹不出改用原生 div
3. EventSource 在浏览器中不能设置自定义 Header，token 必须走 query param
4. 创建模型时 `status` 字段默认值必须设为 1，否则查询不到
5. `@Transactional` 导致内层异常标记 rollback-only 时，外层 catch 也无法提交
6. LiteFlow 不支持动态 EL 执行，改用直接调用 NodeComponent.execute()
7. `mvn package` 在低内存环境下会 OOM 导致 JAR 损坏，改用 `mvn spring-boot:run -pl lightcatch-api -am`
8. 工作流执行时 userId 必须从 JWT 提取，不能传 input 作为 userId
9. 草稿箱表 `ai_flow_output` 没有 `tenant_id`，必须在 MyBatisPlusConfig 的白名单中忽略

## 方案设计检查清单（每次出方案前必过）
1. 数据模型 — 表结构怎么设计？字段够用吗？跟现有表的关系？
2. 持久化 — 数据存哪里？什么时候存？生命周期？要清理吗？
3. API 设计 — 需要几个接口？请求/响应格式？现有接口能复用吗？
4. 前端展示 — 用户在哪里看到？什么形式？怎么操作？
5. 用户体验 — 非技术用户能不学就上手吗？空/加/错误态？
6. 边界情况 — 没数据？输错了？后端挂了？
7. 安全 & 权限 — 谁可以看、改？数据隔离？
8. 可扩展性 — 后面加功能，现在设计改不改得动？

## 技术方案说明规范
每个方案必须说明：怎么做、用什么方案、为什么选这个（对比替代方案）、落地难度与成 本、可行性、方案对比、业务场景。

## 错误闭环
每次我犯错，在这里加一条记录，确保不重犯。
