# aidex-admin

`aidex-admin` 是一个单模块 Spring Boot 后端工程模板，专注于后端代码生成，不包含前端生成、不包含系统管理、不包含登录鉴权。

当前默认主数据源是 `sqlite`，开箱即用，便于本地直接验证生成器。

核心能力：

- 单模块启动，结构简单
- 支持 MyBatis
- 支持多数据源动态切换
- 支持 MySQL 和 SQLite
- 提供数据库表结构探查
- 提供后端 MVC 三层代码生成预览和 ZIP 下载
- 支持启动时按配置自动生成后端代码
- 提供 Linux/Windows 启停脚本

快速开始：

```bash
cd /root/project/aidex-admin
# 已验证命令
/root/tools/apache-maven-3.9.9/bin/mvn clean package -DskipTests
chmod +x bin/*.sh
./bin/start.sh
```

仅用 SQLite 启动：

```bash
java -jar target/aidex-admin.jar \
  --server.port=8888 \
  --aidex.datasource.primary=sqlite \
  --aidex.datasource.sources.master.enabled=false
```

直接修改 `src/main/resources/application.yml`：

```yaml
aidex:
  generator:
    auto-generate:
      enabled: true
      datasource: sqlite
      tables: user,biz_profile
      package-name: com.aidex.template
      module-name: demo
      author: aidex-admin
```

然后正常启动应用，启动类会自动生成配置中的多张表。

默认接口：

- 健康检查：`GET /api/health`
- 项目说明：`GET /api/health/about`
- 数据源列表：`GET /api/generator/datasources`
- 表列表：`GET /api/generator/tables/{datasource}`
- 表详情：`GET /api/generator/tables/{datasource}/{tableName}`
- 代码预览：`POST /api/generator/preview`
- 代码下载：`POST /api/generator/download`

文档：

- [架构说明](docs/ARCHITECTURE.md)
- [部署说明](docs/DEPLOYMENT.md)
- [接口说明](docs/API.md)
- [示例 SQL](docs/SAMPLE_SQL.md)

说明：

- `/api/generator/preview` 会返回生成文件列表
- `/api/generator/download` 会返回 zip，同时把同样的文件落到 `aidex.generator.output-root`
- 当前模板只生成后端代码，不生成前端页面、API JS、Vue 文件
- 生成实体默认继承 `BaseEntity`，基础时间字段 `createTime/updateTime` 不会重复生成
- 默认测试库文件在 `runtime/sqlite/aidex-admin.db`
- 启动自动生成支持多个表，使用英文逗号分隔
- `insert/upsert` 对自增主键采用“有值参与、无值跳过”的策略
