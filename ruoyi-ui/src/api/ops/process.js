import request from '@/utils/request'

// 查询进程列表
export function listProcess(query) {
  return request({
    url: '/ops/process/list',
    method: 'get',
    params: query,
    timeout: 15000 // 稍微延长超时时间
  })
}

// 终止进程
export function killProcess(serverId, pids) {
  return request({
    url: '/ops/process/kill/' + serverId + '/' + pids,
    method: 'delete'
  })
}
