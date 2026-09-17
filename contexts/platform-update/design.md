# 平台更新日志设计

## 数据

`biz_platform_update` 保存面向用户的发布记录。`status` 取 `DRAFT`、`PUBLISHED`、`WITHDRAWN`；查询接口对普通角色固定过滤 `PUBLISHED`，管理员管理接口返回全部状态。

## 接口

- `GET /business/platform-update/list`：教师、教研员、管理员查看已发布记录。
- `GET /business/platform-update/manage/list`：管理员管理查询。
- `POST /business/platform-update`：管理员新增草稿。
- `PUT /business/platform-update`：管理员编辑。
- `PUT /business/platform-update/{id}/status/{status}`：管理员发布、撤回或转草稿。

后端使用 `@PreAuthorize` 同时校验角色和菜单权限，避免只依赖前端按钮隐藏。

## 前端

Vue3 页面位于 `views/business/platformUpdate/index.vue`，复用 Element Plus `el-timeline`、分页和表单组件。管理员页面在同一路由展示维护按钮，教师和教研员只读。

## 发布记录自动化

AI 发布流程在构建、部署和接口探活均成功后调用新增接口写入草稿，再将状态置为 `PUBLISHED`；人工维护记录可直接由管理员页面完成。应用不监听 Git 仓库，也不把 push 事件直接暴露为更新。
