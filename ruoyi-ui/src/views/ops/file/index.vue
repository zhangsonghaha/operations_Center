<template>
  <div class="app-container">
    <el-container style="height: calc(100vh - 84px); border: 1px solid #eee">
      <!-- 左侧服务器列表 -->
      <el-aside width="250px" style="background-color: #fff; border-right: 1px solid #eee; padding: 10px">
        <el-input
          v-model="serverName"
          placeholder="搜索服务器"
          prefix-icon="Search"
          clearable
          style="margin-bottom: 10px"
        />
        <el-menu
          :default-active="currentServerId"
          class="server-menu"
          @select="handleServerSelect"
        >
          <el-menu-item 
            v-for="server in filteredServerList" 
            :key="server.serverId" 
            :index="String(server.serverId)"
          >
            <el-icon><Monitor /></el-icon>
            <span class="server-item-text" :title="server.serverName">{{ server.serverName }}</span>
            <el-tag 
              size="small" 
              :type="statusType(server.serverId)" 
              effect="plain" 
              class="status-badge"
            >
              {{ statusText(server.serverId) }}
            </el-tag>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧文件管理 -->
      <el-main style="padding: 0">
        <div v-if="!currentServerId" class="empty-state">
          <el-empty description="请选择左侧服务器进行文件管理" />
        </div>
        <div v-else-if="serverStatusMap[currentServerId] === 'error'" class="empty-state">
          <el-empty description="连接失败">
            <el-button type="primary" @click="handleRetry">重试连接</el-button>
          </el-empty>
        </div>
        <file-manager v-else :server-id="currentServerId" :key="currentServerId" @connection-status="handleConnectionStatus" />
      </el-main>
    </el-container>
  </div>
</template>

<script setup name="FileIndex">
import { listServer } from "@/api/ops/server";
import { Search, Monitor } from '@element-plus/icons-vue';
import FileManager from '../server/fileManager.vue'; // 复用之前的组件

const route = useRoute();

const serverList = ref([]);
const serverName = ref('');
const currentServerId = ref('');
const serverStatusMap = ref({}); // 存储服务器连接状态: 'connecting', 'connected', 'error'

const filteredServerList = computed(() => {
  if (!serverName.value) return serverList.value;
  return serverList.value.filter(server => 
    server.serverName.toLowerCase().includes(serverName.value.toLowerCase()) ||
    server.publicIp.includes(serverName.value)
  );
});

function getServerList() {
  listServer({ pageNum: 1, pageSize: 1000 }).then(response => {
    serverList.value = response.rows;
    // 如果路由参数中有 serverId，自动选中
    if (route.query.serverId) {
      handleServerSelect(String(route.query.serverId));
    }
  });
}

function handleServerSelect(index) {
  // 如果点击的是当前已经选中的服务器，不做任何操作
  if (currentServerId.value === index) return;
  
  // 如果该服务器已经是“已连接”状态，也不重置为 connecting，
  // 而是让 FileManager 组件自己去决定是否需要刷新（或者保持现状）
  // 但目前 FileManager 是通过 key 强制刷新的，所以每次切换都会重新加载
  // 关键修正：不要在父组件这里手动设置为 connecting，
  // 而是等待 FileManager 组件挂载后发出的 connecting 事件
  // 这样如果快速切换，旧组件卸载发出 idle，新组件挂载发出 connecting，逻辑更顺畅
  
  currentServerId.value = index;
  // serverStatusMap.value[index] = 'connecting'; // 删除这一行，完全交由子组件控制状态
}

function handleConnectionStatus(payload) {
  // 兼容旧格式，虽然现在已经都改成了对象格式
  if (typeof payload === 'string') {
    if (currentServerId.value) {
      serverStatusMap.value[currentServerId.value] = payload;
    }
  } else {
    // 使用子组件传回的 serverId 更新状态，确保多服务器切换时状态不会错乱
    const { status, serverId } = payload;
    if (serverId) {
      serverStatusMap.value[serverId] = status;
    }
  }
}

function handleRetry() {
  if (currentServerId.value) {
    serverStatusMap.value[currentServerId.value] = 'connecting';
    // 强制刷新组件：通过改变key或重新挂载
    // 这里简单地利用 v-if 的特性，先设为空再设回去不太优雅，
    // 更好的方式是 fileManager 组件暴露 retry 方法，或者直接重置状态让 v-else 重新渲染 file-manager
    // 由于 v-else 依赖 serverStatusMap 不为 error，所以我们需要先将状态改为 connecting
    // Vue 会自动卸载 error 界面，挂载 file-manager，从而触发 file-manager 的 mounted 钩子重新加载列表
  }
}

function statusType(serverId) {
  const status = serverStatusMap.value[serverId];
  if (status === 'connecting') return 'warning';
  if (status === 'connected') return 'success';
  if (status === 'error') return 'danger';
  return 'info';
}

function statusText(serverId) {
  const status = serverStatusMap.value[serverId];
  if (status === 'connecting') return '连接中';
  if (status === 'connected') return '已连接';
  if (status === 'error') return '失败';
  return '未连接';
}

getServerList();
</script>

<style scoped>
.server-menu {
  border-right: none;
}
.server-item-text {
  display: inline-block;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}
.ml5 {
  margin-left: 5px;
}
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
:deep(.el-menu-item) {
  height: 40px;
  line-height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 10px !important;
}

.status-badge {
  margin-left: auto;
  min-width: 60px;
  text-align: center;
}
</style>
