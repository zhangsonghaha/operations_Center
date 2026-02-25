<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="连接ID" prop="connId">
        <el-input
          v-model="queryParams.connId"
          placeholder="请输入连接ID"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件名" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名"
          clearable
          style="width: 240px"
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
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:db:backup:add']"
        >新建备份</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:db:backup:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="backupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备份ID" align="center" prop="backupId" />
      <el-table-column label="连接名称" align="center" prop="connId" :formatter="connNameFormat" />
      <el-table-column label="文件名" align="center" prop="fileName" show-overflow-tooltip />
      <el-table-column label="备份类型" align="center" prop="backupType">
        <template #default="scope">
          <span>{{ scope.row.backupType === '0' ? '手动' : '自动' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Download"
            @click="handleDownload(scope.row)"
            v-if="scope.row.status === '0'"
          >下载</el-button>
          <el-button
            link
            type="warning"
            icon="RefreshLeft"
            @click="handleRestore(scope.row)"
            v-if="scope.row.status === '0'"
            v-hasPermi="['system:db:backup:restore']"
          >恢复</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:db:backup:remove']"
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

    <!-- 新建备份对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-steps :active="activeStep" finish-status="success" simple style="margin-bottom: 20px">
        <el-step title="选择连接" />
        <el-step title="配置选项" />
        <el-step title="选择目标" />
      </el-steps>

      <!-- 步骤1: 选择连接 -->
      <div v-show="activeStep === 0">
        <el-form ref="backupRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="选择连接" prop="connId">
            <el-select v-model="form.connId" placeholder="请选择数据库连接" style="width: 100%" @change="handleConnChange">
              <el-option
                v-for="item in connList"
                :key="item.connId"
                :label="item.connName"
                :value="item.connId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="数据库类型">
            <el-tag size="large" type="info">{{ dbTypeLabel }}</el-tag>
          </el-form-item>
        </el-form>
        <div class="step-actions">
          <el-button type="primary" @click="nextStep" :disabled="!form.connId">下一步</el-button>
        </div>
      </div>

      <!-- 步骤2: 配置选项 -->
      <div v-show="activeStep === 1">
        <el-form :model="form" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="备份方式">
                <el-radio-group v-model="form.backupMode">
                  <el-radio-button label="full">全量备份</el-radio-button>
                  <el-radio-button label="incremental">增量备份</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="备份范围">
                <el-radio-group v-model="backupScope" @change="handleScopeChange">
                  <el-radio-button label="all">全库备份</el-radio-button>
                  <el-radio-button label="partial">部分备份</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备份级别" v-if="backupScope === 'partial'">
            <el-radio-group v-model="form.backupLevel">
              <el-radio-button label="database">数据库级</el-radio-button>
              <el-radio-button label="table">表级</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="备份方式">
                <el-select v-model="form.backupMode" placeholder="请选择" style="width: 100%">
                  <el-option label="全量备份" value="full" />
                  <el-option label="增量备份" value="incremental" />
                </el-select>
                <div class="form-tip">
                  <el-icon><InfoFilled /></el-icon>
                  <span v-if="form.backupMode === 'incremental'">基于binlog位置，需要REPLICATION CLIENT权限，无权限时自动回退到全量备份</span>
                  <span v-else>全量备份包含完整的表结构和数据</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="压缩备份">
                <el-switch v-model="form.compressEnabled" active-value="1" inactive-value="0" />
                <div class="form-tip">
                  <el-icon><InfoFilled /></el-icon>
                  <span>启用后使用Gzip压缩，可减少50%-90%存储空间</span>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="存储类型">
                <el-select v-model="form.storageType" placeholder="请选择" style="width: 100%">
                  <el-option label="本地存储" value="local" />
                  <el-option label="FTP服务器" value="ftp" />
                  <el-option label="SFTP服务器" value="sftp" />
                  <el-option label="阿里云OSS" value="aliyun_oss" />
                  <el-option label="腾讯云COS" value="tencent_cos" />
                </el-select>
                <div class="form-tip" v-if="form.storageType !== 'local'">
                  <el-icon><Warning /></el-icon>
                  <span>非本地存储需要在【系统管理-参数配置】中配置对应存储参数</span>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">
            {{ backupScope === 'all' ? '开始备份' : '下一步' }}
          </el-button>
        </div>
      </div>

      <!-- 步骤3: 选择目标（树形结构） -->
      <div v-show="activeStep === 2">
        <div class="tree-header">
          <el-input v-model="treeFilter" placeholder="搜索数据库/表" prefix-icon="Search" clearable style="width: 250px" />
          <el-button type="primary" link @click="checkAll">全选</el-button>
          <el-button type="primary" link @click="uncheckAll">清空</el-button>
        </div>
        <div class="tree-container">
          <el-tree
            ref="treeRef"
            :data="dbTreeData"
            show-checkbox
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            :filter-node-method="filterNode"
            @check-change="handleTreeCheck"
            default-expand-all
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-icon v-if="data.type === 'database'" class="node-icon"><Collection /></el-icon>
                <el-icon v-else class="node-icon"><Grid /></el-icon>
                <span>{{ node.label }}</span>
                <el-tag v-if="data.type === 'database'" size="small" type="info" style="margin-left: 8px">库</el-tag>
                <el-tag v-else size="small" type="success" style="margin-left: 8px">表</el-tag>
              </span>
            </template>
          </el-tree>
        </div>
        <div class="selected-info">
          已选择: <el-tag type="primary">{{ selectedTargets.length }}</el-tag> 个对象
          <el-tag v-if="selectedTargets.length >= 100" type="danger" size="small" style="margin-left: 8px">已达上限</el-tag>
          <span v-else style="margin-left: 8px; color: #909399; font-size: 12px">(最多100个)</span>
        </div>
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">开始备份</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 恢复备份对话框 -->
    <el-dialog 
      title="恢复备份" 
      v-model="restoreOpen" 
      width="900px" 
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="!restoreRunning"
      custom-class="restore-dialog"
    >
      <!-- 步骤1: 选择恢复目标 -->
      <div v-if="restoreStep === 1">
        <el-alert
          title="恢复操作将覆盖目标数据库中的数据，请谨慎操作！"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />
        <el-form ref="restoreRef" :model="restoreForm" :rules="restoreRules" label-width="120px">
          <el-form-item label="备份文件">
            <el-tag type="success">{{ currentBackup?.fileName }}</el-tag>
            <span style="margin-left: 10px; color: #909399; font-size: 13px">
              {{ formatFileSize(currentBackup?.fileSize) }}
            </span>
          </el-form-item>
          <el-form-item label="源数据库">
            <el-tag>{{ getConnNameById(currentBackup?.connId) }}</el-tag>
          </el-form-item>
          <el-form-item label="目标连接" prop="targetConnId">
            <el-select v-model="restoreForm.targetConnId" placeholder="请选择恢复目标" style="width: 100%">
              <el-option
                v-for="item in connList"
                :key="item.connId"
                :label="item.connName + ' (' + item.host + '/' + item.dbName + ')'"
                :value="item.connId"
              />
            </el-select>
            <div class="form-tip">
              <el-icon><Warning /></el-icon>
              <span>恢复操作将覆盖目标数据库中的同名表数据，请确保目标数据库可写</span>
            </div>
          </el-form-item>
        </el-form>
        <div class="step-actions">
          <el-button @click="restoreOpen = false">取消</el-button>
          <el-button type="primary" @click="startRestore">开始恢复</el-button>
        </div>
      </div>

      <!-- 步骤2: 恢复进度 -->
      <div v-else-if="restoreStep === 2">
        <!-- 状态卡片 -->
        <div class="restore-status-cards">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="status-card" :class="restoreStatus">
                <div class="status-icon-container">
                  <el-icon class="status-icon" :size="40">
                    <Loading v-if="restoreStatus === 'running'" />
                    <CircleCheck v-else-if="restoreStatus === 'success'" />
                    <CircleClose v-else-if="restoreStatus === 'error'" />
                    <Timer v-else />
                  </el-icon>
                </div>
                <div class="status-text">{{ restoreStatusText }}</div>
                <div class="status-desc">{{ restoreStepText }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="status-card info">
                <div class="status-value">{{ restoreProgress }}%</div>
                <div class="status-label">恢复进度</div>
                <el-progress 
                  :percentage="restoreProgress" 
                  :status="restoreProgressStatus"
                  :stroke-width="10"
                  :show-text="false"
                  class="progress-bar"
                />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="status-card info">
                <div class="status-value">{{ restoreTableProgress }}</div>
                <div class="status-label">表进度</div>
                <div class="status-desc">{{ restoreScope }}</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 影响范围 -->
        <div class="restore-scope" v-if="restoreScope">
          <el-divider content-position="left">
            <el-icon><InfoFilled /></el-icon>
            影响范围
          </el-divider>
          <el-tag type="info" size="small" effect="plain">{{ restoreScope }}</el-tag>
        </div>

        <!-- 实时日志 -->
        <div class="restore-log-section">
          <el-divider content-position="left">
            <el-icon><Document /></el-icon>
            恢复日志
            <el-tag v-if="restoreRunning" type="danger" size="small" effect="dark" style="margin-left: 8px">实时</el-tag>
          </el-divider>
          <div ref="logContainer" class="restore-log-container">
            <div 
              v-for="(log, index) in restoreLogs" 
              :key="index" 
              class="log-line"
              :class="getLogClass(log)"
            >
              <span class="log-time">{{ log.time }}</span>
              <span class="log-content">{{ log.content }}</span>
            </div>
            <div v-if="restoreLogs.length === 0" class="log-empty">
              <el-empty description="等待恢复开始..." :image-size="80" />
            </div>
          </div>
        </div>

        <div class="step-actions">
          <el-button 
            v-if="restoreStatus === 'success' || restoreStatus === 'error'" 
            @click="closeRestoreDialog"
            :type="restoreStatus === 'success' ? 'primary' : ''"
          >
            {{ restoreStatus === 'success' ? '完成' : '关闭' }}
          </el-button>
          <el-button 
            v-if="restoreStatus === 'running'" 
            type="danger" 
            @click="cancelRestore"
          >
            取消恢复
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="DbBackup">
import { listBackup, delBackup, backup, backupWithOptions, listConn, getTableList, restoreBackup, getRestoreProgress } from "@/api/system/db";
import { Collection, Grid, InfoFilled, Warning, RefreshLeft, Loading, CircleCheck, CircleClose, Timer, Document } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const backupList = ref([]);
const connList = ref([]);
const title = ref("新建备份");
const open = ref(false);
const activeStep = ref(0);
const backupScope = ref('all');
const treeFilter = ref('');
const treeRef = ref(null);
const dbTreeData = ref([]);
const selectedTargets = ref([]);
const submitLoading = ref(false);

// 恢复相关变量
const restoreOpen = ref(false);
const restoreStep = ref(1);
const restoreRunning = ref(false);
const restoreStatus = ref('waiting'); // waiting/running/success/error
const restoreStatusText = ref('准备中');
const restoreStepText = ref('等待开始');
const restoreProgress = ref(0);
const restoreProgressStatus = ref('');
const restoreTableProgress = ref('0/0');
const restoreScope = ref('');
const restoreLogs = ref([]);
const currentBackup = ref(null);
const logContainer = ref(null);
const restoreTaskId = ref(null);

const restoreForm = reactive({
  targetConnId: null
});

const restoreRules = {
  targetConnId: [{ required: true, message: "请选择目标连接", trigger: "change" }]
};

const data = reactive({
  form: {
    connId: null,
    dbType: 'mysql',
    backupMode: 'full',
    backupLevel: 'database',
    targetName: '',
    storageType: 'local',
    compressEnabled: '1'
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    connId: null,
    fileName: null,
  },
  rules: {
    connId: [{ required: true, message: "请选择连接", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

// 计算数据库类型标签
const dbTypeLabel = computed(() => {
  const typeMap = {
    'mysql': 'MySQL/MariaDB',
    'postgresql': 'PostgreSQL',
    'mongodb': 'MongoDB',
    'redis': 'Redis'
  };
  return typeMap[form.value.dbType] || form.value.dbType || 'MySQL/MariaDB';
});

// 监听树形搜索
watch(treeFilter, (val) => {
  treeRef.value?.filter(val);
});

function getList() {
  loading.value = true;
  listBackup(queryParams.value).then(response => {
    backupList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getConnList() {
  listConn().then(response => {
    connList.value = response.rows;
  });
}

function connNameFormat(row) {
  const conn = connList.value.find(c => c.connId === row.connId);
  return conn ? conn.connName : row.connId;
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    connId: null,
    dbType: 'mysql',
    backupMode: 'full',
    backupLevel: 'database',
    targetName: '',
    storageType: 'local',
    compressEnabled: '1'
  };
  proxy.resetForm("backupRef");
  activeStep.value = 0;
  backupScope.value = 'all';
  selectedTargets.value = [];
  dbTreeData.value = [];
  treeFilter.value = '';
}

function handleConnChange(val) {
  const conn = connList.value.find(c => c.connId === val);
  if (conn) {
    form.value.dbType = conn.dbType || 'mysql';
  }
}

// 步骤控制
function nextStep() {
  if (activeStep.value === 0 && !form.value.connId) {
    proxy.$modal.msgError("请选择数据库连接");
    return;
  }
  
  // 如果是全库备份，在第二步直接提交
  if (activeStep.value === 1 && backupScope.value === 'all') {
    submitForm();
    return;
  }
  
  // 进入第三步时加载树形数据
  if (activeStep.value === 1 && backupScope.value === 'partial') {
    loadDbTreeData();
  }
  
  activeStep.value++;
}

function prevStep() {
  activeStep.value--;
}

// 备份范围切换
function handleScopeChange(val) {
  if (val === 'all') {
    form.value.backupLevel = 'instance';
  } else {
    form.value.backupLevel = 'database';
  }
}

// 加载树形数据
function loadDbTreeData() {
  if (!form.value.connId) return;
  
  // 构建树形数据
  const dbName = connList.value.find(c => c.connId === form.value.connId)?.dbName || 'database';
  
  getTableList(form.value.connId).then(response => {
    const tables = response.data || [];
    dbTreeData.value = [{
      id: dbName,
      label: dbName,
      type: 'database',
      children: tables.map((table, index) => ({
        id: `${dbName}.${table}`,
        label: table,
        type: 'table'
      }))
    }];
  });
}

// 树形筛选
function filterNode(value, data) {
  if (!value) return true;
  return data.label.toLowerCase().includes(value.toLowerCase());
}

// 树形选择变化
function handleTreeCheck() {
  const checkedNodes = treeRef.value?.getCheckedNodes(true) || [];
  const tables = checkedNodes.filter(n => n.type === 'table');
  
  // 限制最多选择100个表
  if (tables.length > 100) {
    proxy.$modal.msgWarning("最多只能选择100个表，已自动选择前100个");
    // 只保留前100个
    const limitedNodes = checkedNodes.slice(0, 100);
    treeRef.value?.setCheckedNodes(limitedNodes);
    selectedTargets.value = limitedNodes.filter(n => n.type === 'table').map(n => n.label);
  } else {
    selectedTargets.value = tables.map(n => n.label);
  }
  
  form.value.targetName = selectedTargets.value.join(',');
}

// 全选/清空
function checkAll() {
  const allTables = dbTreeData.value[0]?.children || [];
  if (allTables.length > 100) {
    proxy.$modal.msgWarning(`共有 ${allTables.length} 个表，超过100个限制，将只选择前100个`);
    treeRef.value?.setCheckedNodes(allTables.slice(0, 100));
  } else {
    treeRef.value?.setCheckedNodes(allTables);
  }
  handleTreeCheck();
}

function uncheckAll() {
  treeRef.value?.setCheckedKeys([]);
  handleTreeCheck();
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.backupId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新建备份";
}

function submitForm() {
  // 如果是部分备份，检查是否选择了目标
  if (backupScope.value === 'partial' && !form.value.targetName) {
    proxy.$modal.msgError("请至少选择一个备份目标");
    return;
  }
  
  submitLoading.value = true;
  proxy.$modal.loading("正在备份中，请稍候...");
  
  backupWithOptions(
    form.value.connId,
    form.value.dbType,
    form.value.backupMode,
    backupScope.value === 'all' ? 'instance' : form.value.backupLevel,
    form.value.targetName,
    form.value.storageType,
    form.value.compressEnabled
  ).then(response => {
    submitLoading.value = false;
    proxy.$modal.closeLoading();
    proxy.$modal.msgSuccess("备份成功");
    open.value = false;
    getList();
  }).catch(() => {
    proxy.$modal.closeLoading();
  });
}

function handleDelete(row) {
  const backupIds = row.backupId || ids.value;
  proxy.$modal.confirm('是否确认删除备份记录编号为"' + backupIds + '"的数据项？').then(function() {
    return delBackup(backupIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleDownload(row) {
  const resource = "/profile/backup/" + row.fileName;
  proxy.$download.resource(resource);
}

// ==================== 恢复功能 ====================

function handleRestore(row) {
  currentBackup.value = row;
  restoreOpen.value = true;
  restoreStep.value = 1;
  restoreForm.targetConnId = null;
  resetRestoreStatus();
}

function resetRestoreStatus() {
  restoreRunning.value = false;
  restoreStatus.value = 'waiting';
  restoreStatusText.value = '准备中';
  restoreStepText.value = '等待开始';
  restoreProgress.value = 0;
  restoreProgressStatus.value = '';
  restoreTableProgress.value = '0/0';
  restoreScope.value = '';
  restoreLogs.value = [];
  restoreTaskId.value = 'restore_' + Date.now();
  
  // 停止轮询
  if (pollTimer) {
    clearTimeout(pollTimer);
    pollTimer = null;
  }
}

function startRestore() {
  proxy.$refs.restoreRef.validate(valid => {
    if (!valid) return;
    
    restoreStep.value = 2;
    restoreRunning.value = true;
    restoreStatus.value = 'running';
    restoreStatusText.value = '恢复中';
    
    // 启动恢复任务
    restoreBackup({
      backupId: currentBackup.value.backupId,
      targetConnId: restoreForm.targetConnId
    }).then(response => {
      // 获取任务ID
      restoreTaskId.value = response.data || response.msg;
      addRestoreLog('INFO', '恢复任务已启动，任务ID: ' + restoreTaskId.value);
      
      // 开始轮询进度
      pollRestoreProgress();
    }).catch(error => {
      restoreStatus.value = 'error';
      restoreStatusText.value = '启动失败';
      restoreStepText.value = error.message || '启动恢复任务失败';
      addRestoreLog('ERROR', '启动恢复任务失败: ' + (error.message || '未知错误'));
      restoreRunning.value = false;
    });
  });
}

// 轮询获取恢复进度
let pollTimer = null;

function pollRestoreProgress() {
  if (!restoreTaskId.value || !restoreRunning.value) {
    return;
  }
  
  getRestoreProgress(restoreTaskId.value).then(response => {
    const progress = response.data;
    if (!progress) {
      return;
    }
    
    // 更新状态
    restoreStatus.value = progress.status === 'completed' ? 'success' : 
                        progress.status === 'failed' ? 'error' : 'running';
    
    // 更新进度
    if (progress.status === 'running') {
      restoreProgress.value = progress.progress || 0;
      restoreStatusText.value = '恢复中';
      restoreStepText.value = progress.currentTable || '正在恢复...';
    }
    
    // 更新表进度
    if (progress.totalTables) {
      // 确保completedTables不超过totalTables
      const completed = Math.min(progress.completedTables || 0, progress.totalTables);
      restoreTableProgress.value = `${completed}/${progress.totalTables}`;
    }
    
    // 更新影响范围
    if (progress.targetDatabase) {
      restoreScope.value = progress.targetDatabase;
    }
    
    // 更新日志
    if (progress.logs && progress.logs.length > 0) {
      // 只显示新日志
      const oldLen = restoreLogs.value.length;
      progress.logs.forEach(log => {
        if (!restoreLogs.value.find(l => l.content === log)) {
          const type = log.includes('[ERROR]') ? 'ERROR' :
                      log.includes('[WARN]') ? 'WARN' :
                      log.includes('[SUCCESS]') ? 'SUCCESS' :
                      log.includes('[PROGRESS]') ? 'PROGRESS' : 'INFO';
          addRestoreLog(type, log);
        }
      });
    }
    
    // 检查是否完成
    if (progress.status === 'completed') {
      restoreStatus.value = 'success';
      restoreStatusText.value = '恢复成功';
      restoreStepText.value = '数据恢复完成';
      restoreProgress.value = 100;
      restoreRunning.value = false;
      addRestoreLog('SUCCESS', '恢复完成！');
      getList();
      return;
    }
    
    if (progress.status === 'failed') {
      restoreStatus.value = 'error';
      restoreStatusText.value = '恢复失败';
      restoreStepText.value = progress.errorMessage || '恢复过程中发生错误';
      restoreProgressStatus.value = 'exception';
      restoreRunning.value = false;
      addRestoreLog('ERROR', '恢复失败: ' + (progress.errorMessage || '未知错误'));
      return;
    }
    
    // 继续轮询
    pollTimer = setTimeout(pollRestoreProgress, 1000);
  }).catch(() => {
    // 轮询失败，继续尝试
    if (restoreRunning.value) {
      pollTimer = setTimeout(pollRestoreProgress, 2000);
    }
  });
}

function addRestoreLog(type, content) {
  const now = new Date();
  const time = now.toLocaleTimeString('zh-CN', { hour12: false });
  restoreLogs.value.push({ time, type, content });
  
  // 自动滚动到底部
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight;
    }
  });
}

function getLogClass(log) {
  return {
    'log-info': log.type === 'INFO',
    'log-error': log.type === 'ERROR',
    'log-warn': log.type === 'WARN',
    'log-success': log.type === 'SUCCESS',
    'log-progress': log.type === 'PROGRESS'
  };
}

function cancelRestore() {
  proxy.$modal.confirm('确定要取消恢复操作吗？已恢复的数据不会回滚。').then(() => {
    if (pollTimer) {
      clearTimeout(pollTimer);
      pollTimer = null;
    }
    restoreRunning.value = false;
    restoreStatus.value = 'error';
    restoreStatusText.value = '已取消';
    addRestoreLog('WARN', '用户取消了恢复操作');
  }).catch(() => {});
}

function closeRestoreDialog() {
  if (pollTimer) {
    clearTimeout(pollTimer);
    pollTimer = null;
  }
  restoreOpen.value = false;
  resetRestoreStatus();
}

function getConnNameById(connId) {
  const conn = connList.value.find(c => c.connId === connId);
  return conn ? conn.connName : connId;
}

function formatFileSize(size) {
  if (!size) return '0 B';
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB';
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB';
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
}

getList();
getConnList();
</script>

<style scoped>
.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.tree-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.tree-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
}

.tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.node-icon {
  margin-right: 6px;
  color: #409eff;
}

.selected-info {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

:deep(.el-step__title) {
  font-size: 14px;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-tip .el-icon {
  font-size: 12px;
}

/* 恢复对话框 */
.restore-dialog {
  border-radius: 12px;
  overflow: hidden;
}

/* 恢复状态卡片 */
.restore-status-cards {
  margin-bottom: 30px;
}

.status-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.status-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: transparent;
  transition: all 0.3s ease;
}

.status-card.running {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.status-card.running::before {
  background: linear-gradient(90deg, #409eff, #66b1ff);
}

.status-card.success {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border: 1px solid rgba(103, 194, 58, 0.2);
}

.status-card.success::before {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.status-card.error {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  border: 1px solid rgba(245, 108, 108, 0.2);
}

.status-card.error::before {
  background: linear-gradient(90deg, #f56c6c, #f78989);
}

.status-card.info {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border: 1px solid rgba(220, 224, 229, 0.5);
}

.status-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.status-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  animation: pulse 2s infinite;
}

.status-card.running .status-icon-container {
  background: rgba(64, 158, 255, 0.1);
}

.status-card.success .status-icon-container {
  background: rgba(103, 194, 58, 0.1);
}

.status-card.error .status-icon-container {
  background: rgba(245, 108, 108, 0.1);
}

.status-icon {
  color: #409eff;
  animation: spin 2s linear infinite;
}

.status-card.running .status-icon {
  color: #409eff;
}

.status-card.success .status-icon {
  color: #67c23a;
  animation: none;
}

.status-card.error .status-icon {
  color: #f56c6c;
  animation: none;
}

.status-card:not(.running) .status-icon {
  animation: none;
}

.status-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.status-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.status-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  animation: countUp 1s ease-out;
}

.status-card.success .status-value {
  color: #67c23a;
}

.status-card.error .status-value {
  color: #f56c6c;
}

.status-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.progress-bar {
  margin-top: 8px;
  border-radius: 5px;
  overflow: hidden;
}

:deep(.el-progress__bar) {
  border-radius: 5px;
  transition: width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}

:deep(.el-progress__bar__inner) {
  border-radius: 5px;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  transition: width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.status-card.success :deep(.el-progress__bar__inner) {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.status-card.error :deep(.el-progress__bar__inner) {
  background: linear-gradient(90deg, #f56c6c, #f78989);
}

/* 恢复日志 */
.restore-log-section {
  margin-top: 30px;
}

.restore-log-container {
  height: 300px;
  overflow-y: auto;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 20px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3);
  position: relative;
}

.restore-log-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at top right, rgba(64, 158, 255, 0.05), transparent 50%);
  pointer-events: none;
}

.log-line {
  display: flex;
  gap: 16px;
  margin-bottom: 6px;
  word-break: break-all;
  animation: slideIn 0.3s ease-out;
}

.log-time {
  color: #6c757d;
  flex-shrink: 0;
  min-width: 80px;
  font-weight: 500;
}

.log-content {
  flex: 1;
}

.log-info .log-content {
  color: #e8e8e8;
}

.log-error .log-content {
  color: #f56c6c;
  font-weight: 500;
}

.log-warn .log-content {
  color: #e6a23c;
  font-weight: 500;
}

.log-success .log-content {
  color: #67c23a;
  font-weight: 500;
}

.log-progress .log-content {
  color: #409eff;
  font-weight: 500;
}

.log-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6c757d;
}

/* 恢复范围 */
.restore-scope {
  margin: 20px 0;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 滚动条样式 */
.restore-log-container::-webkit-scrollbar {
  width: 8px;
}

.restore-log-container::-webkit-scrollbar-track {
  background: #2d2d2d;
  border-radius: 4px;
}

.restore-log-container::-webkit-scrollbar-thumb {
  background: #4d4d4d;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.restore-log-container::-webkit-scrollbar-thumb:hover {
  background: #666;
}

/* 动画效果 */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .restore-status-cards {
    margin-bottom: 20px;
  }
  
  .status-card {
    margin-bottom: 16px;
  }
  
  .restore-log-container {
    height: 200px;
  }
}
</style>
