<template>
  <div class="bpmn-viewer-container" v-loading="loading">
    <div ref="canvasRef" class="canvas"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import BpmnViewer from 'bpmn-js/lib/Viewer';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';

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
  }
});

const canvasRef = ref(null);
let viewer = null;
const loading = ref(false);

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
      emit('node-click', element);
    });

    if (props.xml) {
      importXML(props.xml);
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
