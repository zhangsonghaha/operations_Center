<template>
  <div class="app-container">
    <el-container class="monitor-container">
      <!-- 左侧服务器列表 -->
      <el-aside width="260px" class="monitor-sidebar">
        <div class="sidebar-header">
          <div class="sidebar-title">
            <el-icon class="icon"><DataLine /></el-icon>
            <span>服务器列表</span>
          </div>
          <el-input
            v-model="serverName"
            placeholder="输入名称或IP搜索..."
            prefix-icon="Search"
            clearable
            class="search-input"
          />
        </div>
        <el-scrollbar>
          <div v-if="filteredServerList.length === 0" class="no-data">
            <el-empty description="暂无服务器" image-size="60" />
          </div>
          <el-menu
            v-else
            :default-active="currentServerId"
            class="server-menu"
            @select="handleServerSelect"
          >
            <el-menu-item 
              v-for="server in filteredServerList" 
              :key="server.serverId" 
              :index="String(server.serverId)"
            >
              <div class="server-item-content">
                <div class="server-icon-wrapper">
                  <el-icon><Monitor /></el-icon>
                </div>
                <div class="server-info">
                  <div class="server-name-row">
                    <span class="server-name" :title="server.serverName">{{ server.serverName }}</span>
                    <span class="status-dot is-online"></span>
                  </div>
                  <span class="server-ip">{{ server.publicIp }}</span>
                </div>
              </div>
            </el-menu-item>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 右侧监控面板 -->
      <el-main class="monitor-main" v-loading="loading">
        <div v-if="!currentServerId" class="empty-state">
          <div class="empty-content">
            <el-icon class="empty-icon"><Monitor /></el-icon>
            <h3>请选择服务器</h3>
            <p>从左侧列表中选择一台服务器以查看实时监控数据</p>
          </div>
        </div>
        
        <div v-else class="dashboard-content">
          <!-- 顶部状态栏 -->
          <div class="dashboard-header">
             <div class="header-left">
               <div class="server-title-block">
                 <h2>{{ currentServerName }}</h2>
                 <el-tag type="success" effect="light" size="small" round class="status-tag">
                   <span class="dot-indicator"></span> 运行中
                 </el-tag>
               </div>
               <div class="server-meta">
                 <span class="meta-item"><el-icon><Location /></el-icon> {{ currentServerIp }}</span>
                 <span class="meta-item"><el-icon><Timer /></el-icon> 更新于: {{ lastUpdateTime }}</span>
               </div>
             </div>
             <div class="header-right">
               <div class="auto-refresh-wrapper">
                 <span class="label">自动刷新</span>
                 <el-switch 
                   v-model="autoRefresh" 
                   inline-prompt
                   active-text="ON"
                   inactive-text="OFF"
                   @change="handleAutoRefresh" 
                 />
               </div>
               <el-button class="refresh-btn" type="primary" circle icon="Refresh" @click="manualRefresh" title="立即刷新"></el-button>
             </div>
          </div>

          <!-- 核心指标仪表盘 -->
          <el-row :gutter="20" class="metric-row">
            <el-col :span="8">
              <el-card shadow="hover" class="metric-card">
                <div class="metric-header">
                  <span class="title">CPU 使用率</span>
                  <span class="percentage" :style="{ color: getStatusColor(cpuValue) }">{{ cpuValue }}%</span>
                </div>
                <div class="chart-container">
                  <div ref="cpuGaugeRef" class="gauge-chart"></div>
                </div>
                <div class="metric-footer">
                  <span class="status-text" :style="{ color: getStatusColor(cpuValue) }">
                    {{ getStatusText(cpuValue) }}
                  </span>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="metric-card">
                <div class="metric-header">
                  <span class="title">内存使用率</span>
                  <span class="percentage" :style="{ color: getStatusColor(memValue) }">{{ memValue }}%</span>
                </div>
                <div class="chart-container">
                  <div ref="memGaugeRef" class="gauge-chart"></div>
                </div>
                <div class="metric-footer">
                  <span class="status-text" :style="{ color: getStatusColor(memValue) }">
                    {{ getStatusText(memValue) }}
                  </span>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="metric-card">
                <div class="metric-header">
                  <span class="title">磁盘使用率</span>
                  <span class="percentage" :style="{ color: getStatusColor(diskValue) }">{{ diskValue }}%</span>
                </div>
                <div class="chart-container">
                  <div ref="diskGaugeRef" class="gauge-chart"></div>
                </div>
                <div class="metric-footer">
                  <span class="status-text" :style="{ color: getStatusColor(diskValue) }">
                    {{ getStatusText(diskValue) }}
                  </span>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 网络流量实时监控 -->
          <el-card shadow="hover" class="chart-card network-card">
            <template #header>
              <div class="card-header">
                <div class="title-with-icon">
                  <div class="icon-box net-bg">
                    <el-icon><Connection /></el-icon>
                  </div>
                  <span>网络流量实时监控</span>
                </div>
                <div class="net-stats">
                  <div class="stat-item up">
                    <el-icon><Top /></el-icon>
                    <span class="label">上行</span>
                    <span class="value">{{ netTxValue }} KB/s</span>
                  </div>
                  <div class="divider"></div>
                  <div class="stat-item down">
                    <el-icon><Bottom /></el-icon>
                    <span class="label">下行</span>
                    <span class="value">{{ netRxValue }} KB/s</span>
                  </div>
                </div>
              </div>
            </template>
            <div ref="netLineChartRef" class="chart-box"></div>
          </el-card>

          <!-- 历史趋势图 -->
          <el-card shadow="hover" class="chart-card trend-card">
            <template #header>
              <div class="card-header">
                <div class="title-with-icon">
                  <div class="icon-box trend-bg">
                    <el-icon><TrendCharts /></el-icon>
                  </div>
                  <span>系统负载趋势</span>
                </div>
                <el-radio-group v-model="trendRange" size="small" @change="fetchTrendData" class="trend-switch">
                  <el-radio-button label="live">实时</el-radio-button>
                  <el-radio-button label="24h">24小时</el-radio-button>
                  <el-radio-button label="7d">7天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-box"></div>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup name="MonitorIndex">
import { listServer } from "@/api/ops/server";
import { getRealtimeData, getTrendData } from "@/api/ops/monitor";
import { 
  Search, Monitor, Refresh, Top, Bottom, 
  DataLine, Connection, TrendCharts, Location, Timer 
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { onUnmounted } from "vue";

const route = useRoute();
const { proxy } = getCurrentInstance();

const serverList = ref([]);
const serverName = ref('');
const currentServerId = ref('');
const currentServerName = ref('');
const currentServerIp = ref('');
const loading = ref(false);
const trendRange = ref('24h');
const lastUpdateTime = ref('-');
const autoRefresh = ref(false);
let refreshTimer = null;

const cpuValue = ref(0);
const memValue = ref(0);
const diskValue = ref(0);
const netTxValue = ref(0);
const netRxValue = ref(0);

// Chart refs
const cpuGaugeRef = ref(null);
const memGaugeRef = ref(null);
const diskGaugeRef = ref(null);
const netLineChartRef = ref(null);
const trendChartRef = ref(null);

let cpuChart = null;
let memChart = null;
let diskChart = null;
let netChart = null;
let trendChart = null;

// Network data buffer for real-time chart
const netTimeBuffer = ref([]);
const netTxBuffer = ref([]);
const netRxBuffer = ref([]);
const maxBufferLength = 60; // Keep 60 points (e.g. 60 seconds if 1s interval, but here manual/auto refresh)

// Live trend buffers (实时趋势，仅前端缓存)
const liveTimeBuffer = ref([]);
const liveCpuBuffer = ref([]);
const liveMemBuffer = ref([]);
const liveMaxLen = 300; // 约 10 分钟（2s * 300）

const filteredServerList = computed(() => {
  if (!serverName.value) return serverList.value;
  return serverList.value.filter(server => 
    server.serverName.toLowerCase().includes(serverName.value.toLowerCase()) ||
    server.publicIp.includes(serverName.value)
  );
});

function getServerList() {
  listServer({ pageNum: 1, pageSize: 1000 }).then(response => {
    serverList.value = response.rows;
    if (route.query.serverId) {
      handleServerSelect(String(route.query.serverId));
    }
  });
}

function handleServerSelect(index) {
  if (currentServerId.value === index) return;
  currentServerId.value = index;
  const server = serverList.value.find(s => String(s.serverId) === index);
  if (server) {
    currentServerName.value = server.serverName;
    currentServerIp.value = server.publicIp;
    
    // Clear buffers on server switch
    netTimeBuffer.value = [];
    netTxBuffer.value = [];
    netRxBuffer.value = [];

    nextTick(() => {
      initCharts();
      fetchRealtimeData();
      fetchTrendData();
    });
  }
}

function initCharts() {
  // Dispose old instances if any
  [cpuChart, memChart, diskChart, netChart, trendChart].forEach(c => c && c.dispose());

  if (cpuGaugeRef.value) {
    cpuChart = echarts.init(cpuGaugeRef.value);
    memChart = echarts.init(memGaugeRef.value);
    diskChart = echarts.init(diskGaugeRef.value);
    netChart = echarts.init(netLineChartRef.value);
    trendChart = echarts.init(trendChartRef.value);
  }
}

function fetchRealtimeData() {
  // Don't set global loading true on auto refresh to avoid flickering
  if (!autoRefresh.value) loading.value = true;
  
  getRealtimeData(currentServerId.value).then(response => {
    const data = response.data;
    loading.value = false;
    const now = new Date();
    lastUpdateTime.value = now.toLocaleTimeString();
    
    cpuValue.value = data.cpuUsage;
    memValue.value = data.memoryUsage;
    diskValue.value = data.diskUsage;
    netTxValue.value = data.netTxRate;
    netRxValue.value = data.netRxRate;

    updateRingGauge(cpuChart, data.cpuUsage, '#409EFF');
    updateRingGauge(memChart, data.memoryUsage, '#67C23A');
    updateRingGauge(diskChart, data.diskUsage, '#E6A23C');
    
    updateNetworkChart(now);
    // 更新实时趋势
    if (trendRange.value === 'live') {
      updateLiveTrend(now);
    }
  }).catch(() => {
    loading.value = false;
  }).finally(() => {
    // 递归调用，确保上一次请求完成后再发起下一次，避免堆积
    if (autoRefresh.value) {
      refreshTimer = setTimeout(() => {
        fetchRealtimeData();
      }, 2000);
    }
  });
}

function manualRefresh() {
  fetchRealtimeData();
  fetchTrendData();
}

function handleAutoRefresh(val) {
  if (val) {
    // 清除可能存在的旧定时器
    if (refreshTimer) {
      clearTimeout(refreshTimer);
      refreshTimer = null;
    }
    // 立即发起一次，后续由 fetchRealtimeData 的 finally 块接管
    fetchRealtimeData();
  } else {
    if (refreshTimer) {
      clearTimeout(refreshTimer);
      refreshTimer = null;
    }
  }
}

function getStatusColor(value) {
  if (value < 60) return '#67C23A';
  if (value < 80) return '#E6A23C';
  return '#F56C6C';
}

function getStatusText(value) {
  if (value < 60) return '状态良好';
  if (value < 80) return '负载适中';
  return '负载较高';
}

function updateNetworkChart(now) {
  if (!netChart) return;

  const timeStr = now.toLocaleTimeString();
  
  // Update buffers
  netTimeBuffer.value.push(timeStr);
  netTxBuffer.value.push(netTxValue.value);
  netRxBuffer.value.push(netRxValue.value);
  
  if (netTimeBuffer.value.length > maxBufferLength) {
    netTimeBuffer.value.shift();
    netTxBuffer.value.shift();
    netRxBuffer.value.shift();
  }

  netChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' }
    },
    legend: {
      data: ['上行', '下行'],
      top: 0
    },
    grid: {
      left: '10', right: '20', bottom: '10', top: '30',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: netTimeBuffer.value,
      axisLabel: { show: false }, // Hide labels for clean look in real-time
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#E4E7ED' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [
      {
        name: '上行',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: netTxBuffer.value,
        itemStyle: { color: '#F56C6C' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245,108,108,0.3)' },
            { offset: 1, color: 'rgba(245,108,108,0.01)' }
          ])
        }
      },
      {
        name: '下行',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: netRxBuffer.value,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.01)' }
          ])
        }
      }
    ]
  });
}

// Modern Ring Gauge
function updateRingGauge(chart, value, color) {
  if (!chart) return;
  const statusColor = getStatusColor(value);
  
  chart.setOption({
    series: [
      {
        type: 'gauge',
        startAngle: 90,
        endAngle: -270,
        pointer: { show: false },
        progress: {
          show: true,
          overlap: false,
          roundCap: true,
          clip: false,
          itemStyle: { color: statusColor }
        },
        axisLine: {
          lineStyle: {
            width: 8,
            color: [[1, '#F2F6FC']]
          }
        },
        splitLine: { show: false },
        axisTick: { show: false },
        axisLabel: { show: false },
        data: [{ value: value }],
        detail: { show: false }
      }
    ]
  });
}

function fetchTrendData() {
  if (trendRange.value === 'live') {
    // 切换到实时趋势：清空并初始化图表坐标
    liveTimeBuffer.value = [];
    liveCpuBuffer.value = [];
    liveMemBuffer.value = [];
    // 自动开启实时刷新
    if (!autoRefresh.value) {
      autoRefresh.value = true;
      handleAutoRefresh(true);
    }
    if (trendChart) {
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['CPU', '内存'], top: 0 },
        grid: { left: '10', right: '20', bottom: '10', top: '30', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: [],
          axisLine: { lineStyle: { color: '#E4E7ED' } }
        },
        yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
        series: [
          { name: 'CPU', type: 'line', data: [], smooth: true, showSymbol: false, itemStyle: { color: '#409EFF' }, areaStyle: { opacity: 0.1, color: '#409EFF' } },
          { name: '内存', type: 'line', data: [], smooth: true, showSymbol: false, itemStyle: { color: '#67C23A' }, areaStyle: { opacity: 0.1, color: '#67C23A' } }
        ]
      });
    }
    return;
  }
  // 历史趋势：从后端获取
  getTrendData(currentServerId.value, trendRange.value).then(response => {
    const list = response.data;
    const times = list.map(item => item.createTime);
    const cpuData = list.map(item => item.cpuUsage);
    const memData = list.map(item => item.memoryUsage);
    if (trendChart) {
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['CPU', '内存'], top: 0 },
        grid: { left: '10', right: '20', bottom: '10', top: '30', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: times, axisLine: { lineStyle: { color: '#E4E7ED' } } },
        yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
        series: [
          { name: 'CPU', type: 'line', data: cpuData, smooth: true, showSymbol: false, itemStyle: { color: '#409EFF' }, areaStyle: { opacity: 0.1, color: '#409EFF' } },
          { name: '内存', type: 'line', data: memData, smooth: true, showSymbol: false, itemStyle: { color: '#67C23A' }, areaStyle: { opacity: 0.1, color: '#67C23A' } }
        ]
      });
    }
  });
}

// 实时趋势更新（使用前端缓存，不依赖后端定时任务）
function updateLiveTrend(now) {
  if (!trendChart) return;
  const timeStr = now.toLocaleTimeString();
  liveTimeBuffer.value.push(timeStr);
  liveCpuBuffer.value.push(cpuValue.value);
  liveMemBuffer.value.push(memValue.value);
  if (liveTimeBuffer.value.length > liveMaxLen) {
    liveTimeBuffer.value.shift();
    liveCpuBuffer.value.shift();
    liveMemBuffer.value.shift();
  }
  trendChart.setOption({
    xAxis: { data: liveTimeBuffer.value },
    series: [
      { name: 'CPU', data: liveCpuBuffer.value },
      { name: '内存', data: liveMemBuffer.value }
    ]
  });
}

const resizeHandler = () => {
  cpuChart && cpuChart.resize();
  memChart && memChart.resize();
  diskChart && diskChart.resize();
  netChart && netChart.resize();
  trendChart && trendChart.resize();
};

window.addEventListener('resize', resizeHandler);

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler);
  if (refreshTimer) clearTimeout(refreshTimer);
  [cpuChart, memChart, diskChart, netChart, trendChart].forEach(c => c && c.dispose());
});

getServerList();
</script>

<style scoped>
.app-container {
  padding: 0;
  height: calc(100vh - 84px);
  background-color: #f6f8f9;
}

.monitor-container {
  height: 100%;
}

/* Sidebar Styles */
.monitor-sidebar {
  background-color: #fff;
  border-right: 1px solid #f0f2f5;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0,0,0,0.02);
  z-index: 10;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.sidebar-title .icon {
  color: #409EFF;
  font-size: 18px;
}

.no-data {
  padding-top: 40px;
}

.server-menu {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 72px;
  padding: 0 16px !important;
  margin: 8px 12px;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.3s;
}

:deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

:deep(.el-menu-item.is-active .server-name) {
  color: #409EFF;
  font-weight: 600;
}

:deep(.el-menu-item.is-active .server-icon-wrapper) {
  background-color: #409EFF;
  color: #fff;
}

.server-item-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.server-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background-color: #f0f2f5;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-right: 12px;
  transition: all 0.3s;
}

.server-info {
  flex: 1;
  overflow: hidden;
  line-height: 1.4;
}

.server-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2px;
}

.server-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #dcdfe6;
}

.status-dot.is-online {
  background-color: #67C23A;
  box-shadow: 0 0 0 2px rgba(103,194,58,0.2);
}

.server-ip {
  font-size: 12px;
  color: #909399;
}

/* Main Content Styles */
.monitor-main {
  padding: 24px;
  overflow-y: auto;
}

.dashboard-content {
  max-width: 1400px;
  margin: 0 auto;
}

/* Dashboard Header */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #fff;
  padding: 24px 30px;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}

.header-left .server-title-block {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.header-left h2 {
  margin: 0;
  font-size: 22px;
  color: #1f2f3d;
  font-weight: 600;
}

.status-tag .dot-indicator {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #67C23A;
  margin-right: 4px;
  margin-bottom: 1px;
}

.server-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.auto-refresh-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  background-color: #f5f7fa;
  padding: 4px 12px;
  border-radius: 20px;
}

/* Metric Cards */
.metric-row {
  margin-bottom: 24px;
}

.metric-card {
  border: none;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  text-align: center;
  transition: transform 0.3s;
}

.metric-card:hover {
  transform: translateY(-2px);
}

:deep(.el-card__body) {
  padding: 20px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.metric-header .title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.metric-header .percentage {
  font-size: 20px;
  font-weight: 700;
  font-family: 'DIN Alternate', sans-serif;
}

.chart-container {
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.gauge-chart {
  width: 120px;
  height: 120px;
}

.metric-footer {
  margin-top: 15px;
  font-size: 13px;
  font-weight: 500;
}

/* Large Chart Cards */
.chart-card {
  border: none;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.icon-box.net-bg { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
.icon-box.trend-bg { background: rgba(103, 194, 58, 0.1); color: #67C23A; }

.net-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.stat-item .value {
  font-weight: 600;
  font-size: 15px;
  font-family: 'DIN Alternate', sans-serif;
}

.stat-item.up { color: #F56C6C; }
.stat-item.down { color: #409EFF; }

.divider {
  width: 1px;
  height: 14px;
  background-color: #dcdfe6;
}

.chart-box {
  height: 300px;
  width: 100%;
}

.empty-state {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.empty-content {
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #e4e7ed;
  margin-bottom: 16px;
}

.empty-content h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 8px;
}

.empty-content p {
  font-size: 14px;
  color: #909399;
}
</style>
