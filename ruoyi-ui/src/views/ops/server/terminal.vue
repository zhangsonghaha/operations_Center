<template>
  <div class="terminal-container">
    <div class="terminal-header">
      <div class="header-info">
        <span class="server-name">{{ serverName }}</span>
        <span class="connection-status" :class="statusClass">{{ statusText }}</span>
      </div>
      <div class="header-actions">
        <el-button type="info" size="small" @click="handleCopy">复制</el-button>
        <el-button type="info" size="small" @click="handlePaste">粘贴</el-button>
        <el-button type="warning" size="small" @click="handleClear">清屏</el-button>
        <el-button type="danger" size="small" @click="handleDisconnect">断开</el-button>
      </div>
    </div>
    <div class="terminal-body" ref="terminalRef"></div>
  </div>
</template>

<script setup name="Terminal">
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import 'xterm/css/xterm.css'
import { getServer } from "@/api/ops/server";

const { proxy } = getCurrentInstance();
const route = useRoute();
const serverId = route.params.serverId;

const terminalRef = ref(null);
const serverName = ref('Connecting...');
const status = ref('connecting'); // connecting, connected, disconnected
const statusText = computed(() => {
  switch (status.value) {
    case 'connecting': return '连接中...';
    case 'connected': return '已连接';
    case 'disconnected': return '已断开';
    default: return '未知状态';
  }
});
const statusClass = computed(() => {
  switch (status.value) {
    case 'connecting': return 'text-warning';
    case 'connected': return 'text-success';
    case 'disconnected': return 'text-danger';
    default: return 'text-info';
  }
});

let term = null;
let socket = null;
let fitAddon = null;

onMounted(() => {
  initTerminal();
  initServerInfo();
});

onBeforeUnmount(() => {
  closeSocket();
  if (term) {
    term.dispose();
  }
});

function initServerInfo() {
  getServer(serverId).then(response => {
    serverName.value = response.data.serverName + ' (' + response.data.publicIp + ')';
    initSocket();
  }).catch(() => {
    serverName.value = 'Unknown Server';
    status.value = 'disconnected';
  });
}

function initTerminal() {
  term = new Terminal({
    fontSize: 14,
    cursorBlink: true,
    cursorStyle: 'block',
    theme: {
      background: '#1e1e1e',
      foreground: '#ffffff',
    }
  });
  
  fitAddon = new FitAddon();
  term.loadAddon(fitAddon);
  term.open(terminalRef.value);
  fitAddon.fit();

  term.onData(data => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      const msg = {
        type: 'command',
        command: data
      };
      socket.send(JSON.stringify(msg));
    }
  });

  term.onResize(size => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      const msg = {
        type: 'resize',
        cols: size.cols,
        rows: size.rows
      };
      socket.send(JSON.stringify(msg));
    }
  });

  window.addEventListener('resize', () => {
    fitAddon.fit();
  });
}

function initSocket() {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  // 优先使用环境变量中的后端地址，如果没有则回退到当前 host
  // 注意：RuoYi-Vue 前端开发环境通常代理了 /dev-api，但 WebSocket 需要直接连后端端口或通过代理配置
  // 假设后端运行在 8080 端口
  
  let host = window.location.host;
  let path = '/ws/ssh';
  
  // 简单判断开发环境
  if (process.env.NODE_ENV === 'development') {
    // 开发环境通常是 localhost:8080 (后端) 或 localhost:80 (Nginx)
    // 如果前端是 localhost:1024，后端是 localhost:8080
    host = 'localhost:8080'; 
  }
  
  const url = `${protocol}://${host}${path}`;
  console.log('Connecting to WebSocket:', url);

  socket = new WebSocket(url);

  socket.onopen = () => {
    console.log('WebSocket Connected');
    status.value = 'connected';
    // 发送连接初始化消息
    const msg = {
      type: 'connect',
      serverId: Number(serverId),
      cols: term.cols,
      rows: term.rows
    };
    socket.send(JSON.stringify(msg));
  };

  socket.onmessage = (event) => {
    term.write(event.data);
  };

  socket.onclose = (event) => {
    console.log('WebSocket Closed:', event);
    status.value = 'disconnected';
    term.write('\r\n\x1b[31mConnection closed by server. (Code: ' + event.code + ')\x1b[0m\r\n');
  };

  socket.onerror = (error) => {
    console.error('WebSocket Error:', error);
    status.value = 'disconnected';
    term.write('\r\n\x1b[31mWebSocket connection error. Please check console logs.\x1b[0m\r\n');
  };
}

function closeSocket() {
  if (socket) {
    socket.close();
  }
}

function handleCopy() {
  const selection = term.getSelection();
  if (selection) {
    navigator.clipboard.writeText(selection).then(() => {
      proxy.$modal.msgSuccess("已复制到剪贴板");
    });
  }
}

async function handlePaste() {
  try {
    const text = await navigator.clipboard.readText();
    if (text && socket && socket.readyState === WebSocket.OPEN) {
      const msg = {
        type: 'command',
        command: text
      };
      socket.send(JSON.stringify(msg));
    }
  } catch (err) {
    proxy.$modal.msgError("无法读取剪贴板");
  }
}

function handleClear() {
  term.clear();
}

function handleDisconnect() {
  closeSocket();
}
</script>

<style scoped lang="scss">
.terminal-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #1e1e1e;
  
  .terminal-header {
    height: 40px;
    background-color: #2d2d2d;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    border-bottom: 1px solid #333;
    
    .header-info {
      color: #fff;
      font-size: 14px;
      
      .server-name {
        margin-right: 10px;
        font-weight: bold;
      }
      
      .connection-status {
        font-size: 12px;
        
        &.text-success { color: #67c23a; }
        &.text-warning { color: #e6a23c; }
        &.text-danger { color: #f56c6c; }
        &.text-info { color: #909399; }
      }
    }
  }
  
  .terminal-body {
    flex: 1;
    padding: 10px;
    overflow: hidden;
    
    :deep(.xterm-viewport) {
      overflow-y: auto;
    }
  }
}
</style>
