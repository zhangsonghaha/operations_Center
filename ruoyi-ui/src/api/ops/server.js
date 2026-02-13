import request from '@/utils/request'

// 查询服务器资产列表
export function listServer(query) {
  return request({
    url: '/system/server/list',
    method: 'get',
    params: query
  })
}

// 查询服务器资产详细
export function getServer(serverId) {
  return request({
    url: '/system/server/' + serverId,
    method: 'get'
  })
}

// 新增服务器资产
export function addServer(data) {
  return request({
    url: '/system/server',
    method: 'post',
    data: data
  })
}

// 修改服务器资产
export function updateServer(data) {
  return request({
    url: '/system/server',
    method: 'put',
    data: data
  })
}

// 删除服务器资产
export function delServer(serverId) {
  return request({
    url: '/system/server/' + serverId,
    method: 'delete'
  })
}

// 检测服务器连接
export function checkConnection(serverId) {
  return request({
    url: '/system/server/check/' + serverId,
    method: 'get'
  })
}
