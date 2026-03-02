<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="恢复向导"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="!restoring"
    :show-close="!restoring"
    class="restore-wizard"
    append-to-body
  >
    <el-steps :active="currentStep" finish-status="success" simple>
      <el-step title="选择目标" />
      <el-step title="恢复选项" />
      <el-step title="执行恢复" />
    </el-steps>

    <!-- 步骤1: 选择恢复目标 -->
    <div v-if="currentStep === 0" class="wizard-step">
      <el-alert
        title="恢复操作将覆盖目标数据库中的数据，请谨慎操作！"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <div class="backup-info-card">
        <div class="info-header">
          <el-icon class="info-icon" :size="32"><DocumentChecked /></el-icon>
          <div class="info-title">备份文件信息</div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文件名">{{ backup?.fileName }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(backup?.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="数据库">{{ getConnName(backup?.connId) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ parseTime(backup?.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备份方式" :span="2">
            <el-tag>{{ getBackupModeLabel(backup?.backupMode) }}</el-tag>
            <el-tag type="info" style="margin-left: 8px">{{ getBackupLevelLabel(backup?.backupLevel) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form ref="targetFormRef" :model="targetForm" :rules="targetRules" label-width="120px">
        <el-form-item label="目标连接" prop="targetConnId">
          <el-select v-model="targetForm.targetConnId" placeholder="请选择恢复目标" style="width: 100%">
            <el-option
              v-for="conn in connections"
              :key="conn.connId"
              :label="conn.connName + ' (' + conn.host + '/' + conn.dbName + ')'"
              :value="conn.connId"
            />
          </el-select>
          <div class="form-tip">
            <el-icon><Warning /></el-icon>
            <span>恢复操作将覆盖目标数据库中的同名表数据</span>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 步骤2: 恢复选项 -->
    <div v-if="currentStep === 1" class="wizard-step">
      <el-form :model="restoreOptions" label-width="140px">
        <el-divider>恢复范围</el-divider>
        <el-form-item label="恢复模式">
          <el-radio-group v-model="restoreOptions.restoreMode">
            <el-radio-button label="full">完整恢复</el-radio-button>
            <el-radio-button label="partial">部分恢复</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="对象选择" v-if="restoreOptions.restoreMode === 'partial'">
          <el-transfer
            v-model="restoreOptions.selectedObjects"
            :data="availableObjects"
            :titles="['备份中的对象', '要恢复的对象']"
            filterable
          />
        </el-form-item>

        <el-divider>冲突处理</el-divider>
        <el-form-item label="表已存在时">
          <el-radio-group v-model="restoreOptions.conflictAction">
            <el-radio label="drop">删除后重建</el-radio>
            <el-radio label="truncate">清空后插入</el-radio>
            <el-radio label="skip">跳过</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider>高级选项</el-divider>
        <el-form-item>
          <el-checkbox v-model="restoreOptions.disableKeys">禁用外键检查</el-checkbox>
          <el-checkbox v-model="restoreOptions.disableTriggers">禁用触发器</el-checkbox>
          <el-checkbox v-model="restoreOptions.verifyFirst">恢复前验证备份</el-checkbox>
        </el-form-item>
      </el-form>
    </div>

    <!-- 步骤3: 执行恢复 -->
    <div v-if="currentStep === 2" class="wizard-step">
      <!-- 状态卡片 -->
      <div class="status-cards">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="status-card" :class="restoreStatus">
              <el-icon class="status-icon" :size="40">
                <Loading v-if="restoreStatus === 'running'" class="rotating" />
                <CircleCheck v-else-if="restoreStatus === 'success'" />
                <CircleClose v-else-if="restoreStatus === 'error'" />
                <Timer v-else />
              </el-icon>
              <div class="status-title">{{ restoreStatusText }}</div>
              <div class="status-desc">{{ restoreStepText }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="status-card info">
              <div class="status-value">{{ restoreProgress }}%</div>
              <div class="status-label">恢复进度</div>
              <el-progress 
                :percentage="restoreProgress" 
                :status="restoreStatus === 'error' ? 'exception' : ''"
                :stroke-width="8"
                class="progress-bar"
              />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="status-card info">
              <div class="status-value">{{ tableProgress }}</div>
              <div class="status-label">表进度</div>
              <div class="status-desc">{{ currentTable }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 恢复日志 -->
      <div class="log-section">
        <div class="log-header">
          <span class="log-title">
            <el-icon><Document /></el-icon>
            恢复日志
            <el-tag v-if="restoring" type="danger" size="small" effect="dark">实时</el-tag>
          </span>
          <el-button v-if="logs.length > 0" link size="small" @click="clearLogs">
            清空
          </el-button>
        </div>
        <div ref="logContainer" class="log-container">
          <div v-if="logs.length === 0" class="log-empty">
            <el-empty description="等待恢复开始..." :image-size="60" />
          </div>
          <div
            v-for="(log, index) in logs"
            :key="index"
            class="log-line"
            :class="'log-' + log.type"
          >
            <span class="log-time">{{ log.time }}</span>
            <span class="log-type">[{{ log.type.toUpperCase() }}]</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button v-if="currentStep > 0 && !restoring" @click="currentStep--">上一步</el-button>
      <el-button v-if="currentStep < 2 && !restoring" type="primary" @click="nextStep">
        下一步
      </el-button>
      <el-button 
        v-if="currentStep === 2 && !restoring && restoreStatus !== 'success'" 
        type="primary" 
        @click="startRestore"
        :loading="starting"
      >
        开始恢复
      </el-button>
      <el-button 
        v-if="restoreStatus === 'success'" 
        type="primary" 
        @click="closeDialog"
      >
        完成
      </el-button>
      <el-button 
        v-if="restoring" 
        type="danger" 
        @click="cancelRestore"
      >
        取消
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import {
  DocumentChecked, Warning, Loading, CircleCheck, CircleClose,
  Timer, Document
} from '@element-plus/icons-vue'
import { restoreBackup, getRestoreProgress } from '@/api/system/db'

const props = defineProps({
  modelValue: Boolean,
  backup: Object,
  connections: Array
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()

// 步骤控制
const currentStep = ref(0)
const targetFormRef = ref(null)
const targetForm = ref({
  targetConnId: null
})
const targetRules = {
  targetConnId: [{ required: true, message: '请选择目标连接', trigger: 'change' }]
}

// 恢复选项
const restoreOptions = ref({
  restoreMode: 'full',
  selectedObjects: [],
  conflictAction: 'drop',
  disableKeys: true,
  disableTriggers: false,
  verifyFirst: true
})

const availableObjects = ref([])

// 恢复状态
const restoring = ref(false)
const starting = ref(false)
const restoreStatus = ref('waiting') // waiting, running, success, error
const restoreStatusText = ref('准备中')
const restoreStepText = ref('等待开始')
const restoreProgress = ref(0)
const tableProgress = ref('0/0')
const currentTable = ref('--')
const taskId = ref('')

// 日志
const logs = ref([])
const logContainer = ref(null)

// 轮询定时器
let pollTimer = null

function nextStep() {
  if (currentStep.value === 0) {
    targetFormRef.value.validate(valid => {
      if (valid) currentStep.value++
    })
  } else {
    currentStep.value++
  }
}

function startRestore() {
  starting.value = true
  restoreStatus.value = 'running'
  restoreStatusText.value = '恢复中'
  restoring.value = true
  
  restoreBackup({
    backupId: props.backup.backupId,
    targetConnId: targetForm.value.targetConnId,
    options: restoreOptions.value
  }).then(response => {
    taskId.value = response.data
    addLog('info', '恢复任务已启动，任务ID: ' + taskId.value)
    starting.value = false
    pollProgress()
  }).catch(error => {
    restoreStatus.value = 'error'
    restoreStatusText.value = '启动失败'
    addLog('error', '启动恢复任务失败: ' + error.message)
    restoring.value = false
    starting.value = false
  })
}

function pollProgress() {
  if (!restoring.value) return
  
  getRestoreProgress(taskId.value).then(response => {
    const progress = response.data
    if (!progress) return
    
    restoreStatus.value = progress.status === 'completed' ? 'success' : 
                         progress.status === 'failed' ? 'error' : 'running'
    
    if (progress.status === 'running') {
      restoreProgress.value = progress.progress || 0
      restoreStepText.value = progress.currentStep || '正在恢复...'
    }
    
    if (progress.totalTables) {
      const completed = Math.min(progress.completedTables || 0, progress.totalTables)
      tableProgress.value = `${completed}/${progress.totalTables}`
      currentTable.value = progress.currentTable || '--'
    }
    
    if (progress.logs && progress.logs.length > 0) {
      progress.logs.forEach(log => {
        const match = log.match(/^\[(\w+)\]\s*(.+)$/)
        if (match) {
          addLog(match[1].toLowerCase(), match[2])
        } else {
          addLog('info', log)
        }
      })
    }
    
    if (progress.status === 'completed') {
      restoreStatus.value = 'success'
      restoreStatusText.value = '恢复成功'
      restoreStepText.value = '数据恢复完成'
      restoreProgress.value = 100
      restoring.value = false
      addLog('success', '恢复完成！')
      emit('success')
      return
    }
    
    if (progress.status === 'failed') {
      restoreStatus.value = 'error'
      restoreStatusText.value = '恢复失败'
      restoreStepText.value = progress.errorMessage || '恢复过程中发生错误'
      restoring.value = false
      addLog('error', '恢复失败: ' + (progress.errorMessage || '未知错误'))
      return
    }
    
    pollTimer = setTimeout(pollProgress, 1000)
  }).catch(() => {
    if (restoring.value) {
      pollTimer = setTimeout(pollProgress, 2000)
    }
  })
}

function addLog(type, message) {
  const now = new Date()
  const time = now.toLocaleTimeString('zh-CN', { hour12: false })
  logs.value.push({ time, type, message })
  
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

function clearLogs() {
  logs.value = []
}

function cancelRestore() {
  proxy.$modal.confirm('确定要取消恢复操作吗？已恢复的数据不会回滚。').then(() => {
    if (pollTimer) {
      clearTimeout(pollTimer)
      pollTimer = null
    }
    restoring.value = false
    restoreStatus.value = 'error'
    restoreStatusText.value = '已取消'
    addLog('warning', '用户取消了恢复操作')
  })
}

function closeDialog() {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
  emit('update:modelValue', false)
  resetState()
}

function resetState() {
  currentStep.value = 0
  targetForm.value = { targetConnId: null }
  restoreOptions.value = {
    restoreMode: 'full',
    selectedObjects: [],
    conflictAction: 'drop',
    disableKeys: true,
    disableTriggers: false,
    verifyFirst: true
  }
  restoring.value = false
  starting.value = false
  restoreStatus.value = 'waiting'
  restoreStatusText.value = '准备中'
  restoreStepText.value = '等待开始'
  restoreProgress.value = 0
  tableProgress.value = '0/0'
  currentTable.value = '--'
  logs.value = []
  taskId.value = ''
}

// 工具函数
function getConnName(connId) {
  const conn = props.connections.find(c => c.connId === connId)
  return conn ? conn.connName : connId
}

function getBackupModeLabel(mode) {
  const map = { full: '全量', incremental: '增量', differential: '差异' }
  return map[mode] || mode
}

function getBackupLevelLabel(level) {
  const map = { instance: '实例级', database: '数据库级', table: '表级' }
  return map[level] || level
}

function formatFileSize(size) {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(2) + ' ' + units[i]
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val) {
    resetState()
  }
})
</script>

<style scoped>
.wizard-step {
  padding: 20px 0;
}

.backup-info-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.info-icon {
  color: #409eff;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e6a23c;
  font-size: 13px;
}

/* 状态卡片 */
.status-cards {
  margin-bottom: 20px;
}

.status-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.status-card.running {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
}

.status-card.success {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
}

.status-card.error {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
}

.status-icon {
  color: #409eff;
  margin-bottom: 12px;
}

.status-card.success .status-icon {
  color: #67c23a;
}

.status-card.error .status-icon {
  color: #f56c6c;
}

.rotating {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.status-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 13px;
  color: #909399;
}

.status-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.status-card.success .status-value {
  color: #67c23a;
}

.status-card.error .status-value {
  color: #f56c6c;
}

.status-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.progress-bar {
  margin-top: 8px;
}

/* 日志区域 */
.log-section {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.log-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.log-container {
  height: 250px;
  overflow-y: auto;
  background: #1e1e1e;
  padding: 12px 16px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.log-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.log-line {
  display: flex;
  gap: 12px;
  margin-bottom: 4px;
}

.log-time {
  color: #6c757d;
  flex-shrink: 0;
}

.log-type {
  flex-shrink: 0;
  font-weight: 600;
}

.log-info .log-type { color: #409eff; }
.log-success .log-type { color: #67c23a; }
.log-warning .log-type { color: #e6a23c; }
.log-error .log-type { color: #f56c6c; }

.log-message {
  color: #e8e8e8;
}

.log-success .log-message { color: #67c23a; }
.log-warning .log-message { color: #e6a23c; }
.log-error .log-message { color: #f56c6c; }
</style>
