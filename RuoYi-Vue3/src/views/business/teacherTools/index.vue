<template>
  <div class="app-container teacher-tools-page" data-testid="teacher-tools-page">
    <header class="page-head">
      <div>
        <h2>教师工具</h2>
        <p>按学段和年级整理常用教学工具，点击卡片即可在新标签页打开</p>
      </div>
      <el-button v-if="manager" icon="Setting" plain data-testid="teacher-tools-manage" @click="goManage">工具管理</el-button>
    </header>

    <el-card shadow="never" class="search-card">
      <div class="search-row">
        <el-input v-model="keyword" clearable size="large" prefix-icon="Search"
          placeholder="搜索工具名称、用途或标签" data-testid="teacher-tools-search" />
        <span class="tool-count">共 {{ uniqueToolCount }} 个工具</span>
      </div>
      <nav v-if="hasResults" class="anchor-nav" aria-label="工具分类导航">
        <span class="anchor-label"><svg-icon icon-class="list" />快速定位</span>
        <div class="anchor-buttons">
          <button v-if="filtered.recommended.length" type="button" class="primary-anchor" @click="scrollTo('recommended')">
            <svg-icon icon-class="star" />常用推荐
          </button>
          <button v-for="category in filtered.categories" :key="category.categoryId" type="button"
            :class="{ 'primary-anchor': category.sectionLevel === 'PRIMARY' }" @click="scrollTo(category.categoryCode)">
            <svg-icon :icon-class="categoryIcon(category)" />{{ category.categoryName }}
          </button>
        </div>
      </nav>
    </el-card>

    <div v-loading="loading" class="catalog-content">
      <section v-if="filtered.recommended.length" id="recommended" class="tool-section featured-section">
        <div class="section-head">
          <div class="section-title"><svg-icon icon-class="star" /><div><h3>常用推荐</h3><p>教研员精选的高频入口</p></div></div>
          <span>{{ filtered.recommended.length }} 项</span>
        </div>
        <div class="tool-grid">
          <article v-for="tool in filtered.recommended" :key="`recommend-${tool.toolId}`" class="tool-card"
            role="link" tabindex="0" :aria-label="`打开${tool.title}`" :data-testid="`teacher-tool-${tool.toolId}`"
            @click="openTool(tool)" @keydown.enter.prevent="openTool(tool)" @keydown.space.prevent="openTool(tool)">
            <ToolIcon :tool="tool" category-code="recommended" />
            <div class="tool-main"><h4>{{ tool.title }}</h4><p>{{ tool.description }}</p><ToolTags :tool="tool" /></div>
            <el-icon class="open-icon"><TopRight /></el-icon>
          </article>
        </div>
      </section>

      <section v-for="category in filtered.categories" :id="category.categoryCode" :key="category.categoryId"
        class="tool-section" :data-testid="`teacher-tools-category-${category.categoryCode}`">
        <div class="section-head">
          <div class="section-title"><svg-icon :icon-class="categoryIcon(category)" /><div><h3>{{ category.categoryName }}</h3><p>{{ category.description }}</p></div></div>
          <div class="section-actions">
            <span>{{ category.tools.length }} 项</span>
            <el-button v-if="canToggle(category)" link type="primary" @click="toggleCategory(category.categoryCode)">
              {{ isExpanded(category.categoryCode) ? '收起' : '展开全部' }}
              <el-icon><ArrowUp v-if="isExpanded(category.categoryCode)" /><ArrowDown v-else /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="tool-grid">
          <article v-for="tool in toolsForCategory(category)" :key="`${category.categoryId}-${tool.toolId}`" class="tool-card"
            role="link" tabindex="0" :aria-label="`打开${tool.title}`" @click="openTool(tool)"
            @keydown.enter.prevent="openTool(tool)" @keydown.space.prevent="openTool(tool)">
            <ToolIcon :tool="tool" :category-code="category.categoryCode" />
            <div class="tool-main"><h4>{{ tool.title }}</h4><p>{{ tool.description }}</p><ToolTags :tool="tool" /></div>
            <el-icon class="open-icon"><TopRight /></el-icon>
          </article>
        </div>
      </section>

      <el-empty v-if="!loading && !hasResults" description="没有找到匹配的教师工具" />
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
  filterTeacherToolCatalog,
  openTeacherTool,
  resolveTeacherToolCategoryIcon,
  resolveTeacherToolIcon,
  resolveTeacherToolTone,
  splitTags
} from './teacherToolsUtils.js'

const router = useRouter()
const userStore = useUserStore()
const manager = computed(() => userStore.roles.includes('admin') || userStore.roles.includes('researcher'))
const loading = ref(false)
const keyword = ref('')
const catalog = ref({ recommended: [], categories: [] })
const expandedCodes = ref([])
const filtered = computed(() => filterTeacherToolCatalog(catalog.value, keyword.value))
const hasResults = computed(() => filtered.value.recommended.length > 0 || filtered.value.categories.length > 0)
const uniqueToolCount = computed(() => {
  const ids = new Set()
  catalog.value.recommended.forEach(tool => ids.add(tool.toolId))
  catalog.value.categories.forEach(category => category.tools.forEach(tool => ids.add(tool.toolId)))
  return ids.size
})

const ToolIcon = defineComponent({
  props: {
    tool: { type: Object, required: true },
    categoryCode: { type: String, default: '' }
  },
  setup(props) {
    const failed = ref(false)
    return () => h('div', { class: ['tool-icon-wrap', `tone-${resolveTeacherToolTone(props.tool, props.categoryCode)}`] }, props.tool.iconUrl && !failed.value
      ? h('img', { src: props.tool.iconUrl, alt: '', onError: () => { failed.value = true } })
      : h(resolveComponent('svg-icon'), { 'icon-class': resolveTeacherToolIcon(props.tool, props.categoryCode) }))
  }
})

const ToolTags = defineComponent({
  props: { tool: { type: Object, required: true } },
  setup(props) {
    return () => h('div', { class: 'tool-tags' }, [
      ...splitTags(props.tool.tags).slice(0, 2).map(tag => h('span', { class: 'tool-tag', key: tag }, tag)),
      props.tool.accessType && props.tool.accessType !== 'DIRECT'
        ? h('span', { class: `tool-tag access-${props.tool.accessType.toLowerCase()}` }, accessLabel(props.tool.accessType))
        : null
    ])
  }
})

function accessLabel(type) {
  return { LOGIN_REQUIRED: '需登录', INTRANET_ONLY: '限内网', DOWNLOAD: '下载工具' }[type] || ''
}
function categoryIcon(category) { return resolveTeacherToolCategoryIcon(category) }
function isExpanded(code) { return expandedCodes.value.includes(code) }
function toolsForCategory(category) { return categoryToolsForDisplay(category, keyword.value, isExpanded(category.categoryCode)) }
function canToggle(category) {
  return !keyword.value.trim() && category.sectionLevel !== 'PRIMARY' && category.defaultExpanded !== 'Y'
    && category.tools.length > (Number(category.previewLimit) || 4)
}
function toggleCategory(code) {
  expandedCodes.value = isExpanded(code) ? expandedCodes.value.filter(item => item !== code) : [...expandedCodes.value, code]
}
function scrollTo(code) {
  document.getElementById(code)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
function openTool(tool) {
  if (!openTeacherTool(tool, window.open.bind(window))) ElMessage.error('工具地址无效，请联系教研员处理')
}
function goManage() { router.push('/business/teacher-tools/manage') }
async function loadCatalog() {
  loading.value = true
  try {
    const response = await getTeacherToolCatalog()
    catalog.value = response.data || { recommended: [], categories: [] }
  } finally { loading.value = false }
}
onMounted(loadCatalog)
</script>

<style scoped>
.teacher-tools-page { max-width: 1540px; margin: 0 auto; color: var(--el-text-color-primary); }
.page-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-head h2 { margin: 0 0 6px; font-size: 24px; }.page-head p { margin: 0; color: var(--el-text-color-secondary); }
.search-card { position: sticky; top: 0; z-index: 8; margin-bottom: 18px; border-radius: 12px; }
.search-row { display: flex; align-items: center; gap: 16px; }.search-row .el-input { max-width: 620px; }.tool-count { color: var(--el-text-color-secondary); white-space: nowrap; }
.anchor-nav { display: flex; align-items: flex-start; gap: 12px; margin-top: 14px; padding-top: 13px; border-top: 1px solid var(--el-border-color-lighter); }
.anchor-label { display: inline-flex; align-items: center; gap: 6px; flex: 0 0 auto; min-height: 30px; padding: 5px 0; color: var(--el-text-color-secondary); font-size: 13px; font-weight: 600; }
.anchor-label .svg-icon { width: 15px; height: 15px; }
.anchor-buttons { display: flex; flex: 1; flex-wrap: wrap; gap: 7px; min-width: 0; }
.anchor-nav button { display: inline-flex; align-items: center; gap: 5px; min-height: 30px; border: 1px solid var(--el-border-color-lighter); border-radius: 7px; background: var(--el-fill-color-lighter); color: var(--el-text-color-regular); padding: 5px 10px; cursor: pointer; font-size: 13px; line-height: 18px; transition: color .16s ease, border-color .16s ease, background .16s ease; }
.anchor-nav button .svg-icon { width: 14px; height: 14px; color: var(--el-text-color-secondary); }
.anchor-nav button.primary-anchor { color: var(--el-color-primary); border-color: var(--el-color-primary-light-7); background: var(--el-color-primary-light-9); }
.anchor-nav button.primary-anchor .svg-icon { color: var(--el-color-primary); }
.anchor-nav button:hover { color: var(--el-color-primary); border-color: var(--el-color-primary-light-5); background: var(--el-color-primary-light-9); }
.catalog-content { min-height: 320px; }.tool-section { scroll-margin-top: 190px; margin-bottom: 22px; border: 1px solid var(--el-border-color-lighter); border-radius: 12px; background: var(--el-bg-color); padding: 16px; }
.featured-section { border-color: var(--el-color-primary-light-7); background: linear-gradient(135deg, var(--el-color-primary-light-9), var(--el-bg-color) 45%); }
.section-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 14px; }
.section-title { display: flex; align-items: center; gap: 10px; }.section-title > .svg-icon { width: 24px; height: 24px; color: var(--el-color-primary); }
.section-title h3 { margin: 0 0 3px; font-size: 18px; }.section-title p { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; }
.section-actions { display: flex; align-items: center; gap: 10px; color: var(--el-text-color-secondary); white-space: nowrap; }
.tool-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; }
.tool-card { position: relative; display: flex; align-items: center; gap: 12px; min-height: 84px; padding: 13px; border: 1px solid var(--el-border-color-lighter); border-radius: 10px; background: linear-gradient(135deg, var(--el-bg-color), var(--el-fill-color-extra-light)); cursor: pointer; transition: transform .16s ease, box-shadow .16s ease, border-color .16s ease; outline: none; }
.tool-card:hover, .tool-card:focus-visible { transform: translateY(-2px); border-color: var(--el-color-primary-light-5); box-shadow: var(--el-box-shadow-light); }
.tool-icon-wrap { display: grid; place-items: center; flex: 0 0 46px; width: 46px; height: 46px; border-radius: 12px; overflow: hidden; color: var(--tool-accent); background: var(--tool-soft); box-shadow: inset 0 0 0 1px rgb(255 255 255 / 45%); }
.tool-icon-wrap img { width: 100%; height: 100%; object-fit: cover; }.tool-icon-wrap :deep(.svg-icon) { width: 24px; height: 24px; }
.tone-blue { --tool-accent: #2563eb; --tool-soft: #eaf2ff; }.tone-cyan { --tool-accent: #0891b2; --tool-soft: #e6f8fc; }
.tone-green { --tool-accent: #15965f; --tool-soft: #e7f8ef; }.tone-orange { --tool-accent: #d97706; --tool-soft: #fff3dd; }
.tone-purple { --tool-accent: #7c3aed; --tool-soft: #f1eaff; }.tone-rose { --tool-accent: #e14b76; --tool-soft: #ffebf1; }
.tool-main { min-width: 0; flex: 1; }.tool-main h4 { margin: 0 22px 5px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 15px; }
.tool-main p { margin: 0 0 7px; color: var(--el-text-color-secondary); font-size: 12px; line-height: 1.45; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tool-tags { display: flex; gap: 5px; min-height: 20px; overflow: hidden; }.tool-tag { flex: 0 0 auto; padding: 2px 7px; border-radius: 10px; background: var(--el-fill-color-light); color: var(--el-text-color-secondary); font-size: 11px; }
.access-login_required { color: var(--el-color-warning-dark-2); background: var(--el-color-warning-light-9); }.access-intranet_only { color: var(--el-color-success-dark-2); background: var(--el-color-success-light-9); }.access-download { color: var(--el-color-primary); background: var(--el-color-primary-light-9); }
.open-icon { position: absolute; top: 13px; right: 12px; color: var(--el-text-color-placeholder); }
@media (max-width: 768px) { .page-head, .search-row, .section-head { align-items: flex-start; flex-direction: column; }.search-row .el-input { max-width: none; width: 100%; }.anchor-nav { flex-direction: column; gap: 6px; }.anchor-label { min-height: auto; }.anchor-buttons { width: 100%; }.tool-grid { grid-template-columns: 1fr; }.search-card { position: static; }.tool-section { scroll-margin-top: 12px; }.section-actions { align-self: stretch; justify-content: space-between; } }
</style>
