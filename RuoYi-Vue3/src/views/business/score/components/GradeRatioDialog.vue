<template>
  <el-dialog
    v-model="visible"
    title="设置等级比例"
    width="450px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
      <el-alert 
        type="info" 
        :closable="false" 
        style="margin-bottom: 20px;"
      >
        <template #title>
          按总分排名，前 {{ form.excellent }}% 为优秀，{{ form.excellent }}%-{{ form.excellent + form.good }}% 为良好，依此类推
        </template>
      </el-alert>
      
      <el-form-item label="优秀" prop="excellent">
        <el-input-number v-model="form.excellent" :min="0" :max="100" :precision="0" style="width: 120px;" />
        <span style="margin-left: 8px;">%</span>
        <span style="margin-left: 20px; color: #67C23A;">（前 {{ form.excellent }}%）</span>
      </el-form-item>
      
      <el-form-item label="良好" prop="good">
        <el-input-number v-model="form.good" :min="0" :max="100" :precision="0" style="width: 120px;" />
        <span style="margin-left: 8px;">%</span>
        <span style="margin-left: 20px; color: #409EFF;">（{{ form.excellent }}% - {{ form.excellent + form.good }}%）</span>
      </el-form-item>
      
      <el-form-item label="及格" prop="pass">
        <el-input-number v-model="form.pass" :min="0" :max="100" :precision="0" style="width: 120px;" />
        <span style="margin-left: 8px;">%</span>
        <span style="margin-left: 20px; color: #E6A23C;">（{{ form.excellent + form.good }}% - {{ form.excellent + form.good + form.pass }}%）</span>
      </el-form-item>
      
      <el-form-item label="不及格" prop="fail">
        <el-input-number v-model="form.fail" :min="0" :max="100" :precision="0" style="width: 120px;" />
        <span style="margin-left: 8px;">%</span>
        <span style="margin-left: 20px; color: #F56C6C;">（后 {{ form.fail }}%）</span>
      </el-form-item>
      
      <el-form-item>
        <div :style="{ color: totalValid ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
          合计：{{ total }}%（{{ totalValid ? '✓ 正确' : '✗ 必须等于100%' }}）
        </div>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="resetToDefault">恢复默认</el-button>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!totalValid">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  ratios: {
    type: Object,
    default: () => ({ excellent: 25, good: 40, pass: 30, fail: 5 })
  }
});

const emit = defineEmits(['update:modelValue', 'confirm']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const formRef = ref(null);

const defaultRatios = { excellent: 25, good: 40, pass: 30, fail: 5 };

const form = ref({ ...defaultRatios });

// 当对话框打开时，同步外部传入的比例
watch(() => props.modelValue, (val) => {
  if (val) {
    form.value = { ...props.ratios };
  }
});

const total = computed(() => {
  return form.value.excellent + form.value.good + form.value.pass + form.value.fail;
});

const totalValid = computed(() => total.value === 100);

const rules = {
  excellent: [{ required: true, message: '请输入优秀比例', trigger: 'blur' }],
  good: [{ required: true, message: '请输入良好比例', trigger: 'blur' }],
  pass: [{ required: true, message: '请输入及格比例', trigger: 'blur' }],
  fail: [{ required: true, message: '请输入不及格比例', trigger: 'blur' }]
};

function resetToDefault() {
  form.value = { ...defaultRatios };
}

function handleConfirm() {
  if (!totalValid.value) return;
  emit('confirm', { ...form.value });
  visible.value = false;
}
</script>
