import request from '@/utils/request'

// 查询Docker容器列表
export function listContainer(query) {
  return request({
    url: '/ops/docker/container/list',
    method: 'get',
    params: query
  })
}

// 查询Docker容器详细
export function getContainer(containerId) {
  return request({
    url: '/ops/docker/container/' + containerId,
    method: 'get'
  })
}

// 新增Docker容器（部署）
export function deployContainer(data) {
  return request({
    url: '/ops/docker/container/deploy',
    method: 'post',
    data: data
  })
}

// 启动Docker容器
export function startContainer(containerId) {
  return request({
    url: '/ops/docker/container/start/' + containerId,
    method: 'put'
  })
}

// 停止Docker容器
export function stopContainer(containerId) {
  return request({
    url: '/ops/docker/container/stop/' + containerId,
    method: 'put'
  })
}

// 重启Docker容器
export function restartContainer(containerId) {
  return request({
    url: '/ops/docker/container/restart/' + containerId,
    method: 'put'
  })
}

// 删除Docker容器
export function delContainer(containerId) {
  return request({
    url: '/ops/docker/container/' + containerId,
    method: 'delete'
  })
}

// 查询Docker模板列表
export function listTemplate(query) {
  return request({
    url: '/ops/docker/template/list',
    method: 'get',
    params: query
  })
}

// 查询Docker模板详细
export function getTemplate(templateId) {
  return request({
    url: '/ops/docker/template/' + templateId,
    method: 'get'
  })
}

// 检测Docker环境
export function checkDockerEnv(serverId) {
  return request({
    url: '/ops/docker/env/check/' + serverId,
    method: 'get'
  })
}

// 查询容器日志
export function getContainerLogs(containerId, tail) {
  return request({
    url: '/ops/docker/container/logs/' + containerId,
    method: 'get',
    params: { tail }
  })
}

// 查询容器统计信息
export function getContainerStats(containerId) {
  return request({
    url: '/ops/docker/container/stats/' + containerId,
    method: 'get'
  })
}

// 新增Docker模板
export function addTemplate(data) {
  return request({
    url: '/ops/docker/template',
    method: 'post',
    data: data
  })
}

// 修改Docker模板
export function updateTemplate(data) {
  return request({
    url: '/ops/docker/template',
    method: 'put',
    data: data
  })
}

// 删除Docker模板
export function delTemplate(templateId) {
  return request({
    url: '/ops/docker/template/' + templateId,
    method: 'delete'
  })
}
