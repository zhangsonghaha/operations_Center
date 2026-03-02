<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch" label-width="68px">
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
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
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
          <el-tag v-if="scope.row.status == '1'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.status == '2'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.status == '3'" type="danger">已驳回</el-tag>
          <el-tag v-else-if="scope.row.status == '4'" type="primary">已发布</el-tag>
          <el-tag v-else-if="scope.row.status == '5'" type="danger">发布失败</el-tag>
          <el-tag v-else type="info">草稿</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发起人" align="center" prop="applyUserName" width="120">
        <template #default="scope">
          <span>{{ scope.row.applyUserName || scope.row.createBy || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发起时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="View"
            @click="handleViewProcess(scope.row)"
            v-if="scope.row.processInstanceId"
          >流程</el-button>
          <el-button
            size="small"
            type="text"
            icon="Check"
            @click="handleAudit(scope.row)"
            v-hasPermi="['ops:release:audit']"
            v-if="scope.row.status == '1'"
          >审批</el-button>
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

    <el-dialog title="发布审批" v-model="auditOpen" width="500px" append-to-body>
      <el-form ref="auditFormRef" :model="auditForm" label-width="80px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="2">通过</el-radio>
            <el-radio label="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="auditReason">
          <el-input v-model="auditForm.auditReason" type="textarea" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">提 交</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="流程追踪" v-model="processOpen" width="1000px" append-to-body destroy-on-close>
      <div v-if="bpmnXml" style="height: 500px; border: 1px solid #dcdfe6;">
        <BpmnViewer :xml="bpmnXml" :activeActivityIds="activeActivityIds" />
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

    <el-dialog title="部署配置确认" v-model="deployConfirmOpen" width="900px" append-to-body destroy-on-close>
      <div v-if="currentApp">
        <el-descriptions title="应用信息" :column="2" border>
          <el-descriptions-item label="应用名称">{{ currentApp.appName }}</el-descriptions-item>
          <el-descriptions-item label="应用类型">
            <dict-tag :options="sys_ops_app_type" :value="currentApp.appType" />
          </el-descriptions-item>
          <el-descriptions-item label="发布标题">{{ currentReleaseTitle }}</el-descriptions-item>
          <el-descriptions-item label="版本号">{{ currentReleaseVersion }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">部署配置</el-divider>
        
        <el-descriptions :column="1" border style="margin-top: 20px;">
          <el-descriptions-item label="部署路径">{{ currentApp.deployPath || '-' }}</el-descriptions-item>
          <el-descriptions-item label="监控端口">{{ currentApp.monitorPorts || '-' }}</el-descriptions-item>
          <el-descriptions-item label="健康检查">{{ currentApp.healthCheckUrl || '-' }}</el-descriptions-item>
          <el-descriptions-item label="超时时间">{{ currentApp.deployTimeout || 60 }} 秒</el-descriptions-item>
          <el-descriptions-item label="重试次数">{{ currentApp.retryCount || 0 }} 次</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">关联服务器</el-divider>
        
        <el-descriptions :column="1" border style="margin-top: 20px;">
          <el-descriptions-item label="关联服务器">
            <el-tag v-for="serverId in serverIdList" :key="serverId" style="margin-right: 5px; margin-bottom: 5px;">
              {{ serverId }}
            </el-tag>
            <span v-if="!hasServerIds" style="color: #909399">未配置服务器</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">启动脚本</el-divider>
        
        <div style="margin-top: 20px; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; background: #f5f7fa;">
          <pre style="white-space: pre-wrap; margin: 0; font-size: 12px; max-height: 200px; overflow-y: auto;">{{ currentApp.startScript || '未配置启动脚本' }}</pre>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deployConfirmOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmDeploy">确认部署</el-button>
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

<script setup name="ReleasePending">
import { listReleasePending, auditRelease, executeRelease } from "@/api/ops/release";
import { listApp, getApp } from "@/api/ops/app";
import { getProcessProgress } from "@/api/ops/workflow";
import BpmnViewer from "@/components/BpmnViewer";
import { getCurrentInstance, reactive, ref, toRefs, computed, onUnmounted } from 'vue';
import { getDeployRecord } from "@/api/ops/deployRecord";

const { proxy } = getCurrentInstance();
const { sys_ops_app_type } = proxy.useDict('sys_ops_app_type');

const releaseList = ref([]);
const auditOpen = ref(false);
const deployConfirmOpen = ref(false);
const processOpen = ref(false);
const bpmnXml = ref("");
const activeActivityIds = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const total = ref(0);

const appOptions = ref([]);
const currentRelease = ref(null);
const currentApp = ref(null);

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
  auditForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    appId: undefined,
    status: "1"
  }
});

const { queryParams, auditForm } = toRefs(data);

const currentReleaseTitle = computed(() => {
  return currentRelease.value?.title || '-';
});

const currentReleaseVersion = computed(() => {
  return currentRelease.value?.version || '-';
});

const hasServerIds = computed(() => {
  return currentApp.value && currentApp.value.serverIds;
});

const serverIdList = computed(() => {
  if (!currentApp.value || !currentApp.value.serverIds) {
    return [];
  }
  return currentApp.value.serverIds.split(',');
});

function getList() {
  loading.value = true;
  listReleasePending(queryParams.value).then(response => {
    releaseList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getAppList() {
  listApp().then(response => {
    appOptions.value = response.rows;
  });
}

function getAppName(appId) {
  const app = appOptions.value.find(item => item.appId === appId);
  return app ? app.appName : appId;
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryForm");
  queryParams.value.status = "1";
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
}

function handleAudit(row) {
  auditForm.value = {
    id: row.id,
    status: "2",
    auditReason: ""
  };
  auditOpen.value = true;
}

function submitAudit() {
  proxy.$refs["auditFormRef"].validate(valid => {
    if (valid) {
      if (auditForm.value.status === '3' && !auditForm.value.auditReason) {
        proxy.$modal.msgError("驳回时必须填写审批意见");
        return;
      }
      
      if (auditForm.value.status === '2') {
        const releaseItem = releaseList.value.find(item => item.id === auditForm.value.id);
        if (releaseItem) {
          currentRelease.value = releaseItem;
          auditOpen.value = false;
          getApp(releaseItem.appId).then(res => {
            currentApp.value = res.data;
            deployConfirmOpen.value = true;
          });
          return;
        }
      }
      
      auditRelease(auditForm.value).then(() => {
        proxy.$modal.msgSuccess("审批完成");
        auditOpen.value = false;
        getList();
      });
    }
  });
}

function confirmDeploy() {
  auditRelease({
    id: auditForm.value.id,
    status: '2',
    auditReason: auditForm.value.auditReason
  }).then(() => {
    deployConfirmOpen.value = false;
    proxy.$modal.msgSuccess("审批通过，开始部署...");
    executeRelease(auditForm.value.id).then(res => {
      const recordId = res.data;
      proxy.$modal.msgSuccess("部署任务已启动");
      if (recordId) {
        openDeployLog(recordId);
      }
    }).catch(() => {
      proxy.$modal.msgError("部署启动失败");
    });
    getList();
  });
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

function handleViewProcess(row) {
  if (row.processInstanceId) {
    bpmnXml.value = "";
    activeActivityIds.value = [];
    processOpen.value = true;
    getProcessProgress(row.processInstanceId).then(res => {
      bpmnXml.value = res.data.bpmnXml;
      activeActivityIds.value = res.data.activeActivityIds || [];
    }).catch(() => {
      proxy.$modal.msgError("获取流程进度失败");
      processOpen.value = false;
    });
  }
}

getAppList();
getList();

onUnmounted(() => {
  stopDeployLogPolling();
});
</script>
