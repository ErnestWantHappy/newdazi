import test from 'node:test'
import assert from 'node:assert/strict'

import {
  configureStudentUploadWidget,
  parseGuideSheetUploadResponse
} from '../utils/studentUploadAdapter.js'

function createWidgetRef() {
  const calls = {
    before: [],
    success: [],
    headers: [],
    data: []
  }
  const widgetRef = {
    uploadHeaders: {},
    uploadData: { key: '' },
    fileList: [],
    handleOnBeforeUpload(file) {
      calls.before.push(file)
      return true
    },
    handleFileUpload(result, file, fileList) {
      calls.success.push({ result, file, fileList })
    },
    setUploadHeader(name, value) {
      calls.headers.push({ name, value })
      this.uploadHeaders[name] = value
    },
    setUploadData(name, value) {
      calls.data.push({ name, value })
      this.uploadData[name] = value
    }
  }
  return { widgetRef, calls }
}

test('每次上传前刷新认证和答题隔离参数', () => {
  const { widgetRef, calls } = createWidgetRef()
  const file = { uid: 1001, name: 'logo.png' }

  configureStudentUploadWidget(widgetRef, {
    bindingId: 2,
    fieldName: 'studentWork',
    getAuthorizationHeader: () => ({ Authorization: 'Bearer test-token' }),
    getClientUploadId: () => 'upload-client-1',
    notifyError: () => {}
  })
  const accepted = widgetRef.handleOnBeforeUpload(file)

  assert.equal(accepted, true)
  assert.equal(widgetRef.uploadHeaders.Authorization, 'Bearer test-token')
  assert.deepEqual(widgetRef.uploadData, {
    key: '',
    bindingId: '2',
    questionName: 'studentWork',
    clientUploadId: 'upload-client-1'
  })
  assert.deepEqual(calls.before, [file])
})

test('无认证信息时在发起网络请求前阻止上传', () => {
  const { widgetRef, calls } = createWidgetRef()
  const messages = []

  configureStudentUploadWidget(widgetRef, {
    bindingId: 2,
    fieldName: 'studentWork',
    getAuthorizationHeader: () => ({}),
    getClientUploadId: () => 'upload-client-1',
    notifyError: message => messages.push(message)
  })

  assert.equal(widgetRef.handleOnBeforeUpload({ uid: 1001, name: 'logo.png' }), false)
  assert.equal(calls.before.length, 0)
  assert.match(messages[0], /登录状态/)
})

test('业务失败响应不进入 VForm 成功处理且移除伪成功文件', () => {
  const { widgetRef, calls } = createWidgetRef()
  const messages = []
  const previous = { uid: 1000, name: 'report.pdf', status: 'success', url: '/file/1' }
  const failed = { uid: 1001, name: 'logo.png', status: 'success' }
  const fileList = [previous, failed]

  configureStudentUploadWidget(widgetRef, {
    bindingId: 2,
    fieldName: 'studentWork',
    getAuthorizationHeader: () => ({ Authorization: 'Bearer test-token' }),
    getClientUploadId: () => 'upload-client-1',
    notifyError: message => messages.push(message)
  })
  widgetRef.handleFileUpload({ code: 401, msg: '认证失败，无法访问系统资源' }, failed, fileList)

  assert.equal(calls.success.length, 0)
  assert.deepEqual(fileList, [previous])
  assert.match(messages[0], /认证失败/)
})

test('有效响应继续交给 VForm 写入文件模型', () => {
  const { widgetRef, calls } = createWidgetRef()
  const file = { uid: 1001, name: 'logo.png', status: 'success' }
  const fileList = [file]
  const response = {
    code: 200,
    data: { fileName: 'logo.png', accessUrl: '/business/guide-sheet/uploads/1/content' }
  }

  configureStudentUploadWidget(widgetRef, {
    bindingId: 2,
    fieldName: 'studentWork',
    getAuthorizationHeader: () => ({ Authorization: 'Bearer test-token' }),
    getClientUploadId: () => 'upload-client-1',
    notifyError: () => {}
  })
  widgetRef.handleFileUpload(response, file, fileList)

  assert.equal(calls.success.length, 1)
  assert.equal(calls.success[0].result, response)
})

test('图片上传的业务失败也不进入 VForm 成功处理', () => {
  const { widgetRef } = createWidgetRef()
  const pictureSuccessCalls = []
  delete widgetRef.handleFileUpload
  widgetRef.handlePictureUpload = (...args) => pictureSuccessCalls.push(args)
  const failed = { uid: 1001, name: 'logo.png', status: 'success' }
  const fileList = [failed]

  configureStudentUploadWidget(widgetRef, {
    bindingId: 2,
    fieldName: 'studentPicture',
    getAuthorizationHeader: () => ({ Authorization: 'Bearer test-token' }),
    getClientUploadId: () => 'upload-client-1',
    notifyError: () => {}
  })
  widgetRef.handlePictureUpload({ code: 500, msg: '图片存储失败' }, failed, fileList)

  assert.equal(pictureSuccessCalls.length, 0)
  assert.deepEqual(fileList, [])
})

test('重复配置不会叠加钩子且使用最新绑定与认证', () => {
  const { widgetRef, calls } = createWidgetRef()
  const baseContext = {
    fieldName: 'studentWork',
    getClientUploadId: () => 'upload-client-1',
    notifyError: () => {}
  }

  configureStudentUploadWidget(widgetRef, {
    ...baseContext,
    bindingId: 1,
    getAuthorizationHeader: () => ({ Authorization: 'Bearer old-token' })
  })
  configureStudentUploadWidget(widgetRef, {
    ...baseContext,
    bindingId: 2,
    getAuthorizationHeader: () => ({ Authorization: 'Bearer latest-token' })
  })
  widgetRef.handleOnBeforeUpload({ uid: 1001, name: 'logo.png' })

  assert.equal(widgetRef.uploadHeaders.Authorization, 'Bearer latest-token')
  assert.equal(widgetRef.uploadData.bindingId, '2')
  assert.equal(calls.before.length, 1)
})

test('上传响应解析只接受业务成功且文件信息完整的结果', () => {
  assert.deepEqual(parseGuideSheetUploadResponse({ code: 500, msg: '存储失败' }), {
    ok: false,
    message: '存储失败'
  })
  assert.equal(parseGuideSheetUploadResponse({ code: 200, data: {} }).ok, false)
  assert.deepEqual(parseGuideSheetUploadResponse({
    code: 200,
    data: { fileName: 'logo.png', accessUrl: '/uploads/1/content' }
  }, '/prod-api'), {
    ok: true,
    file: { name: 'logo.png', url: '/prod-api/uploads/1/content' }
  })
})
