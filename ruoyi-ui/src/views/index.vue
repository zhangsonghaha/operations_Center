<template>
  <div class="app-container home">
    <!-- Header Stats -->
    <el-row :gutter="20">
      <el-col :sm="24" :lg="6">
        <div class="stat-card">
          <div class="stat-icon icon-blue"><el-icon><Monitor /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.onlineHosts }}</div>
            <div class="stat-label">在线主机</div>
          </div>
        </div>
      </el-col>
      <el-col :sm="24" :lg="6">
        <div class="stat-card">
          <div class="stat-icon icon-red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingAlerts }}</div>
            <div class="stat-label">待处理告警</div>
          </div>
        </div>
      </el-col>
      <el-col :sm="24" :lg="6">
        <div class="stat-card">
          <div class="stat-icon icon-green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.systemAvailability }}</div>
            <div class="stat-label">系统可用性</div>
          </div>
        </div>
      </el-col>
      <el-col :sm="24" :lg="6">
        <div class="stat-card">
          <div class="stat-icon icon-purple"><el-icon><Upload /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayDeployments }}</div>
            <div class="stat-label">今日发布</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Main Content -->
    <el-row :gutter="20" class="mt-20">
      <!-- Left: Charts -->
      <el-col :xs="24" :sm="24" :lg="16">
        <el-card class="box-card minimal-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>系统负载趋势</span>
              <div class="card-actions">
                <el-radio-group v-model="timeRange" size="small">
                  <el-radio-button label="1h">1小时</el-radio-button>
                  <el-radio-button label="24h">24小时</el-radio-button>
                  <el-radio-button label="7d">7天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div class="chart-container" ref="chartRef"></div>
        </el-card>

        <el-card class="box-card minimal-card mt-20" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最新部署记录</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <el-table :data="deployList" style="width: 100%" :header-cell-style="{background:'#f8f9fa'}">
            <el-table-column prop="name" label="应用名称" width="180" />
            <el-table-column prop="version" label="版本" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'" size="small" effect="plain">
                  {{ scope.row.status === 'success' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="发布时间" />
            <el-table-column prop="operator" label="操作人" />
          </el-table>
        </el-card>
      </el-col>

      <!-- Right: Quick Actions & Activity -->
      <el-col :xs="24" :sm="24" :lg="8">
        <el-card class="box-card minimal-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item">
              <div class="action-icon"><el-icon><Plus /></el-icon></div>
              <span>添加主机</span>
            </div>
            <div class="action-item">
              <div class="action-icon"><el-icon><Document /></el-icon></div>
              <span>查看日志</span>
            </div>
            <div class="action-item">
              <div class="action-icon"><el-icon><SwitchButton /></el-icon></div>
              <span>重启服务</span>
            </div>
            <div class="action-item">
              <div class="action-icon"><el-icon><Setting /></el-icon></div>
              <span>系统设置</span>
            </div>
          </div>
        </el-card>

        <el-card class="box-card minimal-card mt-20" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近动态</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in activities"
              :key="index"
              :type="activity.type"
              :timestamp="activity.timestamp"
              :hollow="true"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { getDashboardData } from "@/api/ops/dashboard"
import { Monitor, Warning, CircleCheck, Upload, Plus, Document, SwitchButton, Setting } from '@element-plus/icons-vue'

const timeRange = ref('24h')
const chartRef = ref(null)
let chartInstance = null

// Reactive Data
const stats = ref({
  onlineHosts: 0,
  pendingAlerts: 0,
  systemAvailability: '100%',
  todayDeployments: 0
})
const deployList = ref([])
const activities = ref([])
const monitorLogs = ref([])

onMounted(() => {
  fetchData()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance && chartInstance.dispose()
})

function fetchData() {
  getDashboardData().then(response => {
    const data = response.data
    stats.value = data.stats
    deployList.value = data.deployments
    activities.value = data.activities.map(item => ({
      content: item.alertContent,
      timestamp: item.alertTime,
      type: item.alertType
    }))
    monitorLogs.value = data.monitorLogs
    initChart()
  })
}

function initChart() {
  if (chartRef.value && monitorLogs.value.length > 0) {
    chartInstance = echarts.init(chartRef.value)
    
    const times = monitorLogs.value.map(item => {
      const date = new Date(item.recordTime)
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    })
    const cpuData = monitorLogs.value.map(item => item.cpuLoad)
    const memData = monitorLogs.value.map(item => item.memoryLoad)

    const option = {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: times,
        axisLine: { lineStyle: { color: '#e4e4e7' } },
        axisLabel: { color: '#71717a' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#e4e4e7' } },
        axisLabel: { color: '#71717a' }
      },
      series: [
        {
          name: 'CPU Load',
          type: 'line',
          smooth: true,
          data: cpuData,
          itemStyle: { color: '#2563eb' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(37, 99, 235, 0.2)' },
              { offset: 1, color: 'rgba(37, 99, 235, 0)' }
            ])
          }
        },
        {
          name: 'Memory Usage',
          type: 'line',
          smooth: true,
          data: memData,
          itemStyle: { color: '#10b981' }
        }
      ]
    }
    chartInstance.setOption(option)
  }
}

function resizeChart() {
  chartInstance && chartInstance.resize()
}
</script>

<style lang="scss" scoped>
.home {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 84px);
}

.mt-20 {
  margin-top: 20px;
}

/* Stat Cards */
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    margin-right: 16px;
    
    &.icon-blue { background: rgba(37, 99, 235, 0.1); color: #2563eb; }
    &.icon-red { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
    &.icon-green { background: rgba(16, 185, 129, 0.1); color: #10b981; }
    &.icon-purple { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; }
  }
  
  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #18181b;
      line-height: 1.2;
    }
    .stat-label {
      font-size: 13px;
      color: #71717a;
      margin-top: 4px;
    }
  }
}

/* Minimal Card */
.minimal-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #f4f4f5;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    color: #18181b;
  }
}

.chart-container {
  height: 300px;
}

/* Quick Actions */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 10px 0;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    padding: 12px;
    border-radius: 8px;
    transition: background 0.2s;
    
    &:hover {
      background: #f4f4f5;
    }
    
    .action-icon {
      width: 40px;
      height: 40px;
      background: #f4f4f5;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #18181b;
      margin-bottom: 8px;
    }
    
    span {
      font-size: 12px;
      color: #52525b;
    }
  }
}
</style>
