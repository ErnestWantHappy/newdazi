# VForm3 类原生自定义组件搭建模式

> 基于"图片展示"（image-add）组件的完整实现过程总结，适用于快速搭建新的 VForm3 自定义组件。

---

## 目录

1. [概述](#1-概述)
2. [步骤一：定义 Widget Schema](#2-步骤一定义-widget-schema)
3. [步骤二：创建 Widget 组件](#3-步骤二创建-widget-组件)
4. [步骤三：全局注册组件](#4-步骤三全局注册组件)
5. [步骤四：VForm3 集成注册](#5-步骤四vform3-集成注册)
6. [步骤五：样式统一（类原生风格）](#6-步骤五样式统一类原生风格)
7. [步骤六：选中与交互机制](#7-步骤六选中与交互机制)
8. [步骤七：拖拽手柄与操作按钮](#8-步骤七拖拽手柄与操作按钮)
9. [步骤八：零延迟注册](#9-步骤八零延迟注册)
10. [步骤九：keep-alive 兼容](#10-步骤九keep-alive-兼容)
11. [步骤十：自定义属性与状态持久化](#11-步骤十自定义属性与状态持久化)
12. [常见问题排查](#12-常见问题排查)
13. [完整文件清单](#13-完整文件清单)

---

## 1. 概述

VForm3 是一个预构建的表单设计器包（vform3-builds），支持通过"自定义扩展字段"面板注册自定义组件。要使自定义组件达到"类原生"体验，需要完成以下核心工作：

- **面板可见**：组件出现在左侧"自定义扩展字段"面板中，有可视化文字
- **可选中/拖拽/删除**：与原生组件行为一致
- **属性面板可用**：右侧属性设置面板能正常渲染
- **样式统一**：选中框、操作按钮、拖拽手柄等样式与原生组件一致
- **零延迟出现**：进入设计页立即可见，无需等待
- **状态持久化**：自定义属性随表单 JSON 保存和恢复

---

## 2. 步骤一：定义 Widget Schema

在 `designer.vue` 中定义组件的 schema 常量，作为 VForm3 识别该组件的"身份证"。

### 2.1 Schema 模板

```javascript
// 在 designer.vue 的 <script setup> 中定义
const YOUR_WIDGET_SCHEMA = {
  type: 'your-widget-type',        // 唯一标识符，kebab-case
  icon: 'your-icon-name',          // VForm3 内置图标名称
  formItemFlag: true,              // 必须有，表示这是一个表单项
  options: {
    name: '',                      // 字段名（英文标识）
    label: '组件显示名',            // 默认标签
    labelAlign: '',                // 标签对齐方式
    defaultValue: null,
    columnWidth: '200px',
    size: '',
    labelWidth: null,
    labelHidden: false,
    disabled: false,
    hidden: false,
    required: false,
    requiredHint: '',
    validation: '',
    validationHint: '',
    // === 以下为自定义属性 ===
    // yourCustomProp1: defaultVal,
    // yourCustomProp2: defaultVal,
    customClass: '',
    onCreated: '',
    onMounted: '',
    onChange: '',
    onValidate: ''
  }
}
```

### 2.2 关键要点

| 属性 | 说明 |
|------|------|
| `type` | 唯一标识，对应 `registerCustomWidget` 中的类型名和组件标签名 |
| `icon` | VForm3 内置图标，如 `'picture-upload-field'` |
| `formItemFlag: true` | **必须**，否则组件不会被识别为表单字段 |
| `options` | 组件属性，会出现在右侧属性面板中 |
| `options.label` | 默认标签文字，用户可在属性面板中修改 |

### 2.3 图片展示组件示例

```javascript
// 文件：designer.vue, L1744-1774
const IMAGE_ADD_WIDGET_SCHEMA = {
  type: 'image-add',
  icon: 'picture-upload-field',
  formItemFlag: true,
  options: {
    name: '',
    label: '图片展示',
    labelAlign: '',
    columnWidth: '200px',
    size: '',
    labelWidth: null,
    labelHidden: false,
    disabled: false,
    hidden: false,
    required: false,
    requiredHint: '',
    validation: '',
    validationHint: '',
    imageWidth: 200,       // 自定义属性
    imageHeight: 200,      // 自定义属性
    imageUrl: '',           // 自定义属性
    customClass: '',
    onCreated: '',
    onMounted: '',
    onChange: '',
    onValidate: ''
  }
}
```

---

## 3. 步骤二：创建 Widget 组件

### 3.1 组件目录结构

```
src/components/YourWidget/
├── index.vue          # 组件主文件
```

### 3.2 组件模板结构

组件模板必须遵循以下结构才能被 VForm3 正确识别和管理：

```html
<template>
  <div
    class="your-widget-wrapper field-wrapper design-time-bottom-margin"
    :class="{ 'your-widget-selected': isDesignMode && isSelected }"
    :data-id="field.id"
    @mousedown.stop="selectField"
    @click.stop="selectField"
  >
    <!-- 1. 操作按钮栏（选中时显示） -->
    <div v-if="isDesignMode && isSelected" class="field-action">
      <i title="选中父组件" @click.stop="selectParentWidget">
        <el-icon><Back /></el-icon>
      </i>
      <i v-if="parentList && parentList.length > 1" title="上移组件" @click.stop="moveUpWidget">
        <el-icon><Top /></el-icon>
      </i>
      <i v-if="parentList && parentList.length > 1" title="下移组件" @click.stop="moveDownWidget">
        <el-icon><Bottom /></el-icon>
      </i>
      <i title="移除组件" @click.stop="removeFieldWidget">
        <el-icon><Delete /></el-icon>
      </i>
    </div>

    <!-- 2. 拖拽手柄（选中时显示，始终蓝色背景） -->
    <div v-if="isDesignMode && isSelected" class="drag-handler background-opacity">
      <i title="拖拽手柄">
        <el-icon><Rank /></el-icon>
      </i>
      <i>组件显示名</i>
    </div>

    <!-- 3. 组件主体内容 -->
    <div class="your-widget-body">
      <!-- 设计模式内容 -->
      <div v-if="isDesignMode">
        <!-- 占位符或内容 -->
      </div>
      <!-- 运行模式内容 -->
      <div v-else>
        <!-- 实际渲染内容 -->
      </div>
    </div>

    <!-- 4. 组件标签（位于主体正下方） -->
    <div class="your-widget-label">{{ displayLabel }}</div>
  </div>
</template>
```

### 3.3 组件 Props

VForm3 向自定义组件注入以下 props，**必须全部声明**：

```javascript
const props = defineProps({
  field: { type: Object, default: () => ({}) },       // 字段数据（含 options）
  designer: { type: Object, default: null },           // VForm3 designer 对象
  parentList: { type: Array, default: null },          // 父级 widgetList
  indexOfParentList: { type: Number, default: -1 },    // 在父列表中的索引
  parentWidget: { type: Object, default: null },       // 父级 widget
  designState: { type: Boolean, default: false },      // 是否在设计模式
  subFormRowIndex: { type: Number, default: -1 },
  subFormColIndex: { type: Number, default: -1 },
  subFormRowId: { type: String, default: '' }
})
```

### 3.4 核心 Computed

```javascript
// 判断是否为设计模式
const isDesignMode = computed(() => {
  return !!(props.designState || props.designer)
})

// 是否被选中
const isSelected = computed(() => {
  return !!(props.designer && props.field && props.field.id === props.designer.selectedId)
})

// 显示标签
const displayLabel = computed(() => {
  const opts = props.field?.options
  return (opts && opts.label) ? opts.label : '默认标签'
})
```

### 3.5 核心交互方法

```javascript
// 选中当前组件
function selectField(event) {
  if (props.designer) {
    props.designer.setSelected(props.field)
    props.designer.emitEvent('field-selected', props.parentWidget)
  }
}

// 选中父组件
function selectParentWidget() {
  if (props.designer) {
    if (props.parentWidget) {
      props.designer.setSelected(props.parentWidget)
    } else {
      props.designer.clearSelected()
    }
  }
}

// 上移组件
function moveUpWidget() {
  if (props.designer && props.parentList && props.indexOfParentList > 0) {
    props.designer.moveUpWidget(props.parentList, props.indexOfParentList)
    props.designer.emitHistoryChange()
  }
}

// 下移组件
function moveDownWidget() {
  if (props.designer && props.parentList && props.indexOfParentList < props.parentList.length - 1) {
    props.designer.moveDownWidget(props.parentList, props.indexOfParentList)
    props.designer.emitHistoryChange()
  }
}

// 移除组件
function removeFieldWidget() {
  if (props.designer && props.parentList && props.indexOfParentList >= 0) {
    const nextIndex = props.indexOfParentList >= props.parentList.length - 1
      ? props.indexOfParentList - 1
      : props.indexOfParentList + 1
    props.parentList.splice(props.indexOfParentList, 1)
    if (props.parentList.length > 0 && nextIndex >= 0 && nextIndex < props.parentList.length) {
      props.designer.setSelected(props.parentList[nextIndex])
    } else {
      props.designer.clearSelected()
    }
    props.designer.emitHistoryChange()
  }
}
```

### 3.6 自定义属性存取

自定义属性存储在 `props.field.options` 中，使用 computed 读写：

```javascript
// 确保 options 存在
function ensureOptions() {
  if (!props.field.options) {
    props.field.options = {}
  }
  return props.field.options
}

// 自定义属性（computed 双向绑定）
const yourProp = computed({
  get: () => ensureOptions().yourProp || defaultValue,
  set: (val) => { ensureOptions().yourProp = val }
})
```

### 3.7 完整 Script 示例

参见：[ImageAddWidget/index.vue](file:///e:/Project/newdazi/RuoYi-Vue3/src/components/ImageAddWidget/index.vue) L88-296

---

## 4. 步骤三：全局注册组件

在 `src/main.js` 中注册组件，**组件标签名必须与 schema 的 type 一致**（kebab-case）：

```javascript
// main.js
import YourWidget from '@/components/YourWidget/index.vue'

// 组件标签名 = schema.type（kebab-case）
app.component('your-widget-type', YourWidget)
```

图片展示组件示例：

```javascript
// main.js L50, L76
import ImageAddWidget from '@/components/ImageAddWidget/index.vue'
app.component('image-add-widget', ImageAddWidget)
```

> **注意**：VForm3 使用 `type` 属性查找对应的 Vue 组件。标签名格式为 `{type}-widget`。

---

## 5. 步骤四：VForm3 集成注册

这是最关键的一步，将自定义组件注入 VForm3 设计器的内部机制。

### 5.1 注册函数模板

在 `designer.vue` 中创建注册函数：

```javascript
function registerCustomWidget() {
  try {
    if (!designerRef.value) return
    const vFormInstance = designerRef.value

    // Step 1: 遍历 VNode 树查找 FieldPanel 组件
    let fieldPanel = null
    const root = vFormInstance.$.subTree
    if (!root) return

    function findFieldPanel(vnode) {
      if (!vnode || fieldPanel) return
      if (vnode.component) {
        const comp = vnode.component
        const name = comp.type && (comp.type.name || comp.type.__name)
        if (name === 'FieldPanel' || name === 'WidgetPanel' || name === 'widget-panel') {
          fieldPanel = comp
          return
        }
        if (comp.subTree) findFieldPanel(comp.subTree)
      }
      if (vnode.children && Array.isArray(vnode.children)) {
        for (const child of vnode.children) {
          findFieldPanel(child)
          if (fieldPanel) return
        }
      }
      if (vnode.dynamicChildren && Array.isArray(vnode.dynamicChildren)) {
        for (const child of vnode.dynamicChildren) {
          findFieldPanel(child)
          if (fieldPanel) return
        }
      }
    }
    findFieldPanel(root)
    if (!fieldPanel) return

    // Step 2: 注入到 customFields 数组（面板显示）
    const customFields = fieldPanel.data.customFields
    if (customFields && Array.isArray(customFields)) {
      const alreadyRegistered = customFields.some(f => f.type === 'your-widget-type')
      if (!alreadyRegistered) {
        customFields.push({
          key: 'your_widget_' + Date.now(),
          ...YOUR_WIDGET_SCHEMA,
          displayName: '组件显示名'
        })
      }
    }

    // Step 3: 修复 i18n2t（面板文字显示）
    if (!fieldPanel._i18nPatched) {
      fieldPanel._i18nPatched = true
      const proxy = fieldPanel.proxy
      const originalI18n2t = proxy.i18n2t
      proxy.i18n2t = function(d, e) {
        if (d === 'designer.widgetLabel.your-widget-type') {
          return '组件显示名'
        }
        return originalI18n2t.call(this, d, e)
      }
      // 强制触发响应式更新
      if (customFields && customFields.length > 0) {
        const lastItem = customFields[customFields.length - 1]
        customFields.push({ ...lastItem, key: 'force_update_' + Date.now() })
        customFields.pop()
      }
    }

    // Step 4: 修复 getFieldWidgetByType（属性面板支持）
    const designer = fieldPanel.props.designer
    if (designer && designer.getFieldWidgetByType) {
      const originalGetFieldWidgetByType = designer.getFieldWidgetByType
      if (!designer._yourWidgetPatched) {
        designer._yourWidgetPatched = true
        designer.getFieldWidgetByType = function(type) {
          if (type === 'your-widget-type') {
            return {
              key: 'your_widget',
              ...YOUR_WIDGET_SCHEMA,
              displayName: '组件显示名'
            }
          }
          return originalGetFieldWidgetByType.call(this, type)
        }
      }
    }
  } catch (e) {
    // 静默忽略注册失败
  }
}
```

### 5.2 四个关键步骤解析

| 步骤 | 目的 | 对应代码 |
|------|------|---------|
| Step 1: 查找 FieldPanel | 找到 VForm3 左侧面板组件实例 | 遍历 `vFormInstance.$.subTree` |
| Step 2: 注入 customFields | 组件出现在面板列表中 | `fieldPanel.data.customFields.push(...)` |
| Step 3: 修复 i18n2t | 面板中显示可视化文字（否则为空） | 覆盖 `fieldPanel.proxy.i18n2t` |
| Step 4: 修复 getFieldWidgetByType | 属性面板能正常渲染 | 覆盖 `designer.getFieldWidgetByType` |

### 5.3 关键陷阱

**i18n2t 必须覆盖 `proxy` 而非实例**：
```javascript
// ❌ 错误：Vue 3 渲染代理不会反映实例方法覆盖
fieldPanel.i18n2t = function(d, e) { ... }

// ✅ 正确：覆盖 proxy 上的方法
const proxy = fieldPanel.proxy
proxy.i18n2t = function(d, e) { ... }
```

**`customFields` 在 `data` 中，不在 `setupState` 中**：
```javascript
// ✅ 正确：FieldPanel 使用 Options API
const customFields = fieldPanel.data.customFields

// ❌ 错误：不在 setup 中
const customFields = fieldPanel.setupState.customFields
```

---

## 6. 步骤五：样式统一（类原生风格）

### 6.1 必选样式清单

```css
/* 1. 外层容器：match 原生 VForm3 field-wrapper */
.your-widget-wrapper.field-wrapper {
  position: relative;
  padding: 0;
  margin-bottom: 5px;
}

/* 2. 选中蓝色框 */
.your-widget-wrapper.your-widget-selected {
  outline: 2px solid #409eff !important;
  outline-offset: 0;
}

/* 3. 操作按钮栏：底部右侧蓝色背景 */
.field-action {
  position: absolute;
  bottom: 0;
  right: -2px;
  height: 22px;
  line-height: 22px;
  background: #409EFF;
  z-index: 9;
  display: flex;
  border-radius: 0;
  border: none;
  padding: 0;
}

.field-action i {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
  margin: 0 5px;
  cursor: pointer;
}

.field-action i:hover {
  opacity: 0.8;
}

/* 4. 拖拽手柄：顶部蓝色背景，选中时常亮 */
.drag-handler {
  position: absolute;
  top: 0;
  left: -1px;
  height: 20px;
  line-height: 20px;
  z-index: 9;
  background: #409EFF;  /* 始终蓝色，选中即常亮 */
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: move;
}

.drag-handler i {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-style: normal;
  color: #fff;
  margin: 0 4px;
}

/* 5. 组件标签：位于主体正下方，左对齐 */
.your-widget-label {
  font-size: 12px;
  color: #909399;
  text-align: left;
  margin-top: 4px;
  line-height: 1.4;
}
```

### 6.2 设计模式 vs 运行模式

组件需要区分两种模式，通过 `isDesignMode` computed 控制：

```html
<!-- 设计模式：占位符/编辑界面 -->
<div v-if="isDesignMode" class="design-placeholder">
  <el-icon><Plus /></el-icon>
  <span>点击添加内容</span>
</div>

<!-- 运行模式：实际渲染内容 -->
<div v-else class="runtime-content">
  <el-image :src="yourUrl" />
</div>
```

---

## 7. 步骤六：选中与交互机制

### 7.1 选中事件绑定

在根元素上绑定 `mousedown` 和 `click` 事件，使用 `.stop` 阻止冒泡：

```html
<div
  @mousedown.stop="selectField"
  @click.stop="selectField"
>
```

### 7.2 占位符点击需先选中

如果占位符有独立的点击行为（如打开文件对话框），需要先调用 `selectField()` 再执行后续操作：

```javascript
function onPlaceholderClick() {
  selectField()      // 先选中组件
  // 再执行自定义操作
  triggerUpload()
}
```

> **不先选中直接操作**会导致组件无法被选中，从而无法拖拽、删除。

---

## 8. 步骤七：拖拽手柄与操作按钮

### 8.1 拖拽手柄

- 仅在 **设计模式且选中** 时显示：`v-if="isDesignMode && isSelected"`
- 背景色始终为 `#409EFF`（蓝色），不依赖 hover
- 位置：`position: absolute; top: 0; left: -1px`
- 使用 VForm3 的 `background-opacity` class 与原生风格一致

### 8.2 操作按钮栏

- 仅在 **设计模式且选中** 时显示：`v-if="isDesignMode && isSelected"`
- 位置：`position: absolute; bottom: 0; right: -2px`
- 四个按钮：选中父组件、上移、下移、删除
- 使用 Element Plus 图标：`Back, Top, Bottom, Delete`

### 8.3 操作按钮模板

```html
<div v-if="isDesignMode && isSelected" class="field-action">
  <i title="选中父组件" @click.stop="selectParentWidget">
    <el-icon><Back /></el-icon>
  </i>
  <i v-if="parentList && parentList.length > 1" title="上移组件" @click.stop="moveUpWidget">
    <el-icon><Top /></el-icon>
  </i>
  <i v-if="parentList && parentList.length > 1" title="下移组件" @click.stop="moveDownWidget">
    <el-icon><Bottom /></el-icon>
  </i>
  <i title="移除组件" @click.stop="removeFieldWidget">
    <el-icon><Delete /></el-icon>
  </i>
</div>
```

---

## 9. 步骤八：零延迟注册

### 9.1 问题

VForm3 的 VNode 树构建需要时间，仅依赖轮询（300ms 间隔）会导致组件在面板中出现 2-3 秒延迟。

### 9.2 解决方案：MutationObserver

DOM 渲染比 VNode 树构建更快。使用 `MutationObserver` 监听 `.panel-container`（FieldPanel 的 DOM 根元素）的出现：

```javascript
// 在 designer.vue 中定义模块级变量和函数
let domObserverStarted = false
let domObserver = null

function startDomObserver() {
  if (domObserverStarted) return
  const container = document.querySelector('.designer-card')
  if (!container) {
    setTimeout(startDomObserver, 50)
    return
  }
  domObserverStarted = true
  domObserver = new MutationObserver(() => {
    const panelEl = document.querySelector('.panel-container')
    if (panelEl) {
      registerCustomWidget()  // 立即注册
      if (domObserver) {
        domObserver.disconnect()
        domObserver = null
        domObserverStarted = false
      }
    }
  })
  domObserver.observe(container, { childList: true, subtree: true })
  // 安全兜底：10秒后断开
  setTimeout(() => {
    if (domObserver) {
      domObserver.disconnect()
      domObserver = null
      domObserverStarted = false
    }
  }, 10000)
}
```

### 9.3 多层保障机制

建立四层注册保障，确保组件在任何情况下都能出现：

| 层级 | 触发时机 | 延迟 |
|------|---------|------|
| 第1层：MutationObserver | FieldPanel DOM 渲染完成 | ~0ms |
| 第2层：onFormJsonChange | VForm3 初始化事件 | ~100ms |
| 第3层：轮询注入 | 标签页注入轮询中 | ~300ms |
| 第4层：5秒兜底轮询 | 定时器 | ~5s |

---

## 10. 步骤九：keep-alive 兼容

### 10.1 问题

当页面使用 `<keep-alive>` 缓存时，`onActivated` 代替 `onMounted` 执行。如果在 `onActivated` 中没有重新设置 DOM 观察器，组件注册会失败。

### 10.2 解决方案

在 `onMounted` 和 `onActivated` 中都调用 `startDomObserver`：

```javascript
onMounted(() => {
  nextTick(startDomObserver)
  // ... 其他初始化
})

onActivated(() => {
  // keep-alive 激活时重新设置 DOM 观察器
  nextTick(startDomObserver)
  // ... 其他激活逻辑
})

onBeforeUnmount(() => {
  // 清理观察器
  if (domObserver) {
    domObserver.disconnect()
    domObserver = null
    domObserverStarted = false
  }
  // ... 清理其他定时器
})
```

### 10.3 路由配置

路由名称必须与组件 `name` 一致，确保 `<keep-alive>` 正确缓存：

```javascript
// router/index.js
{
  path: '/your-path',
  component: () => import('@/views/your-component'),
  name: 'YourComponentName',  // 必须与组件的 name 属性一致
  meta: { title: '页面标题' }
}
```

---

## 11. 步骤十：自定义属性与状态持久化

### 11.1 属性存储位置

所有自定义属性存储在 `props.field.options` 中，随表单 JSON 自动序列化：

```javascript
// 保存时：options 中的自定义属性自动包含在 formJson 中
const formJson = designerRef.value.getFormJson()
// formJson.widgetList[0].options.yourProp === 'yourValue'

// 加载时：从 formJson 恢复
const parsed = JSON.parse(formJson)
designerRef.value.setFormJson(parsed)
```

### 11.2 需要额外处理的属性

如果属性需要特殊处理（如评分配置的快照机制），参考 `_scoringConfig` 模式：

```javascript
// 在 setFormJson 之前提取自定义属性
if (parsed._yourCustomData) {
  yourCustomData.value = parsed._yourCustomData
}
designerRef.value.setFormJson(parsed)
```

### 11.3 修复兼容旧数据

如果旧版本数据缺少某些属性，在加载时修复：

```javascript
function fixLegacyData(parsed) {
  // 递归修复所有 widget
  function walk(widgetList) {
    if (!widgetList) return
    for (const w of widgetList) {
      if (w.type === 'your-widget-type' && !w.options.yourNewProp) {
        w.options.yourNewProp = defaultValue
      }
      if (w.widgetList) walk(w.widgetList)
    }
  }
  walk(parsed.widgetList)
}
```

---

## 12. 常见问题排查

### 12.1 面板中组件无文字显示

**原因**：i18n2t 覆盖错误。

**排查**：
- 确认覆盖的是 `fieldPanel.proxy.i18n2t`，不是 `fieldPanel.i18n2t`
- 确认 `designer.widgetLabel.{type}` 格式正确
- 确认触发了强制重新渲染（push/pop customFields）

### 12.2 组件无法选中/拖拽/删除

**原因**：点击事件未正确绑定或未调用 `selectField()`。

**排查**：
- 确认根元素有 `@mousedown.stop="selectField"` 和 `@click.stop="selectField"`
- 确认占位符点击事件先调用 `selectField()` 再执行其他操作
- 确认 `props.designer` 存在

### 12.3 属性面板不显示/无法编辑

**原因**：`getFieldWidgetByType` 未正确覆盖。

**排查**：
- 确认 `designer.getFieldWidgetByType` 被正确覆盖
- 确认 schema 的 `type` 与覆盖函数中的类型匹配
- 确认 `formItemFlag: true`

### 12.4 组件出现延迟 2-3 秒

**原因**：仅依赖轮询，DOM 观察器未设置或未在 keep-alive 激活时重新设置。

**排查**：
- 确认 `startDomObserver` 在 `onMounted` 和 `onActivated` 中都调用
- 确认 `.designer-card` 和 `.panel-container` 选择器正确
- 确认 `onBeforeUnmount` 中清理了观察器

### 12.5 keep-alive 切换后组件消失

**原因**：`onActivated` 未重新设置 DOM 观察器。

**排查**：
- 确认 `onActivated` 中调用了 `nextTick(startDomObserver)`
- 确认 `domObserverStarted` 在观察器断开后重置为 `false`

### 12.6 拖拽组件导致重复或 category undefined

**原因**：`enforceHomeTabConstraints` 使用了深拷贝导致引用丢失。

**排查**：
- 使用原地修改（in-place）而非深拷贝
- 确保 `internal: true` 标记在序列化/反序列化后不丢失

---

## 13. 完整文件清单

以"图片展示"组件为例，涉及的文件：

| 文件 | 作用 |
|------|------|
| `src/components/ImageAddWidget/index.vue` | 组件模板、脚本、样式 |
| `src/views/business/guideSheet/designer.vue` | VForm3 集成注册、DOM 观察器、生命周期管理 |
| `src/main.js` | 全局组件注册（`app.component`） |

### 创建新组件时的修改清单

1. **新建组件文件**：`src/components/YourWidget/index.vue`
2. **定义 Schema**：在 `designer.vue` 中添加 `YOUR_WIDGET_SCHEMA`
3. **编写注册函数**：在 `designer.vue` 中添加 `registerYourWidget()`
4. **修改 DOM 观察器回调**：在 `startDomObserver` 中调用 `registerYourWidget()`
5. **全局注册**：在 `main.js` 中 `app.component('your-widget-type', YourWidget)`

---

> 文档版本：v1.0 | 基于 image-add 组件实现总结 | 更新日期：2026-07-11