<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="应用名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入应用名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="enabled">
        <el-select v-model="queryParams.enabled" placeholder="应用状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
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
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['monitor:jvm:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['monitor:jvm:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['monitor:jvm:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="targetList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="targetId" />
      <el-table-column label="应用名称" align="center" prop="name" />
      <el-table-column label="主机" align="center" prop="host" />
      <el-table-column label="端口" align="center" prop="port" />
      <el-table-column label="状态" align="center" prop="enabled">
        <template #default="scope">
          <el-tag :type="scope.row.enabled === '0' ? 'success' : 'danger'">
            {{ scope.row.enabled === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Monitor"
            @click="handleMonitor(scope.row)"
            v-hasPermi="['monitor:jvm:query']"
          >监控</el-button>
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['monitor:jvm:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['monitor:jvm:remove']"
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

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="jvmFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="应用名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入应用名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="!form.targetId">
            <el-form-item label="关联服务器">
              <el-select 
                v-model="selectedServerId" 
                placeholder="请选择服务器(可选)" 
                clearable 
                filterable
                @change="handleServerSelect"
                style="width: 100%">
                <el-option
                  v-for="server in serverOptions"
                  :key="server.serverId"
                  :label="server.serverName + ' (' + server.publicIp + ')'"
                  :value="server.serverId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主机地址" prop="host">
              <el-input v-model="form.host" placeholder="请输入主机地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="JMX端口" prop="port">
              <el-input-number v-model="form.port" :min="1" :max="65535" label="端口" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入JMX用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入JMX密码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.enabled">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

    <!-- 监控详情对话框 -->
    <el-dialog title="JVM 实时监控" v-model="monitorOpen" width="800px" append-to-body @close="stopMonitor">
      <div v-loading="monitorLoading">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header><span>堆内存 (Heap)</span></template>
              <el-progress type="dashboard" :percentage="heapPercentage" :color="colors"></el-progress>
              <div class="monitor-item">已用: {{ formatBytes(currentMetric.heapUsed) }}</div>
              <div class="monitor-item">最大: {{ formatBytes(currentMetric.heapMax) }}</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header><span>非堆内存 (Non-Heap)</span></template>
              <el-progress type="dashboard" :percentage="nonHeapPercentage" :color="colors"></el-progress>
              <div class="monitor-item">已用: {{ formatBytes(currentMetric.nonHeapUsed) }}</div>
              <div class="monitor-item">最大: {{ formatBytes(currentMetric.nonHeapMax) }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header><span>线程</span></template>
              <div class="monitor-value">{{ currentMetric.threadActive }}</div>
              <div class="monitor-label">当前活跃线程</div>
              <div class="monitor-sub">峰值: {{ currentMetric.threadPeak }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header><span>GC 次数</span></template>
              <div class="monitor-value">{{ currentMetric.gcCount }}</div>
              <div class="monitor-label">总次数</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header><span>GC 时间</span></template>
              <div class="monitor-value">{{ currentMetric.gcTime }} ms</div>
              <div class="monitor-label">总耗时</div>
            </el-card>
          </el-col>
        </el-row>
        <div style="text-align: center; margin-top: 20px;">
             <el-button type="danger" icon="Delete" @click="handleGc">手动触发 GC</el-button>
             <el-button type="primary" icon="Refresh" @click="refreshMonitor">刷新</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="JvmTarget">
import { listTarget, getTarget, delTarget, addTarget, updateTarget, getRealtimeInfo, triggerGc } from "@/api/monitor/jvm";
import { listServer } from "@/api/ops/server";
import { getCurrentInstance, reactive, ref, toRefs, computed, onBeforeUnmount } from 'vue';

const { proxy } = getCurrentInstance();

const targetList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const monitorOpen = ref(false);
const monitorLoading = ref(false);
const monitorTimer = ref(null);
const currentTargetId = ref(null);

const serverOptions = ref([]);
const selectedServerId = ref(null);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    enabled: null
  },
  rules: {
    name: [
      { required: true, message: "应用名称不能为空", trigger: "blur" }
    ],
    host: [
      { required: true, message: "主机地址不能为空", trigger: "blur" }
    ],
    port: [
      { required: true, message: "JMX端口不能为空", trigger: "blur" }
    ]
  },
  currentMetric: {
    heapUsed: 0,
    heapMax: 100,
    nonHeapUsed: 0,
    nonHeapMax: 100,
    threadActive: 0,
    threadPeak: 0,
    gcCount: 0,
    gcTime: 0
  }
});

const { queryParams, form, rules, currentMetric } = toRefs(data);

const colors = [
  {color: '#5cb87a', percentage: 60},
  {color: '#e6a23c', percentage: 80},
  {color: '#f56c6c', percentage: 100}
];

const heapPercentage = computed(() => {
  if (!currentMetric.value.heapMax) return 0;
  return Math.min(Math.round((currentMetric.value.heapUsed / currentMetric.value.heapMax) * 100), 100);
});

const nonHeapPercentage = computed(() => {
  if (!currentMetric.value.nonHeapMax) return 0;
  if (currentMetric.value.nonHeapMax <= 0) return 0;
  return Math.min(Math.round((currentMetric.value.nonHeapUsed / currentMetric.value.nonHeapMax) * 100), 100);
});

/** 查询列表 */
function getList() {
  loading.value = true;
  listTarget(queryParams.value).then(response => {
    targetList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 查询服务器列表 */
function getServerList() {
    listServer({ pageNum: 1, pageSize: 100 }).then(response => {
        serverOptions.value = response.rows;
    });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    targetId: null,
    name: null,
    host: null,
    port: null,
    username: null,
    password: null,
    enabled: "0",
    remark: null
  };
  selectedServerId.value = null;
  proxy.resetForm("jvmFormRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.targetId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  getServerList(); // 获取服务器列表
  open.value = true;
  title.value = "添加监控目标";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const targetId = row.targetId || ids.value;
  getTarget(targetId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改监控目标";
  });
}

/** 服务器选择事件 */
function handleServerSelect(serverId) {
    if (!serverId) {
        form.value.host = '';
        return;
    }
    const server = serverOptions.value.find(s => s.serverId === serverId);
    if (server) {
        form.value.host = server.publicIp || server.privateIp;
        // 如果应用名称为空，也可以自动填充
        if (!form.value.name) {
            form.value.name = server.serverName;
        }
    }
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["jvmFormRef"].validate(valid => {
    if (valid) {
      if (form.value.targetId != null) {
        updateTarget(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addTarget(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const targetIds = row.targetId || ids.value;
  proxy.$modal.confirm('是否确认删除监控目标编号为"' + targetIds + '"的数据项？').then(function() {
    return delTarget(targetIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 监控按钮操作 */
function handleMonitor(row) {
    currentTargetId.value = row.targetId;
    monitorOpen.value = true;
    refreshMonitor();
    // Start timer
    monitorTimer.value = setInterval(refreshMonitor, 5000);
}

function stopMonitor() {
    if (monitorTimer.value) {
        clearInterval(monitorTimer.value);
        monitorTimer.value = null;
    }
}

function refreshMonitor() {
    monitorLoading.value = true;
    getRealtimeInfo(currentTargetId.value).then(res => {
        if (res.data) {
            currentMetric.value = res.data;
        }
        monitorLoading.value = false;
    }).catch(() => {
        monitorLoading.value = false;
        stopMonitor();
    });
}

function handleGc() {
    proxy.$modal.confirm('确认要手动触发GC吗？这将影响应用性能！').then(() => {
        triggerGc(currentTargetId.value).then(() => {
            proxy.$modal.msgSuccess("GC 触发指令已发送");
            // Wait a bit and refresh
            setTimeout(refreshMonitor, 2000);
        });
    });
}

function formatBytes(bytes) {
    if (bytes === 0) return '0 B';
    if (!bytes) return '-';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

onBeforeUnmount(() => {
    stopMonitor();
});

getList();
</script>

<style scoped>
.monitor-item {
    font-size: 14px;
    color: #606266;
    margin-top: 10px;
    text-align: center;
}
.monitor-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    text-align: center;
}
.monitor-label {
    font-size: 12px;
    color: #909399;
    text-align: center;
}
.monitor-sub {
    font-size: 12px;
    color: #909399;
    text-align: center;
    margin-top: 5px;
}
</style>
