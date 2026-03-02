<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="发布标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入发布标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="应用名称" prop="appId">
        <el-select v-model="queryParams.appId" placeholder="请选择应用" clearable filterable>
          <el-option
            v-for="item in appOptions"
            :key="item.appId"
            :label="item.appName"
            :value="item.appId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="草稿" value="0" />
          <el-option label="待审批" value="1" />
          <el-option label="已通过" value="2" />
          <el-option label="已驳回" value="3" />
          <el-option label="已发布" value="4" />
          <el-option label="发布失败" value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['ops:release:add']"
        >新增申请</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['ops:release:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:release:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="releaseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请ID" align="center" prop="id" width="80" />
      <el-table-column label="发布标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="应用名称" align="center" prop="appName">
        <template #default="scope">
          {{ getAppName(scope.row.appId) }}
        </template>
      </el-table-column>
      <el-table-column label="版本号" align="center" prop="version" width="100" />
      <el-table-column label="审批进度" align="center" width="120">
        <template #default="scope">
           <span v-if="scope.row.totalSteps > 0">
             第 {{ scope.row.currentStep }} / {{ scope.row.totalSteps }} 步
           </span>
           <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status == '0'" type="info">草稿</el-tag>
          <el-tag v-else-if="scope.row.status == '1'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.status == '2'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.status == '3'" type="danger">已驳回</el-tag>
          <el-tag v-else-if="scope.row.status == '4'" type="primary">已发布</el-tag>
          <el-tag v-else-if="scope.row.status == '5'" type="danger">发布失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划发布时间" align="center" prop="scheduleTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.scheduleTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请人" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            link
            type="primary"
            icon="View"
            @click="handleViewProcess(scope.row)"
            v-if="scope.row.processInstanceId"
          >流程</el-button>
          <el-button
            size="small"
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['ops:release:edit']"
            v-if="scope.row.status == '0' || scope.row.status == '3'"
          >修改</el-button>
          <el-button
            size="small"
            link
            type="primary"
            icon="VideoPlay"
            @click="handleExecute(scope.row)"
            v-hasPermi="['ops:release:deploy']"
            v-if="scope.row.status == '2'"
          >发布</el-button>
          <el-button
            size="small"
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:release:remove']"
            v-if="scope.row.status == '0' || scope.row.status == '3'"
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

    <!-- 添加或修改发布申请对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="发布标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入发布标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="请输入版本号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择应用" prop="appId">
              <el-select v-model="form.appId" placeholder="请选择应用" filterable style="width: 100%">
                <el-option
                  v-for="item in appOptions"
                  :key="item.appId"
                  :label="item.appName"
                  :value="item.appId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署模板" prop="templateId">
              <el-select v-model="form.templateId" placeholder="请选择部署模板" filterable style="width: 100%">
                <el-option
                  v-for="item in templateOptions"
                  :key="item.id"
                  :label="item.templateName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联需求" prop="requirementIds">
              <el-input v-model="form.requirementIds" placeholder="请输入关联需求ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联Bug" prop="bugIds">
              <el-input v-model="form.bugIds" placeholder="请输入关联BugID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划时间" prop="scheduleTime">
              <el-date-picker
                v-model="form.scheduleTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择计划发布时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发布说明" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入发布变更内容说明" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="风险点" prop="risks">
              <el-input v-model="form.risks" type="textarea" placeholder="请输入可能的风险点" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="回滚方案" prop="rollbackPlan">
              <el-input v-model="form.rollbackPlan" type="textarea" placeholder="请输入回滚方案" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 流程追踪对话框 -->
    <el-dialog title="流程追踪" v-model="processOpen" width="1000px" append-to-body destroy-on-close>
      <div v-if="bpmnXml" style="height: 500px; border: 1px solid #dcdfe6; position: relative;">
        <BpmnViewer :xml="bpmnXml" :activeActivityIds="activeActivityIds" :activeTaskInfo="activeTaskInfo" />
      </div>
      <div v-else style="text-align: center; padding: 20px;">
        暂无流程图数据
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="processOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 部署配置确认对话框 -->
    <el-dialog 
      title="部署配置确认" 
      v-model="deployConfirmOpen" 
      width="950px" 
      append-to-body 
      destroy-on-close
      custom-class="deploy-confirm-dialog"
      :before-close="() => false"
    >
      <div v-if="currentApp" class="deploy-confirm-content">
        <!-- 应用信息卡片 -->
        <el-card class="mb-6 deploy-card">
          <template #header>
            <div class="card-header">
              <el-avatar :size="32" class="avatar">
                <el-icon class="icon"><i-ep-apple /></el-icon>
              </el-avatar>
              <div class="header-text">
                <h3 class="title">应用信息</h3>
                <p class="subtitle">查看应用基本信息和发布详情</p>
              </div>
            </div>
          </template>
          <div class="app-info-grid">
            <div class="info-item">
              <span class="info-label">应用名称</span>
              <span class="info-value">{{ currentApp.appName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布标题</span>
              <span class="info-value">{{ currentRelease.title }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">版本号</span>
              <span class="info-value version">{{ currentRelease.version }}</span>
            </div>
          </div>
        </el-card>
        
        <!-- 部署配置卡片 -->
        <el-card class="mb-6 deploy-card">
          <template #header>
            <div class="card-header">
              <el-avatar :size="32" class="avatar bg-indigo">
                <el-icon class="icon"><i-ep-setting /></el-icon>
              </el-avatar>
              <div class="header-text">
                <h3 class="title">部署配置</h3>
                <p class="subtitle">检查部署路径和相关配置参数</p>
              </div>
            </div>
          </template>
          <div class="config-grid">
            <div class="config-item">
              <div class="config-icon">
                <el-icon><i-ep-folder /></el-icon>
              </div>
              <div class="config-content">
                <span class="config-label">部署路径</span>
                <span class="config-value">{{ currentApp.deployPath || '-' }}</span>
              </div>
            </div>
            <div class="config-item">
              <div class="config-icon">
                <el-icon><i-ep-data-line /></el-icon>
              </div>
              <div class="config-content">
                <span class="config-label">监控端口</span>
                <span class="config-value">{{ currentApp.monitorPorts || '-' }}</span>
              </div>
            </div>
            <div class="config-item">
              <div class="config-icon">
                <el-icon><i-ep-check-circle /></el-icon>
              </div>
              <div class="config-content">
                <span class="config-label">健康检查</span>
                <span class="config-value">{{ currentApp.healthCheckUrl || '-' }}</span>
              </div>
            </div>
            <div class="config-item">
              <div class="config-icon">
                <el-icon><i-ep-timer /></el-icon>
              </div>
              <div class="config-content">
                <span class="config-label">超时时间</span>
                <span class="config-value">{{ currentApp.deployTimeout || 60 }} 秒</span>
              </div>
            </div>
            <div class="config-item">
              <div class="config-icon">
                <el-icon><i-ep-refresh /></el-icon>
              </div>
              <div class="config-content">
                <span class="config-label">重试次数</span>
                <span class="config-value">{{ currentApp.retryCount || 0 }} 次</span>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 关联服务器卡片 -->
        <el-card class="mb-6 deploy-card">
          <template #header>
            <div class="card-header">
              <el-avatar :size="32" class="avatar bg-emerald">
                <el-icon class="icon"><i-ep-server /></el-icon>
              </el-avatar>
              <div class="header-text">
                <h3 class="title">关联服务器</h3>
                <p class="subtitle">查看部署目标服务器</p>
              </div>
            </div>
          </template>
          <div v-if="currentApp.serverIds" class="server-list">
            <div 
              v-for="sid in currentApp.serverIds.split(',')" 
              :key="sid" 
              class="server-item"
            >
              <div class="server-icon">
                <el-icon><i-ep-monitor /></el-icon>
              </div>
              <div class="server-info">
                <div class="server-name">{{ getServerName(sid) }}</div>
                <div class="server-id">服务器 ID: {{ sid }}</div>
              </div>
              <div class="server-status">
                <el-tag size="small" type="success">可用</el-tag>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty 
              description="
                <span class='empty-text'>未配置服务器</span>
              " 
            >
              <template #image>
                <div class="empty-icon">
                  <el-icon><i-ep-server /></el-icon>
                </div>
              </template>
            </el-empty>
          </div>
        </el-card>
        
        <!-- 启动脚本卡片 -->
        <el-card class="deploy-card">
          <template #header>
            <div class="card-header">
              <el-avatar :size="32" class="avatar bg-amber">
                <el-icon class="icon"><i-ep-code /></el-icon>
              </el-avatar>
              <div class="header-text">
                <h3 class="title">启动脚本</h3>
                <p class="subtitle">查看应用启动脚本内容</p>
              </div>
            </div>
          </template>
          <div v-if="currentApp.startScript" class="script-container">
            <div class="script-header">
              <span class="script-name">start.sh</span>
              <span class="script-length">{{ currentApp.startScript.length }} 字符</span>
            </div>
            <div class="script-content">
              <pre>{{ currentApp.startScript }}</pre>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty 
              description="
                <span class='empty-text'>未配置启动脚本</span>
              " 
            >
              <template #image>
                <div class="empty-icon">
                  <el-icon><i-ep-code /></el-icon>
                </div>
              </template>
            </el-empty>
          </div>
        </el-card>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button 
            @click="deployConfirmOpen = false" 
            class="cancel-button"
          >
            <el-icon class="mr-1"><i-ep-close /></el-icon>
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="confirmDeploy"
            class="deploy-button"
          >
            <el-icon class="mr-1"><i-ep-check /></el-icon>
            确认部署
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 部署执行日志对话框 -->
    <el-dialog
      title="部署执行日志"
      v-model="deployLogVisible"
      width="800px"
      append-to-body
      destroy-on-close
    >
      <div style="margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="margin-right: 12px;">记录ID：{{ deployLog.recordId || '-' }}</span>
          <span>状态：
            <el-tag v-if="deployLog.status === '0'" type="warning" size="small">执行中</el-tag>
            <el-tag v-else-if="deployLog.status === '2'" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="deployLog.status === '1'" type="danger" size="small">失败</el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </span>
          <span v-if="deployLog.exitCode !== null" style="margin-left: 12px;">
            退出码：{{ deployLog.exitCode }}
          </span>
        </div>
        <el-button
          size="small"
          type="primary"
          text
          @click="refreshDeployLog"
        >刷新</el-button>
      </div>
      <el-scrollbar height="420px">
        <pre
          style="background:#1e293b;color:#e5e7eb;padding:12px;border-radius:4px;font-size:12px;line-height:1.6;white-space:pre-wrap;margin:0;"
        >{{ deployLog.output || '暂无日志输出' }}</pre>
      </el-scrollbar>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDeployLog">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Release">
import { listRelease, getRelease, delRelease, addRelease, updateRelease, executeRelease } from "@/api/ops/release";
import { listApp, getApp } from "@/api/ops/app";
import { listDeployTemplate } from "@/api/ops/deployTemplate";
import { getProcessProgress } from "@/api/ops/workflow";
import BpmnViewer from "@/components/BpmnViewer";
import { getCurrentInstance, reactive, ref, toRefs, onMounted, onUnmounted } from 'vue';
import { getDeployRecord } from "@/api/ops/deployRecord";

const { proxy } = getCurrentInstance();

const releaseList = ref([]);
const open = ref(false);
const processOpen = ref(false);
const deployConfirmOpen = ref(false);
const currentRelease = ref(null);
const currentApp = ref(null);
const bpmnXml = ref("");
const activeActivityIds = ref([]);
const activeTaskInfo = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const appOptions = ref([]);
const templateOptions = ref([]);
const serverOptions = ref([]);

// 部署日志相关
const deployLogVisible = ref(false);
const deployLog = reactive({
  recordId: null,
  status: '',
  exitCode: null,
  output: '',
});
const deployLogLoading = ref(false);
let deployLogTimer = null;

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    appId: undefined,
    status: undefined
  },
  rules: {
    title: [
      { required: true, message: "发布标题不能为空", trigger: "blur" }
    ],
    version: [
      { required: true, message: "版本号不能为空", trigger: "blur" }
    ],
    appId: [
      { required: true, message: "应用不能为空", trigger: "change" }
    ],
    templateId: [
      { required: true, message: "部署模板不能为空", trigger: "change" }
    ],
    description: [
      { required: true, message: "发布说明不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询发布申请列表 */
function getList() {
  loading.value = true;
  listRelease(queryParams.value).then(response => {
    releaseList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 查询应用列表 */
function getAppList() {
  listApp().then(response => {
    appOptions.value = response.rows;
  });
}

/** 查询模板列表 */
function getTemplateList() {
  listDeployTemplate().then(response => {
    templateOptions.value = response.rows;
  });
}

/** 查询服务器列表 */
function getServerList() {
  import('@/api/ops/server').then(({ listServer }) => {
    listServer().then(response => {
      serverOptions.value = response.rows;
    });
  });
}

/** 获取服务器名称 */
function getServerName(serverId) {
  const server = serverOptions.value.find(item => item.serverId === parseInt(serverId));
  return server ? server.serverName : serverId;
}

/** 获取应用名称 */
function getAppName(appId) {
  const app = appOptions.value.find(item => item.appId === appId);
  return app ? app.appName : appId;
}

function openDeployLog(recordId) {
  if (!recordId) {
    proxy.$modal.msgWarning("未获取到部署记录ID，无法显示日志");
    return;
  }
  deployLog.recordId = recordId;
  deployLog.status = '0';
  deployLog.exitCode = null;
  deployLog.output = '';
  deployLogVisible.value = true;
  startDeployLogPolling();
}

function startDeployLogPolling() {
  stopDeployLogPolling();
  fetchDeployLog();
  deployLogTimer = setInterval(() => {
    fetchDeployLog();
  }, 2000);
}

function stopDeployLogPolling() {
  if (deployLogTimer) {
    clearInterval(deployLogTimer);
    deployLogTimer = null;
  }
}

function fetchDeployLog() {
  if (!deployLog.recordId) return;
  deployLogLoading.value = true;
  getDeployRecord(deployLog.recordId)
    .then(res => {
      const data = res.data || {};
      deployLog.status = data.status || '';
      deployLog.output = '';
      deployLog.exitCode = null;
      if (data.jsonResult) {
        try {
          const parsed = JSON.parse(data.jsonResult);
          deployLog.output = parsed.output || '';
          if (parsed.exitCode !== undefined && parsed.exitCode !== null) {
            deployLog.exitCode = parsed.exitCode;
          }
        } catch (e) {
          deployLog.output = String(data.jsonResult);
        }
      }
      if (deployLog.status === '1' || deployLog.status === '2') {
        stopDeployLogPolling();
      }
    })
    .catch(err => {
      deployLog.output = '获取部署日志失败: ' + (err?.message || '');
      if (!deployLog.status) {
        deployLog.status = '1';
      }
      stopDeployLogPolling();
    })
    .finally(() => {
      deployLogLoading.value = false;
    });
}

function refreshDeployLog() {
  fetchDeployLog();
}

function closeDeployLog() {
  deployLogVisible.value = false;
  stopDeployLogPolling();
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    appId: undefined,
    templateId: undefined,
    version: undefined,
    title: undefined,
    description: undefined,
    risks: undefined,
    rollbackPlan: undefined,
    requirementIds: undefined,
    bugIds: undefined,
    status: "0",
    scheduleTime: undefined
  };
  proxy.resetForm("formRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryForm");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "发起发布申请";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getRelease(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改发布申请";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 默认新增为待审批状态，如果是草稿状态修改提交，可以改为待审批
      if (form.value.status === '0') {
         // Optionally ask user if they want to submit for audit directly or save as draft
         // For simplicity, let's assume 'Submit' button saves and potentially submits.
         // Let's keep status as is if editing, or set to 1 (Pending Audit) if new?
         // User requirement: "发起发布申请...". Usually implies submitting.
         form.value.status = "1"; 
      }
      
      if (form.value.id != null) {
        updateRelease(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        form.value.status = "1"; // Default to pending audit
        addRelease(form.value).then(response => {
          proxy.$modal.msgSuccess("申请提交成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const deleteIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除发布申请编号为"' + deleteIds + '"的数据项？').then(function() {
    return delRelease(deleteIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 执行发布操作 */
function handleExecute(row) {
  currentRelease.value = row;
  getApp(row.appId).then(res => {
    currentApp.value = res.data;
    deployConfirmOpen.value = true;
  });
}

function confirmDeploy() {
  deployConfirmOpen.value = false;
  executeRelease(currentRelease.value.id).then(res => {
    const recordId = res.data;
    proxy.$modal.msgSuccess("发布指令已下发");
    if (recordId) {
      openDeployLog(recordId);
    }
    getList();
  }).catch(() => {
    proxy.$modal.msgError("部署启动失败");
  });
}

/** 流程追踪 */
function handleViewProcess(row) {
  if (row.processInstanceId) {
    bpmnXml.value = "";
    activeActivityIds.value = [];
    activeTaskInfo.value = [];
    processOpen.value = true;
    getProcessProgress(row.processInstanceId).then(res => {
      bpmnXml.value = res.data.bpmnXml;
      activeActivityIds.value = res.data.activeActivityIds || [];
      activeTaskInfo.value = res.data.activeTaskInfo || [];
    }).catch(() => {
      proxy.$modal.msgError("获取流程进度失败");
      processOpen.value = false;
    });
  }
}

onUnmounted(() => {
  stopDeployLogPolling();
});

// Init
getAppList();
getTemplateList();
getServerList();
getList();
</script>

<style scoped>
/* 主对话框样式 */
.deploy-confirm-dialog {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  
  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 24px 32px;
    border-radius: 12px 12px 0 0;
  }
  
  .el-dialog__title {
    font-size: 20px;
    font-weight: 700;
    margin: 0;
  }
  
  .el-dialog__body {
    padding: 32px;
    background: #f8fafc;
  }
  
  .el-dialog__footer {
    padding: 24px 32px;
    background: white;
    border-top: 1px solid #e2e8f0;
    border-radius: 0 0 12px 12px;
  }
}

/* 内容区域 */
.deploy-confirm-content {
  .deploy-card {
    border: none;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
    
    .el-card__body {
      padding: 24px;
    }
  }
  
  /* 卡片头部 */
  .card-header {
    display: flex;
    align-items: center;
    padding: 20px 24px;
    background: white;
    border-bottom: 1px solid #f1f5f9;
    
    .avatar {
      margin-right: 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      
      &.bg-indigo {
        background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
      }
      
      &.bg-emerald {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      }
      
      &.bg-amber {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }
      
      .icon {
        font-size: 18px;
        color: white;
      }
    }
    
    .header-text {
      flex: 1;
      
      .title {
        font-size: 18px;
        font-weight: 600;
        color: #1e293b;
        margin: 0 0 4px 0;
      }
      
      .subtitle {
        font-size: 14px;
        color: #64748b;
        margin: 0;
      }
    }
  }
  
  /* 应用信息网格 */
  .app-info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    
    .info-item {
      display: flex;
      flex-direction: column;
      gap: 8px;
      
      .info-label {
        font-size: 14px;
        color: #64748b;
        font-weight: 500;
      }
      
      .info-value {
        font-size: 16px;
        color: #1e293b;
        font-weight: 600;
        
        &.version {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
      }
    }
  }
  
  /* 配置网格 */
  .config-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    
    .config-item {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding: 16px;
      background: #f8fafc;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #667eea;
        box-shadow: 0 2px 4px rgba(102, 126, 234, 0.1);
      }
      
      .config-icon {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: white;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
        color: #667eea;
        font-size: 20px;
        flex-shrink: 0;
      }
      
      .config-content {
        flex: 1;
        
        .config-label {
          display: block;
          font-size: 14px;
          color: #64748b;
          margin-bottom: 4px;
        }
        
        .config-value {
          font-size: 16px;
          color: #1e293b;
          font-weight: 600;
        }
      }
    }
  }
  
  /* 服务器列表 */
  .server-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .server-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: white;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #10b981;
        box-shadow: 0 2px 4px rgba(16, 185, 129, 0.1);
      }
      
      .server-icon {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        border-radius: 12px;
        color: white;
        font-size: 24px;
        flex-shrink: 0;
      }
      
      .server-info {
        flex: 1;
        
        .server-name {
          font-size: 16px;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 4px;
        }
        
        .server-id {
          font-size: 14px;
          color: #64748b;
        }
      }
      
      .server-status {
        flex-shrink: 0;
      }
    }
  }
  
  /* 脚本容器 */
  .script-container {
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    
    .script-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f8fafc;
      border-bottom: 1px solid #e2e8f0;
      
      .script-name {
        font-size: 14px;
        font-weight: 600;
        color: #1e293b;
      }
      
      .script-length {
        font-size: 12px;
        color: #64748b;
      }
    }
    
    .script-content {
      background: #1e293b;
      padding: 20px;
      
      pre {
        margin: 0;
        font-size: 13px;
        line-height: 1.6;
        color: #f8fafc;
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        white-space: pre-wrap;
        max-height: 300px;
        overflow-y: auto;
        
        &::-webkit-scrollbar {
          width: 6px;
        }
        
        &::-webkit-scrollbar-track {
          background: #2d3748;
          border-radius: 3px;
        }
        
        &::-webkit-scrollbar-thumb {
          background: #4a5568;
          border-radius: 3px;
          
          &:hover {
            background: #718096;
          }
        }
      }
    }
  }
  
  /* 空状态 */
  .empty-state {
    padding: 60px 0;
    text-align: center;
    
    .empty-icon {
      width: 80px;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f8fafc;
      border-radius: 20px;
      margin: 0 auto 20px;
      color: #94a3b8;
      font-size: 40px;
    }
    
    .empty-text {
      font-size: 16px;
      color: #64748b;
      font-weight: 500;
    }
  }
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  
  .cancel-button {
    padding: 10px 24px;
    font-size: 14px;
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.2s ease;
    
    &:hover {
      background: #f1f5f9;
      color: #334155;
    }
  }
  
  .deploy-button {
    padding: 10px 24px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    transition: all 0.2s ease;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .deploy-confirm-dialog {
    width: 95% !important;
    
    .el-dialog__body {
      padding: 20px;
    }
  }
  
  .deploy-confirm-content {
    .config-grid {
      grid-template-columns: 1fr;
    }
    
    .app-info-grid {
      grid-template-columns: 1fr;
    }
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.deploy-card {
  animation: fadeIn 0.4s ease-out;
}

.deploy-card:nth-child(1) { animation-delay: 0.05s; }
.deploy-card:nth-child(2) { animation-delay: 0.1s; }
.deploy-card:nth-child(3) { animation-delay: 0.15s; }
.deploy-card:nth-child(4) { animation-delay: 0.2s; }
</style>
