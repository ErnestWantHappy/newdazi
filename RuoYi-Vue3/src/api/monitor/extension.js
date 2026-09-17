import request from '@/utils/request'

// 扩展机（129）健康看板聚合接口：Judge0 / CryptPad / EMQX / MQTT 接收器
export function getExtensionHealth() {
  return request({
    url: '/monitor/extension/health',
    method: 'get'
  })
}
