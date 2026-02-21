<template>
  <div class="file-manager-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item><a @click="handlePathClick('')">根目录</a></el-breadcrumb-item>
        <el-breadcrumb-item v-for="(item, index) in pathSegments" :key="index">
          <a @click="handlePathClick(item.fullPath)">{{ item.name }}</a>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Upload" @click="handleUpload">上传文件</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="default" plain icon="Refresh" @click="getList">刷新</el-button>
      </el-col>
    </el-row>

    <!-- 文件列表 -->
    <el-table v-loading="loading" :data="fileList" @row-contextmenu="handleContextmenu" height="calc(100vh - 250px)">
      <el-table-column label="名称" prop="name" min-width="200">
        <template #default="scope">
          <div class="file-name" @click="handleFileClick(scope.row)">
            <el-icon class="file-icon" v-if="scope.row.dir"><Folder /></el-icon>
            <el-icon class="file-icon" v-else><Document /></el-icon>
            <span class="name-text">{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="大小" prop="size" width="120" align="right">
        <template #default="scope">
          {{ formatSize(scope.row.size) }}
        </template>
      </el-table-column>
      <el-table-column label="权限" prop="permission" width="120" align="center" />
      <el-table-column label="修改时间" prop="modTime" width="160" align="center" />
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" v-if="!scope.row.dir" @click="handleDownload(scope.row)">下载</el-button>
          <el-button link type="primary" v-if="scope.row.dir" @click="handleCompress(scope.row)">压缩</el-button>
          <el-button link type="primary" v-if="isArchive(scope.row.name)" @click="handleExtract(scope.row)">解压</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 上传弹窗 -->
    <el-dialog title="文件上传" v-model="uploadOpen" width="400px">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :http-request="uploadFileRequest"
        :show-file-list="false"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup name="FileManager">
import { listFiles, uploadFile, deleteFile, compressFile, extractFile } from "@/api/ops/file";
import { Folder, Document, UploadFilled } from '@element-plus/icons-vue';
import { getToken } from "@/utils/auth";

const props = defineProps({
  serverId: {
    type: [Number, String],
    required: true
  }
});

const emit = defineEmits(['connection-status']);

const { proxy } = getCurrentInstance();
const serverId = props.serverId;

const loading = ref(false);
const fileList = ref([]);
const currentPath = ref(''); // 初始为空，由后端返回Home路径
const uploadOpen = ref(false);
let timeoutId = null;
let isUnmounted = false;

const pathSegments = computed(() => {
  if (!currentPath.value || currentPath.value === '/') return [];
  // 移除开头的斜杠，防止空元素
  const path = currentPath.value.startsWith('/') ? currentPath.value.substring(1) : currentPath.value;
  const parts = path.split('/').filter(p => p);
  let full = '';
  return parts.map(p => {
    full += '/' + p;
    return { name: p, fullPath: full };
  });
});

function getList() {
  loading.value = true;
  emit('connection-status', { status: 'connecting', serverId: serverId });
  
  // 清除旧的定时器
  if (timeoutId) clearTimeout(timeoutId);

  // 设置超时定时器
  timeoutId = setTimeout(() => {
    if (isUnmounted) return;
    if (loading.value) {
      loading.value = false;
      emit('connection-status', { status: 'error', serverId: serverId });
    }
  }, 10000); // 10秒超时

  listFiles(serverId, currentPath.value).then(response => {
    if (isUnmounted) return;
    if (timeoutId) clearTimeout(timeoutId);
    
    // 后端现在返回 Map { path: "...", files: [...] }
    if (response.data.path) {
      currentPath.value = response.data.path;
    }
    fileList.value = response.data.files;
    loading.value = false;
    emit('connection-status', { status: 'connected', serverId: serverId });
  }).catch(() => {
    if (isUnmounted) return;
    if (timeoutId) clearTimeout(timeoutId);
    loading.value = false;
    emit('connection-status', { status: 'error', serverId: serverId });
  });
}

onUnmounted(() => {
  isUnmounted = true;
  if (timeoutId) clearTimeout(timeoutId);
  if (loading.value) {
    // 如果正在加载中被卸载，通知父组件重置为 idle 状态，表示连接尝试已取消
    emit('connection-status', { status: 'idle', serverId: serverId });
  }
});

function handlePathClick(path) {
  // 如果是空字符串，表示点击了根目录，但为了体验，点击根目录应该跳转到系统根 /
  // 但这里设计是点击“根目录”跳转到 /
  currentPath.value = path || '/';
  getList();
}

function handleFileClick(row) {
  if (row.dir) {
    // 处理路径拼接，确保不出现双斜杠
    const basePath = currentPath.value.endsWith('/') ? currentPath.value : currentPath.value + '/';
    currentPath.value = basePath + row.name;
    getList();
  }
}

function handleUpload() {
  uploadOpen.value = true;
}

function uploadFileRequest(options) {
  const formData = new FormData();
  formData.append("file", options.file);
  formData.append("serverId", serverId);
  formData.append("path", currentPath.value);
  
  const loading = proxy.$loading({ text: '正在上传...' });
  uploadFile(formData).then(() => {
    loading.close();
    proxy.$modal.msgSuccess("上传成功");
    uploadOpen.value = false;
    getList();
  }).catch(() => {
    loading.close();
  });
}

function handleDownload(row) {
  const basePath = currentPath.value.endsWith('/') ? currentPath.value : currentPath.value + '/';
  const path = basePath + row.name;
  // 直接将 token 放在 Authorization 参数中，后端 Filter 已适配从 Parameter 读取
  // 必须使用 Authorization 作为参数名，因为 Filter 逻辑是先查 Header 再查 Parameter
  // 但为了兼容某些环境，建议直接传 Access-Token 或其他自定义参数名，但这里我们复用了 Header 的配置
  // TokenService 中配置的 header 默认是 Authorization
  const url = import.meta.env.VITE_APP_BASE_API + "/system/server/file/download?serverId=" + serverId + "&path=" + encodeURIComponent(path) + "&Authorization=Bearer " + getToken();
  window.open(url);
}

function handleDelete(row) {
  const basePath = currentPath.value.endsWith('/') ? currentPath.value : currentPath.value + '/';
  const path = basePath + row.name;
  proxy.$modal.confirm('确认删除 "' + row.name + '" 吗？').then(() => {
    return deleteFile(serverId, path, row.dir);
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功");
    getList();
  });
}

function handleCompress(row) {
  const basePath = currentPath.value.endsWith('/') ? currentPath.value : currentPath.value + '/';
  const path = basePath + row.name;
  proxy.$modal.confirm('确认将 "' + row.name + '" 压缩为 .tar.gz 吗？').then(() => {
    const loading = proxy.$loading({ text: '正在压缩...' });
    return compressFile(serverId, path, 'tar');
  }).then(() => {
    proxy.$loading().close();
    proxy.$modal.msgSuccess("压缩成功");
    getList();
  }).catch(() => proxy.$loading().close());
}

function handleExtract(row) {
  const basePath = currentPath.value.endsWith('/') ? currentPath.value : currentPath.value + '/';
  const path = basePath + row.name;
  proxy.$modal.confirm('确认解压 "' + row.name + '" 吗？').then(() => {
    const loading = proxy.$loading({ text: '正在解压...' });
    return extractFile(serverId, path);
  }).then(() => {
    proxy.$loading().close();
    proxy.$modal.msgSuccess("解压成功");
    getList();
  }).catch(() => proxy.$loading().close());
}

function formatSize(size) {
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB';
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(2) + ' MB';
  return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB';
}

function isArchive(name) {
  return name.endsWith('.zip') || name.endsWith('.tar.gz') || name.endsWith('.tgz');
}

getList();
</script>

<style scoped>
.file-manager-container {
  padding: 10px;
  height: 100%;
}
.breadcrumb-container {
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}
.file-name {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.file-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #409eff;
}
.name-text {
  color: #606266;
}
.name-text:hover {
  color: #409eff;
  text-decoration: underline;
}
</style>
