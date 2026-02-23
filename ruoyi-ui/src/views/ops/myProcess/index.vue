<template>
  <div class="app-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>我发起的流程</span>
        </div>
      </template>

      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
        <el-form-item label="流程名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入流程名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="processList">
        <el-table-column label="流程实例ID" prop="id" align="center" width="100" />
        <el-table-column label="流程名称" prop="processDefinitionName" align="center" />
        <el-table-column label="业务Key" prop="businessKey" align="center" />
        <el-table-column label="开始时间" prop="startTime" align="center" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结束时间" prop="endTime" align="center" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.endTime) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="耗时" align="center" width="150">
          <template #default="scope">
            <span>{{ formatDuration(scope.row.durationInMillis) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.endTime" type="success">已结束</el-tag>
            <el-tag v-else type="primary">进行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              size="small"
              type="text"
              icon="View"
              @click="handleView(scope.row)"
            >查看进度</el-button>
            <el-button
              v-if="!scope.row.endTime"
              size="small"
              type="text"
              icon="Bell"
              @click="handleRemind(scope.row)"
            >催办</el-button>
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
    </el-card>

    <!-- 流程进度对话框 -->
    <el-dialog title="流程进度" v-model="openView" width="1000px" append-to-body destroy-on-close>
      <div style="height: 500px; border: 1px solid #dcdfe6; position: relative;">
        <BpmnViewer 
          v-if="bpmnXml"
          :xml="bpmnXml" 
          :activeActivityIds="activeActivityIds" 
          :activeTaskInfo="activeTaskInfo"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MyProcess">
import { listMyStartedProcesses, getProcessProgress, remindProcessInstance } from "@/api/ops/workflow";
import BpmnViewer from "@/components/BpmnViewer";
import { getCurrentInstance, ref, reactive, toRefs, onMounted } from "vue";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const processList = ref([]);
const total = ref(0);
const openView = ref(false);
const bpmnXml = ref("");
const activeActivityIds = ref([]);
const activeTaskInfo = ref([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listMyStartedProcesses(queryParams.value).then(response => {
    processList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleView(row) {
  bpmnXml.value = "";
  activeActivityIds.value = [];
  activeTaskInfo.value = [];
  openView.value = true;
  
  getProcessProgress(row.id).then(res => {
    bpmnXml.value = res.data.bpmnXml;
    activeActivityIds.value = res.data.activeActivityIds || [];
    activeTaskInfo.value = res.data.activeTaskInfo || [];
  });
}

function handleRemind(row) {
  proxy.$modal.confirm('确认向当前审批人发送提醒吗？').then(() => {
    return remindProcessInstance(row.id);
  }).then(() => {
    proxy.$modal.msgSuccess("提醒发送成功");
  }).catch(() => {});
}

function formatDuration(ms) {
  if (!ms) return '-';
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  
  if (days > 0) return `${days}天${hours % 24}小时`;
  if (hours > 0) return `${hours}小时${minutes % 60}分`;
  if (minutes > 0) return `${minutes}分${seconds % 60}秒`;
  return `${seconds}秒`;
}

onMounted(() => {
  getList();
});
</script>
