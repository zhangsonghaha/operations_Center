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
      <el-form-item label="流程名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入流程名称"
          clearable
          size="small"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="只查最新" prop="latestOnly">
        <el-switch v-model="queryParams.latestOnly" active-text="是" inactive-text="否" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="processList">
      <el-table-column label="流程ID" align="center" prop="id" width="280" />
      <el-table-column label="流程Key" align="center" prop="key" />
      <el-table-column label="流程名称" align="center" prop="name" />
      <el-table-column label="版本" align="center" prop="version" width="80">
        <template #default="scope">
          <el-tag>v{{ scope.row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="资源名称" align="center" prop="resourceName" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.suspended" type="danger">挂起</el-tag>
          <el-tag v-else type="success">激活</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            size="small"
            @click="handleView(scope.row)"
          >流程图</el-button>
          <el-button
            link
            size="small"
            :icon="scope.row.suspended ? 'VideoPlay' : 'VideoPause'"
            :type="scope.row.suspended ? 'success' : 'warning'"
            @click="handleState(scope.row)"
          >{{ scope.row.suspended ? '激活' : '挂起' }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            size="small"
            @click="handleDelete(scope.row)"
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

    <!-- 流程图对话框 -->
    <el-dialog :title="viewTitle" v-model="openView" width="1000px" append-to-body destroy-on-close>
      <div style="height: 500px; border: 1px solid #dcdfe6; position: relative;">
        <BpmnViewer :xml="bpmnXml" @node-click="handleNodeClick" />
      </div>
    </el-dialog>

    <!-- 节点详情抽屉 -->
    <el-drawer
      v-model="nodeDrawer"
      title="节点详情"
      :size="400"
      append-to-body
      destroy-on-close
    >
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="节点ID">{{ nodeData.id }}</el-descriptions-item>
        <el-descriptions-item label="节点名称">{{ nodeData.name }}</el-descriptions-item>
        <el-descriptions-item label="节点类型">{{ nodeData.type }}</el-descriptions-item>
        
        <template v-if="nodeData.documentation">
          <el-descriptions-item label="说明">{{ nodeData.documentation }}</el-descriptions-item>
        </template>

        <!-- 用户任务属性 -->
        <template v-if="nodeData.type === 'bpmn:UserTask'">
          <el-descriptions-item label="指定审批人">
            <span v-if="nodeData.assignee">
              {{ nodeData.assigneeName }}
              <span class="text-muted" style="font-size: 12px">({{ nodeData.assignee }})</span>
            </span>
            <span v-else class="text-muted">未设置</span>
          </el-descriptions-item>
          <el-descriptions-item label="候选审批人">
            <span v-if="nodeData.candidateUsers">
              {{ nodeData.candidateUsersNames }}
              <span class="text-muted" style="font-size: 12px">({{ nodeData.candidateUsers }})</span>
            </span>
            <span v-else class="text-muted">未设置</span>
          </el-descriptions-item>
          <el-descriptions-item label="候选审批组">
            <span v-if="nodeData.candidateGroups">
              {{ nodeData.candidateGroupsNames }}
              <span class="text-muted" style="font-size: 12px">({{ nodeData.candidateGroups }})</span>
            </span>
            <span v-else class="text-muted">未设置</span>
          </el-descriptions-item>
          <el-descriptions-item label="表单Key">
            <span v-if="nodeData.formKey">{{ nodeData.formKey }}</span>
            <span v-else class="text-muted">未设置</span>
          </el-descriptions-item>
        </template>

        <!-- 连线属性 -->
        <template v-if="nodeData.type === 'bpmn:SequenceFlow'">
           <el-descriptions-item label="流转条件">
             <code v-if="nodeData.conditionExpression">{{ nodeData.conditionExpression }}</code>
             <span v-else class="text-muted">无条件</span>
           </el-descriptions-item>
        </template>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="ProcessDefinition">
import { listProcessDefinitions, getProcessDefinitionXml, updateProcessDefinitionState, deleteDeployment } from "@/api/ops/workflow";
import { listUser } from "@/api/system/user";
import { listRole } from "@/api/system/role";
import BpmnViewer from "@/components/BpmnViewer";
import { getCurrentInstance, ref, reactive, toRefs, onMounted } from "vue";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const processList = ref([]);
const total = ref(0);
const openView = ref(false);
const viewTitle = ref("");
const bpmnXml = ref("");

// 节点详情相关
const nodeDrawer = ref(false);
const nodeData = ref({});
const userMap = ref({});
const roleMap = ref({});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    key: undefined,
    name: undefined,
    latestOnly: true
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listProcessDefinitions(queryParams.value).then(response => {
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
  viewTitle.value = "流程图 - " + row.name + " (v" + row.version + ")";
  bpmnXml.value = "";
  openView.value = true;
  
  getProcessDefinitionXml(row.deploymentId).then(res => {
    bpmnXml.value = res.data;
  }).catch(() => {
    proxy.$modal.msgError("获取流程图失败");
    openView.value = false;
  });
}

function loadUserAndRoleData() {
  if (Object.keys(userMap.value).length === 0) {
    listUser({ pageSize: 1000 }).then(res => {
      res.rows.forEach(u => {
        userMap.value[u.userName] = u.nickName;
      });
    });
  }
  if (Object.keys(roleMap.value).length === 0) {
    listRole({ pageSize: 1000 }).then(res => {
      res.rows.forEach(r => {
        roleMap.value[r.roleKey] = r.roleName;
      });
    });
  }
}

function formatUser(username) {
  if (!username) return '';
  return userMap.value[username] || username;
}

function formatUsers(usernames) {
  if (!usernames) return '';
  return usernames.split(',').map(u => formatUser(u)).join(', ');
}

function formatRole(roleKey) {
  if (!roleKey) return '';
  return roleMap.value[roleKey] || roleKey;
}

function formatRoles(roleKeys) {
  if (!roleKeys) return '';
  return roleKeys.split(',').map(r => formatRole(r)).join(', ');
}

function handleNodeClick(element) {
  // 确保数据已加载
  loadUserAndRoleData();

  const bizObj = element.businessObject;
  // 提取文档
  let doc = '';
  if (bizObj.documentation && bizObj.documentation.length > 0) {
    doc = bizObj.documentation[0].text;
  }
  
  // 兼容 Flowable 扩展属性
  const attrs = bizObj.$attrs || {};
  
  const assignee = bizObj.assignee || attrs['flowable:assignee'];
  const candidateUsers = bizObj.candidateUsers || attrs['flowable:candidateUsers'];
  const candidateGroups = bizObj.candidateGroups || attrs['flowable:candidateGroups'];

  nodeData.value = {
    id: bizObj.id,
    name: bizObj.name || '',
    type: element.type,
    documentation: doc,
    assignee: assignee,
    assigneeName: formatUser(assignee),
    candidateUsers: candidateUsers,
    candidateUsersNames: formatUsers(candidateUsers),
    candidateGroups: candidateGroups,
    candidateGroupsNames: formatRoles(candidateGroups),
    formKey: bizObj.formKey || attrs['flowable:formKey'],
    conditionExpression: bizObj.conditionExpression ? bizObj.conditionExpression.body : ''
  };
  nodeDrawer.value = true;
}

function handleState(row) {
  const action = row.suspended ? 'activate' : 'suspend';
  const text = row.suspended ? "激活" : "挂起";
  proxy.$modal.confirm('确认要"' + text + '"流程定义"' + row.name + '"吗？').then(function() {
    return updateProcessDefinitionState(row.id, action);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(text + "成功");
  }).catch(() => {});
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.name + '"的数据项？').then(function() {
    return deleteDeployment(row.deploymentId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

onMounted(() => {
  getList();
});
</script>
