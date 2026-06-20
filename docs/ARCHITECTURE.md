# 架构说明

## 1. 目标

`aidex-admin` 简化为一个单模块 Spring Boot 模板，只解决三件事：

1. 后端最小化启动
2. 多数据源访问
3. 后端代码生成

## 2. 目录结构

```text
aidex-admin/
├── bin/
├── docs/
├── src/main/java/com/aidex/
│   ├── common/
│   ├── framework/
│   ├── generator/
│   └── web/
└── src/main/resources/
    ├── mybatis/
    └── templates/generator/backend/
```

## 3. 模块职责

- `common`：最小公共层，只保留基础响应、异常、少量工具和注解
- `framework`：动态数据源、AOP 切面、配置绑定、全局异常处理
- `generator`：表结构读取、类型映射、Velocity 渲染、ZIP 打包
- `web`：健康检查和生成器接口

## 4. 已剔除内容

- 登录、权限、JWT、用户体系
- Redis、验证码、监控、Swagger
- 系统管理和通用后台功能
- 前端 Vue 代码生成

## 5. 数据源设计

- 主数据源由 `aidex.datasource.primary` 指定
- 所有数据源在 `aidex.datasource.sources` 下声明
- 通过 `DynamicDataSource` + `ThreadLocal` 路由
- 当前模板默认提供 `master(MySQL)` 和 `sqlite(SQLite)`

## 6. 代码生成范围

当前只生成后端代码：

1. `domain`
2. `mapper`
3. `mapper.xml`
4. `service`
5. `service.impl`
6. `controller`

## 7. 生成策略

- 表名前缀可通过 `aidex.generator.table-prefixes` 自动剔除
- 生成实体默认继承 `BaseEntity`
- `create_time/update_time` 会映射到父类字段，不在子类重复生成
- `create_at/update_at`、`created_at/updated_at` 也会统一映射到 `createTime/updateTime`
- 主键字段自动识别，并用于 Controller/Service/Mapper 的主键入参
- 自增主键在 `insert/upsert` 中默认按“有值参与、无值跳过”生成
- 字符串查询条件生成 `like` 绑定参数，兼容 MySQL 和 SQLite
- 生成器只保留后端 MVC 三层及 MyBatis XML，不包含前端模板

## 8. 启动自动生成

- 通过 `aidex.generator.auto-generate.enabled=true` 开启
- `aidex.generator.auto-generate.tables` 支持多个表，英文逗号分隔
- 应用启动完成后自动落盘到 `aidex.generator.output-root`
