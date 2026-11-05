<template>
  <section class="python-run" aria-label="Python 作品运行">
    <label :for="inputId">程序输入（每次 input() 的内容填写一行）</label>
    <el-input :id="inputId" v-model="stdin" type="textarea" :rows="3" maxlength="4096" placeholder="程序无需输入时可留空" :disabled="busy" />
    <div class="run-actions">
      <el-button type="primary" :loading="busy" @click="run">运行 Python</el-button>
      <span role="status">{{ status }}</span>
    </div>
    <pre v-if="output" class="run-output" aria-label="Python 运行输出">{{ output }}</pre>
    <p class="run-note">支持控制台程序。桌面窗口、硬件及本地文件依赖请下载后运行，运行结果供人工评分参考。</p>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import request from '@/utils/request';
const props = defineProps({ attachmentId: { type: [Number, String], required: true } });
const inputId = computed(() => 'python-stdin-' + props.attachmentId);
const stdin = ref(''), output = ref(''), status = ref(''), busy = ref(false);
let sequence = 0;
watch(() => props.attachmentId, () => {
  sequence++;
  stdin.value = ''; output.value = ''; status.value = ''; busy.value = false;
});
onBeforeUnmount(() => { sequence++; });
async function run() {
  if (busy.value) return;
  const ticket = ++sequence;
  busy.value = true; output.value = ''; status.value = '正在运行…';
  try {
    const response = await request({
      url: '/business/practical-attachment/' + props.attachmentId + '/run',
      method: 'post', data: { stdin: stdin.value }, timeout: 30000
    });
    if (ticket !== sequence) return;
    status.value = response.data.success ? '运行结束' : '运行未成功';
    output.value = response.data.output || '程序没有输出';
  } catch (error) {
    if (ticket === sequence) {
      status.value = '运行失败，可以重试';
      output.value = error?.msg || error?.message || '运行服务暂不可用，请稍后重试或下载后运行。';
    }
  } finally { if (ticket === sequence) busy.value = false; }
}
</script>

<style scoped>
.python-run { flex: none; min-width: 0; padding: 12px 0; }
.run-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-top: 10px; }
.run-output { max-height: 260px; overflow: auto; white-space: pre-wrap; overflow-wrap: anywhere; padding: 12px; background: #f5f7fa; }
.run-note { color: #606266; font-size: 12px; }
</style>
