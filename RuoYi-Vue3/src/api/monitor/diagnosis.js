import request from '@/utils/request'

// 查询系统诊断中心汇总
export function getDiagnosisSummary(params) {
  return request({
    url: '/monitor/diagnosis/summary',
    method: 'get',
    params
  })
}

// 查询持久化性能事件时间线
export function getDiagnosisEvents(params) {
  return request({
    url: '/monitor/diagnosis/events',
    method: 'get',
    params
  })
}

// 手动清理并重启 LibreOffice 服务池
export function cleanupLibreOffice() {
  return request({
    url: '/monitor/diagnosis/libre-office/cleanup',
    method: 'post'
  })
}

// 主动验证慢 SQL 拦截、异步写入和落库链路
export function perfEventSelfTest() {
  return request({
    url: '/monitor/diagnosis/perf-event/self-test',
    method: 'post'
  })
}
