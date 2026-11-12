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

## 2026-11-12 补充决策（本机，未发布）

- 用户名在执行SQL的线程采集后传给异步落库；只读取当前Authentication中的LoginUser，不为采集身份额外查库，匿名或无身份线程返回空值。
- `oper_name`空值仅表示来源未记录，不能证明后台调度、匿名请求或历史事件的实际来源。准确Mapper ID只能说明用途；禁止从`biz_lesson_checkin`表名推断后台预刷新。
- 耗时达到3000ms或仍有运行中查询优先critical，业务用途不覆盖风险等级；历史慢SQL回显动态重算，纠正旧建议，不批量回填数据库。
- 所有性能事件写入前统一将用户名限制为64字符，沿用既有表列；写入失败保留可观察计数。

## 验证

本机 `POST /monitor/diagnosis/perf-event/self-test` 返回 `intercepted=true, persisted=true, eventId=1426, elapsedMs=1226`；`sys_perf_event` 可查到 `select sleep(1.1) /* diag-self-test */`。
