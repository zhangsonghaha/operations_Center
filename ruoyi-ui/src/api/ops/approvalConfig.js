import request from '@/utils/request'

// 查询审批流程配置列表
export function listApprovalConfig(query) {
  return request({
    url: '/ops/approvalConfig/list',
    method: 'get',
    params: query
  })
}

// 查询审批流程配置详细
export function getApprovalConfig(id) {
  return request({
    url: '/ops/approvalConfig/' + id,
    method: 'get'
  })
}

// 新增审批流程配置
export function addApprovalConfig(data) {
  return request({
    url: '/ops/approvalConfig',
    method: 'post',
    data: data
  })
}

// 修改审批流程配置
export function updateApprovalConfig(data) {
  return request({
    url: '/ops/approvalConfig',
    method: 'put',
    data: data
  })
}

// 删除审批流程配置
export function delApprovalConfig(id) {
  return request({
    url: '/ops/approvalConfig/' + id,
    method: 'delete'
  })
}

export function deployBpmnXml(data) {
  return request({
    url: '/ops/workflow/deployXml',
    method: 'post',
    data: data
  })
}
