import request from '@/utils/request'

// 获取实时监控数据
export function getRealtimeData(serverId) {
  return request({
    url: '/ops/monitor/realtime/' + serverId,
    method: 'get',
    timeout: 20000 // SSH连接和命令执行可能较慢，延长超时时间至20秒
  })
}

// 获取历史趋势数据
export function getTrendData(serverId, range) {
  return request({
    url: '/ops/monitor/trend/' + serverId,
    method: 'get',
    params: { range }
  })
}
