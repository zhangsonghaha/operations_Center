import request from '@/utils/request'

// --- 连接管理 ---

// 查询数据库连接列表
export function listConn(query) {
  return request({
    url: '/system/db/conn/list',
    method: 'get',
    params: query
  })
}

// 查询数据库连接详细
export function getConn(connId) {
  return request({
    url: '/system/db/conn/' + connId,
    method: 'get'
  })
}

// 新增数据库连接
export function addConn(data) {
  return request({
    url: '/system/db/conn',
    method: 'post',
    data: data
  })
}

// 修改数据库连接
export function updateConn(data) {
  return request({
    url: '/system/db/conn',
    method: 'put',
    data: data
  })
}

// 删除数据库连接
export function delConn(connId) {
  return request({
    url: '/system/db/conn/' + connId,
    method: 'delete'
  })
}

// 测试数据库连接
export function testConn(data) {
  return request({
    url: '/system/db/conn/test',
    method: 'post',
    data: data
  })
}

// --- 数据操作 ---

// 获取表列表
export function getTables(connId) {
  return request({
    url: '/system/db/execute/tables',
    method: 'get',
    params: { connId }
  })
}

// 获取数据库元数据
export function getMetadata(connId) {
  return request({
    url: '/system/db/execute/metadata',
    method: 'get',
    params: { connId }
  })
}

// 获取数据库结构（数据库名称、表、视图、存储过程、函数等）
export function getSchema(connId) {
  return request({
    url: '/system/db/execute/schema',
    method: 'get',
    params: { connId }
  })
}

// 获取表结构详情
export function getTableStructure(connId, tableName) {
  return request({
    url: '/system/db/execute/table/structure',
    method: 'get',
    params: { connId, tableName }
  })
}

// 获取存储过程定义
export function getProcedureDefinition(connId, procName) {
  return request({
    url: '/system/db/execute/procedure/definition',
    method: 'get',
    params: { connId, procName }
  })
}

// 获取函数定义
export function getFunctionDefinition(connId, funcName) {
  return request({
    url: '/system/db/execute/function/definition',
    method: 'get',
    params: { connId, funcName }
  })
}

// 更新存储过程
export function updateProcedure(data) {
  return request({
    url: '/system/db/execute/procedure/update',
    method: 'post',
    data: data
  })
}

// 更新函数
export function updateFunction(data) {
  return request({
    url: '/system/db/execute/function/update',
    method: 'post',
    data: data
  })
}

// 执行查询SQL
export function executeSelect(data) {
  return request({
    url: '/system/db/execute/select',
    method: 'post',
    data: data
  })
}

// 执行增删改SQL
export function executeUpdate(data) {
  return request({
    url: '/system/db/execute/update',
    method: 'post',
    data: data
  })
}

// --- 数据监控 ---

// 获取实时连接列表
export function getProcessList(connId) {
  return request({
    url: '/system/db/monitor/process',
    method: 'get',
    params: { connId }
  })
}

// 获取表空间统计
export function getTableStats(connId) {
  return request({
    url: '/system/db/monitor/stats',
    method: 'get',
    params: { connId }
  })
}

// 获取慢查询分析
export function getSlowQueries(connId, limit) {
  return request({
    url: '/system/db/monitor/slow-queries',
    method: 'get',
    params: { connId, limit }
  })
}

// 获取SQL执行统计
export function getSqlStats(connId, limit) {
  return request({
    url: '/system/db/monitor/sql-stats',
    method: 'get',
    params: { connId, limit }
  })
}

// 获取连接数统计
export function getConnectionStats(connId) {
  return request({
    url: '/system/db/monitor/connection-stats',
    method: 'get',
    params: { connId }
  })
}

// 终止数据库连接
export function killProcess(connId, processId) {
  return request({
    url: '/system/db/monitor/kill',
    method: 'post',
    params: { connId, processId }
  })
}

// 查询监控规则列表
export function listMonitorRule(query) {
  return request({
    url: '/system/db/monitor/rules/list',
    method: 'get',
    params: query
  })
}

// 查询监控规则详细
export function getMonitorRule(ruleId) {
  return request({
    url: '/system/db/monitor/rules/' + ruleId,
    method: 'get'
  })
}

// 新增监控规则
export function addMonitorRule(data) {
  return request({
    url: '/system/db/monitor/rules',
    method: 'post',
    data: data
  })
}

// 修改监控规则
export function updateMonitorRule(data) {
  return request({
    url: '/system/db/monitor/rules',
    method: 'put',
    data: data
  })
}

// 删除监控规则
export function delMonitorRule(ruleId) {
  return request({
    url: '/system/db/monitor/rules/' + ruleId,
    method: 'delete'
  })
}

// --- 备份恢复 ---

// 查询备份列表
export function listBackup(query) {
  return request({
    url: '/system/db/backup/list',
    method: 'get',
    params: query
  })
}

// 执行备份
export function backup(connId) {
  return request({
    url: '/system/db/backup/backup',
    method: 'post',
    data: { connId }
  })
}

// 删除备份
export function delBackup(backupId) {
  return request({
    url: '/system/db/backup/' + backupId,
    method: 'delete'
  })
}

// --- 操作日志 ---

// 查询日志列表
export function listLog(query) {
  return request({
    url: '/system/db/log/list',
    method: 'get',
    params: query
  })
}
