import request from '@/utils/request'

// 获取文件列表
export function listFiles(serverId, path) {
  return request({
    url: '/system/server/file/list',
    method: 'get',
    params: { serverId, path }
  })
}

// 上传文件
export function uploadFile(data) {
  return request({
    url: '/system/server/file/upload',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除文件
export function deleteFile(serverId, path, isDir) {
  return request({
    url: '/system/server/file/remove',
    method: 'delete',
    params: { serverId, path, isDir }
  })
}

// 压缩文件
export function compressFile(serverId, path, type) {
  return request({
    url: '/system/server/file/compress',
    method: 'post',
    params: { serverId, path, type }
  })
}

// 解压文件
export function extractFile(serverId, path) {
  return request({
    url: '/system/server/file/extract',
    method: 'post',
    params: { serverId, path }
  })
}
