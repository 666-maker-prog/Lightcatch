# LightCatch（拾光）项目规则

## 技术栈
- 后端：Spring Boot 3.5.5 + JDK 17 + Maven
- 前端：Vue 3 + TypeScript + Vite + Ant Design Vue 4.2.6 + Pinia
- ORM：MyBatis-Plus 3.5.9
- AI 框架：LangChain4j 1.12.2
- 数据库：PostgreSQL 16 + 纯 Java 向量存储（VectorStore）
- 缓存：Redis
- 认证：JWT（jjwt 0.12.6）

## 架构约束

### 后端
- 所有 API 返回统一格式：`Result<T>`（success, message, data）
- Controller 只做参数校验和路由，业务逻辑在 Service 层
- AI 相关代码在 `lightcatch-ai` 模块，不要放到其他模块
- 工作流相关代码在 `lightcatch-workflow` 模块
- 切分策略使用 `ContentChunker`，修改必须同时改此处文档

### 前端
- 所有 API 调用通过 `@/utils/request.ts`（axios 封装）
- 状态管理用 Pinia store
- Modal/弹窗用 Ant Design Vue 组件，如果遇到兼容问题改用原生 div
- SSE 流式聊天在 `stores/chat.ts` 中处理
- 前端调试日志通过 `utils/debug.ts` 发送到后端 `logs/frontend.log`

### API 规范
- 认证方式：`X-Access-Token` Header（axios 自动注入）
- EventSource（SSE）不支持自定义 Header，token 通过 query param `?token=` 传递
- 所有 SSE 事件使用 `SseEmitter.send(jsonString)`，不要手动加 `data: ` 前缀
- 所有 GET 请求加 `_t` 时间戳防缓存

## 验证要求
- 改后端代码后必须 `mvn compile` 或 `mvn install -DskipTests`
- 改前端代码后必须 `npx vue-tsc --noEmit`
- 改 API 后必须 curl 测试并确认返回 200
- 改模型配置相关代码后必须检查 status 默认值

## 常见错误记录
1. SSE 使用 SseEmitter.send() 时不要手动加 `data: ` 前缀，SseEmitter 自动处理
2. Ant Design Vue 4 的 Modal 使用 `v-model:open`（不是 `:open`），如果弹不出改用原生 div
3. EventSource 在浏览器中不能设置自定义 Header，token 必须走 query param
4. 创建模型时 `status` 字段默认值必须设为 1，否则查询不到
5. `@Transactional` 导致内层异常标记 rollback-only 时，外层 catch 也无法提交

## 方案设计检查清单（每次出方案前必过）

1. **数据模型** — 表结构怎么设计？字段够用吗？跟现有表的关系？
2. **持久化** — 数据存哪里？什么时候存？生命周期？要清理吗？
3. **API 设计** — 需要几个接口？请求/响应格式？现有接口能复用吗？
4. **前端展示** — 用户在哪里看到它？什么形式（卡片/列表/弹窗）？怎么操作？
5. **用户体验** — 非技术用户能不学习就上手吗？空状态/加载态/错误态显示什么？
6. **边界情况** — 没数据时？用户输错了？网络断了/后端挂了？
7. **安全 & 权限** — 谁可以看、改？多租户数据隔离？
8. **可扩展性** — 后面要加功能，现在设计改不改得动？

## 技术方案说明规范（出方案时必须包含）
每个技术方案必须从四个维度说明：

1. **怎么做** — 具体技术实现方式
2. **用什么方案** — 选用的框架/库/工具
3. **为什么这样做** — 选型理由
4. **结合场景分析**：
   - **落地难度**：实现需要几天？现有代码能复用多少？
   - **开发成本**：需要额外付费吗（API Key、服务器）？
   - **运维成本**：上线后谁维护？出问题怎么排查？需要额外监控吗？
   - **可行性**：这个方案在我们场景下真的work吗？技术上有风险吗？有没有实际案例验证过？
   - **方案对比**：跟其他方案比为什么选这个？列出至少一个替代方案并说明为什么不选（不要只图简单，要考虑场景适配度）
   - **业务场景**：给谁用？解决什么具体问题？

## 错误闭环
每次我犯错，在这里加一条记录，确保不重犯。
