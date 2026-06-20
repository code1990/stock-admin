# stock-admin 设计文档

## 1. 服务边界

`stock-admin` 只负责：

- 读取原始选股输入表
- 按原始选股逻辑执行公式
- 把命中结果作为 HTTP 响应返回

`stock-admin` 不负责：

- 刷行情
- 跑定时任务
- 写结果表
- 发通知
- 维护快照

结果与服务无关，结果只作为返回值存在。

## 2. 日线逻辑

日线严格按原表名和原逻辑执行：

- 股票池：`t_stock_pool`
- 公式：`t_stock_formula`
- 历史日线：`t_stock_daily_240`
- 实时行情快照：`t_stock_quote`

执行口径：

1. 查股票池
2. 查公式
3. 查 `t_stock_daily_240`
4. 查 `t_stock_quote`
5. 只读合并历史日线和实时行情
6. 执行公式
7. 返回命中结果

## 3. 60 分钟逻辑

60 分钟同样严格按原表名和原逻辑执行：

- 强池：`t_stock_pool_60`
- 公式：`t_stock_formula`
- 历史 60 分钟 K 线：`t_stock_daily_60`
- 盘中 60 分钟快照：`t_stock_quote_60`

执行口径：

1. 查 `t_stock_pool_60`
2. 用 `signal_name + stock_code` 限定股票和公式范围
3. 查 `t_stock_daily_60`
4. 查 `t_stock_quote_60`
5. 只读合并历史 60 分钟 K 线和盘中快照
6. 执行公式
7. 返回命中结果

## 4. 60 分钟数据齐全规则

60 分钟新增一个硬规则：

- 数据不齐全的股票，不参与选股

“齐全”的定义如下：

1. 先确定目标交易日当天的最新有效槽位
2. 槽位口径严格使用原逻辑：
   - `1030` -> 槽位 1
   - `1130` -> 槽位 2
   - `1400` -> 槽位 3
   - `1500` -> 槽位 4
3. 如果当天最新有效槽位是 `N`
4. 则某只股票只有在当天 `1..N` 槽位全部存在时，才允许参与选股

示例：

- 如果当天只更新到 `1130`
  - 必须同时存在 `1030`、`1130`
- 如果当天更新到 `1500`
  - 必须同时存在 `1030`、`1130`、`1400`、`1500`

任一槽位缺失：

- 该股票整只跳过
- 不做公式计算

## 5. 接口

接口拆分为 2 个：

- `POST /api/selection/day/run`
- `POST /api/selection/60min/run`

这样做的原因：

1. 日线和 60 分钟使用的表不同
2. 日线和 60 分钟的目标时间口径不同
3. 60 分钟存在“槽位齐全”校验
4. 60 分钟返回结果需要额外带出槽位信息

### 5.1 日线接口

接口：

- `POST /api/selection/day/run`

请求字段：

- `strategyName` 或 `formulaCode`，二选一
- `tradeDate`，可选，格式 `yyyyMMdd`
- `stockCodes`，可选
- `limit`，可选

返回字段：

- `strategyName`
- `tradeDate`
- `total`
- `items`

`items` 字段：

- `stockCode`
- `stockName`
- `marketCode`
- `tradeDate`
- `hitPrice`

### 5.2 60 分钟接口

接口：

- `POST /api/selection/60min/run`

请求字段：

- `strategyName` 或 `formulaCode`，二选一
- `tradeDate`，可选，格式 `yyyyMMdd`
- `stockCodes`，可选
- `limit`，可选

返回字段：

- `strategyName`
- `tradeDate`
- `total`
- `items`

`items` 字段：

- `stockCode`
- `stockName`
- `marketCode`
- `tradeDate`
- `hitPrice`
- `slotTradeDate`
- `slotIndex`

## 6. 开发方案

### 6.1 Controller 分层

控制器拆成两个入口：

1. `StockDailySelectionController`
2. `Stock60MinSelectionController`

两个接口共享公共响应结构，但不共享 URL。

### 6.2 Service 分层

Service 拆成两条主链路：

1. 日线选股服务
2. 60 分钟选股服务

二者都遵循：

- 查询股票范围
- 查询公式
- 查询 K 线
- 查询快照
- 只读合并
- 执行公式
- 返回结果

### 6.3 MyBatis 分层

日线 Mapper：

- `t_stock_pool`
- `t_stock_formula`
- `t_stock_daily_240`
- `t_stock_quote`

60 分钟 Mapper：

- `t_stock_pool_60`
- `t_stock_formula`
- `t_stock_daily_60`
- `t_stock_quote_60`

### 6.4 60 分钟完整性校验位置

完整性校验放在：

- `60min K线合并完成之后`
- `公式执行之前`

处理顺序：

1. 先取某股票历史 60 分钟 K 线
2. 再合并 `t_stock_quote_60` 当天快照
3. 计算目标交易日最新有效槽位 `N`
4. 校验该股票是否完整具备 `1..N`
5. 不完整直接跳过
6. 完整才执行公式

### 6.5 结果对象

结果对象与服务状态无关：

- 不落库
- 不缓存结果
- 不做异步通知
- 不做调度

接口返回即结果本身。

## 7. 当前实现原则

1. 所有查询走 MyBatis
2. 严格使用原表名
3. 严格保留原逻辑的“查询 + 合并 + 计算”部分
4. 严格去掉“刷新 + 持久化 + 通知 + 调度”部分
