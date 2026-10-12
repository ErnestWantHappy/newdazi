import axios from 'axios'

// 概览请求不携带令牌，也不触发全局登录过期弹窗；失败由展厅各自展示重试入口。
const previewRequest = axios.create({ baseURL: import.meta.env.VITE_APP_BASE_API, timeout: 15000 })

async function loadPreview(path, signal) {
  const { data } = await previewRequest.get(`/business/login-preview/${path}`, { signal })
  if (Number(data?.code) !== 200 || !Array.isArray(data.data)) throw new Error('概览加载失败')
  return data.data
}

export const getLoginActivityPreviews = signal => loadPreview('activities', signal)
export const getLoginSchoolPreviews = signal => loadPreview('lessons', signal)
export const getLoginFocusSlides = signal => loadPreview('focus-slides', signal)

