# 机电设备在线招投标系统

一个面向面试的**机电设备在线招投标系统**：三种角色（厂家 / 招标单位 / 管理员），覆盖注册登录、招标发布、投标与评标、留言板、管理员审核等完整业务流程。

> 后端已从 Node.js + Express **重写为 Spring Boot 3 + MyBatis-Plus + Spring Security + JWT**，数据库重新设计，并修复了原项目的越权漏洞、重复投标、无分层等问题。详见 [`docs/面试指南.md`](docs/面试指南.md)。

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5 · Java 17 · MyBatis-Plus 3.5.7 · Spring Security · JWT (jjwt 0.12.5) · Maven |
| 数据库 | MySQL 8（schema + seed 见 `database/`）|
| 前端 | Vue 2.6 · Vuex · Vue Router · Element UI · axios |

## 目录结构

```
bidding-system/
├── backend/          # Spring Boot 后端（src/main/java/com/bidding）
│   ├── controller/   # 接口层
│   ├── service/      # 业务层（接口 + impl）
│   ├── mapper/       # MyBatis-Plus Mapper
│   ├── entity/ dto/ vo/  # 实体 / 入参 / 出参
│   ├── config/       # Security / MyBatis-Plus / Jackson / CORS
│   └── security/     # JWT 过滤器、工具类
├── database/         # schema.sql + seed.sql
├── docs/             # 面试指南、设计文档
└── src/              # Vue 前端
```

## 快速开始

**环境要求：** JDK 17、Maven 3.9+、MySQL 8、Node 16+。

**① 建库 + 灌数据（一次即可）**
```bash
mysql -uroot -p --default-character-set=utf8mb4 < database/schema.sql
mysql -uroot -p --default-character-set=utf8mb4 < database/seed.sql
```

**② 启动后端（8080）**
```bash
cd backend
mvn spring-boot:run
```
数据库账号密码可通过环境变量 `DB_USER` / `DB_PASSWORD` 覆盖（见 `backend/src/main/resources/application.yml`）。

**③ 启动前端（8081，代理到 8080）**
```bash
npm install
npm run serve
```
浏览器打开 `http://localhost:8081`。

## 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 厂家 | mfg01 / mfg02 / mfg03 | 123456 |
| 招标单位 | tender01 / tender02 | 123456 |
