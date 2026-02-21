<template>
  <div class="bpmn-designer-container">
    <div class="designer-header">
      <el-button-group>
        <el-button icon="FolderOpened" size="small" @click="handleImport">导入XML</el-button>
        <el-button icon="Download" size="small" @click="handleDownload">下载XML</el-button>
        <el-button icon="VideoPlay" type="primary" size="small" @click="handleDeploy" v-hasPermi="['ops:approvalConfig:edit']">部署流程</el-button>
      </el-button-group>
      <el-button-group style="margin-left: 10px;">
        <el-button icon="ZoomIn" size="small" @click="handleZoom(0.1)"></el-button>
        <el-button icon="ZoomOut" size="small" @click="handleZoom(-0.1)"></el-button>
        <el-button icon="Rank" size="small" @click="handleZoom('fit')">适应</el-button>
      </el-button-group>
      <span style="margin-left: 20px; font-size: 12px; color: #909399;">
        提示: 从左侧拖拽节点到画布，选中节点可在右侧配置属性
      </span>
    </div>

    <div class="designer-body">
      <!-- Left Palette -->
      <div class="palette-panel">
        <div class="palette-header">
          <span>元素组件库</span>
        </div>
        <el-collapse v-model="activePaletteNames">
          <el-collapse-item title="开始事件" name="start">
            <div class="palette-group">
              <div class="palette-item" draggable="true" @dragstart="handleDragStart($event, 'bpmn:StartEvent')">
                <i class="bpmn-icon-start-event-none"></i>
                <span>开始事件</span>
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item title="任务" name="task">
            <div class="palette-group">
              <div class="palette-item" draggable="true" @dragstart="handleDragStart($event, 'bpmn:UserTask')">
                <i class="bpmn-icon-user-task"></i>
                <span>用户任务</span>
              </div>
              <div class="palette-item" draggable="true" @dragstart="handleDragStart($event, 'bpmn:ServiceTask')">
                <i class="bpmn-icon-service-task"></i>
                <span>服务任务</span>
              </div>
              <div class="palette-item" draggable="true" @dragstart="handleDragStart($event, 'bpmn:ScriptTask')">
                <i class="bpmn-icon-script-task"></i>
                <span>脚本任务</span>
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item title="网关" name="gateway">
            <div class="palette-group">
              <div class="palette-item" draggable="true" @dragstart="handleDragStart($event, 'bpmn:ExclusiveGateway')">
                <i class="bpmn-icon-gateway-xor"></i>
                <span>互斥网关</span>
              </div>
              <div class="palette-item" draggable="true" @dragstart="handleDragStart($event, 'bpmn:ParallelGateway')">
                <i class="bpmn-icon-gateway-parallel"></i>
                <span>并行网关</span>
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item title="结束事件" name="end">
            <div class="palette-group">
              <div class="palette-item" draggable="true" @dragstart="handleDragStart($event, 'bpmn:EndEvent')">
                <i class="bpmn-icon-end-event-none"></i>
                <span>结束事件</span>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- Center Canvas -->
      <div class="canvas-wrapper" @dragover.prevent @drop="handleDrop">
        <div ref="canvasRef" class="canvas-container"></div>
      </div>
      
      <!-- Right Properties -->
      <div class="properties-panel">
        <div class="panel-header">
          <el-icon><Setting /></el-icon>
          <span style="margin-left: 5px;">流程属性</span>
        </div>
        
        <el-collapse v-model="activePropsNames">
          <el-collapse-item title="常规" name="general">
            <el-form :model="elementForm" label-width="80px" size="small">
              <el-form-item label="ID">
                <el-input v-model="elementForm.id" @change="updateId" />
              </el-form-item>
              <el-form-item label="名称">
                <el-input v-model="elementForm.name" @change="updateName" />
              </el-form-item>
              <el-form-item label="类型" v-if="elementForm.elementTypeLabel">
                 <el-tag>{{ elementForm.elementTypeLabel }}</el-tag>
              </el-form-item>
              <el-form-item label="文档">
                 <el-input type="textarea" v-model="elementForm.documentation" @change="updateDocumentation" />
              </el-form-item>
              
              <template v-if="isProcess">
                <el-form-item label="可执行">
                  <el-switch v-model="elementForm.isExecutable" @change="updateIsExecutable" />
                </el-form-item>
              </template>
            </el-form>
          </el-collapse-item>

          <el-collapse-item title="任务配置" name="task" v-if="isUserTask">
            <el-form :model="elementForm" label-width="80px" size="small">
              <el-form-item label="审批类型">
                <el-radio-group v-model="elementForm.assigneeType" @change="updateAssigneeType">
                  <el-radio value="user">指定用户</el-radio>
                  <el-radio value="group">候选组</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item :label="elementForm.assigneeType === 'user' ? '用户' : '角色'">
                <el-select 
                  v-model="elementForm.assigneeValue" 
                  placeholder="请选择" 
                  filterable 
                  @change="updateAssignee"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in (elementForm.assigneeType === 'user' ? userOptions : roleOptions)"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="候选用户">
                <el-input v-model="elementForm.candidateUsers" placeholder="user1,user2" @change="updateCandidateUsers" />
              </el-form-item>
              <el-form-item label="表单Key">
                <el-input v-model="elementForm.formKey" @change="updateFormKey" />
              </el-form-item>
              <el-form-item label="到期时间">
                <el-input v-model="elementForm.dueDate" @change="updateDueDate" />
              </el-form-item>
              <el-form-item label="优先级">
                <el-input v-model="elementForm.priority" @change="updatePriority" />
              </el-form-item>
            </el-form>
          </el-collapse-item>

          <el-collapse-item title="服务配置" name="service" v-if="isServiceTask">
            <el-form :model="elementForm" label-width="80px" size="small">
              <el-form-item label="实现方式">
                <el-select v-model="elementForm.serviceTaskType" @change="updateServiceTask">
                  <el-option label="Java类" value="class" />
                  <el-option label="表达式" value="expression" />
                  <el-option label="委派表达式" value="delegateExpression" />
                </el-select>
              </el-form-item>
              <el-form-item label="实现值">
                <el-input v-model="elementForm.serviceTaskValue" @change="updateServiceTask" />
              </el-form-item>
            </el-form>
          </el-collapse-item>

          <el-collapse-item title="网关配置" name="gateway" v-if="isGateway">
            <el-form :model="elementForm" label-width="80px" size="small">
              <el-form-item label="方向">
                <el-select v-model="elementForm.gatewayDirection" @change="updateGatewayDirection">
                  <el-option label="未指定" value="Unspecified" />
                  <el-option label="分支" value="Diverging" />
                  <el-option label="汇聚" value="Converging" />
                  <el-option label="混合" value="Mixed" />
                </el-select>
              </el-form-item>
              <el-form-item label="默认流">
                <el-select v-model="elementForm.defaultFlowId" @change="updateGatewayDefaultFlow">
                  <el-option label="无" value="" />
                  <el-option
                    v-for="item in gatewayOutgoingOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </el-collapse-item>

          <el-collapse-item title="连线配置" name="sequence" v-if="isSequenceFlow">
            <el-form :model="elementForm" label-width="80px" size="small">
              <el-form-item label="条件表达式">
                <el-input v-model="elementForm.conditionExpression" @change="updateConditionExpression" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    
    <input type="file" ref="fileInput" style="display: none" accept=".xml,.bpmn" @change="onFileSelected" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick, computed, shallowRef, toRaw, markRaw } from 'vue';
import BpmnModeler from 'bpmn-js/lib/Modeler';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import { ElMessage } from 'element-plus';
import customTranslate from './customTranslate';
import flowableModdleDescriptor from './flowable.json';
import { listUser } from "@/api/system/user";
import { listRole } from "@/api/system/role";

const props = defineProps({
  modelValue: String,
  readOnly: Boolean
});

const emit = defineEmits(['update:modelValue', 'deploy']);

const canvasRef = ref(null);
const fileInput = ref(null);
const selectedElement = shallowRef(null);
let bpmnModeler = null;
let isImporting = false;
let saveTimer = null;

const activePaletteNames = ref(['start', 'task', 'gateway', 'end']);
const activePropsNames = ref(['general', 'task', 'service', 'gateway', 'sequence']);

const userOptions = ref([]);
const roleOptions = ref([]);

const elementForm = ref({
  id: '',
  name: '',
  elementTypeLabel: '',
  isExecutable: true,
  assigneeType: 'user',
  assigneeValue: '',
  documentation: '',
  gatewayDirection: 'Unspecified',
  conditionExpression: '',
  defaultFlowId: '',
  formKey: '',
  dueDate: '',
  priority: '',
  candidateUsers: '',
  serviceTaskType: 'class',
  serviceTaskValue: ''
});

const isProcess = computed(() => {
  return selectedElement.value && selectedElement.value.type === 'bpmn:Process';
});

const isUserTask = computed(() => {
  return selectedElement.value && (selectedElement.value.type === 'bpmn:UserTask' || selectedElement.value.type === 'bpmn:Task');
});
const isGateway = computed(() => {
  return selectedElement.value && selectedElement.value.type && selectedElement.value.type.includes('Gateway');
});
const isSequenceFlow = computed(() => {
  return selectedElement.value && selectedElement.value.type === 'bpmn:SequenceFlow';
});
const isServiceTask = computed(() => {
  return selectedElement.value && selectedElement.value.type === 'bpmn:ServiceTask';
});
const gatewayOutgoingOptions = computed(() => {
  if (!selectedElement.value || !selectedElement.value.outgoing) return [];
  return selectedElement.value.outgoing.map(flow => ({
    label: flow.businessObject && flow.businessObject.name ? `${flow.id}(${flow.businessObject.name})` : flow.id,
    value: flow.id
  }));
});

// Default XML template
const defaultXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:flowable="http://flowable.org/bpmn" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="173" y="102" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

onMounted(() => {
  initModeler();
  
  // Load users and roles
  listUser({ pageSize: 1000 }).then(res => {
    userOptions.value = res.rows.map(u => ({ label: u.nickName, value: u.userName }));
  });
  listRole({ pageSize: 1000 }).then(res => {
    roleOptions.value = res.rows.map(r => ({ label: r.roleName, value: r.roleKey }));
  });
});

onBeforeUnmount(() => {
  if (bpmnModeler) {
    bpmnModeler.destroy();
  }
});

watch(() => props.modelValue, (val) => {
  if (val && bpmnModeler) {
    bpmnModeler.saveXML({ format: true }).then(({ xml }) => {
      if (xml !== val) {
        importXML(val);
      }
    }).catch(() => {
      importXML(val);
    });
  }
});

function initModeler() {
  bpmnModeler = new BpmnModeler({
    container: canvasRef.value,
    additionalModules: [
      {
        translate: [ 'value', customTranslate ]
      }
    ],
    moddleExtensions: {
      flowable: flowableModdleDescriptor
    }
  });

  bpmnModeler.on('selection.changed', (e) => {
    const selection = e.newSelection;
    if (selection.length === 1) {
      const current = selection[0];
      const ensured = ensureUserTask(current);
      selectedElement.value = ensured;
      initForm(ensured);
    } else {
      // If nothing selected, select Root Element (Process)
      const canvas = bpmnModeler.get('canvas');
      const rootElement = canvas.getRootElement();
      if (rootElement) {
        selectedElement.value = rootElement;
        initForm(rootElement);
      } else {
        selectedElement.value = null;
      }
    }
  });

  bpmnModeler.on('element.changed', (e) => {
    // Sync updates if needed
    if (!isImporting) {
      saveXML();
    }
  });

  // Initial Import
  importXML(props.modelValue || defaultXml);
}

function handleDragStart(event, type) {
  event.dataTransfer.setData('type', type);
}

function handleDrop(event) {
  const type = event.dataTransfer.getData('type');
  if (type) {
    const point = { x: event.clientX, y: event.clientY };
    // Convert client coordinates to canvas coordinates
    // We need to use bpmn-js API to map point
    // Actually, create.start is for mouse drag.
    // For drop, we can use create.start if we simulate a drag?
    // Or just createShape at position.
    // bpmn-js doesn't have a simple 'drop' API that takes clientX/Y directly for creation without drag context usually.
    // But we can calculate relative position.
    
    // Better way:
    // When dragstart, we don't do much.
    // The drop target needs to calculate position.
    // But bpmn-js canvas is scaled and scrolled.
    
    // Let's try simple creation at center or try to map coordinates.
    // Modeler.get('canvas').viewbox() gives scroll/scale.
    // event.layerX/Y might be relative to canvas container.
    
    const rect = canvasRef.value.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    // Convert to diagram coordinates
    // viewbox: { x, y, width, height, scale, inner... }
    const viewbox = bpmnModeler.get('canvas').viewbox();
    const diagramX = viewbox.x + x / viewbox.scale;
    const diagramY = viewbox.y + y / viewbox.scale;
    
    const elementFactory = bpmnModeler.get('elementFactory');
    const shape = elementFactory.createShape({ type });
    
    const create = bpmnModeler.get('create');
    
    // create.start usually requires interaction event.
    // If we just want to place it:
    const modeling = bpmnModeler.get('modeling');
    // modeling.createShape(shape, { x: diagramX, y: diagramY }, root)
    
    // We need root element
    const root = bpmnModeler.get('canvas').getRootElement();
    
    modeling.createShape(shape, { x: diagramX, y: diagramY }, root);
  }
}

function importXML(xml) {
  isImporting = true;
  bpmnModeler.importXML(xml).then(() => {
    bpmnModeler.get('canvas').zoom('fit-viewport');
    isImporting = false;
    // Select root initially
    const root = bpmnModeler.get('canvas').getRootElement();
    if (root) {
      selectedElement.value = root;
      initForm(root);
    }
  }).catch(err => {
    console.error(err);
    ElMessage.error('加载BPMN XML失败');
    isImporting = false;
  });
}

function ensureUserTask(element) {
  if (!element || element.type !== 'bpmn:Task') {
    return element;
  }
  const bpmnReplace = bpmnModeler.get('bpmnReplace');
  const replaced = bpmnReplace.replaceElement(element, { type: 'bpmn:UserTask' });
  return replaced || element;
}

function saveXML() {
  if (saveTimer) clearTimeout(saveTimer);
  saveTimer = setTimeout(() => {
    bpmnModeler.saveXML({ format: true }).then(({ xml }) => {
      emit('update:modelValue', xml);
    });
  }, 300);
}

function initForm(element) {
  if (!element || !element.businessObject) {
    return;
  }
  const businessObject = element.businessObject;
  elementForm.value.id = businessObject.id;
  elementForm.value.name = businessObject.name || '';
  elementForm.value.elementTypeLabel = getElementTypeLabel(element.type);
  elementForm.value.isExecutable = !!businessObject.isExecutable;
  
  // Documentation
  if (businessObject.documentation && businessObject.documentation.length > 0) {
    elementForm.value.documentation = businessObject.documentation[0].text;
  } else {
    elementForm.value.documentation = '';
  }

  // User Task specific
  if (element.type === 'bpmn:UserTask') {
    const attrs = businessObject.$attrs || {};
    const assignee = businessObject.assignee || attrs['flowable:assignee'];
    const candidateGroups = businessObject.candidateGroups || attrs['flowable:candidateGroups'];
    const candidateUsers = businessObject.candidateUsers || attrs['flowable:candidateUsers'];
    const formKey = businessObject.formKey || attrs['flowable:formKey'];
    const dueDate = businessObject.dueDate || attrs['flowable:dueDate'];
    const priority = businessObject.priority || attrs['flowable:priority'];
    if (assignee) {
      elementForm.value.assigneeType = 'user';
      elementForm.value.assigneeValue = String(assignee);
    } else if (candidateGroups) {
      elementForm.value.assigneeType = 'group';
      elementForm.value.assigneeValue = String(candidateGroups);
    } else {
      elementForm.value.assigneeType = 'user';
      elementForm.value.assigneeValue = '';
    }
    elementForm.value.candidateUsers = candidateUsers ? String(candidateUsers) : '';
    elementForm.value.formKey = formKey ? String(formKey) : '';
    elementForm.value.dueDate = dueDate ? String(dueDate) : '';
    elementForm.value.priority = priority ? String(priority) : '';
  } else {
    elementForm.value.assigneeType = 'user';
    elementForm.value.assigneeValue = '';
    elementForm.value.candidateUsers = '';
    elementForm.value.formKey = '';
    elementForm.value.dueDate = '';
    elementForm.value.priority = '';
  }

  if (element.type && element.type.includes('Gateway')) {
    elementForm.value.gatewayDirection = businessObject.gatewayDirection || 'Unspecified';
    elementForm.value.defaultFlowId = businessObject.default ? businessObject.default.id || businessObject.default : '';
  } else {
    elementForm.value.gatewayDirection = 'Unspecified';
    elementForm.value.defaultFlowId = '';
  }

  if (element.type === 'bpmn:SequenceFlow') {
    const condition = businessObject.conditionExpression;
    if (condition && condition.body != null) {
      elementForm.value.conditionExpression = condition.body;
    } else {
      elementForm.value.conditionExpression = '';
    }
  } else {
    elementForm.value.conditionExpression = '';
  }

  if (element.type === 'bpmn:ServiceTask') {
    const attrs = businessObject.$attrs || {};
    const classValue = businessObject.class || attrs['flowable:class'];
    const expressionValue = businessObject.expression || attrs['flowable:expression'];
    const delegateExpressionValue = businessObject.delegateExpression || attrs['flowable:delegateExpression'];
    if (classValue) {
      elementForm.value.serviceTaskType = 'class';
      elementForm.value.serviceTaskValue = String(classValue);
    } else if (expressionValue) {
      elementForm.value.serviceTaskType = 'expression';
      elementForm.value.serviceTaskValue = String(expressionValue);
    } else if (delegateExpressionValue) {
      elementForm.value.serviceTaskType = 'delegateExpression';
      elementForm.value.serviceTaskValue = String(delegateExpressionValue);
    } else {
      elementForm.value.serviceTaskType = 'class';
      elementForm.value.serviceTaskValue = '';
    }
  } else {
    elementForm.value.serviceTaskType = 'class';
    elementForm.value.serviceTaskValue = '';
  }
}

function updateId() {
  const modeling = bpmnModeler.get('modeling');
  modeling.updateProperties(toRaw(selectedElement.value), {
    id: elementForm.value.id
  });
}

function updateName() {
  const modeling = bpmnModeler.get('modeling');
  modeling.updateProperties(toRaw(selectedElement.value), {
    name: elementForm.value.name
  });
}

function updateIsExecutable() {
  const modeling = bpmnModeler.get('modeling');
  modeling.updateProperties(toRaw(selectedElement.value), {
    isExecutable: elementForm.value.isExecutable
  });
}

function updateAssigneeType() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  
  modeling.updateProperties(shape, {
    assignee: undefined,
    candidateGroups: undefined
  });
  elementForm.value.assigneeValue = '';
}

function updateAssignee() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  
  if (!shape) return;

  const value = elementForm.value.assigneeValue ? String(elementForm.value.assigneeValue) : '';
  const props = {};
  if (elementForm.value.assigneeType === 'user') {
    props.assignee = value || undefined;
    props.candidateGroups = undefined;
  } else {
    props.candidateGroups = value || undefined;
    props.assignee = undefined;
  }
  modeling.updateProperties(shape, props);
}

function updateCandidateUsers() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  
  const value = elementForm.value.candidateUsers ? String(elementForm.value.candidateUsers) : '';
  modeling.updateProperties(shape, {
    candidateUsers: value || undefined
  });
}

function updateFormKey() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  
  const value = elementForm.value.formKey ? String(elementForm.value.formKey) : '';
  modeling.updateProperties(shape, {
    formKey: value || undefined
  });
}

function updateDueDate() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  
  const value = elementForm.value.dueDate ? String(elementForm.value.dueDate) : '';
  modeling.updateProperties(shape, {
    dueDate: value || undefined
  });
}

function updatePriority() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  
  const value = elementForm.value.priority ? String(elementForm.value.priority) : '';
  modeling.updateProperties(shape, {
    priority: value || undefined
  });
}

function updateDocumentation() {
  const modeling = bpmnModeler.get('modeling');
  const bpmnFactory = bpmnModeler.get('bpmnFactory');
  const newDocumentation = bpmnFactory.create('bpmn:Documentation', {
    text: elementForm.value.documentation
  });
  modeling.updateProperties(toRaw(selectedElement.value), {
    documentation: [newDocumentation]
  });
}

function updateGatewayDirection() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  modeling.updateProperties(shape, {
    gatewayDirection: elementForm.value.gatewayDirection
  });
}

function updateGatewayDefaultFlow() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  const flowId = elementForm.value.defaultFlowId;
  if (flowId) {
    const flow = elementRegistry.get(flowId);
    if (flow) {
      modeling.updateProperties(shape, { default: flow.businessObject });
      return;
    }
  }
  modeling.updateProperties(shape, { default: undefined });
}

function updateConditionExpression() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  const bpmnFactory = bpmnModeler.get('bpmnFactory');
  const exprValue = elementForm.value.conditionExpression ? String(elementForm.value.conditionExpression) : '';
  if (exprValue) {
    const conditionExpression = bpmnFactory.create('bpmn:FormalExpression', { body: exprValue });
    modeling.updateProperties(shape, {
      conditionExpression
    });
  } else {
    modeling.updateProperties(shape, {
      conditionExpression: undefined
    });
  }
}

function updateServiceTask() {
  const modeling = bpmnModeler.get('modeling');
  const elementRegistry = bpmnModeler.get('elementRegistry');
  const shape = elementRegistry.get(elementForm.value.id);
  if (!shape) return;
  
  const value = elementForm.value.serviceTaskValue ? String(elementForm.value.serviceTaskValue) : '';
  const props = {
    class: undefined,
    expression: undefined,
    delegateExpression: undefined
  };
  
  if (value) {
    if (elementForm.value.serviceTaskType === 'class') {
      props.class = value;
    } else if (elementForm.value.serviceTaskType === 'expression') {
      props.expression = value;
    } else {
      props.delegateExpression = value;
    }
  }
  modeling.updateProperties(shape, props);
}

function getElementTypeLabel(type) {
  const map = {
    'bpmn:Process': '流程',
    'bpmn:StartEvent': '开始事件',
    'bpmn:EndEvent': '结束事件',
    'bpmn:UserTask': '用户任务',
    'bpmn:ServiceTask': '服务任务',
    'bpmn:ScriptTask': '脚本任务',
    'bpmn:ManualTask': '手工任务',
    'bpmn:BusinessRuleTask': '业务规则任务',
    'bpmn:ReceiveTask': '接收任务',
    'bpmn:SendTask': '发送任务',
    'bpmn:CallActivity': '调用活动',
    'bpmn:ExclusiveGateway': '互斥网关',
    'bpmn:ParallelGateway': '并行网关',
    'bpmn:InclusiveGateway': '相容网关',
    'bpmn:ComplexGateway': '复杂网关',
    'bpmn:EventBasedGateway': '事件网关',
    'bpmn:SequenceFlow': '顺序流',
    'bpmn:SubProcess': '子流程'
  };
  return map[type] || type || '';
}

function handleZoom(val) {
  if (val === 'fit') {
    bpmnModeler.get('canvas').zoom('fit-viewport');
  } else {
    const current = bpmnModeler.get('canvas').zoom();
    bpmnModeler.get('canvas').zoom(current + val);
  }
}

function handleImport() {
  fileInput.value.click();
}

function onFileSelected(e) {
  const file = e.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (data) => {
      importXML(data.target.result);
    };
    reader.readAsText(file);
  }
}

function handleDownload() {
  bpmnModeler.saveXML({ format: true }).then(({ xml }) => {
    const blob = new Blob([xml], { type: 'application/xml' });
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = 'process.bpmn20.xml';
    link.click();
  });
}

function handleDeploy() {
  saveXML();
  emit('deploy');
}
</script>

<style scoped>
.bpmn-designer-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.designer-header {
  padding: 10px;
  border-bottom: 1px solid #dcdfe6;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
}
.designer-body {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}
.palette-panel {
  width: 200px;
  border-right: 1px solid #dcdfe6;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.palette-header {
  padding: 10px;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}
.palette-group {
  padding: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.palette-item {
  width: 70px;
  height: 70px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: grab;
  border: 1px solid transparent;
  border-radius: 4px;
  transition: all 0.2s;
}
.palette-item:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
}
.palette-item i {
  font-size: 30px;
  margin-bottom: 5px;
  color: #409eff;
}
.palette-item span {
  font-size: 12px;
  color: #606266;
}
.canvas-wrapper {
  flex: 1;
  position: relative;
  background-color: #f8f8f8;
  overflow: hidden;
}
.canvas-container {
  height: 100%;
  width: 100%;
  background-image: linear-gradient(90deg, rgba(220, 220, 220, 0.5) 1px, transparent 0),
                    linear-gradient(rgba(220, 220, 220, 0.5) 1px, transparent 0);
  background-size: 20px 20px;
}
.properties-panel {
  width: 320px;
  border-left: 1px solid #dcdfe6;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}
.panel-header {
  padding: 10px 15px;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
  display: flex;
  align-items: center;
}
/* Hide default BPMN palette and logo */
:deep(.djs-palette) {
  display: none;
}
:deep(.bjs-powered-by) {
  display: none;
}
:deep(.el-collapse-item__header) {
  padding-left: 15px;
  font-weight: bold;
  background-color: #fafafa;
}
:deep(.el-collapse-item__content) {
  padding: 15px;
}
</style>
