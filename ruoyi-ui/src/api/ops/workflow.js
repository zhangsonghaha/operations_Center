import request from '@/utils/request'

// 部署流程定义
export function deployBpmnXml(data) {
  return request({
    url: '/ops/workflow/deployXml',
    method: 'post',
    data: data
  })
}

// 获取流程定义XML (By Deployment ID)
export function getProcessDefinitionXml(deploymentId) {
  return request({
    url: '/ops/workflow/definition/' + deploymentId + '/xml',
    method: 'get'
  })
}

// 获取最新流程定义XML (By Key)
export function getProcessDefinitionXmlByKey(processKey) {
  return request({
    url: '/ops/workflow/definition/key/' + processKey + '/xml',
    method: 'get'
  })
}

// 获取我发起的流程列表
export function listMyStartedProcesses(query) {
  return request({
    url: '/ops/workflow/my-started',
    method: 'get',
    params: query
  })
}

// 获取流程进度（XML + 高亮节点）
export function getProcessProgress(processInstanceId) {
  return request({
    url: '/ops/workflow/instance/' + processInstanceId + '/progress',
    method: 'get'
  })
}

// 提醒审批人
export function remindProcessInstance(processInstanceId) {
  return request({
    url: '/ops/workflow/instance/' + processInstanceId + '/remind',
    method: 'post'
  })
}

// 查询流程定义列表
export function listProcessDefinitions(query) {
  return request({
    url: '/ops/workflow/definition/list',
    method: 'get',
    params: query
  })
}

// 挂起/激活流程定义
export function updateProcessDefinitionState(processDefinitionId, action) {
  return request({
    url: '/ops/workflow/definition/' + processDefinitionId + '/state',
    method: 'post',
    params: { action }
  })
}

// 删除流程部署
export function deleteDeployment(deploymentId) {
  return request({
    url: '/ops/workflow/deployment/' + deploymentId,
    method: 'delete'
  })
}

// 监控流程实例列表 (管理员)
export function listMonitorProcessInstances(query) {
  return request({
    url: '/ops/workflow/monitor/list',
    method: 'get',
    params: query
  })
}

// 删除流程实例 (Batch)
export function deleteProcessInstance(processInstanceIds) {
  return request({
    url: '/ops/workflow/instance/' + processInstanceIds,
    method: 'delete'
  })
}

// 强制终止流程实例 (Batch)
export function cancelProcessInstance(processInstanceIds) {
  return request({
    url: '/ops/workflow/instance/cancel',
    method: 'post',
    data: processInstanceIds
  })
}
