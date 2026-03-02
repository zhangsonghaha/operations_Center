<template>
  <div class="app-container backup-center">
    <!-- 仪表盘区域 -->
    <div class="dashboard-section">
      <div class="dashboard-header">
        <div class="header-title">
          <el-icon class="title-icon"><DataLine /></el-icon>
          <span>备份管理中心</span>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus" @click="handleQuickBackup">
            快速备份
          </el-button>
          <el-button :icon="Refresh" @click="refreshAll" :loading="refreshing">
            刷新
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card primary">
          <div class="stat-visual">
            <div class="stat-ring">
              <el-icon><Collection /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalBackups }}</div>
            <div class="stat-label">总备份数</div>
            <div class="stat-trend">
              <span class="trend-up">+{{ stats.todayBackups }}</span>
              <span class="trend-label">今日新增</span>
            </div>
          </div>
        </div>

        <div class="stat-card success">
          <div class="stat-visual">
            <div class="stat-ring">
              <el-icon><CircleCheck /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.successRate }}%</div>
            <div class="stat-label">成功率</div>
            <div class="stat-trend">
              <el-progress :percentage="stats.successRate" :stroke-width="4" :show-text="false" />
            </div>
          </div>
        </div>

        <div class="stat-card warning">
          <div class="stat-visual">
            <div class="stat-ring">
              <el-icon><Timer /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeStrategies }}</div>
            <div class="stat-label">启用策略</div>
            <div class="stat-trend">
              <span class="text-muted">下次执行: {{ stats.nextBackupTime }}</span>
            </div>
          </div>
        </div>

        <div class="stat-card danger">
          <div class="stat-visual">
            <div class="stat-ring">
              <el-icon><Coin /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatStorage(stats.usedStorage) }}</div>
            <div class="stat-label">存储使用</div>
            <div class="stat-trend">
              <el-progress 
                :percentage="storagePercent" 
                :stroke-width="4" 
                :status="storagePercent > 80 ? 'exception' : ''"
                :show-text="false" 
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-grid">
        <div class="chart-panel">
          <div class="panel-header">
            <span class="panel-title">备份趋势</span>
            <el-radio-group v-model="trendPeriod" size="small">
              <el-radio-button label="7d">7天</el-radio-button>
              <el-radio-button label="30d">30天</el-radio-button>
              <el-radio-button label="90d">90天</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChart" class="chart-body"></div>
        </div>

        <div class="chart-panel">
          <div class="panel-header">
            <span class="panel-title">存储分布</span>
          </div>
          <div ref="storageChart" class="chart-body"></div>
        </div>
      </div>
    </div>

    <!-- 策略管理区域 -->
    <div class="strategy-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><Setting /></el-icon>
          <span>备份策略</span>
          <el-tag type="info" size="small">{{ strategyList.length }}个策略</el-tag>
        </div>
        <el-button type="primary" link :icon="Plus" @click="handleAddStrategy">
          新增策略
        </el-button>
      </div>

      <div class="strategy-grid">
        <div 
          v-for="strategy in strategyList" 
          :key="strategy.strategyId"
          class="strategy-card"
          :class="{ 'strategy-enabled': strategy.enabled === '0' }"
          @click="showStrategyDetail(strategy)"
        >
          <div class="strategy-header">
            <div class="strategy-icon" :class="`icon-${strategy.dbType}`">
              <el-icon><DataBase /></el-icon>
            </div>
            <div class="strategy-status">
              <el-switch 
                v-model="strategy.enabled" 
                active-value="0" 
                inactive-value="1"
                @click.stop
                @change="handleStrategyStatusChange(strategy)"
              />
            </div>
          </div>
          <div class="strategy-body">
            <div class="strategy-name">{{ strategy.strategyName }}</div>
            <div class="strategy-meta">
              <el-tag size="small" :type="getBackupModeType(strategy.backupMode)">
                {{ getBackupModeLabel(strategy.backupMode) }}
              </el-tag>
              <el-tag size="small" type="info">
                {{ getBackupLevelLabel(strategy.backupLevel) }}
              </el-tag>
            </div>
            <div class="strategy-schedule">
              <el-icon><Clock /></el-icon>
              <span>{{ strategy.cronExpression }}</span>
            </div>
          </div>
          <div class="strategy-footer">
            <div class="strategy-stats">
              <span>保留: {{ strategy.retentionDays }}天</span>
              <span v-if="strategy.compressEnabled === '1'">压缩: {{ strategy.compressType }}</span>
            </div>
            <div class="strategy-actions">
              <el-button 
                type="primary" 
                link 
                size="small" 
                :icon="VideoPlay"
                @click.stop="handleExecuteStrategy(strategy)"
              >
                执行
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 备份记录区域 -->
    <div class="backup-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><FolderOpened /></el-icon>
          <span>备份记录</span>
        </div>
        <div class="section-filters">
          <el-select v-model="filterStrategy" placeholder="全部策略" clearable size="small" style="width: 150px">
            <el-option label="手动备份" value="manual" />
            <el-option v-for="s in strategyList" :key="s.strategyId" :label="s.strategyName" :value="s.strategyId" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="全部状态" clearable size="small" style="width: 120px">
            <el-option label="成功" value="0" />
            <el-option label="失败" value="1" />
          </el-select>
          <el-button :icon="Delete" size="small" @click="handleCleanExpired">
            清理过期
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="filteredBackupList" stripe>
        <el-table-column type="selection" width="50" />
        <el-table-column label="备份文件" min-width="250">
          <template #default="scope">
            <div class="backup-file">
              <el-icon class="file-icon" :size="20"><Document /></el-icon>
              <div class="file-info">
                <div class="file-name">{{ scope.row.fileName }}</div>
                <div class="file-meta">
                  <el-tag size="small" :type="scope.row.backupType === '0' ? 'info' : 'success'">
                    {{ scope.row.backupType === '0' ? '手动' : '自动' }}
                  </el-tag>
                  <span v-if="scope.row.strategyId" class="strategy-tag">
                    {{ getStrategyName(scope.row.strategyId) }}
                  </span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数据库" width="150">
          <template #default="scope">
            <div class="db-info">
              <el-icon><DataBase /></el-icon>
              <span>{{ getConnName(scope.row.connId) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="scope">
            <span class="file-size">{{ formatFileSize(scope.row.fileSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <div class="backup-status">
              <el-icon v-if="scope.row.status === '0'" class="status-success"><CircleCheck /></el-icon>
              <el-icon v-else class="status-error"><CircleClose /></el-icon>
              <span>{{ scope.row.status === '0' ? '成功' : '失败' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="scope">
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Download" @click="handleDownload(scope.row)">
              下载
            </el-button>
            <el-button link type="warning" :icon="RefreshLeft" @click="handleRestore(scope.row)">
              恢复
            </el-button>
            <el-dropdown trigger="click">
              <el-button link type="primary">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :icon="View" @click="handleVerify(scope.row)">
                    验证完整性
                  </el-dropdown-item>
                  <el-dropdown-item :icon="Delete" divided @click="handleDelete(scope.row)">
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <!-- 快速备份对话框 -->
    <el-dialog title="快速备份" v-model="quickBackupOpen" width="600px" append-to-body>
      <el-steps :active="quickBackupStep" finish-status="success" simple>
        <el-step title="选择策略" />
        <el-step title="确认执行" />
      </el-steps>

      <div v-if="quickBackupStep === 0" class="quick-backup-step">
        <div class="strategy-select-hint">选择要执行的备份策略，或创建临时备份</div>
        <div class="strategy-options">
          <div 
            v-for="strategy in enabledStrategies" 
            :key="strategy.strategyId"
            class="strategy-option"
            :class="{ active: selectedStrategy?.strategyId === strategy.strategyId }"
            @click="selectedStrategy = strategy"
          >
            <div class="option-icon">
              <el-icon><DataBase /></el-icon>
            </div>
            <div class="option-info">
              <div class="option-name">{{ strategy.strategyName }}</div>
              <div class="option-desc">
                {{ getBackupModeLabel(strategy.backupMode) }} · 
                {{ getBackupLevelLabel(strategy.backupLevel) }} · 
                {{ strategy.cronExpression }}
              </div>
            </div>
          </div>
          <div 
            class="strategy-option manual-option"
            :class="{ active: selectedStrategy === null }"
            @click="selectedStrategy = null"
          >
            <div class="option-icon">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="option-info">
              <div class="option-name">临时备份</div>
              <div class="option-desc">不保存为策略，仅执行一次</div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="quick-backup-step">
        <el-alert
          v-if="selectedStrategy"
          :title="'将使用策略 [' + selectedStrategy.strategyName + '] 执行备份'"
          type="info"
          :closable="false"
          show-icon
        />
        <el-alert
          v-else
          title="将创建临时备份，使用默认配置"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-form :model="quickBackupForm" label-width="100px" style="margin-top: 20px">
          <el-form-item label="目标连接">
            <el-select v-model="quickBackupForm.connId" placeholder="请选择" style="width: 100%">
              <el-option v-for="conn in connList" :key="conn.connId" :label="conn.connName" :value="conn.connId" />
            </el-select>
          </el-form-item>
          <el-form-item label="备份级别" v-if="!selectedStrategy">
            <el-radio-group v-model="quickBackupForm.backupLevel">
              <el-radio-button label="instance">实例级</el-radio-button>
              <el-radio-button label="database">数据库级</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button v-if="quickBackupStep > 0" @click="quickBackupStep--">上一步</el-button>
        <el-button v-if="quickBackupStep < 1" type="primary" @click="quickBackupStep++" :disabled="!selectedStrategy && quickBackupStep === 0">
          下一步
        </el-button>
        <el-button v-else type="primary" @click="submitQuickBackup" :loading="backupLoading">
          开始备份
        </el-button>
      </template>
    </el-dialog>

    <!-- 策略详情抽屉 -->
    <el-drawer v-model="strategyDetailOpen" :title="currentStrategy?.strategyName" size="600px">
      <div v-if="currentStrategy" class="strategy-detail">
        <div class="detail-section">
          <div class="detail-title">基本信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="数据库类型">{{ currentStrategy.dbType }}</el-descriptions-item>
            <el-descriptions-item label="备份方式">{{ getBackupModeLabel(currentStrategy.backupMode) }}</el-descriptions-item>
            <el-descriptions-item label="备份级别">{{ getBackupLevelLabel(currentStrategy.backupLevel) }}</el-descriptions-item>
            <el-descriptions-item label="定时表达式">{{ currentStrategy.cronExpression }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="detail-title">保留策略</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="保留天数">{{ currentStrategy.retentionDays }}天</el-descriptions-item>
            <el-descriptions-item label="保留数量">{{ currentStrategy.retentionCount }}个</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="detail-title">存储配置</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="存储类型">{{ currentStrategy.storageType }}</el-descriptions-item>
            <el-descriptions-item label="压缩">{{ currentStrategy.compressEnabled === '1' ? currentStrategy.compressType : '不压缩' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="detail-title">
            关联备份记录
            <el-link type="primary" @click="filterByStrategy(currentStrategy.strategyId)">查看全部</el-link>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="backup in strategyBackups.slice(0, 5)"
              :key="backup.backupId"
              :type="backup.status === '0' ? 'success' : 'danger'"
              :timestamp="parseTime(backup.createTime)"
            >
              <div class="timeline-content">
                <span>{{ backup.fileName }}</span>
                <el-tag size="small" :type="backup.status === '0' ? 'success' : 'danger'">
                  {{ backup.status === '0' ? '成功' : '失败' }}
                </el-tag>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-drawer>

    <!-- 恢复向导对话框 -->
    <RestoreWizard
      v-model="restoreOpen"
      :backup="currentBackup"
      :connections="connList"
      @success="getList"
    />

    <!-- 新增策略对话框 -->
    <el-dialog :title="strategyDialogTitle" v-model="strategyDialogOpen" width="700px" append-to-body>
      <el-form ref="strategyFormRef" :model="strategyForm" :rules="strategyRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="策略名称" prop="strategyName">
              <el-input v-model="strategyForm.strategyName" placeholder="请输入策略名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择连接" prop="connId">
              <el-select v-model="strategyForm.connId" placeholder="请选择数据库连接" style="width: 100%">
                <el-option v-for="item in connList" :key="item.connId" :label="item.connName" :value="item.connId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据库类型" prop="dbType">
              <el-select v-model="strategyForm.dbType" placeholder="请选择" style="width: 100%">
                <el-option label="MySQL/MariaDB" value="mysql" />
                <el-option label="PostgreSQL" value="postgresql" />
                <el-option label="MongoDB" value="mongodb" />
                <el-option label="Redis" value="redis" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备份方式" prop="backupMode">
              <el-select v-model="strategyForm.backupMode" placeholder="请选择" style="width: 100%">
                <el-option label="全量备份" value="full" />
                <el-option label="增量备份" value="incremental" />
                <el-option label="差异备份" value="differential" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="备份级别" prop="backupLevel">
              <el-select v-model="strategyForm.backupLevel" placeholder="请选择" style="width: 100%">
                <el-option label="实例级" value="instance" />
                <el-option label="数据库级" value="database" />
                <el-option label="表级" value="table" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备份目标" prop="targetName">
              <el-input v-model="strategyForm.targetName" placeholder="数据库名/表名，多个用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>定时配置</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Cron表达式" prop="cronExpression">
              <el-input v-model="strategyForm.cronExpression" placeholder="如: 0 0 2 * * ?" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="策略状态">
              <el-radio-group v-model="strategyForm.enabled">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>保留策略</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保留天数">
              <el-input-number v-model="strategyForm.retentionDays" :min="1" :max="365" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保留数量">
              <el-input-number v-model="strategyForm.retentionCount" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>高级选项</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用压缩">
              <el-switch v-model="strategyForm.compressEnabled" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="压缩类型" v-if="strategyForm.compressEnabled === '1'">
              <el-select v-model="strategyForm.compressType" placeholder="请选择" style="width: 100%">
                <el-option label="Gzip" value="gzip" />
                <el-option label="Bzip2" value="bzip2" />
                <el-option label="Zip" value="zip" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存储类型" prop="storageType">
              <el-select v-model="strategyForm.storageType" placeholder="请选择" style="width: 100%">
                <el-option label="本地存储" value="local" />
                <el-option label="FTP服务器" value="ftp" />
                <el-option label="SFTP服务器" value="sftp" />
                <el-option label="阿里云OSS" value="aliyun_oss" />
                <el-option label="腾讯云COS" value="tencent_cos" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用告警">
              <el-switch v-model="strategyForm.alertEnabled" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="strategyForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="strategyDialogOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitStrategyForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BackupCenter">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  Plus, Refresh, DataLine, Collection, CircleCheck, Timer, Coin,
  Setting, FolderOpened, Coin as DataBase, Clock, Document, Download,
  RefreshLeft, View, Delete, More, VideoPlay
} from '@element-plus/icons-vue'
import {
  listBackup, delBackup, backupWithOptions, listStrategy, executeStrategy as apiExecuteStrategy,
  updateStrategy, addStrategy, listConn, cleanExpired, verifyBackup
} from '@/api/system/db'
import RestoreWizard from './components/RestoreWizard.vue'

const { proxy } = getCurrentInstance()

// 统计数据
const stats = ref({
  totalBackups: 0,
  todayBackups: 0,
  successRate: 98,
  activeStrategies: 0,
  nextBackupTime: '--',
  usedStorage: 0,
  totalStorage: 107374182400 // 100GB
})
const storagePercent = computed(() => Math.round((stats.value.usedStorage / stats.value.totalStorage) * 100))

// 策略列表
const strategyList = ref([])
const enabledStrategies = computed(() => strategyList.value.filter(s => s.enabled === '0'))

// 备份列表
const loading = ref(false)
const backupList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10
})
const filterStrategy = ref('')
const filterStatus = ref('')

const filteredBackupList = computed(() => {
  let list = backupList.value
  if (filterStrategy.value) {
    if (filterStrategy.value === 'manual') {
      list = list.filter(b => !b.strategyId)
    } else {
      list = list.filter(b => b.strategyId === filterStrategy.value)
    }
  }
  if (filterStatus.value !== '') {
    list = list.filter(b => b.status === filterStatus.value)
  }
  return list
})

// 连接列表
const connList = ref([])

// 图表
const trendChart = ref(null)
const storageChart = ref(null)
let trendChartInstance = null
let storageChartInstance = null
const trendPeriod = ref('7d')

// 快速备份
const quickBackupOpen = ref(false)
const quickBackupStep = ref(0)
const selectedStrategy = ref(null)
const quickBackupForm = ref({
  connId: null,
  backupLevel: 'instance'
})
const backupLoading = ref(false)

// 策略详情
const strategyDetailOpen = ref(false)
const currentStrategy = ref(null)
const strategyBackups = ref([])

// 恢复
const restoreOpen = ref(false)
const currentBackup = ref(null)

// 刷新状态
const refreshing = ref(false)

// 获取统计数据
function getStats() {
  // 计算统计数据
  stats.value.totalBackups = backupList.value.length
  const today = new Date().toDateString()
  stats.value.todayBackups = backupList.value.filter(b => 
    new Date(b.createTime).toDateString() === today
  ).length
  
  const successCount = backupList.value.filter(b => b.status === '0').length
  stats.value.successRate = backupList.value.length > 0 
    ? Math.round((successCount / backupList.value.length) * 100) 
    : 100
  
  stats.value.activeStrategies = strategyList.value.filter(s => s.enabled === '0').length
  
  const totalSize = backupList.value.reduce((sum, b) => sum + (b.fileSize || 0), 0)
  stats.value.usedStorage = totalSize
}

// 获取策略列表
function getStrategyList() {
  listStrategy({ pageSize: 100 }).then(response => {
    strategyList.value = response.rows || []
    getStats()
  })
}

// 获取备份列表
function getList() {
  loading.value = true
  listBackup(queryParams.value).then(response => {
    backupList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
    getStats()
    updateCharts()
  })
}

// 获取连接列表
function getConnList() {
  listConn().then(response => {
    connList.value = response.rows || []
  })
}

// 刷新全部
function refreshAll() {
  refreshing.value = true
  Promise.all([getStrategyList(), getList()]).finally(() => {
    refreshing.value = false
  })
}

// 快速备份
function handleQuickBackup() {
  quickBackupOpen.value = true
  quickBackupStep.value = 0
  selectedStrategy.value = null
  quickBackupForm.value = { connId: null, backupLevel: 'instance' }
}

function submitQuickBackup() {
  backupLoading.value = true
  
  const params = selectedStrategy.value ? {
    connId: quickBackupForm.value.connId || selectedStrategy.value.connId,
    dbType: selectedStrategy.value.dbType,
    backupMode: selectedStrategy.value.backupMode,
    backupLevel: selectedStrategy.value.backupLevel,
    targetName: selectedStrategy.value.targetName,
    storageType: selectedStrategy.value.storageType,
    compressEnabled: selectedStrategy.value.compressEnabled,
    strategyId: selectedStrategy.value.strategyId
  } : {
    connId: quickBackupForm.value.connId,
    dbType: 'mysql',
    backupMode: 'full',
    backupLevel: quickBackupForm.value.backupLevel,
    targetName: '',
    storageType: 'local',
    compressEnabled: '1'
  }
  
  backupWithOptions(params).then(() => {
    proxy.$modal.msgSuccess('备份任务已启动')
    quickBackupOpen.value = false
    getList()
  }).finally(() => {
    backupLoading.value = false
  })
}

// 策略相关
const strategyDialogOpen = ref(false)
const strategyDialogTitle = ref('新增策略')
const strategyFormRef = ref(null)
const strategyForm = ref({
  strategyName: '',
  connId: null,
  dbType: 'mysql',
  backupMode: 'full',
  backupLevel: 'database',
  targetName: '',
  cronExpression: '0 0 2 * * ?',
  enabled: '0',
  retentionDays: 7,
  retentionCount: 10,
  compressEnabled: '1',
  compressType: 'gzip',
  storageType: 'local',
  alertEnabled: '0',
  remark: ''
})
const strategyRules = {
  strategyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  connId: [{ required: true, message: '请选择连接', trigger: 'change' }],
  dbType: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  backupMode: [{ required: true, message: '请选择备份方式', trigger: 'change' }],
  backupLevel: [{ required: true, message: '请选择备份级别', trigger: 'change' }],
  cronExpression: [{ required: true, message: '请输入定时表达式', trigger: 'blur' }],
  storageType: [{ required: true, message: '请选择存储类型', trigger: 'change' }]
}

function handleAddStrategy() {
  strategyDialogTitle.value = '新增策略'
  strategyForm.value = {
    strategyName: '',
    connId: null,
    dbType: 'mysql',
    backupMode: 'full',
    backupLevel: 'database',
    targetName: '',
    cronExpression: '0 0 2 * * ?',
    enabled: '0',
    retentionDays: 7,
    retentionCount: 10,
    compressEnabled: '1',
    compressType: 'gzip',
    storageType: 'local',
    alertEnabled: '0',
    remark: ''
  }
  strategyDialogOpen.value = true
}

function submitStrategyForm() {
  strategyFormRef.value.validate(valid => {
    if (valid) {
      addStrategy(strategyForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        strategyDialogOpen.value = false
        getStrategyList()
      })
    }
  })
}

function showStrategyDetail(strategy) {
  currentStrategy.value = strategy
  strategyBackups.value = backupList.value.filter(b => b.strategyId === strategy.strategyId)
  strategyDetailOpen.value = true
}

function handleStrategyStatusChange(strategy) {
  updateStrategy(strategy).then(() => {
    proxy.$modal.msgSuccess('状态更新成功')
  })
}

function handleExecuteStrategy(strategy) {
  proxy.$modal.confirm(`确定要立即执行策略 "${strategy.strategyName}" 吗？`).then(() => {
    apiExecuteStrategy(strategy.strategyId).then(() => {
      proxy.$modal.msgSuccess('备份任务已启动')
      setTimeout(getList, 3000)
    })
  })
}

function filterByStrategy(strategyId) {
  filterStrategy.value = strategyId
  strategyDetailOpen.value = false
}

// 备份操作
function handleDownload(row) {
  const resource = '/profile/backup/' + row.fileName
  proxy.$download.resource(resource)
}

function handleRestore(row) {
  currentBackup.value = row
  restoreOpen.value = true
}

function handleVerify(row) {
  proxy.$modal.loading('正在验证备份文件...')
  verifyBackup(row.backupId).then(() => {
    proxy.$modal.closeLoading()
    proxy.$modal.msgSuccess('验证成功')
  }).catch(() => {
    proxy.$modal.closeLoading()
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该备份记录？').then(() => {
    delBackup(row.backupId).then(() => {
      proxy.$modal.msgSuccess('删除成功')
      getList()
    })
  })
}

function handleCleanExpired() {
  proxy.$modal.prompt('请输入保留天数', '清理过期备份', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[1-9]\d*$/,
    inputErrorMessage: '请输入有效的天数'
  }).then(({ value }) => {
    cleanExpired({ retentionDays: parseInt(value) }).then(response => {
      proxy.$modal.msgSuccess(response.msg)
      getList()
    })
  })
}

// 图表初始化
function initCharts() {
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
  }
  if (storageChart.value) {
    storageChartInstance = echarts.init(storageChart.value)
  }
  updateCharts()
}

function updateCharts() {
  if (!trendChartInstance || !storageChartInstance) return
  
  // 备份趋势图
  const dates = []
  const successData = []
  const failData = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const dateStr = date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    dates.push(dateStr)
    
    const dayBackups = backupList.value.filter(b => {
      const backupDate = new Date(b.createTime)
      return backupDate.toDateString() === date.toDateString()
    })
    successData.push(dayBackups.filter(b => b.status === '0').length)
    failData.push(dayBackups.filter(b => b.status === '1').length)
  }
  
  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['成功', '失败'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '成功', type: 'bar', data: successData, itemStyle: { color: '#67c23a' } },
      { name: '失败', type: 'bar', data: failData, itemStyle: { color: '#f56c6c' } }
    ]
  })
  
  // 存储分布图
  const storageByStrategy = {}
  backupList.value.forEach(b => {
    const key = b.strategyId ? getStrategyName(b.strategyId) : '手动备份'
    storageByStrategy[key] = (storageByStrategy[key] || 0) + (b.fileSize || 0)
  })
  
  storageChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      data: Object.entries(storageByStrategy).map(([name, value]) => ({ name, value })),
      label: { show: false }
    }]
  })
}

// 工具函数
function getStrategyName(strategyId) {
  const strategy = strategyList.value.find(s => s.strategyId === strategyId)
  return strategy ? strategy.strategyName : '未知策略'
}

function getConnName(connId) {
  const conn = connList.value.find(c => c.connId === connId)
  return conn ? conn.connName : connId
}

function getBackupModeLabel(mode) {
  const map = { full: '全量', incremental: '增量', differential: '差异' }
  return map[mode] || mode
}

function getBackupModeType(mode) {
  const map = { full: 'primary', incremental: 'success', differential: 'warning' }
  return map[mode] || ''
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

function formatStorage(bytes) {
  return formatFileSize(bytes)
}

// 监听趋势周期变化
watch(trendPeriod, () => {
  updateCharts()
})

onMounted(() => {
  getStrategyList()
  getList()
  getConnList()
  nextTick(() => {
    initCharts()
  })
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
    storageChartInstance?.resize()
  })
})

onUnmounted(() => {
  trendChartInstance?.dispose()
  storageChartInstance?.dispose()
})
</script>

<style scoped>
.backup-center {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 84px);
}

/* 仪表盘区域 */
.dashboard-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.title-icon {
  font-size: 24px;
  color: #409eff;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card.primary { border-left: 4px solid #409eff; }
.stat-card.success { border-left: 4px solid #67c23a; }
.stat-card.warning { border-left: 4px solid #e6a23c; }
.stat-card.danger { border-left: 4px solid #f56c6c; }

.stat-visual {
  margin-right: 16px;
}

.stat-ring {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  font-size: 24px;
}

.stat-card.success .stat-ring { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
.stat-card.warning .stat-ring { background: rgba(230, 162, 60, 0.1); color: #e6a23c; }
.stat-card.danger .stat-ring { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  margin-top: 8px;
}

.trend-up {
  color: #67c23a;
  font-weight: 600;
}

.trend-label {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-panel {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-title {
  font-weight: 600;
  color: #303133;
}

.chart-body {
  height: 240px;
}

/* 策略区域 */
.strategy-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.strategy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.strategy-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.strategy-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.strategy-enabled {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);
}

.strategy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.strategy-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  color: #fff;
  font-size: 20px;
}

.strategy-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.strategy-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.strategy-schedule {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

.strategy-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.strategy-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

/* 备份记录区域 */
.backup-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-filters {
  display: flex;
  gap: 12px;
}

.backup-file {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  color: #409eff;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-weight: 500;
  color: #303133;
}

.file-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.strategy-tag {
  font-size: 12px;
  color: #409eff;
}

.db-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.file-size {
  color: #606266;
  font-weight: 500;
}

.backup-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-success {
  color: #67c23a;
}

.status-error {
  color: #f56c6c;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

/* 快速备份对话框 */
.quick-backup-step {
  padding: 20px 0;
}

.strategy-select-hint {
  color: #909399;
  margin-bottom: 16px;
  text-align: center;
}

.strategy-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.strategy-option {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.strategy-option:hover {
  background: #ecf5ff;
}

.strategy-option.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.option-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.manual-option .option-icon {
  background: #67c23a;
}

.option-name {
  font-weight: 600;
  color: #303133;
}

.option-desc {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

/* 策略详情 */
.strategy-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .strategy-grid {
    grid-template-columns: 1fr;
  }
}
</style>
