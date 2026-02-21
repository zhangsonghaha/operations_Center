import request from '@/utils/request'

// 查询命令模板列表
export function listTemplate(query) {
  return request({
    url: '/ops/batch/template/list',
    method: 'get',
    params: query
  })
}

// 新增命令模板
export function addTemplate(data) {
  return request({
    url: '/ops/batch/template',
    method: 'post',
    data: data
  })
}

// 修改命令模板
export function updateTemplate(data) {
  return request({
    url: '/ops/batch/template',
    method: 'put',
    data: data
  })
}

// 删除命令模板
export function delTemplate(templateId) {
  return request({
    url: '/ops/batch/template/' + templateId,
    method: 'delete'
  })
}

// 查询任务列表
export function listTask(query) {
  return request({
    url: '/ops/batch/task/list',
    method: 'get',
    params: query
  })
}

// 查询任务详细
export function getTask(taskId) {
  return request({
    url: '/ops/batch/task/' + taskId,
    method: 'get'
  })
}

// 查询任务明细列表
export function listTaskDetail(query) {
  return request({
    url: '/ops/batch/task/detail/list',
    method: 'get',
    params: query
  })
}

// 执行批量任务
export function executeTask(data) {
  return request({
    url: '/ops/batch/execute',
    method: 'post',
    data: data
  })
}
