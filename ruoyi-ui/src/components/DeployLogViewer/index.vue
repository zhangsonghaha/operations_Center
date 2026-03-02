<template>
  <div class="deploy-log-viewer">
    <div class="log-header">
      <div class="log-info">
        <div class="log-title">
          <span class="app-name">{{ logInfo.appName }}</span>
          <el-tag :type="statusType" size="small" class="status-tag">
            {{ statusText }}
          </el-tag>
        </div>
        <div class="log-meta">
          <span class="meta-item">
            <i class="el-icon-user"></i>
            {{ logInfo.executor }}
          </span>
          <span class="meta-item">
            <i class="el-icon-time"></i>
            {{ logInfo.startTime }}
          </span>
          <span v-if="logInfo.endTime" class="meta-item">
            <i class="el-icon-check"></i>
            耗时: {{ duration }}
          </span>
        </div>
      </div>
      
      <div class="log-actions">
        <el-button-group>
          <el-button 
            :icon="autoScroll ? 'VideoPause' : 'VideoPlay'" 
            size="small"
            @click="toggleAutoScroll"
          >
            {{ autoScroll ? '暂停滚动' : '自动滚动' }}
          </el-button>
          <el-button 
            icon="Search" 
            size="small"
            @click="showSearch = !showSearch"
          >
            搜索
          </el-button>
          <el-button 
            icon="Download" 
            size="small"
            @click="downloadLog"
          >
            下载
          </el-button>
          <el-button 
            icon="Refresh" 
            size="small"
            @click="clearLog"
          >
            清空
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 步骤进度条 -->
    <div class="log-progress" v-if="currentStep">
      <el-steps :active="currentStepIndex" align-center finish-status="success">
        <el-step 
          v-for="(step, index) in steps" 
          :key="index"
          :title="step.title"
          :description="step.description"
        />
      </el-steps>
    </div>

    <div v-if="showSearch" class="log-search">
      <el-input
        v-model="searchKeyword"
        placeholder="输入关键词搜索日志..."
        prefix-icon="Search"
        clearable
        @input="handleSearch"
      >
        <template #append>
          <span class="search-count">{{ searchMatches }}/{{ totalLines }}</span>
        </template>
      </el-input>
    </div>

    <div class="log-container" ref="logContainer">
      <div class="log-content">
        <div 
          v-for="(line, index) in displayLines" 
          :key="index"
          :class="['log-line', getLineClass(line), { 'highlight': isHighlighted(line), 'step-header': isStepHeader(line) }]"
        >
          <span class="line-number">{{ index + 1 }}</span>
          <span class="line-content" v-html="highlightText(line)"></span>
        </div>
        <div v-if="isConnecting" class="log-line connecting">
          <span class="line-content">
            <i class="el-icon-loading"></i> 正在连接 WebSocket...
          </span>
        </div>
        <div v-if="!wsConnected && !isConnecting && totalLines === 0" class="log-line error">
          <span class="line-content">
            <i class="el-icon-warning"></i> WebSocket 连接失败，请检查后端服务是否正常运行
          </span>
        </div>
      </div>
    </div>

    <div class="log-footer">
      <div class="footer-info">
        <span class="info-item">总行数: {{ totalLines }}</span>
        <span class="info-item">连接状态: 
          <el-tag :type="wsConnected ? 'success' : 'danger'" size="small">
            {{ wsConnected ? '已连接' : '未连接' }}
          </el-tag>
        </span>
        <span class="info-item" v-if="currentStep">
          当前步骤: {{ currentStep }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { getDeployLog } from '@/api/ops/deployLog'
import { ElMessage } from 'element-plus'

const props = defineProps({
  logId: {
    type: [String, Number],
    required: true
  }
})

const logInfo = ref({
  appName: '',
  executor: '',
  startTime: '',
  endTime: '',
  deployStatus: 'running'
})

const logLines = ref([])
const displayLines = computed(() => logLines.value)
const totalLines = computed(() => logLines.value.length)

const autoScroll = ref(true)
const showSearch = ref(false)
const searchKeyword = ref('')
const searchMatches = ref(0)
const isConnecting = ref(true)
const wsConnected = ref(false)

// 步骤相关
const currentStep = ref('')
const currentStepIndex = ref(0)
const steps = ref([
  { title: '连接服务器', description: '建立 SSH 连接' },
  { title: '上传文件', description: '上传应用包' },
  { title: '执行脚本', description: '运行部署脚本' },
  { title: '完成', description: '部署完成' }
])

const logContainer = ref(null)
let ws = null
let reconnectTimer = null
let heartbeatTimer = null

const statusType = computed(() => {
  const status = logInfo.value.deployStatus
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  return 'warning'
})

const statusText = computed(() => {
  const status = logInfo.value.deployStatus
  if (status === 'success') return '部署成功'
  if (status === 'failed') return '部署失败'
  return '部署中'
})

const duration = computed(() => {
  if (!logInfo.value.startTime || !logInfo.value.endTime) return '-'
  const start = new Date(logInfo.value.startTime)
  const end = new Date(logInfo.value.endTime)
  const diff = Math.floor((end - start) / 1000)
  return `${diff}秒`
})

const getLineClass = (line) => {
  if (line.includes('[ERROR]') || line.includes('失败') || line.includes('error')) return 'error'
  if (line.includes('[WARN]') || line.includes('警告') || line.includes('warning')) return 'warning'
  if (line.includes('[INFO]') || line.includes('成功') || line.includes('success') || line.includes('[SUCCESS]')) return 'info'
  if (line.includes('[DEBUG]')) return 'debug'
  return ''
}

const isStepHeader = (line) => {
  return line.includes('==========') && line.includes('步骤')
}

const isHighlighted = (line) => {
  if (!searchKeyword.value) return false
  return line.toLowerCase().includes(searchKeyword.value.toLowerCase())
}

const highlightText = (text) => {
  if (!searchKeyword.value) return text
  const regex = new RegExp(`(${searchKeyword.value})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

const handleSearch = () => {
  if (!searchKeyword.value) {
    searchMatches.value = 0
    return
  }
  
  let count = 0
  logLines.value.forEach(line => {
    if (line.toLowerCase().includes(searchKeyword.value.toLowerCase())) {
      count++
    }
  })
  searchMatches.value = count
}

const toggleAutoScroll = () => {
  autoScroll.value = !autoScroll.value
  if (autoScroll.value) {
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

const downloadLog = () => {
  const content = logLines.value.join('\n')
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `deploy-log-${props.logId}-${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('日志下载成功')
}

const clearLog = () => {
  logLines.value = []
  ElMessage.success('日志已清空')
}

// 解析步骤信息
const parseStep = (line) => {
  const stepMatch = line.match(/步骤\s+(\d+)\/(\d+):\s*(.+?)\s*=/)
  if (stepMatch) {
    const [, current, total, stepName] = stepMatch
    currentStep.value = stepName
    currentStepIndex.value = parseInt(current)
    
    // 动态更新步骤列表
    if (parseInt(total) !== steps.value.length - 1) {
      // 根据实际步骤数调整
      const newSteps = []
      for (let i = 1; i <= parseInt(total); i++) {
        newSteps.push({ title: `步骤 ${i}`, description: '' })
      }
      newSteps.push({ title: '完成', description: '部署完成' })
      steps.value = newSteps
    }
    
    // 更新当前步骤描述
    if (steps.value[currentStepIndex.value - 1]) {
      steps.value[currentStepIndex.value - 1].title = stepName
    }
  } else if (line.includes('部署完成') || line.includes('启动完成') || line.includes('停止完成')) {
    currentStep.value = '完成'
    currentStepIndex.value = steps.value.length
  }
}

const connectWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const wsHost = import.meta.env.DEV ? 'localhost:8080' : host
  const wsUrl = `${protocol}//${wsHost}/websocket/deployLog/${props.logId}`
  
  console.log('连接 WebSocket:', wsUrl)
  
  try {
    ws = new WebSocket(wsUrl)
    
    ws.onopen = () => {
      console.log('WebSocket 连接已建立')
      wsConnected.value = true
      isConnecting.value = false
      
      // 启动心跳
      startHeartbeat()
    }
    
    ws.onmessage = (event) => {
      const message = event.data
      console.log('收到消息:', message)
      
      if (message && message.trim()) {
        const lines = message.split('\n')
        lines.forEach(line => {
          if (line.trim()) {
            logLines.value.push(line)
            // 解析步骤信息
            parseStep(line)
          }
        })
        
        if (autoScroll.value) {
          scrollToBottom()
        }
      }
    }
    
    ws.onerror = (error) => {
      console.error('WebSocket 错误:', error)
      wsConnected.value = false
      isConnecting.value = false
      ElMessage.error('WebSocket 连接错误，请检查后端服务')
    }
    
    ws.onclose = (event) => {
      console.log('WebSocket 连接已关闭', event)
      wsConnected.value = false
      isConnecting.value = false
      stopHeartbeat()
      
      // 如果不是正常关闭，尝试重连
      if (event.code !== 1000 && !reconnectTimer) {
        console.log('尝试重新连接...')
        reconnectTimer = setTimeout(() => {
          reconnectTimer = null
          if (!wsConnected.value) {
            connectWebSocket()
          }
        }, 3000)
      }
    }
  } catch (error) {
    console.error('创建 WebSocket 失败:', error)
    wsConnected.value = false
    isConnecting.value = false
    ElMessage.error('无法创建 WebSocket 连接')
  }
}

// 心跳机制
const startHeartbeat = () => {
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send('ping')
    }
  }, 30000) // 每30秒发送一次心跳
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

const loadLogInfo = async () => {
  try {
    const response = await getDeployLog(props.logId)
    logInfo.value = response.data
    
    // 加载已有日志内容
    if (response.data.logContent) {
      const lines = response.data.logContent.split('\n')
      logLines.value = lines.filter(line => line.trim())
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载日志信息失败:', error)
    ElMessage.error('加载日志信息失败')
  }
}

onMounted(() => {
  loadLogInfo()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
  stopHeartbeat()
})

watch(searchKeyword, () => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.deploy-log-viewer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.log-progress {
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  
  :deep(.el-steps) {
    .el-step__title {
      color: rgba(255, 255, 255, 0.7);
      font-size: 13px;
    }
    
    .el-step__description {
      color: rgba(255, 255, 255, 0.5);
      font-size: 12px;
    }
    
    .el-step.is-process {
      .el-step__title {
        color: #409eff;
        font-weight: 600;
      }
      
      .el-step__icon {
        border-color: #409eff;
        color: #409eff;
        animation: pulse 1.5s ease-in-out infinite;
      }
    }
    
    .el-step.is-finish {
      .el-step__title {
        color: #67c23a;
      }
      
      .el-step__icon {
        border-color: #67c23a;
        color: #67c23a;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.log-info {
  flex: 1;
}

.log-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
}

.status-tag {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.log-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  
  i {
    font-size: 14px;
  }
}

.log-actions {
  :deep(.el-button-group) {
    .el-button {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.15);
      color: rgba(255, 255, 255, 0.9);
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.15);
        border-color: rgba(255, 255, 255, 0.3);
        color: #fff;
        transform: translateY(-2px);
      }
    }
  }
}

.log-search {
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-count {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  padding: 0 12px;
}

.log-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  background: #0a0e27;
  
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.log-content {
  padding: 0 24px;
}

.log-line {
  display: flex;
  padding: 6px 0;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.2s ease;
  animation: fadeIn 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.03);
  }
  
  &.highlight {
    background: rgba(255, 193, 7, 0.15);
    
    :deep(mark) {
      background: #ffc107;
      color: #000;
      padding: 2px 4px;
      border-radius: 2px;
    }
  }
  
  &.step-header {
    color: #409eff;
    font-weight: 600;
    font-size: 14px;
    background: rgba(64, 158, 255, 0.1);
    padding: 10px 0;
    margin: 8px 0;
    border-left: 3px solid #409eff;
    padding-left: 10px;
  }
  
  &.error {
    color: #ff6b6b;
    background: rgba(255, 107, 107, 0.08);
  }
  
  &.warning {
    color: #ffd93d;
    background: rgba(255, 217, 61, 0.08);
  }
  
  &.info {
    color: #6bcf7f;
  }
  
  &.debug {
    color: rgba(255, 255, 255, 0.5);
  }
  
  &.connecting {
    color: #4dabf7;
    justify-content: center;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.line-number {
  display: inline-block;
  width: 50px;
  text-align: right;
  margin-right: 20px;
  color: rgba(255, 255, 255, 0.3);
  user-select: none;
  font-size: 12px;
}

.line-content {
  flex: 1;
  word-break: break-all;
  white-space: pre-wrap;
}

.log-footer {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.03);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
