<template>
  <div class="app-container">
    <el-container class="process-container">
      <!-- 左侧服务器列表 -->
      <el-aside width="260px" class="process-sidebar">
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
            <el-empty description="暂无服务器" :image-size="60" />
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

      <!-- 右侧进程管理面板 -->
      <el-main class="process-main">
        <div v-if="!currentServerId" class="empty-state">
          <div class="empty-content">
            <el-icon class="empty-icon"><Monitor /></el-icon>
            <h3>请选择服务器</h3>
            <p>从左侧列表中选择一台服务器以管理进程</p>
          </div>
        </div>

        <div v-else class="process-content">
          <!-- 顶部工具栏 -->
          <el-card shadow="never" class="mb-20">
            <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
              <el-form-item label="PID" prop="pid">
                <el-input
                  v-model="queryParams.pid"
                  placeholder="请输入PID"
                  clearable
                  style="width: 150px"
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="进程名称" prop="name">
                <el-input
                  v-model="queryParams.name"
                  placeholder="请输入进程名称"
                  clearable
                  style="width: 200px"
                  @keyup.enter="handleQuery"
                />
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
                  @click="handleKill"
                  v-hasPermi="['ops:process:kill']"
                >终止进程</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button
                  type="warning"
                  plain
                  icon="Download"
                  @click="handleExport"
                >导出</el-button>
              </el-col>
              <div class="right-toolbar">
                 <div class="auto-refresh-wrapper">
                   <span class="label">自动刷新 (30s)</span>
                   <el-switch 
                     v-model="autoRefresh" 
                     @change="handleAutoRefresh"
                   />
                 </div>
              </div>
            </el-row>
          </el-card>

          <!-- 进程列表表格 -->
          <el-card shadow="never">
            <el-table 
              v-loading="loading" 
              :data="pagedProcessList" 
              @selection-change="handleSelectionChange"
              stripe
              height="calc(100vh - 350px)"
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="PID" align="center" prop="pid" width="100" sortable />
              <el-table-column label="进程名称" align="left" prop="name" :show-overflow-tooltip="true" sortable />
              <el-table-column label="CPU使用率" align="center" prop="cpuUsage" width="120" sortable>
                <template #default="scope">
                  <span>{{ scope.row.cpuUsage }}%</span>
                </template>
              </el-table-column>
              <el-table-column label="内存使用率" align="center" prop="memoryUsage" width="120" sortable>
                <template #default="scope">
                  <span>{{ scope.row.memoryUsage }}%</span>
                </template>
              </el-table-column>
              <el-table-column label="启动时间" align="center" prop="startTime" width="220" />
              <el-table-column label="状态" align="center" prop="status" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'Running' ? 'success' : 'info'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                  <el-button
                    type="danger"
                    link
                    icon="Delete"
                    @click="handleKill(scope.row)"
                    v-hasPermi="['ops:process:kill']"
                  >终止</el-button>
                </template>
              </el-table-column>
            </el-table>

            <pagination
              v-show="total>0"
              :total="total"
              v-model:page="queryParams.pageNum"
              v-model:limit="queryParams.pageSize"
              @pagination="handlePagination"
            />
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup name="ProcessIndex">
import { listServer } from "@/api/ops/server";
import { listProcess, killProcess } from "@/api/ops/process";
import { 
  Search, Monitor, Refresh, DataLine, Delete, Download 
} from '@element-plus/icons-vue';
import { ElMessageBox, ElMessage } from 'element-plus';

const { proxy } = getCurrentInstance();

// Data
const serverList = ref([]);
const serverName = ref('');
const currentServerId = ref('');
const currentServerName = ref('');
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const multiple = ref(true);
const autoRefresh = ref(false);
let refreshTimer = null;

// Process Data
const processList = ref([]);
const total = ref(0);
const queryParams = ref({
  pageNum: 1,
  pageSize: 20,
  pid: undefined,
  name: undefined
});

// Computed
const filteredServerList = computed(() => {
  if (!serverName.value) return serverList.value;
  return serverList.value.filter(server => 
    server.serverName.toLowerCase().includes(serverName.value.toLowerCase()) ||
    server.publicIp.includes(serverName.value)
  );
});

// Client-side pagination
const pagedProcessList = computed(() => {
  const start = (queryParams.value.pageNum - 1) * queryParams.value.pageSize;
  const end = start + queryParams.value.pageSize;
  return processList.value.slice(start, end);
});

// Methods
function getServerList() {
  listServer({ pageNum: 1, pageSize: 1000 }).then(response => {
    serverList.value = response.rows;
  });
}

function handleServerSelect(index) {
  if (currentServerId.value === index) return;
  currentServerId.value = index;
  const server = serverList.value.find(s => String(s.serverId) === index);
  if (server) {
    currentServerName.value = server.serverName;
    handleQuery();
  }
}

function getList() {
  if (!currentServerId.value) return;
  loading.value = true;
  
  const params = {
    serverId: currentServerId.value,
    pid: queryParams.value.pid,
    name: queryParams.value.name
  };

  listProcess(params).then(response => {
    // 假设后端返回的是 rows 列表
    processList.value = response.rows || [];
    total.value = processList.value.length;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryForm");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.pid);
  multiple.value = !selection.length;
}

function handleKill(row) {
  const pids = row.pid ? [row.pid] : ids.value;
  const serverId = currentServerId.value;
  if (!pids.length) return;
  
  ElMessageBox.confirm('是否确认终止选中的进程？此操作不可逆！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return killProcess(serverId, pids.join(','));
  }).then(() => {
    getList();
    ElMessage.success("终止命令已发送");
  }).catch(() => {});
}

function handleExport() {
  // Simple CSV Export
  if (!processList.value.length) {
    ElMessage.warning("暂无数据可导出");
    return;
  }
  
  const headers = ["PID", "进程名称", "CPU使用率(%)", "内存使用率(%)", "启动时间", "状态"];
  const keys = ["pid", "name", "cpuUsage", "memoryUsage", "startTime", "status"];
  
  let csvContent = headers.join(",") + "\n";
  processList.value.forEach(row => {
    const rowData = keys.map(key => `"${row[key] || ''}"`).join(",");
    csvContent += rowData + "\n";
  });
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement("a");
  const url = URL.createObjectURL(blob);
  link.setAttribute("href", url);
  link.setAttribute("download", `process_list_${currentServerName.value}_${new Date().getTime()}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function handleAutoRefresh(val) {
  if (val) {
    if (refreshTimer) clearInterval(refreshTimer);
    refreshTimer = setInterval(() => {
      getList();
    }, 30000); // 30s
  } else {
    if (refreshTimer) clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

function handlePagination({ page, limit }) {
  // Just update values, computed property handles slicing
}

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer);
});

getServerList();
</script>

<style scoped>
.app-container {
  padding: 0;
  height: calc(100vh - 84px);
  background-color: #f6f8f9;
}

.process-container {
  height: 100%;
}

/* Sidebar (Reuse from Monitor) */
.process-sidebar {
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

/* Main Content */
.process-main {
  padding: 20px;
  overflow-y: auto;
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

.right-toolbar {
  float: right;
  display: flex;
  align-items: center;
}

.auto-refresh-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  margin-right: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
