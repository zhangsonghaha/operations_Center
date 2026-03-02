<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="容器名称" prop="containerName">
        <el-input
          v-model="queryParams.containerName"
          placeholder="请输入容器名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="镜像名称" prop="imageName">
        <el-input
          v-model="queryParams.imageName"
          placeholder="请输入镜像名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务器" prop="serverId">
        <el-select v-model="queryParams.serverId" placeholder="请选择服务器" clearable style="width: 200px">
          <el-option
            v-for="server in serverList"
            :key="server.serverId"
            :label="server.serverName"
            :value="server.serverId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px">
          <el-option label="运行中" value="running" />
          <el-option label="已停止" value="stopped" />
          <el-option label="已暂停" value="paused" />
          <el-option label="已退出" value="exited" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleDeploy"
          v-hasPermi="['ops:docker:deploy']"
        >部署容器</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:docker:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 容器列表 -->
    <el-table v-loading="loading" :data="containerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="容器名称" align="center" prop="containerName" min-width="150" />
      <el-table-column label="镜像" align="center" min-width="180">
        <template #default="scope">
          <span>{{ scope.row.imageName }}:{{ scope.row.imageTag }}</span>
        </template>
      </el-table-column>
      <el-table-column label="服务器" align="center" prop="serverName" min-width="120" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="端口映射" align="center" min-width="150">
        <template #default="scope">
          <div v-if="scope.row.ports">
            <el-tag
              v-for="(port, index) in parseJsonSafe(scope.row.ports).slice(0, 2)"
              :key="index"
              size="small"
              class="mr5 mb5"
            >
              {{ port.hostPort }}:{{ port.containerPort }}
            </el-tag>
            <el-tag v-if="parseJsonSafe(scope.row.ports).length > 2" size="small">
              +{{ parseJsonSafe(scope.row.ports).length - 2 }}
            </el-tag>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="资源限制" align="center" min-width="120">
        <template #default="scope">
          <div v-if="scope.row.cpuLimit || scope.row.memoryLimit">
            <div v-if="scope.row.cpuLimit">CPU: {{ scope.row.cpuLimit }}核</div>
            <div v-if="scope.row.memoryLimit">内存: {{ scope.row.memoryLimit }}</div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 'stopped' || scope.row.status === 'exited'"
            link
            type="primary"
            icon="VideoPlay"
            @click="handleStart(scope.row)"
            v-hasPermi="['ops:docker:start']"
          >启动</el-button>
          <el-button
            v-if="scope.row.status === 'running'"
            link
            type="warning"
            icon="VideoPause"
            @click="handleStop(scope.row)"
            v-hasPermi="['ops:docker:stop']"
          >停止</el-button>
          <el-button
            v-if="scope.row.status === 'running'"
            link
            type="info"
            icon="RefreshRight"
            @click="handleRestart(scope.row)"
            v-hasPermi="['ops:docker:restart']"
          >重启</el-button>
          <el-button
            link
            type="primary"
            icon="Document"
            @click="handleViewLogs(scope.row)"
            v-hasPermi="['ops:docker:logs']"
          >日志</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:docker:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 部署对话框 -->
    <deploy-dialog
      v-model="deployDialogVisible"
      :server-list="serverList"
      @success="getList"
    />

    <!-- 日志查看对话框 -->
    <el-dialog
      title="容器日志"
      v-model="logsDialogVisible"
      width="900px"
      append-to-body
    >
      <div class="logs-container">
        <div class="logs-toolbar">
          <el-select v-model="logsTail" size="small" style="width: 150px" @change="loadContainerLogs">
            <el-option label="最后100行" :value="100" />
            <el-option label="最后200行" :value="200" />
            <el-option label="最后500行" :value="500" />
            <el-option label="全部" :value="0" />
          </el-select>
          <el-button size="small" icon="Refresh" @click="loadContainerLogs" class="ml10">刷新</el-button>
        </div>
        <pre class="logs-content" v-loading="logsLoading">{{ containerLogs || '暂无日志' }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="DockerContainer">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listContainer,
  startContainer,
  stopContainer,
  restartContainer,
  delContainer,
  getContainerLogs
} from '@/api/ops/docker'
import { listServer } from '@/api/ops/server'
import DeployDialog from './DeployDialog.vue'

// 数据
const loading = ref(true)
const showSearch = ref(true)
const containerList = ref([])
const serverList = ref([])
const total = ref(0)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const queryFormRef = ref(null)

// 部署对话框
const deployDialogVisible = ref(false)

// 日志对话框
const logsDialogVisible = ref(false)
const logsLoading = ref(false)
const containerLogs = ref('')
const logsTail = ref(100)
const currentContainer = ref(null)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  containerName: undefined,
  imageName: undefined,
  serverId: undefined,
  status: undefined
})

// 获取容器列表
function getList() {
  loading.value = true
  listContainer(queryParams).then(response => {
    containerList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 获取服务器列表
function getServerList() {
  listServer({}).then(response => {
    serverList.value = response.rows || []
  })
}

// 搜索
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

// 重置
function resetQuery() {
  queryFormRef.value.resetFields()
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.containerId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 部署容器
function handleDeploy() {
  deployDialogVisible.value = true
}

// 启动容器
function handleStart(row) {
  ElMessageBox.confirm('确认启动容器 "' + row.containerName + '" 吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return startContainer(row.containerId)
  }).then(() => {
    getList()
    ElMessage.success('启动成功')
  }).catch(() => {})
}

// 停止容器
function handleStop(row) {
  ElMessageBox.confirm('确认停止容器 "' + row.containerName + '" 吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return stopContainer(row.containerId)
  }).then(() => {
    getList()
    ElMessage.success('停止成功')
  }).catch(() => {})
}

// 重启容器
function handleRestart(row) {
  ElMessageBox.confirm('确认重启容器 "' + row.containerName + '" 吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return restartContainer(row.containerId)
  }).then(() => {
    getList()
    ElMessage.success('重启成功')
  }).catch(() => {})
}

// 查看日志
function handleViewLogs(row) {
  currentContainer.value = row
  logsDialogVisible.value = true
  loadContainerLogs()
}

// 加载容器日志
function loadContainerLogs() {
  if (!currentContainer.value) return
  
  logsLoading.value = true
  getContainerLogs(currentContainer.value.containerId, logsTail.value).then(response => {
    containerLogs.value = response.data || ''
    logsLoading.value = false
  }).catch(() => {
    logsLoading.value = false
  })
}

// 删除容器
function handleDelete(row) {
  const containerIds = row.containerId || ids.value
  const containerNames = row.containerName || containerList.value
    .filter(item => ids.value.includes(item.containerId))
    .map(item => item.containerName)
    .join('、')
  
  ElMessageBox.confirm('确认删除容器 "' + containerNames + '" 吗？删除后无法恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const deleteIds = Array.isArray(containerIds) ? containerIds : [containerIds]
    return Promise.all(deleteIds.map(id => delContainer(id)))
  }).then(() => {
    getList()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 获取状态类型
function getStatusType(status) {
  const statusMap = {
    running: 'success',
    stopped: 'info',
    paused: 'warning',
    exited: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
function getStatusText(status) {
  const statusMap = {
    running: '运行中',
    stopped: '已停止',
    paused: '已暂停',
    exited: '已退出'
  }
  return statusMap[status] || status
}

// 安全解析JSON
function parseJsonSafe(jsonStr) {
  try {
    return jsonStr ? JSON.parse(jsonStr) : []
  } catch (e) {
    return []
  }
}

// 初始化
onMounted(() => {
  getList()
  getServerList()
})
</script>

<style scoped lang="scss">
.logs-container {
  .logs-toolbar {
    margin-bottom: 10px;
    display: flex;
    align-items: center;
  }
  
  .logs-content {
    background-color: #1e1e1e;
    color: #d4d4d4;
    padding: 15px;
    border-radius: 4px;
    max-height: 500px;
    overflow-y: auto;
    font-family: 'Courier New', Courier, monospace;
    font-size: 13px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
}

.mr5 {
  margin-right: 5px;
}

.mb5 {
  margin-bottom: 5px;
}

.ml10 {
  margin-left: 10px;
}
</style>
