<template>
  <div class="app-container">
    <div class="search-panel">
      <div class="search-header">
        <h3><el-icon><Search /></el-icon> 筛选条件</h3>
        <el-button text @click="toggleSearch">
          {{ showSearch ? '收起' : '展开' }}
          <el-icon><ArrowUp v-if="showSearch" /><ArrowDown v-else /></el-icon>
        </el-button>
      </div>
      
      <el-collapse-transition>
        <div v-show="showSearch" class="search-form">
          <el-form :model="queryParams" ref="queryRef" :inline="false" label-width="110px">
            <div class="search-grid">
              <div class="search-item search-item-full">
                <el-form-item label="数据库连接" prop="connId">
                  <el-select 
                    v-model="queryParams.connId" 
                    placeholder="请选择数据库连接" 
                    clearable 
                    style="width: 100%;"
                    popper-class="wide-select-dropdown"
                    @change="handleConnChange"
                  >
                    <el-option
                      v-for="conn in connList"
                      :key="conn.connId"
                      :label="conn.connName"
                      :value="conn.connId"
                    >
                      <div class="conn-option">
                        <span class="conn-name">{{ conn.connName }}</span>
                        <el-tag size="small" :type="getDbTypeTag(conn.dbType)">{{ conn.dbType }}</el-tag>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </div>
              <div class="search-item search-item-full">
                <el-form-item label="数据库实例" prop="targetName">
                  <el-select 
                    v-model="queryParams.targetName" 
                    placeholder="请选择数据库实例" 
                    clearable 
                    style="width: 100%;"
                    popper-class="wide-select-dropdown"
                    :disabled="!queryParams.connId"
                  >
                    <el-option
                      v-for="dbName in dbNameList"
                      :key="dbName"
                      :label="dbName"
                      :value="dbName"
                    >
                      <div class="db-option">
                        <el-icon><Folder /></el-icon>
                        <span>{{ dbName }}</span>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </div>
              <div class="search-item">
                <el-form-item label="操作类型" prop="operationType">
                  <el-select 
                    v-model="queryParams.operationType" 
                    placeholder="请选择操作类型" 
                    clearable 
                    style="width: 100%"
                    popper-class="wide-select-dropdown"
                  >
                    <el-option label="数据备份" value="BACKUP">
                      <div class="operation-option">
                        <el-icon><Upload /></el-icon>
                        <span>数据备份</span>
                      </div>
                    </el-option>
                    <el-option label="数据恢复" value="RESTORE">
                      <div class="operation-option">
                        <el-icon><Download /></el-icon>
                        <span>数据恢复</span>
                      </div>
                    </el-option>
                    <el-option label="SQL执行" value="EXECUTE">
                      <div class="operation-option">
                        <el-icon><Monitor /></el-icon>
                        <span>SQL执行</span>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </div>
              <div class="search-item">
                <el-form-item label="执行状态" prop="status">
                  <el-select 
                    v-model="queryParams.status" 
                    placeholder="请选择状态" 
                    clearable 
                    style="width: 100%"
                    popper-class="wide-select-dropdown"
                  >
                    <el-option label="执行成功" value="0">
                      <div class="status-option success">
                        <el-icon><CircleCheck /></el-icon>
                        <span>执行成功</span>
                      </div>
                    </el-option>
                    <el-option label="执行失败" value="1">
                      <div class="status-option danger">
                        <el-icon><CircleClose /></el-icon>
                        <span>执行失败</span>
                      </div>
                    </el-option>
                    <el-option label="执行中" value="2">
                      <div class="status-option warning">
                        <el-icon><Loading /></el-icon>
                        <span>执行中</span>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </div>
              <div class="search-item">
                <el-form-item label="操作人员" prop="createBy">
                  <el-input v-model="queryParams.createBy" placeholder="请输入操作人员" clearable style="width: 100%" />
                </el-form-item>
              </div>
              <div class="search-item">
                <el-form-item label="操作时间">
                  <el-date-picker
                    v-model="dateRange"
                    type="datetimerange"
                    range-separator="-"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              <div class="search-item">
                <el-form-item label="关键词" prop="keyword">
                  <el-input v-model="queryParams.keyword" placeholder="输入SQL内容或错误信息搜索" clearable style="width: 100%">
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </div>
              <div class="search-item search-item-buttons">
                <el-form-item>
                  <el-button type="primary" @click="handleQuery">
                    <el-icon><Search /></el-icon> 搜索
                  </el-button>
                  <el-button @click="resetQuery">
                    <el-icon><Refresh /></el-icon> 重置
                  </el-button>
                </el-form-item>
              </div>
            </div>
          </el-form>
        </div>
      </el-collapse-transition>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总记录数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.success }}</div>
            <div class="stat-label">成功操作</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon danger">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.failed }}</div>
            <div class="stat-label">失败操作</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon running">
            <el-icon><Loading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.running }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain @click="handleExport">
          <el-icon><Download /></el-icon> 导出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :disabled="multiple" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table 
      v-loading="loading" 
      :data="logList" 
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      class="log-table"
      :row-class-name="getRowClassName"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志ID" align="center" prop="logId" width="80" sortable="custom" />
      <el-table-column label="操作类型" align="center" prop="operationType" width="120" sortable="custom">
        <template #default="scope">
          <el-tag :type="getOperationTypeTag(scope.row.operationType)" effect="dark">
            <el-icon v-if="scope.row.operationType === 'BACKUP'"><Upload /></el-icon>
            <el-icon v-else-if="scope.row.operationType === 'RESTORE'"><Download /></el-icon>
            <el-icon v-else><Monitor /></el-icon>
            {{ getOperationTypeLabel(scope.row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数据库连接" align="center" prop="connId" min-width="150">
        <template #default="scope">
          <div class="conn-cell">
            <span class="conn-name">{{ getConnName(scope.row.connId) }}</span>
            <el-tag size="small" type="info">{{ getConnDbType(scope.row.connId) }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="执行内容" align="left" prop="sqlContent" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <div class="sql-content" v-if="scope.row.sqlContent">
            <code>{{ truncateSql(scope.row.sqlContent) }}</code>
          </div>
          <div class="operation-desc" v-else>
            {{ getOperationDesc(scope.row) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="影响行数" align="center" prop="affectedRows" width="100" sortable="custom">
        <template #default="scope">
          <span v-if="scope.row.affectedRows !== null && scope.row.affectedRows !== undefined">{{ scope.row.affectedRows }} 行</span>
          <span v-else class="na-text">-</span>
        </template>
      </el-table-column>
      <el-table-column label="耗时" align="center" prop="costTime" width="100" sortable="custom">
        <template #default="scope">
          <span v-if="scope.row.costTime" :class="getCostTimeClass(scope.row.costTime)">
            {{ scope.row.costTime }} ms
          </span>
          <span v-else class="na-text">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100" sortable="custom">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)" effect="light">
            <el-icon v-if="scope.row.status === '2'" class="is-loading"><Loading /></el-icon>
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作人员" align="center" prop="createBy" width="100" sortable="custom" />
      <el-table-column label="操作时间" align="center" prop="createTime" width="170" sortable="custom">
        <template #default="scope">
          <span class="time-text">{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">
            <el-icon><View /></el-icon> 详情
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
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

    <el-dialog 
      title="操作日志详情" 
      v-model="detailOpen" 
      width="800px" 
      append-to-body
      class="detail-dialog"
    >
      <div v-if="currentLog" class="detail-content">
        <el-descriptions :column="2" border class="detail-info">
          <el-descriptions-item label="日志ID">{{ currentLog.logId }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTag(currentLog.operationType)" effect="dark">
              {{ getOperationTypeLabel(currentLog.operationType || 'EXECUTE') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数据库连接">{{ getConnName(currentLog.connId) }}</el-descriptions-item>
          <el-descriptions-item label="数据库类型">{{ getConnDbType(currentLog.connId) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(currentLog.status)" effect="light">
              {{ getStatusLabel(currentLog.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="影响行数">{{ currentLog.affectedRows !== null && currentLog.affectedRows !== undefined ? currentLog.affectedRows : '-' }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ currentLog.costTime ? currentLog.costTime + ' ms' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作人员">{{ currentLog.createBy }}</el-descriptions-item>
          <el-descriptions-item label="操作时间" :span="2">{{ parseTime(currentLog.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section" v-if="currentLog.sqlContent">
          <h4><el-icon><Document /></el-icon> 执行语句</h4>
          <pre class="sql-code"><code>{{ currentLog.sqlContent }}</code></pre>
        </div>

        <div class="detail-section" v-if="currentLog.status === '1' && currentLog.errorMsg">
          <h4><el-icon><Warning /></el-icon> 错误信息</h4>
          <div class="error-msg">
            <el-alert type="error" :closable="false" show-icon>
              {{ currentLog.errorMsg }}
            </el-alert>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="DbLog">
import { listLog, listConn, delLog, exportLog } from "@/api/system/db";
import { Search, ArrowUp, ArrowDown, Upload, Download, Monitor, CircleCheck, CircleClose, Loading, Document, Delete, View, Warning, Refresh, Folder } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const logList = ref([]);
const connList = ref([]);
const dbNameList = ref([]);
const ids = ref([]);
const dateRange = ref([]);
const multiple = ref(true);

const detailOpen = ref(false);
const currentLog = ref(null);

const stats = reactive({
  total: 0,
  success: 0,
  failed: 0,
  running: 0
});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    connId: null,
    operationType: null,
    status: null,
    createBy: null,
    keyword: null,
    targetName: null,
    orderByColumn: 'createTime',
    isAsc: 'desc'
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  const params = {
    ...queryParams.value,
    dateRange: dateRange.value
  };
  listLog(params).then(response => {
    logList.value = response.rows;
    total.value = response.total;
    updateStats();
    loading.value = false;
  });
}

function getConnList() {
  listConn().then(response => {
    connList.value = response.rows || [];
  });
}

function handleConnChange(connId) {
  queryParams.value.targetName = null;
  dbNameList.value = [];
  if (connId) {
    getDbNameList(connId);
  }
}

function getDbNameList(connId) {
  const conn = connList.value.find(c => c.connId === connId);
  if (conn && conn.dbNames) {
    dbNameList.value = conn.dbNames;
  } else {
    dbNameList.value = [];
  }
}

function updateStats() {
  stats.total = total.value;
  stats.success = logList.value.filter(l => l.status === '0').length;
  stats.failed = logList.value.filter(l => l.status === '1').length;
  stats.running = logList.value.filter(l => l.status === '2').length;
}

function toggleSearch() {
  showSearch.value = !showSearch.value;
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.logId);
  multiple.value = !selection.length;
}

function handleSortChange({ prop, order }) {
  queryParams.value.orderByColumn = prop;
  queryParams.value.isAsc = order === 'ascending' ? 'asc' : 'desc';
  getList();
}

function handleDetail(row) {
  currentLog.value = row;
  detailOpen.value = true;
}

function handleDelete(row) {
  const logIds = row.logId ? [row.logId] : ids.value;
  proxy.$modal.confirm('是否确认删除选中的日志记录?').then(() => {
    return delLog(logIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleBatchDelete() {
  proxy.$modal.confirm('是否确认批量删除选中的日志记录？').then(() => {
    return delLog(ids.value);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("批量删除成功");
  }).catch(() => {});
}

function handleExport() {
  proxy.download('system/db/log/export', {
    ...queryParams.value
  }, `db_log_${new Date().getTime()}.xlsx`);
}

function getConnName(connId) {
  const conn = connList.value.find(c => c.connId === connId);
  return conn ? conn.connName : `连接#${connId}`;
}

function getConnDbType(connId) {
  const conn = connList.value.find(c => c.connId === connId);
  return conn ? conn.dbType : '-';
}

function getDbTypeTag(dbType) {
  const map = {
    'mysql': '',
    'postgresql': 'success',
    'mongodb': 'warning',
    'redis': 'danger'
  };
  return map[dbType] || 'info';
}

function getOperationTypeLabel(type) {
  const map = {
    'BACKUP': '备份',
    'RESTORE': '恢复',
    'EXECUTE': '执行'
  };
  return map[type] || type || '执行';
}

function getOperationTypeTag(type) {
  const map = {
    'BACKUP': 'primary',
    'RESTORE': 'success',
    'EXECUTE': 'warning'
  };
  return map[type] || '';
}

function getStatusLabel(status) {
  const map = {
    '0': '成功',
    '1': '失败',
    '2': '进行中'
  };
  return map[status] || status;
}

function getStatusTag(status) {
  const map = {
    '0': 'success',
    '1': 'danger',
    '2': 'warning'
  };
  return map[status] || 'info';
}

function getCostTimeClass(costTime) {
  if (costTime < 1000) return 'cost-fast';
  if (costTime < 5000) return 'cost-normal';
  return 'cost-slow';
}

function getRowClassName({ row }) {
  if (row.status === '1') return 'row-error';
  if (row.status === '2') return 'row-running';
  return '';
}

function truncateSql(sql) {
  if (!sql) return '';
  return sql.length > 100 ? sql.substring(0, 100) + '...' : sql;
}

function getOperationDesc(row) {
  if (row.operationType === 'BACKUP') {
    return `备份文件: ${row.fileName || '-'}`;
  } else if (row.operationType === 'RESTORE') {
    return `恢复到: ${row.targetConnName || '-'}`;
  }
  return '-';
}

getList();
getConnList();
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-panel {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.search-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-form {
  padding: 20px;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.search-item {
  min-width: 0;
}

.search-item-full {
  grid-column: span 3;
}

.search-item-buttons {
  display: flex;
  align-items: flex-end;
}

.search-item-buttons .el-form-item {
  margin-bottom: 0;
}

.search-grid .el-form-item {
  margin-bottom: 0;
}

.search-grid .el-select,
.search-grid .el-input,
.search-grid .el-date-picker {
  width: 100% !important;
}

.conn-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.conn-option .conn-name {
  flex: 1;
}

.db-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.operation-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-option.success { color: #67c23a; }
.status-option.danger { color: #f56c6c; }
.status-option.warning { color: #e6a23c; }

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon.total { background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff; }
.stat-icon.success { background: linear-gradient(135deg, #67c23a, #85ce61); color: #fff; }
.stat-icon.danger { background: linear-gradient(135deg, #f56c6c, #f78989); color: #fff; }
.stat-icon.running { background: linear-gradient(135deg, #e6a23c, #ebb563); color: #fff; }

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.log-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.conn-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conn-cell .conn-name {
  font-weight: 500;
}

.sql-content code {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.operation-desc {
  color: #909399;
  font-size: 13px;
}

.cost-fast { color: #67c23a; }
.cost-normal { color: #e6a23c; }
.cost-slow { color: #f56c6c; }

.time-text {
  font-size: 13px;
  color: #606266;
}

.na-text {
  color: #c0c4cc;
}

:deep(.row-error) {
  background: #fef0f0;
}

:deep(.row-running) {
  background: #fdf6ec;
}

.detail-content {
  padding: 10px;
}

.detail-info {
  margin-bottom: 20px;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sql-code {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
}

.error-msg {
  background: #fef0f0;
  padding: 12px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
  
  .search-form :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>

<style>
.db-select {
  width: 100% !important;
}

.db-select .el-select__wrapper {
  width: 100% !important;
}

.wide-select-dropdown {
  min-width: 600px !important;
  max-width: 900px !important;
  width: 600px !important;
}

.wide-select-dropdown.el-select-dropdown {
  width: 600px !important;
  min-width: 600px !important;
}

.wide-select-dropdown .el-select-dropdown__list {
  width: 100%;
}

.wide-select-dropdown .el-select-dropdown__wrap {
  max-height: 400px !important;
}

.wide-select-dropdown .el-select-dropdown__item {
  min-height: 50px;
  line-height: 50px;
  font-size: 15px;
  padding: 0 20px;
  white-space: normal;
  word-break: break-all;
}

.wide-select-dropdown .el-select-dropdown__item:hover {
  background-color: #ecf5ff;
}

.wide-select-dropdown .el-select-dropdown__item.is-selected {
  background-color: #409eff;
  color: #fff;
  font-weight: 600;
}

.wide-select-dropdown .el-select-dropdown__item.is-selected:hover {
  background-color: #337ecc;
}

:deep(.el-select) {
  width: 100% !important;
}

:deep(.el-select .el-select__wrapper) {
  width: 100% !important;
  padding: 8px 15px;
}

:deep(.el-select .el-input__inner) {
  font-size: 15px;
  font-weight: 500;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset !important;
}

:deep(.el-select__wrapper) {
  min-height: 42px;
}

.conn-option,
.db-option,
.operation-option,
.status-option {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
}

.conn-option .conn-name,
.db-option span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
