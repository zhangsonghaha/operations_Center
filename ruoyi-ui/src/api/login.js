import request from '@/utils/request'

// 登录方法
export function login(username, password, code, uuid) {
  const data = {
    username,
    password,
    code,
    uuid
  }
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: data
  })
}

// 注册方法
export function register(data) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}

// 获取登录二维码
export function getQrCode() {
  return request({
    url: '/login/qr/code',
    headers: {
      isToken: false
    },
    method: 'get'
  })
}

// 检查二维码状态
export function checkQrCode(uuid) {
  return request({
    url: '/login/qr/check',
    headers: {
      isToken: false
    },
    method: 'get',
    params: { uuid }
  })
}

// 模拟扫描二维码 (仅测试用)
export function scanQrCode(uuid) {
  return request({
    url: '/login/qr/scan',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { uuid }
  })
}

// 模拟确认登录 (仅测试用)
export function confirmQrCode(uuid, username = '') {
  return request({
    url: '/login/qr/confirm',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { uuid, username }
  })
}

// 绑定并登录
export function bindQrUser(uuid, username, password) {
  return request({
    url: '/login/qr/bind',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { uuid, username, password }
  })
}

// 微信扫码登录 (提交Code)
export function wechatLogin(uuid, code) {
  return request({
    url: '/login/qr/wechat',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { uuid, code }
  })
}