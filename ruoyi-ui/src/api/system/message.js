import request from '@/utils/request'

// 查询站内信列表
export function listMessage(query) {
  return request({
    url: '/system/message/list',
    method: 'get',
    params: query
  })
}

// 查询未读数量
export function getUnreadCount() {
  return request({
    url: '/system/message/unreadCount',
    method: 'get'
  })
}

// 查询站内信详细
export function getMessage(messageId) {
  return request({
    url: '/system/message/' + messageId,
    method: 'get'
  })
}

// 新增站内信
export function addMessage(data) {
  return request({
    url: '/system/message',
    method: 'post',
    data: data
  })
}

// 修改站内信
export function updateMessage(data) {
  return request({
    url: '/system/message',
    method: 'put',
    data: data
  })
}

// 删除站内信
export function delMessage(messageId) {
  return request({
    url: '/system/message/' + messageId,
    method: 'delete'
  })
}

// 导出站内信
export function exportMessage(query) {
  return request({
    url: '/system/message/export',
    method: 'post',
    params: query
  })
}
