<template>
  <div class="app-container">
    <el-alert title="学生下载后直接运行，自动识别IP并沿用网页登录关联，不需要填写注册码。老师可后续设置机房和机位；不使用助手照常上课。" type="info" :closable="false" />
    <el-form label-width="130px" class="settings">
      <el-form-item label="学校"><el-tree-select v-model="deptId" :data="schools" :props="{value:'deptId',label:'deptName',children:'children'}" check-strictly style="width: 300px" @change="load" /></el-form-item>
      <el-form-item label="启用机位助手"><el-switch v-model="form.enabled" :disabled="!data.available" /></el-form-item>
      <el-form-item label="平台网页地址"><el-input v-model="form.platformUrl" placeholder="http://10.52.1.130 或 https://平台域名" style="width: 460px" /></el-form-item>
      <el-form-item><el-button type="primary" :disabled="loading || !deptId || !data.available" :loading="busy" @click="save">保存学校设置</el-button></el-form-item>
    </el-form>
    <el-alert v-if="data.available === false" title="服务尚未启用，请先执行配套SQL、配置 seat-helper.enabled=true 并放置客户端程序。" type="warning" :closable="false" />
    <template v-if="form.enabled && data.available">
      <el-form inline><el-form-item label="机房"><el-input :disabled="busy" v-model="roomName" maxlength="80" /></el-form-item><el-form-item label="机位"><el-input :disabled="busy" v-model="seatName" maxlength="40" /></el-form-item><el-button :disabled="loading || !roomName || !seatName" :loading="busy" @click="enroll">生成一次性注册码</el-button></el-form>
      <el-alert v-if="code" :title="`学校 ${deptId} · ${roomName} · ${seatName} 注册码（30分钟内有效，仅使用一次）：${code}`" description="由管理员在对应电脑首次启动助手时填写。复制已配置的数据文件到别的电脑不能完成注册。" type="success" :closable="false" />
    </template>
    <el-table :data="data.devices || []">
      <el-table-column prop="roomName" label="机房" /><el-table-column prop="seatName" label="机位" />
      <el-table-column label="状态"><template #default="{row}">{{ row.active ? '已注册' : '已撤销' }}</template></el-table-column>
      <el-table-column label="操作"><template #default="{row}"><el-button v-if="row.active" :disabled="busy || loading" link type="danger" @click="revoke(row)">撤销设备</el-button></template></el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';
import { listDept } from '@/api/system/dept';
import { handleTree } from '@/utils/ruoyi';
const schools = ref([]), deptId = ref(null), data = ref({}), busy = ref(false), code = ref(''), roomName = ref(''), seatName = ref('');
const form = ref({ enabled: false, platformUrl: window.location.origin });
const loading = ref(false);
watch(() => [roomName.value, seatName.value], () => { code.value = ''; });
let sequence = 0;
const scope = () => ({ school: deptId.value, ticket: sequence });
const active = ctx => ctx.school === deptId.value && ctx.ticket === sequence;
async function load() {
  ++sequence;
  const ctx = scope();
  code.value = ''; data.value = {}; busy.value = false;
  form.value = { enabled: false, platformUrl: window.location.origin };
  loading.value = !!ctx.school;
  if (!ctx.school) return;
  try {
    const response = await request({ url: '/business/seat-helper/admin', params: { deptId: ctx.school } });
    if (!active(ctx)) return;
    data.value = response.data;
    form.value = { enabled: !!data.value.school?.enabled, platformUrl: data.value.school?.platformUrl || window.location.origin };
  } finally { if (active(ctx)) loading.value = false; }
}
async function save() {
  if (loading.value || busy.value || !deptId.value) return;
  const ctx = scope(); busy.value = true;
  try {
    await request({ url: '/business/seat-helper/admin/configure', method: 'post', data: { deptId: ctx.school, ...form.value } });
    if (!active(ctx)) return;
    ElMessage.success('设置已保存'); await load();
  } finally { if (active(ctx)) busy.value = false; }
}
async function enroll() {
  if (loading.value || busy.value || !deptId.value) return;
  const ctx = scope(), room = roomName.value, seat = seatName.value;
  busy.value = true; code.value = '';
  try {
    const response = await request({ url: '/business/seat-helper/admin/enrollment', method: 'post', data: { deptId: ctx.school, roomName: room, seatName: seat } });
    if (active(ctx) && room === roomName.value && seat === seatName.value) code.value = response.data.code;
  } finally { if (active(ctx)) busy.value = false; }
}
async function revoke(row) {
  if (busy.value || loading.value) return;
  const ctx = scope(); busy.value = true;
  try {
    try { await ElMessageBox.confirm(`撤销 ${row.roomName} ${row.seatName} 后，该设备不能继续关联账号。`, '撤销设备'); } catch { return; }
    if (!active(ctx)) return;
    await request({ url: `/business/seat-helper/admin/revoke/${row.deviceId}`, method: 'post' });
    if (active(ctx)) await load();
  } finally { if (active(ctx)) busy.value = false; }
}
onBeforeUnmount(() => { ++sequence; });
onMounted(async () => { const response = await listDept(); schools.value = handleTree(response.data, 'deptId'); });
</script>
<style scoped>.settings { margin: 20px 0; }</style>
