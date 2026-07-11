<template>
  <el-dialog
    v-model="visible"
    title="导出选项"
    width="500px"
    :close-on-click-modal="false"
  >
    <div style="margin-bottom: 15px;">
      <el-button size="small" @click="selectAll">全选</el-button>
      <el-button size="small" @click="selectNone">全不选</el-button>
      <span style="margin-left: 15px; color: #909399;">已选 {{ selectedColumns.length }} 列</span>
    </div>
    
    <el-checkbox-group v-model="selectedColumns">
      <div class="column-grid">
        <el-checkbox 
          v-for="col in availableColumns" 
          :key="col.key" 
          :label="col.key"
          :disabled="col.required"
        >
          {{ col.label }}
          <span v-if="col.required" style="color: #909399; font-size: 12px;">（必选）</span>
        </el-checkbox>
      </div>
    </el-checkbox-group>
    
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleExport" :loading="exporting">
        <el-icon><Download /></el-icon> 导出 Excel
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { Download } from '@element-plus/icons-vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  columns: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue', 'export']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const exporting = ref(false);

// 可用列配置
const availableColumns = computed(() => props.columns);

// 选中的列
const selectedColumns = ref([]);

// 打开对话框时默认选中所有列
watch(() => props.modelValue, (val) => {
  if (val) {
    selectedColumns.value = props.columns.map(c => c.key);
  }
});

function selectAll() {
  selectedColumns.value = props.columns.map(c => c.key);
}

function selectNone() {
  // 保留必选列
  selectedColumns.value = props.columns.filter(c => c.required).map(c => c.key);
}

async function handleExport() {
  if (selectedColumns.value.length === 0) {
    return;
  }
  exporting.value = true;
  try {
    await emit('export', selectedColumns.value);
  } finally {
    exporting.value = false;
    visible.value = false;
  }
}
</script>

<style scoped>
.column-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
</style>
