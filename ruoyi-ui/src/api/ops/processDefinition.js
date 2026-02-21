import request from '@/utils/request'

// 查询流程定义列表
export function listProcessDefinition(query) {
  return request({
    url: '/ops/processDefinition/list',
    method: 'get',
    params: query
  })
}

// 挂起流程定义
export function suspendProcessDefinition(processDefinitionId) {
  return request({
    url: '/ops/processDefinition/suspend/' + processDefinitionId,
    method: 'put'
  })
}

// 激活流程定义
export function activateProcessDefinition(processDefinitionId) {
  return request({
    url: '/ops/processDefinition/activate/' + processDefinitionId,
    method: 'put'
  })
}

// 删除部署
export function delDeployment(deploymentId) {
  return request({
    url: '/ops/processDefinition/deployment/' + deploymentId,
    method: 'delete'
  })
}
