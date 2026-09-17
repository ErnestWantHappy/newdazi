<template>
  <div class="resource-fields">
    <el-divider content-position="left">课程信息</el-divider>
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <el-form-item label="学段" prop="schoolType">
          <el-select v-model="form.schoolType" placeholder="请选择" @change="handleSchoolTypeChange">
            <el-option v-for="item in SCHOOL_TYPES" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-form-item label="年级" prop="grade">
          <el-select v-model="form.grade" placeholder="请先选择学段">
            <el-option v-for="grade in grades" :key="grade" :label="gradeLabel(grade)" :value="grade" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-form-item label="学期" prop="semester">
          <el-select v-model="form.semester" placeholder="请选择">
            <el-option v-for="item in SEMESTERS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <el-form-item label="课次类型" prop="lessonKind">
          <el-select v-model="form.lessonKind" @change="handleLessonKindChange">
            <el-option v-for="item in LESSON_KINDS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col v-if="form.lessonKind === 'N'" :xs="24" :sm="8">
        <el-form-item label="第几课" prop="lessonNo">
          <el-input-number v-model="form.lessonNo" :min="1" :max="999" controls-position="right" />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="form.lessonKind === 'N' ? 8 : 16">
        <el-form-item label="课程标题" prop="courseTitle">
          <el-input v-model="form.courseTitle" maxlength="200" show-word-limit />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="课后反思与资源说明" prop="contentHtml">
      <ResearchRichEditor v-model="form.contentHtml" :min-height="240" />
    </el-form-item>

    <el-divider content-position="left">主课件（可选，一个）</el-divider>
    <div v-if="existingFile" class="existing-file">
      <span>{{ existingFile.originalFileName }}</span>
      <el-radio-group v-model="form.fileAction" size="small">
        <el-radio-button value="KEEP">保留</el-radio-button>
        <el-radio-button value="REMOVE">移除</el-radio-button>
        <el-radio-button value="REPLACE">替换</el-radio-button>
      </el-radio-group>
    </div>
    <el-upload
      v-if="!existingFile || form.fileAction === 'REPLACE'"
      drag
      action="#"
      :auto-upload="false"
      :limit="1"
      :on-change="handleFileChange"
      :on-remove="handleFileRemove"
      accept=".zip,.rar,.7z"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">拖入或点击选择 ZIP / RAR / 7z，严格不超过 50 MiB</div>
    </el-upload>

    <el-divider content-position="left">云盘链接（可选，最多三个）</el-divider>
    <div v-for="(link, index) in form.links" :key="index" class="link-row">
      <div class="link-row__head">
        <strong>云盘 {{ index + 1 }}</strong>
        <el-button link type="danger" @click="removeLink(index)">删除</el-button>
      </div>
      <el-row :gutter="12">
        <el-col :xs="24" :sm="8"><el-input v-model="link.resourceName" maxlength="255" placeholder="资源名称" /></el-col>
        <el-col :xs="24" :sm="12"><el-input v-model="link.linkUrl" maxlength="1000" placeholder="https://..." /></el-col>
        <el-col :xs="24" :sm="4"><el-input v-model="link.extractCode" maxlength="64" placeholder="提取码" /></el-col>
      </el-row>
      <el-row :gutter="12" class="link-row__second">
        <el-col :xs="24" :sm="6"><el-switch v-model="link.permanent" active-text="永久有效" /></el-col>
        <el-col v-if="!link.permanent" :xs="24" :sm="9">
          <el-date-picker v-model="link.expireTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择未来过期时间" />
        </el-col>
        <el-col :xs="24" :sm="link.permanent ? 18 : 9"><el-input v-model="link.description" maxlength="500" placeholder="补充说明（可选）" /></el-col>
      </el-row>
    </div>
    <el-button v-if="form.links.length < 3" plain icon="Plus" @click="addLink">添加云盘链接</el-button>
    <div class="resource-tip">主课件和云盘链接至少填写一项，可以同时提供。</div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import ResearchRichEditor from './ResearchRichEditor.vue'
import { SCHOOL_TYPES, SEMESTERS, LESSON_KINDS, gradeLabel, gradesForSchoolType } from '../utils/researchActivityFormat.js'
import { createEmptyLink, validatePackageFile } from '../utils/resourceForm.js'

const props = defineProps({ modelValue: { type: Object, required: true }, resources: { type: Array, default: () => [] } })
const emit = defineEmits(['update:modelValue', 'update:file'])
const form = computed({ get: () => props.modelValue, set: value => emit('update:modelValue', value) })
const grades = computed(() => gradesForSchoolType(form.value.schoolType))
const existingFile = computed(() => props.resources.find(item => item.resourceType === 'F'))

function handleSchoolTypeChange() {
  if (!grades.value.includes(Number(form.value.grade))) form.value.grade = null
}
function handleLessonKindChange() {
  if (form.value.lessonKind !== 'N') form.value.lessonNo = null
}
function addLink() { form.value.links.push(createEmptyLink()) }
function removeLink(index) { form.value.links.splice(index, 1) }
function handleFileChange(uploadFile) {
  const error = validatePackageFile(uploadFile.raw)
  if (error) {
    ElMessage.error(error)
    emit('update:file', null)
    return
  }
  if (existingFile.value) form.value.fileAction = 'REPLACE'
  emit('update:file', uploadFile.raw)
}
function handleFileRemove() { emit('update:file', null) }
</script>

<style scoped>
.resource-fields :deep(.el-select), .resource-fields :deep(.el-date-editor), .resource-fields :deep(.el-input-number) { width: 100%; }
.existing-file, .link-row__head { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.existing-file { padding: 10px 14px; margin-bottom: 12px; background: var(--el-fill-color-light); border-radius: 6px; }
.link-row { padding: 14px; margin-bottom: 12px; border: 1px solid var(--el-border-color); border-radius: 8px; }
.link-row__head { margin-bottom: 10px; }
.link-row__second { margin-top: 10px; align-items: center; }
.resource-tip { margin-top: 10px; color: var(--el-text-color-secondary); font-size: 13px; }
</style>
