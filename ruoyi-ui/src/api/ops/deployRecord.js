import request from '@/utils/request'

export function getDeployRecord(id) {
  return request({
    url: '/ops/deployRecord/' + id,
    method: 'get'
  })
}

export function listDeployRecord(query) {
  return request({
    url: '/ops/deployRecord/list',
    method: 'get',
    params: query
  })
}
