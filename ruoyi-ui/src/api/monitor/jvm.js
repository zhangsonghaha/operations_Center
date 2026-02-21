import request from '@/utils/request'

// 查询监控目标列表
export function listTarget(query) {
  return request({
    url: '/monitor/jvm/target/list',
    method: 'get',
    params: query
  })
}

// 查询监控目标详细
export function getTarget(targetId) {
  return request({
    url: '/monitor/jvm/target/' + targetId,
    method: 'get'
  })
}

// 新增监控目标
export function addTarget(data) {
  return request({
    url: '/monitor/jvm/target',
    method: 'post',
    data: data
  })
}

// 修改监控目标
export function updateTarget(data) {
  return request({
    url: '/monitor/jvm/target',
    method: 'put',
    data: data
  })
}

// 删除监控目标
export function delTarget(targetId) {
  return request({
    url: '/monitor/jvm/target/' + targetId,
    method: 'delete'
  })
}

// 获取实时监控数据
export function getRealtimeInfo(targetId) {
  return request({
    url: '/monitor/jvm/realtime/' + targetId,
    method: 'get'
  })
}

// 触发 GC
export function triggerGc(targetId) {
  return request({
    url: '/monitor/jvm/gc/' + targetId,
    method: 'post'
  })
}
