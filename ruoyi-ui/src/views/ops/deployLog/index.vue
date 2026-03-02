<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="应用名称" prop="appName">
        <el-input
          v-model="queryParams.appName"
          placeholder="请输入应用名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="部署类型" prop="deployType">
        <el-select v-model="queryParams.deployType" placeholder="请选择部署类型" clearable>
          <el-option label="部署" value="deploy" />
          <el-option label="启动" value="start" />
          <el-option label="停止" value="stop" />
          <el-option label="重启" value="restart" />
        </el-select>
      </el-form-item>
      <el-form-item label="部署状态" prop="deployStatus">
        <el-select v-model="queryParams.deployStatus" placeholder="请选择部署状态" clearable>
          <el-option label="进行中" value="running" />
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:deployLog:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deployLogList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志ID" align="center" prop="logId" width="80" />
      <el-table-column label="应用名称" align="center" prop="appName" :show-overflow-tooltip="true" />
      <el-table-column label="服务器" align="center" prop="serverName" :show-overflow-tooltip="true" />
      <el-table-column label="部署类型" align="center" prop="deployType" width="100">
        <template #default="scope">
          <el-tag :type="getDeployTypeTag(scope.row.deployType)" size="small">
            {{ getDeployTypeName(scope.row.deployType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="部署状态" align="center" prop="deployStatus" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.deployStatus)" size="small">
            {{ getStatusName(scope.row.deployStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="执行人" align="center" prop="executor" width="100" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template #default="scope">
          <span>{{ scope.row.endTime ? parseTime(scope.row.endTime) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handleViewLog(scope.row)"
            v-hasPermi="['ops:deployLog:query']"
          >查看日志</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:deployLog:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 日志查看对话框 -->
    <el-dialog 
      v-model="logDialogVisible" 
      :title="`部署日志 - ${currentLog.appName}`"
      width="80%"
      top="5vh"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <DeployLogViewer v-if="logDialogVisible" :logId="currentLog.logId" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { listDeployLog, delDeployLog } from '@/api/ops/deployLog'
import { ElMessage, ElMessageBox } from 'element-plus'
import DeployLogViewer from '@/components/DeployLogViewer/index.vue'

const loading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const deployLogList = ref([])
const logDialogVisible = ref(false)
const currentLog = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  appName: null,
  deployType: null,
  deployStatus: null
})

const getList = () => {
  loading.value = true
  listDeployLog(queryParams).then(response => {
    deployLogList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.appName = null
  queryParams.deployType = null
  queryParams.deployStatus = null
  handleQuery()
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.logId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

const handleViewLog = (row) => {
  currentLog.value = row
  logDialogVisible.value = true
}

const handleDelete = (row) => {
  const logIds = row.logId || ids.value
  ElMessageBox.confirm('是否确认删除选中的部署日志?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delDeployLog(logIds)
  }).then(() => {
    getList()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const getDeployTypeName = (type) => {
  const map = {
    'deploy': '部署',
    'start': '启动',
    'stop': '停止',
    'restart': '重启'
  }
  return map[type] || type
}

const getDeployTypeTag = (type) => {
  const map = {
    'deploy': 'primary',
    'start': 'success',
    'stop': 'warning',
    'restart': 'info'
  }
  return map[type] || ''
}

const getStatusName = (status) => {
  const map = {
    'running': '进行中',
    'success': '成功',
    'failed': '失败'
  }
  return map[status] || status
}

const getStatusTag = (status) => {
  const map = {
    'running': 'warning',
    'success': 'success',
    'failed': 'danger'
  }
  return map[status] || ''
}

getList()
</script>
