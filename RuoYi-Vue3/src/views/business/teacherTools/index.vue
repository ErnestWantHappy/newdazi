<template>
  <div class="app-container teacher-tools-page" data-testid="teacher-tools-page">
    <header class="page-head">
      <div>
        <h2>教师工具</h2>
        <p>按名称搜索、按教学任务筛选，点击卡片在新标签页打开</p>
      </div>
      <el-button v-if="manager" icon="Setting" plain data-testid="teacher-tools-manage" @click="goManage">工具管理</el-button>
    </header>

    <section class="search-panel" aria-label="搜索与筛选">
      <div class="search-row">
        <el-input v-model="keyword" clearable size="large" prefix-icon="Search"
          placeholder="搜索名称、用途或常见说法，如“压缩图片”“PPT”" data-testid="teacher-tools-search"
          @keyup.enter="onKeywordEnter" />
        <span class="tool-count" data-testid="teacher-tools-count">{{ countText }}</span>
      </div>
      <div class="filter-row">
        <span class="filter-label">教学任务</span>
        <div class="filter-chips">
          <button type="button" :class="{ active: !filters.taskCode }" data-testid="teacher-tools-task-all" @click="setTask('')">全部</button>
          <button v-for="task in tasks" :key="task.taskCode" type="button" :class="{ active: filters.taskCode === task.taskCode }"
            :data-testid="`teacher-tools-task-${task.taskCode}`" @click="setTask(task.taskCode)">
            {{ task.taskName }}<small>{{ task.toolCount }}</small>
          </button>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">条件</span>
        <div class="filter-selects">
          <el-select v-model="filters.categoryCode" clearable size="small" placeholder="学段与分类" style="width: 132px" data-testid="teacher-tools-category-filter">
            <el-option v-for="category in categories" :key="category.categoryCode" :label="category.categoryName" :value="category.categoryCode" />
          </el-select>
          <el-select v-model="filters.sourceType" clearable size="small" placeholder="来源" style="width: 118px" data-testid="teacher-tools-source-filter">
            <el-option v-for="(label, value) in sourceOptions" :key="value" :label="label" :value="value" />
          </el-select>
          <el-select v-model="filters.accessType" clearable size="small" placeholder="访问条件" style="width: 118px" data-testid="teacher-tools-access-filter">
            <el-option v-for="(label, value) in accessOptions" :key="value" :label="label" :value="value" />
          </el-select>
        </div>
      </div>
      <div v-if="activeConditions.length" class="active-row" data-testid="teacher-tools-active-filters">
        <span class="filter-label">已选</span>
        <span v-for="item in activeConditions" :key="item.key" class="active-chip">{{ item.label }}</span>
        <el-button link type="primary" size="small" data-testid="teacher-tools-clear" @click="clearFilters">清除全部条件</el-button>
      </div>
    </section>

    <div v-if="loadError" class="state-block" data-testid="teacher-tools-load-error">
      <el-icon><WarningFilled /></el-icon>
      <p>工具目录加载失败，可能是网络或服务暂时不可用。</p>
      <el-button type="primary" :loading="loading" @click="loadCatalog">重新加载</el-button>
    </div>

    <div v-else v-loading="loading" class="catalog-content">
      <template v-if="searching">
        <section class="result-section" data-testid="teacher-tools-results">
          <div class="section-head">
            <div class="section-title"><svg-icon icon-class="search" /><div><h3>搜索结果</h3><p>共找到 {{ results.length }} 个工具，名称和别名优先显示</p></div></div>
          </div>
          <div v-if="results.length" class="tool-grid">
            <ToolCard v-for="tool in results" :key="tool.toolId" :tool="tool" :favorite="isFavorite(tool.toolId)"
              :source-label="sourceLabel(tool)" :testid-prefix="'teacher-tools-search'" @open="openTool" @toggle-favorite="toggleFavorite" />
          </div>
          <div v-else class="state-block" data-testid="teacher-tools-no-result">
            <el-icon><Search /></el-icon>
            <p>没有找到与“{{ keyword.trim() }}”相关的工具。</p>
            <div class="state-actions">
              <el-button @click="clearFilters">清除全部条件</el-button>
              <el-button type="primary" plain @click="fillExample">换个词试试</el-button>
            </div>
          </div>
        </section>
      </template>

      <template v-else>
        <section v-if="favoriteTools.length || recentTools.length" class="tool-section personal-section" data-testid="teacher-tools-personal">
          <div class="section-head">
            <div class="section-title"><svg-icon icon-class="star" /><div>
              <h3>我的常用</h3>
              <p>仅保存在当前浏览器，换设备或清除浏览器数据后需要重新添加</p>
            </div></div>
          </div>
          <div v-if="favoriteTools.length" class="tool-grid">
            <ToolCard v-for="tool in favoriteTools" :key="`fav-${tool.toolId}`" :tool="tool" :favorite="true" :testid-prefix="'teacher-tools-fav'"
              :source-label="sourceLabel(tool)" @open="openTool" @toggle-favorite="toggleFavorite" />
          </div>
          <p v-else class="hint-line" data-testid="teacher-tools-favorite-empty">
            还没有收藏工具。在任意工具卡片右上角点“☆ 收藏”，下次打开就能直接找到。
          </p>
          <!-- 最近打开与收藏是两种事实：前者只是“从本平台发起过打开”，不能当收藏用。 -->
          <div v-if="recentTools.length" class="recent-row">
            <span class="filter-label">最近打开</span>
            <!-- 原生按钮回车与空格都会触发点击，这里不再额外监听键盘，避免空格打开两次。 -->
            <button v-for="tool in recentTools" :key="`recent-${tool.toolId}`" type="button" @click="openTool(tool)">
              {{ tool.title }}
            </button>
          </div>
        </section>

        <!-- 没有收藏也没有最近打开时只给一行入口，不留大块空白。 -->
        <section v-else class="personal-hint" data-testid="teacher-tools-personal">
          <span class="filter-label">我的常用</span>
          <span>在任意工具卡片右上角点“☆ 收藏”，下次打开就能直接找到；收藏仅保存在当前浏览器。</span>
        </section>

        <!-- 有非关键词条件时只显示“符合条件的工具”，隐藏精选与全部分类，避免同一工具重复出现。 -->
        <section v-if="!hasConditions && curatedTools.length" class="tool-section" data-testid="teacher-tools-curated">
          <div class="section-head">
            <div class="section-title"><svg-icon icon-class="star" /><div><h3>教研精选</h3><p>教研员统一推荐的高频入口</p></div></div>
          </div>
          <div class="tool-grid">
            <ToolCard v-for="tool in curatedTools" :key="`curated-${tool.toolId}`" :tool="tool" :favorite="isFavorite(tool.toolId)"
              :source-label="sourceLabel(tool)" :testid-prefix="'teacher-tools-curated'" @open="openTool" @toggle-favorite="toggleFavorite" />
          </div>
        </section>

        <section v-if="hasConditions" class="result-section" data-testid="teacher-tools-filtered">
          <div class="section-head">
            <div class="section-title"><svg-icon icon-class="list" /><div><h3>符合条件的工具</h3><p>共 {{ results.length }} 个</p></div></div>
          </div>
          <div v-if="results.length" class="tool-grid">
            <ToolCard v-for="tool in results" :key="`filter-${tool.toolId}`" :tool="tool" :favorite="isFavorite(tool.toolId)"
              :source-label="sourceLabel(tool)" :testid-prefix="'teacher-tools-filter'" @open="openTool" @toggle-favorite="toggleFavorite" />
          </div>
          <div v-else class="state-block" data-testid="teacher-tools-filter-empty">
            <p>当前条件下没有工具。</p>
            <el-button type="primary" plain @click="clearFilters">清除全部条件</el-button>
          </div>
        </section>

        <section v-if="!hasConditions" class="tool-section" data-testid="teacher-tools-all">
          <div class="section-head">
            <div class="section-title"><svg-icon icon-class="tool" /><div><h3>全部工具</h3><p>按分类浏览；筛选结果中同一工具只出现一次</p></div></div>
            <span>{{ allTools.length }} 个</span>
          </div>
          <div class="category-list">
            <div v-for="category in visibleCategories" :id="category.categoryCode" :key="category.categoryId" class="category-block"
              :data-testid="`teacher-tools-category-${category.categoryCode}`">
              <div class="category-head">
                <div class="category-title"><svg-icon :icon-class="categoryIcon(category)" /><span>{{ category.categoryName }}</span><small>{{ category.tools.length }}</small></div>
                <el-button v-if="canToggle(category)" link type="primary" @click="toggleCategory(category.categoryCode)">
                  {{ isExpanded(category.categoryCode) ? '收起' : '展开全部' }}
                  <el-icon><ArrowUp v-if="isExpanded(category.categoryCode)" /><ArrowDown v-else /></el-icon>
                </el-button>
              </div>
              <div class="tool-grid">
                <ToolCard v-for="tool in toolsForCategory(category)" :key="`${category.categoryId}-${tool.toolId}`" :tool="tool"
                  :favorite="isFavorite(tool.toolId)" :source-label="sourceLabel(tool)" :category-code="category.categoryCode"
                  :testid-prefix="'teacher-tools-category'" @open="openTool" @toggle-favorite="toggleFavorite" />
              </div>
            </div>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { defineComponent, h, resolveComponent } from 'vue'
import { ElMessage } from 'element-plus'
import useUserStore from '@/store/modules/user.js'
import { getTeacherToolCatalog } from '@/api/business/teacherTools.js'
import {
  categoryToolsForDisplay,
  filterTeacherTools,
  hasActiveFilters,
  normalizeToolFilters,
  openTeacherTool,
  pickToolsByIds,
  pushRecentId,
  readFavoriteIds,
  readRecentIds,
  resolveAccessLabel,
  resolveTeacherToolCategoryIcon,
  resolveTeacherToolIcon,
  resolveTeacherToolTone,
  SOURCE_TYPE_LABELS,
  splitTags,
  toggleFavoriteId,
  mergeCatalogTools
} from './teacherToolsUtils.js'

const router = useRouter()
const userStore = useUserStore()
// roles 未加载完成时按非管理员处理，避免管理按钮闪现或直接抛异常。
const manager = computed(() => (userStore.roles || []).includes('admin') || (userStore.roles || []).includes('researcher'))
const loading = ref(false)
const loadError = ref(false)
const keyword = ref('')
const catalog = ref({ recommended: [], categories: [], tasks: [], terms: [] })
const expandedCodes = ref([])
const filters = reactive({ taskCode: '', categoryCode: '', sourceType: '', accessType: '' })
const favoriteIds = ref([])
const recentIds = ref([])
const userId = computed(() => userStore.id)

const sourceOptions = SOURCE_TYPE_LABELS
const accessOptions = { DIRECT: '直接打开', LOGIN_REQUIRED: '需登录', INTRANET_ONLY: '限内网', DOWNLOAD: '下载工具' }
const tasks = computed(() => catalog.value.tasks || [])
const categories = computed(() => (catalog.value.categories || []).filter(category => category.tools?.length))
const allTools = computed(() => mergeCatalogTools(catalog.value))
const results = computed(() => filterTeacherTools(catalog.value, { ...filters, keyword: keyword.value }))
const searching = computed(() => Boolean(keyword.value.trim()))
const hasConditions = computed(() => hasActiveFilters(filters))
const visibleCategories = computed(() => filters.categoryCode
  ? categories.value.filter(category => category.categoryCode === filters.categoryCode)
  : categories.value)
const countText = computed(() => {
  if (searching.value || hasConditions.value) return `全部 ${allTools.value.length} 个 · 当前 ${results.value.length} 个`
  return `共 ${allTools.value.length} 个工具`
})
const activeConditions = computed(() => {
  const active = normalizeToolFilters({ ...filters, keyword: keyword.value })
  const taskName = tasks.value.find(task => task.taskCode === active.taskCode)?.taskName
  const categoryName = categories.value.find(category => category.categoryCode === active.categoryCode)?.categoryName
  return [
    active.keyword ? { key: 'keyword', label: `关键词：${active.keyword}` } : null,
    taskName ? { key: 'task', label: `教学任务：${taskName}` } : null,
    categoryName ? { key: 'category', label: `分类：${categoryName}` } : null,
    active.sourceType ? { key: 'source', label: `来源：${SOURCE_TYPE_LABELS[active.sourceType] || active.sourceType}` } : null,
    active.accessType ? { key: 'access', label: `访问：${accessOptions[active.accessType] || active.accessType}` } : null
  ].filter(Boolean)
})
const favoriteTools = computed(() => pickToolsByIds(allTools.value, favoriteIds.value))
const recentTools = computed(() => pickToolsByIds(allTools.value, recentIds.value.filter(id => !favoriteIds.value.includes(id))).slice(0, 4))
const curatedTools = computed(() => allTools.value.filter(tool => tool.recommended))

const ToolIcon = defineComponent({
  props: { tool: { type: Object, required: true }, categoryCode: { type: String, default: '' } },
  setup(props) {
    // 图片失败只回退到语义图标，不请求第三方图标服务。
    const failed = ref(false)
    watch(() => props.tool.iconUrl, () => { failed.value = false })
    return () => h('div', { class: ['tool-icon-wrap', `tone-${resolveTeacherToolTone(props.tool, props.categoryCode)}`] },
      props.tool.iconUrl && !failed.value
        ? h('img', { src: props.tool.iconUrl, alt: '', onError: () => { failed.value = true } })
        : h(resolveComponent('svg-icon'), { 'icon-class': resolveTeacherToolIcon(props.tool, props.categoryCode) }))
  }
})

const ToolCard = defineComponent({
  props: {
    tool: { type: Object, required: true },
    favorite: { type: Boolean, default: false },
    sourceLabel: { type: String, default: '' },
    // 同一工具会在常用、精选、分类里各出现一次，用前缀区分验收定位点。
    testidPrefix: { type: String, default: 'card' },
    categoryCode: { type: String, default: '' }
  },
  emits: ['open', 'toggle-favorite'],
  setup(props, { emit }) {
    const Tag = ({ text, extra }) => h('span', { class: ['tool-tag', ...(extra || [])] }, text)
    return () => {
        const access = resolveAccessLabel(props.tool)
      const tags = splitTags(props.tool.tags).slice(0, 1)
      return h('article', {
        class: ['tool-card', { 'is-favorite': props.favorite }],
        role: 'link',
        tabindex: 0,
        'data-testid': `${props.testidPrefix}-tool-${props.tool.toolId}`,
        'aria-label': `打开${props.tool.title}`,
        onClick: () => emit('open', props.tool),
        onKeydown: event => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); emit('open', props.tool) } }
      }, [
        // 卡片主体只保留名称、一句用途和必要标签；长文字换行阅读，不靠悬停。
        h(ToolIcon, { tool: props.tool, categoryCode: props.categoryCode }),
        h('div', { class: 'tool-main' }, [
          h('h4', { title: props.tool.title }, props.tool.title),
          h('p', { title: props.tool.description }, props.tool.description),
          h('div', { class: 'tool-tags' }, [
            ...(access ? [Tag({ text: access, extra: [`access-${String(props.tool.accessType || '').toLowerCase()}`] })] : []),
            // accessType 理论上后端恒有值；转字符串后再取小写，避免脏数据导致整张卡片渲染失败。
            ...(props.sourceLabel ? [Tag({ text: props.sourceLabel })] : []),
            ...tags.map(tag => Tag({ text: tag }))
          ])
        ]),
        // 收藏是独立按钮，不触发打开；点击后由父组件在当前卡片处给出结果提示。
        h('button', {
          type: 'button',
          class: ['favorite-btn', { active: props.favorite }],
          title: props.favorite ? '从我的常用中移除' : '加入我的常用',
          'aria-pressed': props.favorite ? 'true' : 'false',
          'data-testid': `${props.testidPrefix}-favorite-${props.tool.toolId}`,
          onClick: event => { event.stopPropagation(); emit('toggle-favorite', props.tool) },
          // 键盘聚焦在收藏按钮时只做收藏，不冒泡到卡片再打开新标签页。
          // 原生按钮已处理回车/空格的键盘激活，这里只阻断冒泡，不阻止默认行为，否则空格键会无法收藏。
          onKeydown: event => { event.stopPropagation() }
        }, [h('span', { class: 'fav-icon' }, props.favorite ? '★' : '☆'), h('span', { class: 'fav-text' }, props.favorite ? '已收藏' : '收藏')])
      ])
    }
  }
})

function categoryIcon(category) { return resolveTeacherToolCategoryIcon(category) }
function sourceLabel(tool) { return SOURCE_TYPE_LABELS[tool?.sourceType] || '' }
// 收藏 id 一律存成数字，目录侧可能是字符串，这里统一转后再比较，避免回显丢失。
function isFavorite(toolId) { return favoriteIds.value.includes(Number(toolId)) }
function isExpanded(code) { return expandedCodes.value.includes(code) }
function canToggle(category) {
  return category.sectionLevel !== 'PRIMARY' && category.defaultExpanded !== 'Y'
    && category.tools.length > (Number(category.previewLimit) || 4)
}
function toolsForCategory(category) { return categoryToolsForDisplay(category, '', isExpanded(category.categoryCode)) }
function toggleCategory(code) {
  expandedCodes.value = isExpanded(code) ? expandedCodes.value.filter(item => item !== code) : [...expandedCodes.value, code]
}
function setTask(code) { filters.taskCode = filters.taskCode === code ? '' : code }
function clearFilters() {
  keyword.value = ''
  Object.assign(filters, { taskCode: '', categoryCode: '', sourceType: '', accessType: '' })
}
function fillExample() { keyword.value = '压缩图片' }
function onKeywordEnter() {
  if (!searching.value) return
  nextTick(() => document.querySelector('[data-testid="teacher-tools-results"]')?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
}
function loadFavorites() {
  favoriteIds.value = readFavoriteIds(userId.value)
  recentIds.value = readRecentIds(userId.value)
}
function toggleFavorite(tool) {
  if (!userId.value) {
    ElMessage.warning('登录信息尚未加载，请稍后重试')
    return
  }
  const result = toggleFavoriteId(userId.value, tool.toolId)
  favoriteIds.value = result.ids
  ElMessage.success(result.added ? `已加入我的常用：${tool.title}` : `已从我的常用移除：${tool.title}`)
}
function openTool(tool) {
  // 原生 window.open 直接透传：拦截判断与 opener 隔离都在 openTeacherTool 内完成。
  if (!openTeacherTool(tool, window.open.bind(window))) {
    // 弹窗拦截与无效地址都走这里：无效地址找教研，拦截提示允许弹窗。
    ElMessage.error('未能打开工具：地址无效或被浏览器拦截，请检查弹窗设置或联系教研员')
    return
  }
  // 账号信息尚未加载完成时只跳过最近打开，不阻止工具本身打开。
  if (userId.value) {
    recentIds.value = pushRecentId(userId.value, tool.toolId)
  }
}
function goManage() { router.push('/business/teacher-tools/manage') }
async function loadCatalog() {
  loading.value = true
  loadError.value = false
  try {
    const response = await getTeacherToolCatalog()
    catalog.value = { recommended: [], categories: [], tasks: [], terms: [], ...(response.data || {}) }
  } catch {
    // 接口失败必须与“没有匹配结果”区分，避免显示成空目录。
    loadError.value = true
  } finally { loading.value = false }
}
watch(userId, loadFavorites)
onMounted(() => { loadFavorites(); loadCatalog() })
</script>

<style scoped>
.teacher-tools-page { max-width: 1540px; margin: 0 auto; color: var(--el-text-color-primary); }
.page-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-head h2 { margin: 0 0 6px; font-size: 24px; }.page-head p { margin: 0; color: var(--el-text-color-secondary); }
.search-panel { position: sticky; top: 0; z-index: 8; margin-bottom: 18px; padding: 14px 16px; border: 1px solid var(--el-border-color-lighter); border-radius: 12px; background: var(--el-bg-color); }
.search-row { display: flex; align-items: center; gap: 16px; }.search-row .el-input { max-width: 620px; }
.tool-count { color: var(--el-text-color-secondary); white-space: nowrap; font-size: 13px; }
.filter-row { display: flex; align-items: center; gap: 12px; margin-top: 10px; }
.filter-label { flex: 0 0 auto; color: var(--el-text-color-secondary); font-size: 13px; font-weight: 600; }
.filter-chips { display: flex; flex-wrap: wrap; gap: 7px; }
.filter-chips button { display: inline-flex; align-items: center; gap: 4px; min-height: 30px; padding: 4px 11px; border: 1px solid var(--el-border-color-lighter); border-radius: 7px; background: var(--el-fill-color-lighter); color: var(--el-text-color-regular); font-size: 13px; cursor: pointer; }
.filter-chips button small { color: var(--el-text-color-placeholder); font-size: 11px; }
.filter-chips button.active { border-color: var(--el-color-primary); background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
.filter-selects { display: flex; flex-wrap: wrap; gap: 8px; }
.active-row { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-top: 10px; padding-top: 10px; border-top: 1px dashed var(--el-border-color-lighter); }
.active-chip { padding: 2px 9px; border-radius: 10px; background: var(--el-color-primary-light-9); color: var(--el-color-primary); font-size: 12px; }
.catalog-content { min-height: 320px; }
.tool-section, .result-section { scroll-margin-top: 210px; margin-bottom: 20px; padding: 16px; border: 1px solid var(--el-border-color-lighter); border-radius: 12px; background: linear-gradient(180deg, var(--el-color-primary-light-9) 0, var(--el-bg-color) 92px); }
.personal-section { border-color: var(--el-color-warning-light-7); background: linear-gradient(180deg, var(--el-color-warning-light-9) 0, var(--el-bg-color) 92px); }
.section-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 14px; }
.section-title { display: flex; align-items: center; gap: 10px; }.section-title > .svg-icon { width: 22px; height: 22px; color: var(--el-color-primary); }
.section-title h3 { margin: 0 0 3px; font-size: 18px; }.section-title p { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; }
.hint-line { margin: 0 0 4px; color: var(--el-text-color-secondary); font-size: 13px; }
.personal-hint { display: flex; align-items: baseline; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; padding: 10px 14px; border: 1px solid var(--el-border-color-lighter); border-radius: 10px; color: var(--el-text-color-secondary); font-size: 13px; }
.recent-row { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.recent-row button { max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 3px 10px; border: 1px solid var(--el-border-color-lighter); border-radius: 12px; background: var(--el-fill-color-lighter); color: var(--el-text-color-regular); font-size: 12px; cursor: pointer; }
.recent-row button:hover { border-color: var(--el-color-primary-light-5); color: var(--el-color-primary); }
.category-list { display: flex; flex-direction: column; gap: 16px; }
.category-block { scroll-margin-top: 210px; }
.category-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 10px; }
.category-title { display: inline-flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; }
.category-title .svg-icon { width: 18px; height: 18px; color: var(--el-text-color-secondary); }
.category-title small { color: var(--el-text-color-placeholder); font-weight: 400; font-size: 12px; }
.tool-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; }
.state-block { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 48px 16px; border: 1px dashed var(--el-border-color); border-radius: 12px; color: var(--el-text-color-secondary); text-align: center; }
.state-block .el-icon { font-size: 28px; }.state-block p { margin: 0; }
.state-actions { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
@media (max-width: 768px) {
  .page-head { align-items: flex-start; flex-direction: column; }
  .search-panel { position: static; }
  .search-row { flex-direction: column; align-items: stretch; gap: 6px; }.search-row .el-input { max-width: none; width: 100%; }
  .filter-row { flex-direction: column; align-items: flex-start; gap: 6px; }
  .filter-chips button { min-height: 34px; }
  .filter-selects { width: 100%; }.filter-selects :deep(.el-select) { flex: 1 1 45%; width: auto !important; }
  .tool-grid { grid-template-columns: 1fr; }
  .tool-section, .result-section { padding: 12px; }
  .section-title p { font-size: 12px; }
}
</style>

<style>
/* ToolCard 与 ToolIcon 由 render 函数动态创建，内层元素不会带父组件的 scoped 标识。
   这些样式必须用页面根类约束，否则卡片只剩白底黑字；选择器不外溢到其他页面。 */
.teacher-tools-page .tool-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 13px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition: border-color .16s ease, background .16s ease, box-shadow .16s ease, transform .16s ease;
  outline: none;
}
.teacher-tools-page .tool-card:hover,
.teacher-tools-page .tool-card:focus-visible {
  transform: translateY(-1px);
  border-color: var(--el-color-primary-light-5);
  background: var(--el-fill-color-extra-light);
  box-shadow: 0 4px 14px rgb(37 99 235 / 8%);
}
.teacher-tools-page .tool-card.is-favorite {
  border-color: var(--el-color-warning-light-5);
}
/* 图标容器固定尺寸并完整显示图片，不裁切品牌 Logo。 */
.teacher-tools-page .tool-icon-wrap {
  display: grid;
  place-items: center;
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  padding: 4px;
  box-sizing: border-box;
  border-radius: 10px;
  overflow: hidden;
  color: var(--tool-accent);
  background: var(--tool-soft);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 55%);
}
.teacher-tools-page .tool-icon-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.teacher-tools-page .tool-icon-wrap .svg-icon {
  width: 22px;
  height: 22px;
}
.teacher-tools-page .tone-blue { --tool-accent: #2563eb; --tool-soft: #eaf2ff; }
.teacher-tools-page .tone-cyan { --tool-accent: #0891b2; --tool-soft: #e6f8fc; }
.teacher-tools-page .tone-green { --tool-accent: #15965f; --tool-soft: #e7f8ef; }
.teacher-tools-page .tone-orange { --tool-accent: #d97706; --tool-soft: #fff3dd; }
.teacher-tools-page .tone-purple { --tool-accent: #7c3aed; --tool-soft: #f1eaff; }
.teacher-tools-page .tone-rose { --tool-accent: #e14b76; --tool-soft: #ffebf1; }
.teacher-tools-page .tool-main {
  flex: 1;
  min-width: 0;
  padding-right: 64px;
}
.teacher-tools-page .tool-main h4 {
  margin: 0 0 4px;
  font-size: 15px;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}
.teacher-tools-page .tool-main p {
  margin: 0 0 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}
.teacher-tools-page .tool-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}
.teacher-tools-page .tool-tag {
  padding: 1px 7px;
  border-radius: 10px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 11px;
}
.teacher-tools-page .access-login_required {
  color: var(--el-color-warning-dark-2);
  background: var(--el-color-warning-light-9);
}
.teacher-tools-page .access-intranet_only {
  color: var(--el-color-success-dark-2);
  background: var(--el-color-success-light-9);
}
.teacher-tools-page .access-download {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.teacher-tools-page .favorite-btn {
  position: absolute;
  top: 9px;
  right: 9px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  min-height: 26px;
  padding: 1px 7px;
  white-space: nowrap;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-bg-color);
  color: var(--el-text-color-secondary);
  font-size: 12px;
  cursor: pointer;
}
.teacher-tools-page .favorite-btn:hover,
.teacher-tools-page .favorite-btn.active {
  border-color: var(--el-color-warning-light-3);
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning-dark-2);
}
@media (max-width: 768px) {
  .teacher-tools-page .favorite-btn {
    justify-content: center;
    width: 26px;
    padding: 0;
  }
  .teacher-tools-page .favorite-btn .fav-text {
    display: none;
  }
  .teacher-tools-page .favorite-btn .fav-icon {
    font-size: 15px;
  }
  .teacher-tools-page .tool-main {
    padding-right: 30px;
  }
}
</style>
