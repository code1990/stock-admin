# 接口说明

## 1. 健康检查

`GET /api/health`

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP"
  }
}
```

## 1.1 项目说明

`GET /api/health/about`

## 2. 数据源列表

`GET /api/generator/datasources`

## 3. 查询表列表

`GET /api/generator/tables/{datasource}`

示例：

```text
GET /api/generator/tables/master
```

## 4. 查询表详情

`GET /api/generator/tables/{datasource}/{tableName}`

## 5. 代码预览

`POST /api/generator/preview`

请求体：

```json
{
  "datasource": "master",
  "tableName": "biz_demo",
  "packageName": "com.demo.generator",
  "moduleName": "demo",
  "businessName": "demo",
  "author": "aidex"
}
```

返回值中的每一项包含：

- `fileName`
- `content`

说明：

- 生成结果固定为后端文件：`domain`、`mapper`、`mapper.xml`、`service`、`serviceImpl`、`controller`
- 生成实体会自动跳过 `BaseEntity` 已承载的 `createTime/updateTime`

## 6. 下载后端代码

`POST /api/generator/download`

请求体与预览一致，返回 zip 文件流。

同时服务端会把生成结果写入：

```text
aidex.generator.output-root
```
