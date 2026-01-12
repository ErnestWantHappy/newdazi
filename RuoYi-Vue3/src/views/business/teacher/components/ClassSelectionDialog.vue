<template>
  <el-dialog 
    title="请选择班级" 
    v-model="visible" 
    width="400px" 
    append-to-body 
    align-center
    @closed="handleClose"
  >
    <div class="class-list">
      <el-button 
        v-for="cls in classes" 
        :key="cls" 
        type="primary" 
        plain 
        class="class-btn"
        @click="handleSelect(cls)"
      >
        {{ cls }}
      </el-button>
    </div>
    <div v-if="classes.length === 0" class="empty-tip">
      该年级暂无班级
    </div>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue';

const visible = ref(false);
const classes = ref([]);
const resolvePromise = ref(null);

/**
 * 打开选择框
 * @param {Array} classList 班级列表字符串数组
 * @returns {Promise<String|null>} 返回选中的班级名称，未选中返回null
 */
function open(classList) {
  classes.value = classList || [];
  visible.value = true;
  return new Promise((resolve) => {
    resolvePromise.value = resolve;
  });
}

function handleSelect(cls) {
  // 从 "705班" 提取 "705"
  const pureClassCode = cls.replace('班', '');
  
  if (resolvePromise.value) {
    resolvePromise.value(pureClassCode);
    resolvePromise.value = null;
  }
  visible.value = false;
}

function handleClose() {
  if (resolvePromise.value) {
    resolvePromise.value(null);
    resolvePromise.value = null;
  }
}

defineExpose({
  open
});
</script>

<style scoped>
.class-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  padding: 10px;
}
.class-btn {
  width: 100%;
  margin: 0 !important;
  height: 40px;
}
.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
