import request from '@/utils/request'

// 查询部署模板文档列表
export function listDoc(query) {
  return request({
    url: '/ops/deployDoc/list',
    method: 'get',
    params: query
  })
}

// 查询部署模板文档详细
export function getDoc(docId) {
  return request({
    url: '/ops/deployDoc/' + docId,
    method: 'get'
  })
}

// 新增部署模板文档
export function addDoc(data) {
  return request({
    url: '/ops/deployDoc',
    method: 'post',
    data: data
  })
}

// 修改部署模板文档
export function updateDoc(data) {
  return request({
    url: '/ops/deployDoc',
    method: 'put',
    data: data
  })
}

// 删除部署模板文档
export function delDoc(docId) {
  return request({
    url: '/ops/deployDoc/' + docId,
    method: 'delete'
  })
}

// 上传文档
export function uploadDoc(data) {
  return request({
    url: '/ops/deployDoc/upload',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
