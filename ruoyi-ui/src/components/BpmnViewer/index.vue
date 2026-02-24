<template>
  <div class="bpmn-viewer-container" v-loading="loading">
    <div class="bpmn-toolbar">
      <el-button-group>
        <el-button icon="ZoomIn" @click="handleZoom(0.1)" title="放大"></el-button>
        <el-button icon="ZoomOut" @click="handleZoom(-0.1)" title="缩小"></el-button>
        <el-button icon="ScaleToOriginal" @click="handleZoom('reset')" title="还原"></el-button>
        <el-button icon="FullScreen" @click="handleZoom('fit')" title="适应屏幕"></el-button>
      </el-button-group>
      <el-button-group style="margin-left: 10px">
        <el-button icon="Download" @click="handleExport('svg')" title="导出SVG">SVG</el-button>
        <el-button icon="Picture" @click="handleExport('png')" title="导出PNG">PNG</el-button>
      </el-button-group>
    </div>
    <div ref="canvasRef" class="canvas"></div>

    <el-dialog title="节点详情" v-model="dialogVisible" width="500px" append-to-body>
        <div v-loading="dialogLoading">
            <el-descriptions :column="1" border>
                <el-descriptions-item label="节点名称">{{ nodeDetail.activityName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="类型">{{ nodeDetail.activityType || '-' }}</el-descriptions-item>
                <el-descriptions-item label="开始时间">{{ nodeDetail.startTime ? new Date(nodeDetail.startTime).toLocaleString() : '-' }}</el-descriptions-item>
                <el-descriptions-item label="结束时间">{{ nodeDetail.endTime ? new Date(nodeDetail.endTime).toLocaleString() : '-' }}</el-descriptions-item>
                <el-descriptions-item label="耗时">{{ nodeDetail.durationInMillis ? (nodeDetail.durationInMillis / 1000).toFixed(2) + '秒' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="办理人">{{ nodeDetail.assigneeName || nodeDetail.assignee || '-' }}</el-descriptions-item>
                <el-descriptions-item label="审批意见">
                    <div v-if="nodeDetail.comments && nodeDetail.comments.length">
                        <div v-for="(comment, index) in nodeDetail.comments" :key="index">{{ comment }}</div>
                    </div>
                    <span v-else>-</span>
                </el-descriptions-item>
            </el-descriptions>
        </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import BpmnViewer from 'bpmn-js/lib/Viewer';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import { ZoomIn, ZoomOut, ScaleToOriginal, FullScreen, Download, Picture } from '@element-plus/icons-vue'
import { getNodeDetail } from '@/api/ops/workflow'

const props = defineProps({
  xml: {
    type: String,
    default: ''
  },
  activeActivityIds: {
    type: Array,
    default: () => []
  },
  activeTaskInfo: {
    type: Array,
    default: () => []
  },
  processInstanceId: {
    type: String,
    default: ''
  }
});

const canvasRef = ref(null);
let viewer = null;
const loading = ref(false);
const dialogVisible = ref(false);
const nodeDetail = ref({});
const dialogLoading = ref(false);

onMounted(() => {
  initViewer();
});

onBeforeUnmount(() => {
  if (viewer) {
    viewer.destroy();
  }
});

watch(() => props.xml, (val) => {
  if (val) {
    importXML(val);
  }
});

watch(() => props.activeActivityIds, () => {
  highlightNodes();
}, { deep: true });

watch(() => props.activeTaskInfo, () => {
  highlightNodes();
}, { deep: true });

const emit = defineEmits(['node-click']);

  function initViewer() {
    viewer = new BpmnViewer({
      container: canvasRef.value
    });
    
    // Add Event Listener
    const eventBus = viewer.get('eventBus');
    eventBus.on('element.click', function(e) {
      const { element } = e;
      if (props.processInstanceId && element.type !== 'bpmn:SequenceFlow') {
          handleNodeClick(element);
      }
      emit('node-click', element);
    });

    if (props.xml) {
      importXML(props.xml);
    }
  }

async function handleNodeClick(element) {
    dialogVisible.value = true;
    dialogLoading.value = true;
    nodeDetail.value = {};
    try {
        const res = await getNodeDetail(props.processInstanceId, element.id);
        nodeDetail.value = res.data;
    } catch (e) {
        nodeDetail.value = { error: "无法获取节点详情" };
    } finally {
        dialogLoading.value = false;
    }
}

async function importXML(xml) {
  loading.value = true;
  try {
    await viewer.importXML(xml);
    const canvas = viewer.get('canvas');
    canvas.zoom('fit-viewport');
    highlightNodes();
  } catch (err) {
    console.error('Render error', err);
  } finally {
    loading.value = false;
  }
}

function handleZoom(val) {
    if (!viewer) return;
    const canvas = viewer.get('canvas');
    if (val === 'fit') {
        canvas.zoom('fit-viewport');
    } else if (val === 'reset') {
        canvas.zoom(1.0);
    } else {
        canvas.zoom(canvas.zoom() + val);
    }
}

async function handleExport(type) {
    if (!viewer) return;
    try {
        if (type === 'svg') {
            const { svg } = await viewer.saveSVG();
            downloadFile(svg, 'process.svg', 'image/svg+xml');
        } else if (type === 'png') {
             const { svg } = await viewer.saveSVG();
             // Convert SVG to PNG using Canvas
             const canvas = document.createElement('canvas');
             const ctx = canvas.getContext('2d');
             const img = new Image();
             const svgBlob = new Blob([svg], {type: 'image/svg+xml;charset=utf-8'});
             const url = URL.createObjectURL(svgBlob);
             
             img.onload = function() {
                 canvas.width = img.width;
                 canvas.height = img.height;
                 ctx.drawImage(img, 0, 0);
                 const pngUrl = canvas.toDataURL('image/png');
                 downloadFile(pngUrl, 'process.png', 'image/png', true);
                 URL.revokeObjectURL(url);
             };
             img.src = url;
        }
    } catch (err) {
        console.error('Export error', err);
    }
}

function downloadFile(data, name, mimeType, isUrl = false) {
    const link = document.createElement('a');
    link.download = name;
    if (isUrl) {
        link.href = data;
    } else {
        const blob = new Blob([data], { type: mimeType });
        link.href = URL.createObjectURL(blob);
    }
    link.click();
}

function highlightNodes() {
  if (!viewer) return;
  const canvas = viewer.get('canvas');
  const elementRegistry = viewer.get('elementRegistry');
  const overlays = viewer.get('overlays');
  
  // Clear existing overlays
  overlays.clear();
  
  if (props.activeActivityIds && props.activeActivityIds.length > 0) {
    props.activeActivityIds.forEach(id => {
      if (elementRegistry.get(id)) {
        try {
           canvas.addMarker(id, 'highlight');
           canvas.addMarker(id, 'highlight-animation'); // Optional animation
        } catch (e) {}
      }
    });
  }
  
  if (props.activeTaskInfo && props.activeTaskInfo.length > 0) {
    props.activeTaskInfo.forEach(info => {
      if (elementRegistry.get(info.activityId)) {
        const html = `
          <div class="bpmn-overlay-task-info" style="background: rgba(255, 255, 255, 0.95); padding: 8px; border: 1px solid #1890ff; border-radius: 4px; font-size: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.15); min-width: 150px;">
            <div style="font-weight: bold; color: #333; margin-bottom: 4px;">当前节点: ${info.taskName || ''}</div>
            <div style="color: #666; margin-bottom: 2px;">办理人: <span style="color: #1890ff;">${info.assigneeNickName || info.assignee || '未签收'}</span></div>
            <div style="color: #999; font-size: 11px;">开始时间: ${info.createTime ? new Date(info.createTime).toLocaleString() : ''}</div>
          </div>
        `;
        
        try {
            overlays.add(info.activityId, {
              position: {
                bottom: -10,
                left: 0
              },
              html: html
            });
        } catch (e) {
            console.warn("Could not add overlay for " + info.activityId);
        }
      }
    });
  }
}
</script>

<style scoped>
.bpmn-viewer-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  background-color: #fff;
  overflow: hidden;
  position: relative;
}
.bpmn-toolbar {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 100;
    background: rgba(255,255,255,0.8);
    padding: 5px;
    border-radius: 4px;
}
.canvas {
  width: 100%;
  height: 100%;
}

/* Highlight Styles */
:deep(.highlight:not(.djs-connection) .djs-visual > :nth-child(1)) {
  stroke: #1890ff !important;
  fill: #e6f7ff !important;
  stroke-width: 2px !important;
}

:deep(.highlight.djs-connection .djs-visual > :nth-child(1)) {
  stroke: #1890ff !important;
}

/* Animation for active task */
@keyframes breath {
  0% {
    stroke-opacity: 1;
    fill-opacity: 1;
  }
  50% {
    stroke-opacity: 0.5;
    fill-opacity: 0.5;
  }
  100% {
    stroke-opacity: 1;
    fill-opacity: 1;
  }
}

:deep(.highlight-animation:not(.djs-connection) .djs-visual > :nth-child(1)) {
  animation: breath 2s infinite ease-in-out;
}
</style>
