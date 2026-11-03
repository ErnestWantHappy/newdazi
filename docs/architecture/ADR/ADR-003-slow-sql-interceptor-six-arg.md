# ADR-003 慢 SQL 拦截必须覆盖 PageHelper 的 6 参数 Executor.query

- 状态：已采纳（2026-10-28，已随 1.30.60 发布）
- 关联：`contexts/diagnosis-perf-20261028/`

## 背景

`SlowSqlInterceptor` 原先只声明 `Executor.query` 的 4 参数签名和 `Executor.update`。PageHelper 是外层 MyBatis 插件，在 `PageInterceptor.intercept` 中直接调用 `Executor.query` 的 **6 参数**重载（带 `CacheKey` 和 `BoundSql`），不经过 `invocation.proceed()` 的 4 参数路径，因此慢 SQL 拦截器完全没有执行。

这解释了 130 诊断中心“Druid 有 1s+ SQL、`sys_perf_event` 近 7 天 0 条 slow_sql”的现象；本机用 `SELECT SLEEP(1.1)` 复现，Druid 记录 1114ms，但拦截计数为 0。

## 决策

`@Intercepts` 同时声明：

- `Executor.update(MappedStatement, Object)`
- `Executor.query(MappedStatement, Object, RowBounds, ResultHandler)`
- `Executor.query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)`

保留 4 参数签名兼容非 PageHelper 路径；6 参数签名覆盖 PageHelper 分页查询。增加静态 `getInterceptCount()`/`getSlowDetectCount()` 供 `perf-event/self-test` 判断“拦截器是否执行”和“是否识别为慢 SQL”。

## 后果

- 所有经过 PageHelper 的查询都能被计时和持久化，诊断时间线不再缺失慢 SQL。
- 每次查询多一层计时开销，可忽略；事件写入仍异步，入库失败有计数器。
- 自检接口会用 1.1 秒慢查询主动验证链路；生产执行时会产生 1 条 slow_sql 事件和 1 条 Druid 统计，属预期。

## 验证

本机 `POST /monitor/diagnosis/perf-event/self-test` 返回 `intercepted=true, persisted=true, eventId=1426, elapsedMs=1226`；`sys_perf_event` 可查到 `select sleep(1.1) /* diag-self-test */`。
