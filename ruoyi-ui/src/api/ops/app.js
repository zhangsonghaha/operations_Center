import request from '@/utils/request'

// 查询应用注册列表
export function listApp(query) {
  return request({
    url: '/system/app/list',
    method: 'get',
    params: query
  })
}

// 查询应用注册详细
export function getApp(appId) {
  return request({
    url: '/system/app/' + appId,
    method: 'get'
  })
}

// 新增应用注册
export function addApp(data) {
  return request({
    url: '/system/app',
    method: 'post',
    data: data
  })
}

// 修改应用注册
export function updateApp(data) {
  return request({
    url: '/system/app',
    method: 'put',
    data: data
  })
}

// 删除应用注册
export function delApp(appId) {
  return request({
    url: '/system/app/' + appId,
    method: 'delete'
  })
}

// 上传应用包
export function uploadAppPackage(data) {
  return request({
    url: '/system/app/upload',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
