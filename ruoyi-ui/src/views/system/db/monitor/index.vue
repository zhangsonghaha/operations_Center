<template>
  <div class="app-container db-monitor">
    <!-- 顶部连接选择器 -->
    <div class="monitor-header">
      <div class="conn-selector">
        <span class="label">数据库连接</span>
        <el-select v-model="connId" placeholder="请选择连接" size="default" @change="handleConnChange">
          <el-option
            v-for="item in connList"
            :key="item.connId"
            :label="item.connName"
            :value="item.connId"
          />
        </el-select>
        <el-button type="primary" :icon="Refresh" size="default" :loading="refreshing" @click="handleRefresh">
          刷新
        </el-button>
      </div>
      <div class="auto-refresh">
        <el-switch v-model="autoRefresh" active-text="自动刷新(5s)" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card primary">
        <div class="stat-icon">
          <Link />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ connectionStats.totalConnections || 0 }}</div>
          <div class="stat-label">总连接数</div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon">
          <TrendCharts />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ connectionStats.activeConnections || 0 }}</div>
          <div class="stat-label">活跃连接</div>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon">
          <Timer />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ connectionStats.slowQueries || 0 }}</div>
          <div class="stat-label">慢查询</div>
        </div>
      </div>
      <div class="stat-card danger">
        <div class="stat-icon">
          <Coin />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ formatBytes(totalTableSpace) }}</div>
          <div class="stat-label">总表空间</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-container">
        <div class="chart-header">
          <span class="chart-title">连接数趋势</span>
        </div>
        <div ref="connectionChart" class="chart-body"></div>
      </div>
      <div class="chart-container">
        <div class="chart-header">
          <span class="chart-title">表空间TOP10</span>
        </div>
        <div ref="tableSpaceChart" class="chart-body"></div>
      </div>
    </div>

    <!-- 标签页内容 -->
    <div class="tabs-container">
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <el-tab-pane label="实时连接" name="process">
          <div class="tab-content">
            <el-table v-loading="loading" :data="processList" border stripe size="small">
              <el-table-column prop="Id" label="ID" width="80" />
              <el-table-column prop="User" label="用户" width="100" />
              <el-table-column prop="Host" label="主机" width="140" />
              <el-table-column prop="db" label="数据库" width="120" />
              <el-table-column prop="Command" label="命令" width="100">
                <template #default="scope">
                  <el-tag :type="getCommandType(scope.row.Command)" size="small">
                    {{ scope.row.Command }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="Time" label="时长(s)" width="100" sortable>
                <template #default="scope">
                  <span :class="getTimeClass(scope.row.Time)">{{ scope.row.Time }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="State" label="状态" width="120" />
              <el-table-column prop="Info" label="SQL语句" min-width="200" show-overflow-tooltip />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="scope">
                  <el-button
                    v-hasRole="['admin']"
                    type="danger"
                    link
                    size="small"
                    @click="handleKill(scope.row)"
                  >
                    终止
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="慢查询分析" name="slow">
          <div class="tab-content">
            <div class="toolbar">
              <el-radio-group v-model="slowQueryLimit" size="small" @change="loadSlowQueries">
                <el-radio-button :label="10">TOP 10</el-radio-button>
                <el-radio-button :label="20">TOP 20</el-radio-button>
                <el-radio-button :label="50">TOP 50</el-radio-button>
              </el-radio-group>
            </div>
            <el-table v-loading="loading" :data="slowQueries" border stripe size="small">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="query" label="SQL语句" min-width="300">
                <template #default="scope">
                  <el-link type="primary" :underline="false" @click="showSqlDetail(scope.row.query)">
                    <span class="sql-preview">{{ scope.row.query }}</span>
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="db" label="数据库" width="100" />
              <el-table-column prop="exec_count" label="执行次数" width="90" sortable />
              <el-table-column prop="total_latency" label="总耗时(s)" width="100" sortable />
              <el-table-column prop="avg_latency" label="平均耗时(s)" width="110" sortable>
                <template #default="scope">
                  <span :class="getLatencyClass(scope.row.avg_latency)">{{ scope.row.avg_latency }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="max_latency" label="最大耗时(s)" width="110" sortable />
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="表空间统计" name="stats">
          <div class="tab-content">
            <el-table v-loading="loading" :data="tableStats" border stripe size="small" :default-sort="{prop: 'data_length', order: 'descending'}">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column label="表名" sortable min-width="150">
                <template #default="scope">
                  {{ getFieldValue(scope.row, ['table_name', 'TABLE_NAME', 'tableName']) }}
                </template>
              </el-table-column>
              <el-table-column label="注释" min-width="150" show-overflow-tooltip>
                <template #default="scope">
                  {{ getFieldValue(scope.row, ['table_comment', 'TABLE_COMMENT', 'tableComment']) }}
                </template>
              </el-table-column>
              <el-table-column label="引擎" width="90">
                <template #default="scope">
                  {{ getFieldValue(scope.row, ['engine', 'ENGINE']) }}
                </template>
              </el-table-column>
              <el-table-column label="行数" width="100" sortable>
                <template #default="scope">
                  {{ formatNumber(getFieldValue(scope.row, ['table_rows', 'TABLE_ROWS', 'tableRows'])) }}
                </template>
              </el-table-column>
              <el-table-column label="数据大小" width="110" sortable>
                <template #default="scope">
                  {{ formatBytes(getFieldValue(scope.row, ['data_length', 'DATA_LENGTH', 'dataLength'])) }}
                </template>
              </el-table-column>
              <el-table-column label="索引大小" width="110" sortable>
                <template #default="scope">
                  {{ formatBytes(getFieldValue(scope.row, ['index_length', 'INDEX_LENGTH', 'indexLength'])) }}
                </template>
              </el-table-column>
              <el-table-column label="总大小" width="110" sortable>
                <template #default="scope">
                  <span class="size-highlight">{{ formatBytes(getFieldValue(scope.row, ['total_length', 'TOTAL_LENGTH', 'totalLength'])) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="150">
                <template #default="scope">
                  {{ getFieldValue(scope.row, ['create_time', 'CREATE_TIME', 'createTime']) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="SQL统计" name="sql">
          <div class="tab-content">
            <div class="toolbar">
              <el-radio-group v-model="sqlStatsLimit" size="small" @change="loadSqlStats">
                <el-radio-button :label="10">TOP 10</el-radio-button>
                <el-radio-button :label="20">TOP 20</el-radio-button>
                <el-radio-button :label="50">TOP 50</el-radio-button>
              </el-radio-group>
            </div>
            <el-table v-loading="loading" :data="sqlStats" border stripe size="small">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="query" label="SQL语句" min-width="300">
                <template #default="scope">
                  <el-link type="primary" :underline="false" @click="showSqlDetail(scope.row.query)">
                    <span class="sql-preview">{{ scope.row.query }}</span>
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="db" label="数据库" width="100" />
              <el-table-column prop="exec_count" label="执行次数" width="90" sortable />
              <el-table-column prop="errors" label="错误数" width="80" sortable>
                <template #default="scope">
                  <span v-if="scope.row.errors > 0" class="error-text">{{ scope.row.errors }}</span>
                  <span v-else>0</span>
                </template>
              </el-table-column>
              <el-table-column prop="total_latency" label="总耗时(s)" width="100" sortable />
              <el-table-column prop="avg_latency" label="平均耗时(s)" width="110" sortable />
              <el-table-column prop="rows_sent" label="返回行数" width="90" sortable />
              <el-table-column prop="rows_examined" label="扫描行数" width="90" sortable />
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="监控规则" name="rules" v-hasRole="['admin']">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" :icon="Plus" size="small" @click="handleAddRule">新增规则</el-button>
            </div>
            <el-table v-loading="loading" :data="ruleList" border stripe size="small">
              <el-table-column prop="ruleId" label="ID" width="60" />
              <el-table-column prop="ruleType" label="规则类型" width="120">
                <template #default="scope">
                  <el-tag :type="getRuleTypeTag(scope.row.ruleType)">
                    {{ getRuleTypeLabel(scope.row.ruleType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="metricName" label="指标名称" width="120" />
              <el-table-column prop="condition" label="条件" width="80">
                <template #default="scope">
                  {{ getConditionLabel(scope.row.condition) }}
                </template>
              </el-table-column>
              <el-table-column prop="threshold" label="阈值" width="100" />
              <el-table-column prop="enabled" label="状态" width="80">
                <template #default="scope">
                  <el-switch v-model="scope.row.enabled" active-value="0" inactive-value="1" @change="handleRuleStatusChange(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
              <el-table-column prop="createTime" label="创建时间" width="150" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button type="primary" link size="small" @click="handleEditRule(scope.row)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="handleDeleteRule(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 规则编辑对话框 -->
    <el-dialog :title="ruleDialogTitle" v-model="ruleDialogVisible" width="600px" append-to-body>
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-width="100px">
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" style="width: 100%" @change="handleRuleTypeChange">
            <el-option label="慢查询" value="slow_query" />
            <el-option label="连接数" value="connection" />
            <el-option label="表空间" value="table_space" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标名称" prop="metricName">
          <el-select v-model="ruleForm.metricName" placeholder="请先选择规则类型，再选择指标" style="width: 100%" :disabled="!ruleForm.ruleType">
            <el-option
              v-for="item in metricOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="条件" prop="condition">
          <el-select v-model="ruleForm.condition" placeholder="请选择条件" style="width: 100%">
            <el-option label="大于" value="GT" />
            <el-option label="小于" value="LT" />
            <el-option label="等于" value="EQ" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input-number v-model="ruleForm.threshold" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-radio-group v-model="ruleForm.enabled">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="ruleForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitRuleForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- SQL详情对话框 -->
    <el-dialog title="SQL详情" v-model="sqlDialogVisible" width="800px" append-to-body>
      <div class="sql-detail-container">
        <pre class="sql-code">{{ currentSql }}</pre>
      </div>
      <template #footer>
        <el-button @click="sqlDialogVisible = false">关 闭</el-button>
        <el-button type="primary" @click="copySql">复制SQL</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DbMonitor">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Link, TrendCharts, Timer, Coin } from '@element-plus/icons-vue'
import {
  listConn,
  getProcessList,
  getTableStats,
  getSlowQueries,
  getSqlStats,
  getConnectionStats,
  killProcess,
  listMonitorRule,
  addMonitorRule,
  updateMonitorRule,
  delMonitorRule
} from '@/api/system/db'

// 连接选择
const connId = ref(null)
const connList = ref([])
const refreshing = ref(false)
const autoRefresh = ref(false)
let refreshTimer = null

// 标签页
const activeTab = ref('process')
const loading = ref(false)

// 统计数据
const connectionStats = ref({})
const processList = ref([])
const tableStats = ref([])
const slowQueries = ref([])
const sqlStats = ref([])
const slowQueryLimit = ref(20)
const sqlStatsLimit = ref(20)
const ruleList = ref([])

// 图表
const connectionChart = ref(null)
const tableSpaceChart = ref(null)
let connectionChartInstance = null
let tableSpaceChartInstance = null

// 连接历史数据（用于趋势图）
const connectionHistory = ref([])

// 规则对话框
const ruleDialogVisible = ref(false)
const ruleDialogTitle = ref('新增规则')
const ruleFormRef = ref(null)
const ruleForm = ref({
  ruleId: null,
  ruleType: '',
  metricName: '',
  condition: 'GT',
  threshold: 0,
  enabled: '0',
  remark: ''
})
const ruleRules = {
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  metricName: [{ required: true, message: '请选择指标名称', trigger: 'change' }],
  condition: [{ required: true, message: '请选择条件', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }]
}

// SQL详情对话框
const sqlDialogVisible = ref(false)
const currentSql = ref('')

// 指标名称字典（根据规则类型联动）
const metricOptions = computed(() => {
  const options = {
    slow_query: [
      { label: '查询执行时间(秒)', value: 'query_time' },
      { label: '扫描行数', value: 'rows_examined' },
      { label: '返回行数', value: 'rows_sent' }
    ],
    connection: [
      { label: '当前连接数', value: 'current_connections' },
      { label: '活跃连接数', value: 'active_connections' },
      { label: '最大连接数', value: 'max_connections' }
    ],
    table_space: [
      { label: '表数据大小(MB)', value: 'data_length_mb' },
      { label: '表总大小(MB)', value: 'table_size_mb' },
      { label: '表行数', value: 'table_rows' }
    ]
  }
  return options[ruleForm.value.ruleType] || []
})

// 计算总表空间
const totalTableSpace = computed(() => {
  return tableStats.value.reduce((sum, table) => sum + (table.total_length || 0), 0)
})

// 获取连接列表
function getConnList() {
  listConn().then(response => {
    connList.value = response.rows
    if (connList.value.length > 0 && !connId.value) {
      connId.value = connList.value[0].connId
      handleRefresh()
    }
  })
}

// 连接切换
function handleConnChange() {
  connectionHistory.value = []
  handleRefresh()
}

// 刷新数据
function handleRefresh() {
  if (!connId.value) return
  refreshing.value = true
  loading.value = true

  // 获取连接统计
  getConnectionStats(connId.value).then(response => {
    connectionStats.value = response.data
    // 添加到历史数据
    connectionHistory.value.push({
      time: new Date().toLocaleTimeString(),
      total: response.data.totalConnections || 0,
      active: response.data.activeConnections || 0
    })
    // 保留最近20个数据点
    if (connectionHistory.value.length > 20) {
      connectionHistory.value.shift()
    }
    updateConnectionChart()
  })

  // 根据当前标签页加载数据
  loadTabData()
}

// 加载标签页数据
function loadTabData() {
  switch (activeTab.value) {
    case 'process':
      loadProcessList()
      break
    case 'slow':
      loadSlowQueries()
      break
    case 'stats':
      loadTableStats()
      break
    case 'sql':
      loadSqlStats()
      break
    case 'rules':
      loadRules()
      break
  }
}

// 加载连接列表
function loadProcessList() {
  getProcessList(connId.value).then(response => {
    processList.value = response.data
    loading.value = false
    refreshing.value = false
  }).catch(() => {
    loading.value = false
    refreshing.value = false
  })
}

// 加载慢查询
function loadSlowQueries() {
  getSlowQueries(connId.value, slowQueryLimit.value).then(response => {
    slowQueries.value = response.data || []
    loading.value = false
    refreshing.value = false
  }).catch(() => {
    loading.value = false
    refreshing.value = false
  })
}

// 加载表空间统计
function loadTableStats() {
  getTableStats(connId.value).then(response => {
    tableStats.value = response.data || []
    loading.value = false
    refreshing.value = false
    updateTableSpaceChart()
  }).catch(() => {
    loading.value = false
    refreshing.value = false
  })
}

// 加载SQL统计
function loadSqlStats() {
  getSqlStats(connId.value, sqlStatsLimit.value).then(response => {
    sqlStats.value = response.data || []
    loading.value = false
    refreshing.value = false
  }).catch(() => {
    loading.value = false
    refreshing.value = false
  })
}

// 加载规则列表
function loadRules() {
  listMonitorRule({ pageSize: 100 }).then(response => {
    ruleList.value = response.rows || []
    loading.value = false
    refreshing.value = false
  }).catch(() => {
    loading.value = false
    refreshing.value = false
  })
}

// 标签页切换
function handleTabChange(tab) {
  loading.value = true
  loadTabData()
}

// 终止连接
function handleKill(row) {
  ElMessageBox.confirm(`确定要终止连接 ${row.Id} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    killProcess(connId.value, row.Id).then(() => {
      ElMessage.success('终止成功')
      loadProcessList()
    })
  })
}

// 初始化连接趋势图
function initConnectionChart() {
  if (!connectionChart.value) return
  connectionChartInstance = echarts.init(connectionChart.value)
  updateConnectionChart()
}

// 更新连接趋势图
function updateConnectionChart() {
  if (!connectionChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['总连接数', '活跃连接'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: connectionHistory.value.map(item => item.time)
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '总连接数',
        type: 'line',
        smooth: true,
        data: connectionHistory.value.map(item => item.total),
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
          ])
        }
      },
      {
        name: '活跃连接',
        type: 'line',
        smooth: true,
        data: connectionHistory.value.map(item => item.active),
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
          ])
        }
      }
    ]
  }
  connectionChartInstance.setOption(option)
}

// 初始化表空间图
function initTableSpaceChart() {
  if (!tableSpaceChart.value) return
  tableSpaceChartInstance = echarts.init(tableSpaceChart.value)
  updateTableSpaceChart()
}

// 更新表空间图
function updateTableSpaceChart() {
  if (!tableSpaceChartInstance) return
  
  const top10 = tableStats.value.slice(0, 10)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const data = params[0]
        return `${data.name}<br/>${data.seriesName}: ${formatBytes(data.value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        formatter: function(value) {
          return formatBytes(value)
        }
      }
    },
    yAxis: {
      type: 'category',
      data: top10.map(item => item.TABLE_NAME || item.table_name || item.tableName || '未知表').reverse()
    },
    series: [
      {
        name: '表空间',
        type: 'bar',
        data: top10.map(item => {
          const size = item.TOTAL_LENGTH || item.total_length || item.totalLength || 
                       ((item.DATA_LENGTH || item.data_length || 0) + (item.INDEX_LENGTH || item.index_length || 0))
          return size || 0
        }).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#8b5cf6' },
            { offset: 1, color: '#6366f1' }
          ])
        }
      }
    ]
  }
  tableSpaceChartInstance.setOption(option)
}

// 格式化字节
function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化数字
function formatNumber(num) {
  if (!num) return '0'
  return num.toLocaleString()
}

// 获取字段值（支持多种字段名格式）
function getFieldValue(row, fieldNames) {
  if (!row) return ''
  for (const name of fieldNames) {
    if (row[name] !== undefined && row[name] !== null) {
      return row[name]
    }
  }
  return ''
}

// 获取命令类型标签
function getCommandType(command) {
  if (command === 'Query') return 'primary'
  if (command === 'Sleep') return 'info'
  if (command === 'Connect') return 'success'
  return ''
}

// 获取时间样式
function getTimeClass(time) {
  if (time > 60) return 'danger-text'
  if (time > 10) return 'warning-text'
  return ''
}

// 获取耗时样式
function getLatencyClass(latency) {
  if (latency > 10) return 'danger-text'
  if (latency > 2) return 'warning-text'
  return ''
}

// 规则相关
function getRuleTypeTag(type) {
  const map = { slow_query: 'danger', connection: 'primary', table_space: 'warning' }
  return map[type] || ''
}

function getRuleTypeLabel(type) {
  const map = { slow_query: '慢查询', connection: '连接数', table_space: '表空间' }
  return map[type] || type
}

function getConditionLabel(condition) {
  const map = { GT: '大于', LT: '小于', EQ: '等于' }
  return map[condition] || condition
}

function handleAddRule() {
  ruleDialogTitle.value = '新增规则'
  ruleForm.value = {
    ruleId: null,
    ruleType: '',
    metricName: '',
    condition: 'GT',
    threshold: 0,
    enabled: '0',
    remark: ''
  }
  ruleDialogVisible.value = true
}

// 规则类型变化时重置指标名称
function handleRuleTypeChange() {
  ruleForm.value.metricName = ''
}

function handleEditRule(row) {
  ruleDialogTitle.value = '编辑规则'
  ruleForm.value = { ...row }
  ruleDialogVisible.value = true
}

function handleDeleteRule(row) {
  ElMessageBox.confirm(`确定要删除规则 "${row.metricName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    delMonitorRule(row.ruleId).then(() => {
      ElMessage.success('删除成功')
      loadRules()
    })
  })
}

function handleRuleStatusChange(row) {
  updateMonitorRule(row).then(() => {
    ElMessage.success('状态更新成功')
  })
}

function submitRuleForm() {
  ruleFormRef.value.validate(valid => {
    if (valid) {
      const api = ruleForm.value.ruleId ? updateMonitorRule : addMonitorRule
      api(ruleForm.value).then(() => {
        ElMessage.success(ruleForm.value.ruleId ? '修改成功' : '新增成功')
        ruleDialogVisible.value = false
        loadRules()
        // 如果添加的是慢查询规则，自动刷新慢查询统计
        if (ruleForm.value.ruleType === 'slow_query') {
          loadSlowQueries()
        }
      })
    }
  })
}

// 显示SQL详情
function showSqlDetail(sql) {
  currentSql.value = sql || ''
  sqlDialogVisible.value = true
}

// 复制SQL
function copySql() {
  if (!currentSql.value) return
  navigator.clipboard.writeText(currentSql.value).then(() => {
    ElMessage.success('SQL已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 自动刷新
watch(autoRefresh, (val) => {
  if (val) {
    refreshTimer = setInterval(() => {
      handleRefresh()
    }, 5000)
  } else {
    clearInterval(refreshTimer)
  }
})

onMounted(() => {
  getConnList()
  nextTick(() => {
    initConnectionChart()
    initTableSpaceChart()
  })
  window.addEventListener('resize', () => {
    connectionChartInstance?.resize()
    tableSpaceChartInstance?.resize()
  })
})

onUnmounted(() => {
  clearInterval(refreshTimer)
  connectionChartInstance?.dispose()
  tableSpaceChartInstance?.dispose()
})
</script>

<style scoped>
.db-monitor {
  padding: 20px;
  background: #f8fafc;
  min-height: 100vh;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.conn-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.conn-selector .label {
  color: #475569;
  font-size: 14px;
  font-weight: 500;
}

.auto-refresh {
  color: #475569;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  border-left: 4px solid transparent;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card.primary {
  border-left-color: #3b82f6;
}

.stat-card.success {
  border-left-color: #10b981;
}

.stat-card.warning {
  border-left-color: #f59e0b;
}

.stat-card.danger {
  border-left-color: #ef4444;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-right: 16px;
  font-size: 24px;
}

.stat-card.primary .stat-icon {
  background: #eff6ff;
  color: #3b82f6;
}

.stat-card.success .stat-icon {
  background: #ecfdf5;
  color: #10b981;
}

.stat-card.warning .stat-icon {
  background: #fffbeb;
  color: #f59e0b;
}

.stat-card.danger .stat-icon {
  background: #fef2f2;
  color: #ef4444;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.chart-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  background: #fafafa;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.chart-body {
  height: 280px;
  padding: 10px;
}

/* 标签页 */
.tabs-container {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.tabs-container :deep(.el-tabs__header) {
  margin: 0;
  background: #fafafa;
  border-bottom: 1px solid #e2e8f0;
}

.tabs-container :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: #e2e8f0;
}

.tabs-container :deep(.el-tabs__item) {
  color: #64748b;
  font-weight: 500;
  padding: 0 20px;
  height: 44px;
  line-height: 44px;
}

.tabs-container :deep(.el-tabs__item.is-active) {
  color: #3b82f6;
  background: #ffffff;
  border-bottom: 2px solid #3b82f6;
}

.tabs-container :deep(.el-tabs__item:hover) {
  color: #3b82f6;
}

.tabs-container :deep(.el-tabs__content) {
  padding: 0;
}

.tab-content {
  padding: 20px;
}

.toolbar {
  margin-bottom: 16px;
}

/* 表格样式 */
:deep(.el-table) {
  background: #ffffff;
  border-radius: 8px;
}

:deep(.el-table th) {
  background: #f8fafc !important;
  color: #475569;
  font-weight: 600;
  border-bottom: 1px solid #e2e8f0;
}

:deep(.el-table td) {
  background: #ffffff;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background: #f1f5f9;
}

/* 文本样式 */
.danger-text {
  color: #ef4444;
  font-weight: 600;
}

.warning-text {
  color: #f59e0b;
  font-weight: 600;
}

.error-text {
  color: #ef4444;
}

.size-highlight {
  color: #8b5cf6;
  font-weight: 600;
}

/* SQL预览 */
.sql-preview {
  display: inline-block;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
}

/* SQL详情 */
.sql-detail-container {
  background: #1e293b;
  border-radius: 8px;
  padding: 16px;
  max-height: 500px;
  overflow: auto;
}

.sql-code {
  margin: 0;
  color: #e2e8f0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  .monitor-header {
    flex-direction: column;
    gap: 12px;
  }
}
</style>