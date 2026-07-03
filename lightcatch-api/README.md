# LightCatch API — 应用入口

## 启动方式
```
mvn spring-boot:run -pl lightcatch-api -DskipTests
# 或 java -jar target/lightcatch-api-1.0.0.jar
```

## 配置
- `application.yml`：主配置（数据库、JWT、AI 模型参数）
- `logback-spring.xml`：日志配置（输出到 `logs/` 目录）

## API 端点
| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 登录 |
| `/api/auth/register` | POST | 注册 |
| `/api/ai/chat/stream` | GET/POST | SSE 流式聊天 |
| `/api/ai/model/*` | CRUD | 模型管理 |
| `/api/ai/knowledge/*` | CRUD | 素材库管理 |
| `/api/ai/writing/*` | POST | AI 写作 |
| `/api/ai/flow/*` | CRUD | 工作流 |
| `/api/debug/log` | POST | 前端调试日志 |
