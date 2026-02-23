import request from '@/utils/request'

// 查询环境配置列表
export function listEnvironment(query) {
  return request({
    url: '/ops/environment/list',
    method: 'get',
    params: query
  })
}

// 查询环境配置列表(不分页)
export function listAllEnvironment(query) {
  return request({
    url: '/ops/environment/listAll',
    method: 'get',
    params: query
  })
}

// 查询环境配置详细
export function getEnvironment(id) {
  return request({
    url: '/ops/environment/' + id,
    method: 'get'
  })
}

// 新增环境配置
export function addEnvironment(data) {
  return request({
    url: '/ops/environment',
    method: 'post',
    data: data
  })
}

// 修改环境配置
export function updateEnvironment(data) {
  return request({
    url: '/ops/environment',
    method: 'put',
    data: data
  })
}

// 删除环境配置
export function delEnvironment(id) {
  return request({
    url: '/ops/environment/' + id,
    method: 'delete'
  })
}
