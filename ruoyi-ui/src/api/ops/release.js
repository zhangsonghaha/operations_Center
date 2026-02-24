import request from '@/utils/request'

// 查询发布申请列表
export function listRelease(query) {
  return request({
    url: '/ops/release/list',
    method: 'get',
    params: query
  })
}

// 查询待审批发布申请列表
export function listReleasePending(query) {
  return request({
    url: '/ops/release/pending',
    method: 'get',
    params: query
  })
}

// 查询发布申请详细
export function getRelease(id) {
  return request({
    url: '/ops/release/' + id,
    method: 'get'
  })
}

// 新增发布申请
export function addRelease(data) {
  return request({
    url: '/ops/release',
    method: 'post',
    data: data
  })
}

// 修改发布申请
export function updateRelease(data) {
  return request({
    url: '/ops/release',
    method: 'put',
    data: data
  })
}

// 删除发布申请
export function delRelease(id) {
  return request({
    url: '/ops/release/' + id,
    method: 'delete'
  })
}

// 审批发布申请
export function auditRelease(data) {
  return request({
    url: '/ops/release/audit',
    method: 'put',
    data: data
  })
}

// 执行发布
export function executeRelease(id) {
  return request({
    url: '/ops/release/execute/' + id,
    method: 'post'
  })
}

// 撤销发布申请
export function cancelRelease(id) {
  return request({
    url: '/ops/release/cancel/' + id,
    method: 'post'
  })
}
