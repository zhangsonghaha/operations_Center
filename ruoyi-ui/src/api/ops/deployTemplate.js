import request from '@/utils/request'

// 查询部署模板列表
export function listDeployTemplate(query) {
  return request({
    url: '/ops/deployTemplate/list',
    method: 'get',
    params: query
  })
}

// 查询部署模板详细
export function getDeployTemplate(id) {
  return request({
    url: '/ops/deployTemplate/' + id,
    method: 'get'
  })
}

// 新增部署模板
export function addDeployTemplate(data) {
  return request({
    url: '/ops/deployTemplate',
    method: 'post',
    data: data
  })
}

// 修改部署模板
export function updateDeployTemplate(data) {
  return request({
    url: '/ops/deployTemplate',
    method: 'put',
    data: data
  })
}

// 删除部署模板
export function delDeployTemplate(id) {
  return request({
    url: '/ops/deployTemplate/' + id,
    method: 'delete'
  })
}

// 查询版本历史
export function listDeployTemplateVersions(id) {
  return request({
    url: `/ops/deployTemplate/${id}/versions`,
    method: 'get'
  })
}

// 回滚版本
export function rollbackDeployTemplate(id, versionId) {
  return request({
    url: `/ops/deployTemplate/${id}/rollback`,
    method: 'post',
    params: { versionId }
  })
}

// 执行部署
export function deployTemplate(id, appId, params) {
  return request({
    url: `/ops/deployTemplate/${id}/deploy`,
    method: 'post',
    data: { appId, params }
  })
}
