# 示例 SQL

## 1. MySQL 示例

```sql
create table if not exists biz_demo_order (
    id bigint primary key auto_increment comment '主键',
    order_no varchar(64) not null comment '订单号',
    customer_name varchar(100) comment '客户名称',
    amount decimal(10,2) comment '订单金额',
    status varchar(20) comment '订单状态',
    create_time datetime comment '创建时间',
    update_time datetime comment '更新时间'
) comment='订单示例表';
```

推荐生成请求：

```json
{
  "datasource": "master",
  "tableName": "biz_demo_order",
  "packageName": "com.demo.generator",
  "moduleName": "demo",
  "businessName": "order",
  "author": "aidex"
}
```

## 2. SQLite 示例

```sql
create table if not exists biz_demo_note (
    id integer primary key autoincrement,
    title text not null,
    content text,
    status text,
    create_time datetime,
    update_time datetime
);
```

推荐生成请求：

```json
{
  "datasource": "sqlite",
  "tableName": "biz_demo_note",
  "packageName": "com.demo.generator",
  "moduleName": "demo",
  "businessName": "note",
  "author": "aidex"
}
```

## 3. 说明

- 默认会自动移除表前缀 `t_`、`biz_`
- `biz_demo_order` 会生成 `DemoOrder`
- `create_time`、`update_time` 由 `BaseEntity` 承载，不会在子实体重复声明
