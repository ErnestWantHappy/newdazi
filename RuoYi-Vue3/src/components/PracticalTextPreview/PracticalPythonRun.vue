<template>
  <!-- 作品运行与学生做题使用同一个终端：高版本浏览器本机跑，其余自动走服务器，交互方式一致 -->
  <section class="python-run" aria-label="Python 作品运行">
    <python-terminal
      :code="code || ''"
      :input-count="inputCount"
      :server-run="serverRun"
      note="支持控制台程序，运行结果仅供人工评分参考，不写入成绩。桌面窗口、硬件和本地文件依赖请下载后运行。"
    />
  </section>
</template>

<script setup>
import { computed } from 'vue';
import request from '@/utils/request';
import PythonTerminal from '@/components/PythonRunner/PythonTerminal.vue';
import { guessInputCount } from '@/components/PythonRunner/serverInteractive';
const props = defineProps({
  attachmentId: { type: [Number, String], required: true },
  // 作品源码由预览组件提供；没有源码时仍可走服务器运行（服务端会读取附件）
  code: { type: String, default: '' }
});
const inputCount = computed(() => guessInputCount(props.code));

/** 服务器运行：把当前累计的输入交给后端，由后端回放成“提示语 + 输入 + 输出”。 */
function serverRun(payload) {
  return request({
    url: '/business/practical-attachment/' + props.attachmentId + '/run',
    method: 'post',
    data: { stdin: payload?.stdin ?? '' },
    timeout: 30000
  });
}
</script>

<style scoped>
.python-run { flex: none; min-width: 0; padding: 12px 0; }
</style>
