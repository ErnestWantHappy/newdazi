<template>
   <el-form ref="userRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="用户昵称" prop="nickName">
         <el-input v-model="form.nickName" maxlength="30" />
      </el-form-item>
      <el-form-item>
      <el-button type="primary" @click="submit">保存</el-button>
      <el-button type="danger" @click="close">关闭</el-button>
      </el-form-item>
   </el-form>
</template>

<script setup>
import { updateUserProfile } from "@/api/system/user"

const props = defineProps({
  user: {
    type: Object
  }
})

const { proxy } = getCurrentInstance()

const form = ref({})
const rules = ref({
  nickName: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
})

/** 提交按钮 */
function submit() {
  proxy.$refs.userRef.validate(valid => {
    if (valid) {
      updateUserProfile(form.value).then(response => {
        proxy.$modal.msgSuccess("修改成功")
      })
    }
  })
}

/** 关闭按钮 */
function close() {
  proxy.$tab.closePage()
}

// 回显当前登录用户信息
watch(() => props.user, user => {
  if (user) {
    form.value = { nickName: user.nickName }
  }
},{ immediate: true })
</script>
