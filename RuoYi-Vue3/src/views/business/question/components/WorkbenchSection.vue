<!-- 题型模块：标题 + 真实题量 + 新增/查看全部，内部为缩略卡片网格并支持加载更多。 -->
<template>
  <section class="qmodule" :class="`qmodule--${tone}`">
    <header class="qmodule__head">
      <div class="qmodule__heading">
        <span v-if="icon" class="qmodule__badge"><el-icon><component :is="iconComponent" /></el-icon></span>
        <h3>{{ title }}</h3>
        <span class="qmodule__total" :class="{ 'is-error': state.error }">
          <template v-if="state.loading && !state.loaded">加载中…</template>
          <template v-else-if="state.error">题量未知</template>
          <template v-else>{{ state.total }} 题</template>
        </span>
      </div>
      <div class="qmodule__ops">
        <el-button v-if="showAll && state.loaded && !state.error" link type="primary" @click="emit('view-all')">
          查看全部<span v-if="!state.error"> {{ state.total }} 题</span>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button v-if="canAdd" type="primary" plain size="small" @click="emit('add')">
          <el-icon><Plus /></el-icon>新增
        </el-button>
      </div>
    </header>

    <p v-if="hint" class="qmodule__hint">{{ hint }}</p>
    <!-- 操作题在此插入作答方式切换区 -->
    <slot />

    <div v-if="state.loading && !state.loaded" class="qmodule__skeleton">正在加载题目…</div>
    <div v-else-if="state.error" class="qmodule__state qmodule__state--error">
      <span>该模块加载失败，其他模块不受影响。</span>
      <el-button link type="primary" @click="emit('retry')">重试</el-button>
    </div>
    <div v-else-if="!state.items.length" class="qmodule__state">
      暂无符合条件的题目<template v-if="canAdd">，可从右上角新增</template>。
    </div>
    <template v-else>
      <div class="qmodule__grid">
        <question-card
          v-for="row in state.items"
          :key="row.questionId"
          :question="row"
          :actions="actionsOf(row)"
          :grade-dict="gradeDict"
          :semester-dict="semesterDict"
          @view="emit('view', row)"
          @preview="emit('preview', row)"
          @edit="emit('edit', row)"
          @remove="emit('remove', row)"
          @primary="emit('primary', row)"
        />
      </div>
      <div v-if="state.hasMore || state.loadingMore || state.loadMoreError" class="qmodule__more">
        <el-button size="small" :loading="state.loadingMore" @click="emit('load-more')">
          {{ state.loadMoreError ? '重新加载更多' : '加载更多' }}
        </el-button>
        <span v-if="state.loadMoreError" class="qmodule__state--error">追加失败，已保留当前列表</span>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { Check, Document, List, Grid } from '@element-plus/icons-vue'
import QuestionCard from './QuestionCard.vue'

const props = defineProps({
  title: { type: String, required: true },
  icon: { type: String, default: 'Document' },
  tone: { type: String, default: 'neutral' },
  hint: { type: String, default: '' },
  state: { type: Object, required: true },
  canAdd: { type: Boolean, default: false },
  showAll: { type: Boolean, default: true },
  gradeDict: { type: Array, default: () => [] },
  semesterDict: { type: Array, default: () => [] },
  actionsOf: { type: Function, default: () => [] },
})
const emit = defineEmits(['add', 'view-all', 'retry', 'load-more', 'view', 'preview', 'edit', 'remove', 'primary'])
const ICONS = { Grid, List, Check, Document }
// 图标以字符串传入，这里映射成已注册组件，父组件不必关心图标来源。
const iconComponent = computed(() => ICONS[props.icon] || Document)
</script>

<style scoped>
.qmodule {
  display: flex;
  flex-direction: column;
  padding: 14px 16px 16px;
  border: 1px solid #e6e9ef;
  border-radius: 12px;
  background: #fff;
}
.qmodule__head { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; justify-content: space-between; }
.qmodule__heading { display: flex; gap: 8px; align-items: center; min-width: 0; }
.qmodule__heading h3 { margin: 0; font-size: 17px; color: #1f2329; }
.qmodule__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  color: #fff;
  background: #8a9099;
}
.qmodule--typing .qmodule__badge { background: #7c5cf0; }
.qmodule--choice .qmodule__badge { background: #3b82f6; }
.qmodule--judgment .qmodule__badge { background: #16a34a; }
.qmodule--practical .qmodule__badge { background: #f59e0b; }
.qmodule__total { padding: 1px 8px; border-radius: 10px; background: #f2f4f7; color: #5b6472; font-size: 12px; }
.qmodule__total.is-error { background: #fff3f0; color: #c45656; }
.qmodule__ops { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
.qmodule__hint { margin: 6px 0 0; color: #8a9099; font-size: 12px; }
.qmodule__grid { display: grid; gap: 10px; margin-top: 12px; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.qmodule--typing .qmodule__grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.qmodule__skeleton { height: 180px; margin-top: 12px; }
.qmodule__state { margin-top: 12px; padding: 18px; border: 1px dashed #e6e9ef; border-radius: 8px; color: #8a9099; font-size: 13px; text-align: center; }
.qmodule__state--error { color: #c45656; }
.qmodule__more { display: flex; gap: 10px; align-items: center; justify-content: center; margin-top: 12px; font-size: 12px; }

/* 桌面用断点放大网格，不依赖 JS 监听窗口尺寸 */
@media (min-width: 1200px) {
  .qmodule--typing .qmodule__grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
  /* 选择/判断模块本身只有半宽，2 列即 2×3 */
  .qmodule--simple .qmodule__grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .qmodule--practical .qmodule__grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 1400px) {
  .qmodule--simple .qmodule__grid { grid-template-columns: minmax(0, 1fr); }
}
/* 窄屏统一单列，卡片正文与按钮不被压缩 */
@media (max-width: 768px) {
  .qmodule { padding: 12px; }
  .qmodule__grid,
  .qmodule--typing .qmodule__grid,
  .qmodule--practical .qmodule__grid { grid-template-columns: minmax(0, 1fr); }
}
</style>
