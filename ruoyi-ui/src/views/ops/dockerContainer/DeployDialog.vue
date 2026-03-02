<template>
  <el-dialog
    title="部署 Docker 容器"
    v-model="visible"
    width="1000px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-steps :active="currentStep" finish-status="success" align-center class="mb20">
      <el-step title="选择服务器" />
      <el-step title="配置容器" />
      <el-step title="部署中" />
    </el-steps>

    <!-- 步骤1: 选择服务器 -->
    <div v-show="currentStep === 0">
      <el-form :model="deployForm" ref="serverFormRef" label-width="100px">
        <el-form-item label="目标服务器" prop="serverId" :rules="[{ required: true, message: '请选择服务器', trigger: 'change' }]">
          <el-select
            v-model="deployForm.serverId"
            placeholder="请选择服务器"
            style="width: 100%"
            @change="handleServerChange"
          >
            <el-option
              v-for="server in serverList"
              :key="server.serverId"
              :label="`${server.serverName} (${server.publicIp})`"
              :value="server.serverId"
            />
          </el-select>
        </el-form-item>

        <!-- Docker环境检测结果 -->
        <el-alert
          v-if="envCheckResult"
          :title="envCheckResult.dockerInstalled ? 'Docker 环境正常' : 'Docker 未安装'"
          :type="envCheckResult.dockerInstalled && envCheckResult.dockerRunning ? 'success' : 'error'"
          :closable="false"
          class="mb20"
        >
          <template v-if="envCheckResult.dockerInstalled">
            <div>Docker 版本: {{ envCheckResult.dockerVersion }}</div>
            <div v-if="envCheckResult.storageDriver">存储驱动: {{ envCheckResult.storageDriver }}</div>
            <div v-if="envCheckResult.totalSpace">
              磁盘空间: {{ formatBytes(envCheckResult.usedSpace) }} / {{ formatBytes(envCheckResult.totalSpace) }}
            </div>
            <div v-if="!envCheckResult.dockerRunning" class="text-danger">
              Docker 服务未运行，请先启动 Docker 服务
            </div>
          </template>
          <template v-else>
            <div>请先在目标服务器上安装 Docker</div>
          </template>
        </el-alert>
      </el-form>

      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="nextStep"
          :disabled="!deployForm.serverId || !envCheckResult || !envCheckResult.dockerInstalled || !envCheckResult.dockerRunning"
        >下一步</el-button>
      </div>
    </div>

    <!-- 步骤2: 配置容器 -->
    <div v-show="currentStep === 1">
      <el-form :model="deployForm" ref="configFormRef" label-width="120px">
        <!-- 模板选择 -->
        <el-form-item label="使用模板">
          <el-select
            v-model="selectedTemplate"
            placeholder="选择模板快速配置（可选）"
            clearable
            style="width: 100%"
            @change="handleTemplateChange"
          >
            <el-option
              v-for="template in templateList"
              :key="template.templateId"
              :label="template.templateName"
              :value="template.templateId"
            />
          </el-select>
        </el-form-item>

        <el-divider />

        <!-- 基础配置 -->
        <el-form-item label="容器名称" prop="containerName" :rules="[
          { required: true, message: '请输入容器名称', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线和连字符', trigger: 'blur' }
        ]">
          <el-input v-model="deployForm.containerName" placeholder="例如: my-nginx" />
        </el-form-item>

        <el-form-item label="镜像名称" prop="imageName" :rules="[{ required: true, message: '请输入镜像名称', trigger: 'blur' }]">
          <el-input v-model="deployForm.imageName" placeholder="例如: nginx" />
        </el-form-item>

        <el-form-item label="镜像标签" prop="imageTag">
          <el-input v-model="deployForm.imageTag" placeholder="默认: latest" />
        </el-form-item>

        <!-- 端口映射 -->
        <el-form-item label="端口映射">
          <div v-for="(port, index) in deployForm.ports" :key="index" class="port-item">
            <el-input
              v-model.number="port.hostPort"
              placeholder="主机端口"
              type="number"
              style="width: 150px"
            />
            <span class="port-separator">:</span>
            <el-input
              v-model.number="port.containerPort"
              placeholder="容器端口"
              type="number"
              style="width: 150px"
            />
            <span class="port-separator">/</span>
            <el-select v-model="port.protocol" style="width: 100px">
              <el-option label="tcp" value="tcp" />
              <el-option label="udp" value="udp" />
            </el-select>
            <el-button
              type="danger"
              icon="Delete"
              circle
              size="small"
              @click="removePort(index)"
              class="ml10"
            />
          </div>
          <el-button type="primary" icon="Plus" size="small" @click="addPort">添加端口</el-button>
        </el-form-item>

        <!-- 环境变量 -->
        <el-form-item label="环境变量">
          <div v-for="(env, index) in deployForm.envVars" :key="index" class="env-item">
            <el-input
              v-model="env.key"
              placeholder="变量名"
              style="width: 200px"
            />
            <span class="env-separator">=</span>
            <el-input
              v-model="env.value"
              placeholder="变量值"
              style="width: 300px"
            />
            <el-button
              type="danger"
              icon="Delete"
              circle
              size="small"
              @click="removeEnv(index)"
              class="ml10"
            />
          </div>
          <el-button type="primary" icon="Plus" size="small" @click="addEnv">添加环境变量</el-button>
        </el-form-item>

        <!-- 卷挂载 -->
        <el-form-item label="卷挂载">
          <div v-for="(volume, index) in deployForm.volumes" :key="index" class="volume-item">
            <el-input
              v-model="volume.hostPath"
              placeholder="主机路径"
              style="width: 250px"
            />
            <span class="volume-separator">:</span>
            <el-input
              v-model="volume.containerPath"
              placeholder="容器路径"
              style="width: 250px"
            />
            <el-select v-model="volume.mode" style="width: 80px" class="ml10">
              <el-option label="rw" value="rw" />
              <el-option label="ro" value="ro" />
            </el-select>
            <el-button
              type="danger"
              icon="Delete"
              circle
              size="small"
              @click="removeVolume(index)"
              class="ml10"
            />
          </div>
          <el-button type="primary" icon="Plus" size="small" @click="addVolume">添加卷挂载</el-button>
        </el-form-item>

        <!-- 资源限制 -->
        <el-form-item label="CPU 限制">
          <el-input-number
            v-model="deployForm.cpuLimit"
            :min="0.1"
            :max="4"
            :step="0.5"
            :precision="2"
            placeholder="核数"
          />
          <span class="ml10 text-muted">核（0.1-4.0，默认2.0）</span>
        </el-form-item>

        <el-form-item label="内存限制">
          <el-input
            v-model="deployForm.memoryLimit"
            placeholder="例如: 512m, 2g"
            style="width: 200px"
          />
          <span class="ml10 text-muted">支持单位: m(MB), g(GB)，默认2g</span>
        </el-form-item>

        <!-- 高级配置 -->
        <el-form-item label="重启策略">
          <el-select v-model="deployForm.restartPolicy" style="width: 200px">
            <el-option label="除非手动停止" value="unless-stopped" />
            <el-option label="总是重启" value="always" />
            <el-option label="失败时重启" value="on-failure" />
            <el-option label="不重启" value="no" />
          </el-select>
        </el-form-item>

        <el-form-item label="网络模式">
          <el-select v-model="deployForm.networkMode" clearable style="width: 200px">
            <el-option label="桥接网络" value="bridge" />
            <el-option label="主机网络" value="host" />
            <el-option label="无网络" value="none" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="dialog-footer">
        <el-button @click="prevStep">上一步</el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleDeploy" :loading="deploying">开始部署</el-button>
      </div>
    </div>

    <!-- 步骤3: 部署中 -->
    <div v-show="currentStep === 2">
      <div class="deploy-log-container">
        <div class="deploy-log-header">
          <span>部署日志</span>
          <el-tag :type="deployStatus === 'success' ? 'success' : deployStatus === 'failed' ? 'danger' : 'info'">
            {{ deployStatusText }}
          </el-tag>
        </div>
        <pre class="deploy-log-content" ref="logContentRef">{{ deployLog }}</pre>
      </div>

      <div class="dialog-footer">
        <el-button @click="handleClose" v-if="deployStatus !== 'deploying'">关闭</el-button>
        <el-button type="primary" @click="handleViewContainer" v-if="deployStatus === 'success'">查看容器</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { deployContainer, checkDockerEnv, listTemplate, getTemplate } from '@/api/ops/docker'

// Props
const props = defineProps({
  modelValue: Boolean,
  serverList: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// 数据
const visible = ref(false)
const currentStep = ref(0)
const serverFormRef = ref(null)
const configFormRef = ref(null)
const logContentRef = ref(null)

// 环境检测
const envCheckResult = ref(null)

// 模板
const templateList = ref([])
const selectedTemplate = ref(null)

// 部署表单
const deployForm = reactive({
  serverId: null,
  containerName: '',
  imageName: '',
  imageTag: 'latest',
  ports: [],
  envVars: [],
  volumes: [],
  cpuLimit: 2.0,
  memoryLimit: '2g',
  restartPolicy: 'unless-stopped',
  networkMode: ''
})

// 部署状态
const deploying = ref(false)
const deployStatus = ref('') // deploying, success, failed
const deployLog = ref('')
const deployLogId = ref(null)
let wsConnection = null

// 监听 modelValue
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadTemplates()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 计算部署状态文本
const deployStatusText = computed(() => {
  const statusMap = {
    deploying: '部署中...',
    success: '部署成功',
    failed: '部署失败'
  }
  return statusMap[deployStatus.value] || '准备部署'
})

// 加载模板列表
function loadTemplates() {
  listTemplate({}).then(response => {
    templateList.value = response.rows || []
  })
}

// 服务器变更
function handleServerChange(serverId) {
  if (!serverId) {
    envCheckResult.value = null
    return
  }
  
  // 检测Docker环境
  checkDockerEnv(serverId).then(response => {
    envCheckResult.value = response.data
  }).catch(() => {
    envCheckResult.value = null
    ElMessage.error('环境检测失败')
  })
}

// 模板变更
function handleTemplateChange(templateId) {
  if (!templateId) return
  
  getTemplate(templateId).then(response => {
    const template = response.data
    deployForm.imageName = template.imageName || ''
    deployForm.imageTag = template.imageTag || 'latest'
    
    // 解析端口
    if (template.ports) {
      try {
        deployForm.ports = JSON.parse(template.ports)
      } catch (e) {
        deployForm.ports = []
      }
    }
    
    // 解析环境变量
    if (template.envVars) {
      try {
        const envObj = JSON.parse(template.envVars)
        deployForm.envVars = Object.entries(envObj).map(([key, value]) => ({ key, value }))
      } catch (e) {
        deployForm.envVars = []
      }
    }
    
    // 解析卷挂载
    if (template.volumes) {
      try {
        deployForm.volumes = JSON.parse(template.volumes)
      } catch (e) {
        deployForm.volumes = []
      }
    }
    
    if (template.cpuLimit) {
      deployForm.cpuLimit = template.cpuLimit
    }
    if (template.memoryLimit) {
      deployForm.memoryLimit = template.memoryLimit
    }
    if (template.restartPolicy) {
      deployForm.restartPolicy = template.restartPolicy
    }
    if (template.networkMode) {
      deployForm.networkMode = template.networkMode
    }
    
    ElMessage.success('模板已应用')
  })
}

// 添加端口
function addPort() {
  deployForm.ports.push({
    hostPort: null,
    containerPort: null,
    protocol: 'tcp'
  })
}

// 删除端口
function removePort(index) {
  deployForm.ports.splice(index, 1)
}

// 添加环境变量
function addEnv() {
  deployForm.envVars.push({
    key: '',
    value: ''
  })
}

// 删除环境变量
function removeEnv(index) {
  deployForm.envVars.splice(index, 1)
}

// 添加卷挂载
function addVolume() {
  deployForm.volumes.push({
    hostPath: '',
    containerPath: '',
    mode: 'rw'
  })
}

// 删除卷挂载
function removeVolume(index) {
  deployForm.volumes.splice(index, 1)
}

// 下一步
function nextStep() {
  if (currentStep.value === 0) {
    serverFormRef.value.validate((valid) => {
      if (valid) {
        currentStep.value = 1
      }
    })
  }
}

// 上一步
function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 执行部署
function handleDeploy() {
  configFormRef.value.validate((valid) => {
    if (!valid) return
    
    // 转换数据格式
    const deployData = {
      serverId: deployForm.serverId,
      containerName: deployForm.containerName,
      imageName: deployForm.imageName,
      imageTag: deployForm.imageTag || 'latest',
      ports: deployForm.ports.filter(p => p.hostPort && p.containerPort),
      envVars: deployForm.envVars
        .filter(e => e.key && e.value)
        .reduce((obj, e) => {
          obj[e.key] = e.value
          return obj
        }, {}),
      volumes: deployForm.volumes.filter(v => v.hostPath && v.containerPath),
      cpuLimit: deployForm.cpuLimit,
      memoryLimit: deployForm.memoryLimit,
      restartPolicy: deployForm.restartPolicy,
      networkMode: deployForm.networkMode
    }
    
    deploying.value = true
    deployStatus.value = 'deploying'
    currentStep.value = 2
    deployLog.value = '正在连接服务器...\n'
    
    // 调用部署接口
    deployContainer(deployData).then(response => {
      deployLogId.value = response.data.logId
      
      // 建立WebSocket连接接收实时日志
      connectWebSocket(deployLogId.value)
    }).catch(error => {
      deploying.value = false
      deployStatus.value = 'failed'
      deployLog.value += '\n[ERROR] 部署失败: ' + (error.msg || error.message || '未知错误')
    })
  })
}

// 连接WebSocket接收实时日志
function connectWebSocket(logId) {
  const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/websocket/deployLog/${logId}`
  
  wsConnection = new WebSocket(wsUrl)
  
  wsConnection.onopen = () => {
    console.log('WebSocket connected')
  }
  
  wsConnection.onmessage = (event) => {
    const message = event.data
    deployLog.value += message
    
    // 自动滚动到底部
    nextTick(() => {
      if (logContentRef.value) {
        logContentRef.value.scrollTop = logContentRef.value.scrollHeight
      }
    })
    
    // 检查是否完成
    if (message.includes('[SUCCESS] 部署完成') || message.includes('========== 部署完成 ==========')) {
      deploying.value = false
      deployStatus.value = 'success'
      if (wsConnection) {
        wsConnection.close()
      }
    } else if (message.includes('[ERROR] 部署失败')) {
      deploying.value = false
      deployStatus.value = 'failed'
      if (wsConnection) {
        wsConnection.close()
      }
    }
  }
  
  wsConnection.onerror = (error) => {
    console.error('WebSocket error:', error)
    deploying.value = false
    deployStatus.value = 'failed'
    deployLog.value += '\n[ERROR] WebSocket连接失败'
  }
  
  wsConnection.onclose = () => {
    console.log('WebSocket closed')
  }
}

// 查看容器
function handleViewContainer() {
  emit('success')
  handleClose()
}

// 关闭对话框
function handleClose() {
  // 关闭WebSocket
  if (wsConnection) {
    wsConnection.close()
    wsConnection = null
  }
  
  // 重置表单
  currentStep.value = 0
  envCheckResult.value = null
  selectedTemplate.value = null
  Object.assign(deployForm, {
    serverId: null,
    containerName: '',
    imageName: '',
    imageTag: 'latest',
    ports: [],
    envVars: [],
    volumes: [],
    cpuLimit: 2.0,
    memoryLimit: '2g',
    restartPolicy: 'unless-stopped',
    networkMode: ''
  })
  deploying.value = false
  deployStatus.value = ''
  deployLog.value = ''
  deployLogId.value = null
  
  visible.value = false
}

// 格式化字节
function formatBytes(bytes) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}
</script>

<style scoped lang="scss">
.mb20 {
  margin-bottom: 20px;
}

.ml10 {
  margin-left: 10px;
}

.text-muted {
  color: #909399;
  font-size: 13px;
}

.text-danger {
  color: #f56c6c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.port-item,
.env-item,
.volume-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.port-separator,
.env-separator,
.volume-separator {
  margin: 0 10px;
  font-weight: bold;
}

.deploy-log-container {
  .deploy-log-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
    font-weight: bold;
  }
  
  .deploy-log-content {
    background-color: #1e1e1e;
    color: #d4d4d4;
    padding: 15px;
    border-radius: 4px;
    height: 400px;
    overflow-y: auto;
    font-family: 'Courier New', Courier, monospace;
    font-size: 13px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
}
</style>
