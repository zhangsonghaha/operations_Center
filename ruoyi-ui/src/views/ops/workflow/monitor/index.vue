<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="流程Key" prop="key">
        <el-input
          v-model="queryParams.key"
          placeholder="请输入流程定义Key"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发起人ID" prop="startedBy">
        <el-input
          v-model="queryParams.startedBy"
          placeholder="请输入发起人ID"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="instanceList">
      <el-table-column label="实例ID" align="center" prop="id" width="100" />
      <el-table-column label="流程名称" align="center" prop="processDefinitionName" />
      <el-table-column label="流程Key" align="center" prop="processDefinitionKey" />
      <el-table-column label="发起人" align="center" prop="startUserNickName" width="100">
        <template #default="scope">
          <span>{{ scope.row.startUserNickName || scope.row.startUserId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.endTime" type="info">已结束</el-tag>
          <el-tag v-else type="success">运行中</el-tag>
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
            size="small"
            type="text"
            icon="Delete"
            style="color: #F56C6C"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:workflow:monitor']"
          >删除</el-button>
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

    <!-- 流程进度对话框 -->
    <el-dialog title="流程进度" v-model="openView" width="1000px" append-to-body destroy-on-close>
      <div style="height: 500px; border: 1px solid #dcdfe6;">
        <BpmnViewer 
          :xml="bpmnXml" 
          :activeActivityIds="activeActivityIds" 
          :activeTaskInfo="activeTaskInfo"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="WorkflowMonitor">
import { listMonitorProcessInstances, getProcessProgress, deleteProcessInstance } from "@/api/ops/workflow";
import BpmnViewer from "@/components/BpmnViewer";
import { getCurrentInstance, ref, reactive, toRefs, onMounted } from "vue";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const instanceList = ref([]);
const total = ref(0);
const openView = ref(false);
const bpmnXml = ref("");
const activeActivityIds = ref([]);
const activeTaskInfo = ref([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    key: undefined,
    startedBy: undefined
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listMonitorProcessInstances(queryParams.value).then(response => {
    instanceList.value = response.rows;
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

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该流程实例？').then(function() {
    return deleteProcessInstance(row.id);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

onMounted(() => {
  getList();
});
</script>
