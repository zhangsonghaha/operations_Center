import request from '@/utils/request'

// 查询部署日志列表
export function listDeployLog(query) {
  return request({
    url: '/system/deployLog/list',
    method: 'get',
    params: query
  })
}

// 查询部署日志详细
export function getDeployLog(logId) {
  return request({
    url: '/system/deployLog/' + logId,
    method: 'get'
  })
}

// 删除部署日志
export function delDeployLog(logId) {
  return request({
    url: '/system/deployLog/' + logId,
    method: 'delete'
  })
}

// 开始部署
export function startDeploy(data) {
  return request({
    url: '/system/deployLog/start',
    method: 'post',
    params: data
  })
}
