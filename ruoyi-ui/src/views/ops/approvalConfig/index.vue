<template>
  <div class="app-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>审批流程可视化配置</span>
        </div>
      </template>
      <div class="config-header">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="68px">
          <el-form-item label="环境" prop="env">
            <el-select v-model="queryParams.env" placeholder="请选择环境" @change="handleQuery">
              <el-option label="生产环境" value="prod" />
              <el-option label="测试环境" value="test" />
            </el-select>
          </el-form-item>
          <el-form-item label="应用" prop="appId">
            <el-select v-model="queryParams.appId" placeholder="请选择应用" filterable @change="handleQuery">
              <el-option label="全局默认" :value="0" />
              <el-option
                v-for="item in appOptions"
                :key="item.appId"
                :label="item.appName"
                :value="item.appId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="流程Key" prop="processKey">
            <el-input v-model="queryParams.processKey" placeholder="自定义流程Key" style="width: 200px;" @change="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" size="small" @click="handleQuery">刷新配置</el-button>
            <el-button type="info" icon="View" size="small" @click="handleViewDeployed">查看已部署流程</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-tabs v-model="activeTab" style="margin-bottom: 10px;">
        <el-tab-pane label="可视化配置" name="visual"></el-tab-pane>
        <el-tab-pane label="BPMN代码" name="code"></el-tab-pane>
        <el-tab-pane label="BPMN设计器" name="designer"></el-tab-pane>
      </el-tabs>

      <div v-show="activeTab === 'visual'">
        <div class="process-container">
          <div class="process-steps">
            <div class="start-node">
              <div class="node-icon"><el-icon><VideoPlay /></el-icon></div>
              <div class="node-label">开始</div>
            </div>
            <div class="connector"></div>
            
            <draggable 
              v-model="sortedConfigList" 
              item-key="id" 
              class="draggable-area"
              @end="onDragEnd"
            >
              <template #item="{element, index}">
                <div class="process-node-wrapper">
                  <div class="process-node">
                    <div class="node-header">
                  <span>第 {{ index + 1 }} 步</span>
                  <el-button link class="delete-btn" @click="handleDelete(element)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
                    <div class="node-content" @click="handleUpdate(element)">
                      <div class="approver-type">
                        <el-tag size="small" :type="element.approverType === 'role' ? '' : 'warning'">
                          {{ element.approverType === 'role' ? '角色' : '用户' }}
                        </el-tag>
                      </div>
                      <div class="approver-name">
                        {{ getApproverName(element.approverType, element.approverId) }}
                      </div>
                      <div class="node-remark" v-if="element.remark">{{ element.remark }}</div>
                    </div>
                  </div>
                  <div class="connector"></div>
                </div>
              </template>
            </draggable>

            <div class="add-node-btn" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              <span>添加审批节点</span>
            </div>
            
            <div class="connector"></div>
            <div class="end-node">
              <div class="node-icon"><el-icon><Flag /></el-icon></div>
              <div class="node-label">结束</div>
            </div>
          </div>
        </div>
      </div>

      <div v-show="activeTab === 'code'">
        <div style="margin-bottom: 10px; display: flex; gap: 10px;">
          <el-button type="primary" size="small" @click="generateFromVisual">从可视化生成BPMN</el-button>
          <el-button type="success" size="small" @click="deployBpmn" :loading="deploying" v-hasPermi="['ops:approvalConfig:edit']">部署BPMN</el-button>
          <el-button size="small" @click="downloadBpmn">下载BPMN</el-button>
        </div>
        <div style="border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden;">
          <VueMonacoEditor
            v-if="activeTab==='code'"
            v-model:value="bpmnXml"
            theme="vs-dark"
            language="xml"
            height="500px"
            :options="{
              automaticLayout: true,
              minimap: { enabled: false },
              fontSize: 13,
              scrollBeyondLastLine: false
            }"
          />
        </div>
      </div>

      <div v-if="activeTab === 'designer'" style="height: 600px; border: 1px solid #dcdfe6;">
        <BpmnDesigner 
          v-model="bpmnXml" 
          @deploy="deployBpmn" 
        />
      </div>
      
      <div class="save-tip" v-if="hasChanges">
        <el-alert title="检测到配置变更，请确保步骤顺序正确" type="warning" show-icon :closable="false" />
      </div>
    </el-card>

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="环境" prop="env">
          <el-select v-model="form.env" placeholder="请选择环境" disabled>
            <el-option label="生产环境" value="prod" />
            <el-option label="测试环境" value="test" />
          </el-select>
        </el-form-item>
        <el-form-item label="应用" prop="appId">
          <el-select v-model="form.appId" placeholder="请选择应用" filterable disabled>
            <el-option label="全局默认" :value="0" />
            <el-option
              v-for="item in appOptions"
              :key="item.appId"
              :label="item.appName"
              :value="item.appId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审批类型" prop="approverType">
          <el-radio-group v-model="form.approverType">
            <el-radio label="role">角色</el-radio>
            <el-radio label="user">用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批主体" prop="approverId">
          <el-select v-model="form.approverId" placeholder="请选择审批主体" filterable>
            <el-option
              v-for="item in approverOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog title="已部署流程图" v-model="showDeployed" width="1000px" append-to-body>
      <div style="height: 500px; border: 1px solid #dcdfe6;">
        <BpmnViewer :xml="deployedXml" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="ApprovalConfig">
import { listApprovalConfig, getApprovalConfig, delApprovalConfig, addApprovalConfig, updateApprovalConfig, deployBpmnXml } from "@/api/ops/approvalConfig";
import { getProcessDefinitionXmlByKey } from "@/api/ops/workflow";
import { listApp } from "@/api/ops/app";
import { listUser } from "@/api/system/user";
import { listRole } from "@/api/system/role";
import { getCurrentInstance, reactive, ref, toRefs, watch, onMounted, computed, nextTick } from 'vue';
import draggable from 'vuedraggable';
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'

import BpmnDesigner from './BpmnDesigner';
import BpmnViewer from '@/components/BpmnViewer';

const { proxy } = getCurrentInstance();

const configList = ref([]);
const open = ref(false);
const loading = ref(true);
const title = ref("");
const hasChanges = ref(false);
const activeTab = ref('visual');
const bpmnXml = ref('');
const deploying = ref(false);
const showDeployed = ref(false);
const deployedXml = ref('');

const appOptions = ref([]);
const userOptions = ref([]);
const roleOptions = ref([]);
const approverOptions = ref([]);

watch(activeTab, (val) => {
  if (val === 'code' || val === 'designer') {
    if (!bpmnXml.value) {
      generateFromVisual();
    }
  }
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 100, // Load all for drag and drop
    env: "prod", // Default to prod
    appId: 0,    // Default to global
    processKey: "release_approval_0_prod"
  },
  rules: {
    env: [{ required: true, message: "环境不能为空", trigger: "change" }],
    approverType: [{ required: true, message: "审批类型不能为空", trigger: "change" }],
    approverId: [{ required: true, message: "审批主体不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

// Using a writable computed or just a ref that syncs
const sortedConfigList = computed({
  get: () => {
    if (!configList.value) return [];
    return configList.value.sort((a, b) => a.step - b.step);
  },
  set: (val) => {
    configList.value = val;
    // Reorder steps
    configList.value.forEach((item, index) => {
      item.step = index + 1;
      // Trigger update for each item to persist order
      updateApprovalConfig(item); 
    });
    hasChanges.value = true;
    setTimeout(() => { hasChanges.value = false; }, 2000);
  }
});

function onDragEnd() {
  // Triggered by draggable
}

/** 查询列表 */
function getList() {
  loading.value = true;
  listApprovalConfig(queryParams.value).then(response => {
    configList.value = response.rows;
    loading.value = false;
  });
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
    appId: queryParams.value.appId,
    env: queryParams.value.env,
    step: configList.value.length + 1,
    approverType: "role",
    approverId: undefined,
    remark: undefined
  };
  proxy.resetForm("formRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加审批节点";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  // Clone to avoid direct mutation
  form.value = { ...row };
  open.value = true;
  title.value = "修改审批节点";
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateApprovalConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        // Auto set step to last + 1
        form.value.step = configList.value.length + 1;
        addApprovalConfig(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认移除该审批节点？').then(function() {
    return delApprovalConfig(row.id);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("移除成功");
    // Reorder remaining steps? ideally backend or next refresh handles it
    // But for strict order, we might need to re-index. 
    // For simplicity, let's assume backend or next save fixes it, or we trigger reorder.
    // Actually, deleting a step in middle leaves a gap. 
    // We should re-fetch list then re-save all with new indices.
    setTimeout(() => {
       reorderSteps();
    }, 500);
  }).catch(() => {});
}

function reorderSteps() {
  listApprovalConfig(queryParams.value).then(response => {
    const list = response.rows.sort((a, b) => a.step - b.step);
    list.forEach((item, index) => {
      if (item.step !== index + 1) {
        item.step = index + 1;
        updateApprovalConfig(item);
      }
    });
    configList.value = list;
  });
}

/** 获取应用名称 */
function getAppName(appId) {
  if (!appId || appId === 0) return "全局默认";
  const app = appOptions.value.find(item => item.appId === appId);
  return app ? app.appName : appId;
}

/** 获取审批人名称 */
function getApproverName(type, id) {
  if (type === 'role') {
    const role = roleOptions.value.find(item => item.roleId === id);
    return role ? role.roleName : id;
  } else {
    const user = userOptions.value.find(item => item.userId === id);
    return user ? user.nickName : id;
  }
}

/** 监听审批类型变化，更新选项 */
watch(() => form.value.approverType, (val) => {
  // Only reset if changed by user interaction, not initial load
  // But hard to distinguish.
  // Let's check if current approverId is valid for new type?
  // Easier: always reset if type changes
  // But wait, when editing, we set form value which triggers this watch.
  // We need to check if open is true?
  updateApproverOptions(val);
});

function updateApproverOptions(type) {
  if (type === 'role') {
    approverOptions.value = roleOptions.value.map(r => ({ label: r.roleName, value: r.roleId }));
  } else {
    approverOptions.value = userOptions.value.map(u => ({ label: u.nickName, value: u.userId }));
  }
}

// Watch open to handle edit mode options
watch(() => open.value, (val) => {
  if (val) {
    updateApproverOptions(form.value.approverType);
  }
});

onMounted(() => {
  // Load Apps
  listApp({ pageSize: 1000 }).then(res => {
    appOptions.value = res.rows;
  });
  // Load Roles
  listRole({ pageSize: 1000 }).then(res => {
    roleOptions.value = res.rows;
  });
  // Load Users
  listUser({ pageSize: 1000 }).then(res => {
    userOptions.value = res.rows;
  });
  getList();
});

// Watch env and appId to update processKey automatically
watch(() => [queryParams.value.env, queryParams.value.appId], () => {
  const a = queryParams.value.appId || 0;
  const e = queryParams.value.env || 'prod';
  queryParams.value.processKey = `release_approval_${a}_${e}`;
});

function processKeyComputed() {
  if (queryParams.value.processKey) return queryParams.value.processKey;
  const a = queryParams.value.appId || 0;
  const e = queryParams.value.env || 'prod';
  return `release_approval_${a}_${e}`;
}

function processNameComputed() {
  const a = queryParams.value.appId || 0;
  const e = queryParams.value.env || 'prod';
  return `${getAppName(a)}-${e}发布审批`;
}

function generateFromVisual() {
  const key = processKeyComputed();
  const name = processNameComputed();
  const steps = sortedConfigList.value || [];
  const lines = [];
  lines.push('<?xml version="1.0" encoding="UTF-8"?>');
  lines.push('<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:flowable="http://flowable.org/bpmn" targetNamespace="http://www.flowable.org/processdef">');
  lines.push(`  <process id="${key}" name="${name}" isExecutable="true">`);
  lines.push('    <startEvent id="startEvent"/>');
  if (steps.length > 0) {
    lines.push('    <sequenceFlow id="flow_start_step1" sourceRef="startEvent" targetRef="step_1"/>');
  } else {
    lines.push('    <sequenceFlow id="flow_start_end" sourceRef="startEvent" targetRef="endEvent"/>');
  }
  steps.forEach((s, idx) => {
    const id = s.approverId;
    const type = s.approverType;
    const nameStep = s.remark ? s.remark : `第${idx + 1}步`;
    if (type === 'user') {
      lines.push(`    <userTask id="step_${idx + 1}" name="${nameStep}" flowable:assignee="${id}"/>`);
    } else {
      lines.push(`    <userTask id="step_${idx + 1}" name="${nameStep}" flowable:candidateGroups="${id}"/>`);
    }
    if (idx < steps.length - 1) {
      lines.push(`    <sequenceFlow id="flow_step_${idx + 1}_step_${idx + 2}" sourceRef="step_${idx + 1}" targetRef="step_${idx + 2}"/>`);
    }
  });
  lines.push('    <endEvent id="endEvent"/>');
  if (steps.length > 0) {
    lines.push(`    <sequenceFlow id="flow_last_end" sourceRef="step_${steps.length}" targetRef="endEvent"/>`);
  }
  lines.push('  </process>');
  
  // DI Generation
  lines.push('  <bpmndi:BPMNDiagram id="BPMNDiagram_1">');
  lines.push(`    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">`);
  
  // StartEvent: 150, 100
  lines.push('      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="startEvent">');
  lines.push('        <omgdc:Bounds x="150" y="100" width="36" height="36" />');
  lines.push('      </bpmndi:BPMNShape>');
  
  let x = 250;
  let lastX = 186; // StartEvent right edge (150+36)
  let lastY = 118; // StartEvent center y
  
  steps.forEach((s, idx) => {
    // UserTask: 100x80
    lines.push(`      <bpmndi:BPMNShape id="BPMNShape_step_${idx + 1}" bpmnElement="step_${idx + 1}">`);
    lines.push(`        <omgdc:Bounds x="${x}" y="78" width="100" height="80" />`);
    lines.push('      </bpmndi:BPMNShape>');
    
    // Flow from prev to curr
    let flowId = idx === 0 ? 'flow_start_step1' : `flow_step_${idx}_step_${idx + 1}`;
    
    lines.push(`      <bpmndi:BPMNEdge id="BPMNEdge_${flowId}" bpmnElement="${flowId}">`);
    lines.push(`        <omgdi:waypoint x="${lastX}" y="118" />`);
    lines.push(`        <omgdi:waypoint x="${x}" y="118" />`);
    lines.push('      </bpmndi:BPMNEdge>');
    
    lastX = x + 100;
    x += 150;
  });
  
  // EndEvent
  lines.push('      <bpmndi:BPMNShape id="BPMNShape_endEvent" bpmnElement="endEvent">');
  lines.push(`        <omgdc:Bounds x="${x}" y="100" width="36" height="36" />`);
  lines.push('      </bpmndi:BPMNShape>');
  
  // Flow to End
  let lastFlowId = steps.length > 0 ? `flow_last_end` : 'flow_start_end';
  lines.push(`      <bpmndi:BPMNEdge id="BPMNEdge_${lastFlowId}" bpmnElement="${lastFlowId}">`);
  lines.push(`        <omgdi:waypoint x="${lastX}" y="118" />`);
  lines.push(`        <omgdi:waypoint x="${x}" y="118" />`);
  lines.push('      </bpmndi:BPMNEdge>');
  
  lines.push('    </bpmndi:BPMNPlane>');
  lines.push('  </bpmndi:BPMNDiagram>');
  
  lines.push('</definitions>');
  bpmnXml.value = lines.join('\n');
  activeTab.value = 'code';
}

function deployBpmn() {
  if (!bpmnXml.value || bpmnXml.value.trim() === '') {
    proxy.$modal.msgError('BPMN内容为空');
    return;
  }
  const key = processKeyComputed();
  const name = processNameComputed();
  
  // 强制替换 XML 中的 process id 和 name，确保与顶部输入框一致
  let xml = bpmnXml.value;
  // 替换 process id (支持带命名空间的 process 标签)
  // 查找 id="xxx" 并在 process 标签内
  // 正则说明：匹配 <...process... id="..." ...>
  // 注意：这里简单假设 id 属性在 process 标签的第一行或附近，且格式标准
  // 更严谨的做法是解析 XML，但前端引入 xml parser 较重
  
  // 尝试替换 process id="xxx"
  if (xml.match(/<[^>]*process[^>]*\s+id="([^"]+)"/)) {
    xml = xml.replace(/(<[^>]*process[^>]*\s+id=")([^"]+)(")/, `$1${key}$3`);
  }
  // 尝试替换 process name="xxx"
  if (xml.match(/<[^>]*process[^>]*\s+name="([^"]+)"/)) {
    xml = xml.replace(/(<[^>]*process[^>]*\s+name=")([^"]+)(")/, `$1${name}$3`);
  }
  
  deploying.value = true;
  deployBpmnXml({ processKey: key, processName: name, bpmnXml: xml })
    .then(res => {
      proxy.$modal.msgSuccess('部署成功');
    })
    .catch(err => {
      const msg = (err && err.msg) || '部署失败';
      proxy.$modal.msgError(msg);
    })
    .finally(() => {
      deploying.value = false;
    });
}

function downloadBpmn() {
  const key = processKeyComputed();
  const blob = new Blob([bpmnXml.value || ''], { type: 'application/xml' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = key + '.bpmn20.xml';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

function handleViewDeployed() {
  const key = processKeyComputed();
  getProcessDefinitionXmlByKey(key).then(res => {
    deployedXml.value = res.data;
    showDeployed.value = true;
  }).catch(err => {
    proxy.$modal.msgError("获取已部署流程失败或流程未部署");
  });
}
</script>

<style scoped>
.config-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}
.process-container {
  padding: 20px;
  overflow-x: auto;
  background-color: #f8f9fa;
  border-radius: 4px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.process-steps {
  display: flex;
  align-items: center;
}
.draggable-area {
  display: flex;
  align-items: center;
}
.process-node-wrapper {
  display: flex;
  align-items: center;
}
.process-node {
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 180px;
  padding: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}
.process-node:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}
.delete-btn {
  padding: 0;
  color: #f56c6c;
}
.node-content {
  text-align: center;
}
.approver-type {
  margin-bottom: 5px;
}
.approver-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}
.node-remark {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.connector {
  width: 40px;
  height: 2px;
  background-color: #dcdfe6;
  margin: 0 10px;
  position: relative;
}
.connector::after {
  content: '';
  position: absolute;
  right: 0;
  top: -4px;
  width: 0;
  height: 0;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 6px solid #dcdfe6;
}
.start-node, .end-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #f0f2f5;
  border: 2px solid #dcdfe6;
  color: #909399;
}
.start-node {
  border-color: #67c23a;
  color: #67c23a;
  background-color: #f0f9eb;
}
.end-node {
  border-color: #909399;
  background-color: #f4f4f5;
}
.node-label {
  font-size: 12px;
  margin-top: 2px;
}
.add-node-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px dashed #409eff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: white;
  transition: all 0.3s;
  flex-direction: column;
}
.add-node-btn span {
  display: none;
}
.add-node-btn:hover {
  background-color: #ecf5ff;
  width: 120px;
  border-radius: 20px;
}
.add-node-btn:hover span {
  display: inline;
  font-size: 12px;
  margin-left: 5px;
}
.add-node-btn:hover .el-icon {
  margin-bottom: 0;
}
</style>
