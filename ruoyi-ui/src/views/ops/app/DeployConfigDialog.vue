<template>
  <el-dialog
    v-model="visible"
    :title="`部署配置 - ${appInfo.appName}`"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <!-- 应用类型选择 -->
      <el-form-item label="应用类型" prop="appType">
        <el-select v-model="form.appType" placeholder="选择应用类型" @change="handleAppTypeChange">
          <el-option label="Spring Boot JAR" value="springboot-jar" />
          <el-option label="Spring Boot WAR" value="springboot-war" />
          <el-option label="Node.js" value="nodejs" />
          <el-option label="Python" value="python" />
          <el-option label="Docker" value="docker" />
          <el-option label="静态网站" value="static" />
          <el-option label="自定义脚本" value="custom" />
        </el-select>
      </el-form-item>

      <!-- Spring Boot JAR 配置 -->
      <template v-if="form.appType === 'springboot-jar'">
        <el-divider content-position="left">Spring Boot 配置</el-divider>
        
        <el-form-item label="JAR 文件名" prop="jarFile">
          <el-input v-model="form.jarFile" placeholder="app.jar" />
        </el-form-item>

        <el-form-item label="JVM 参数">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="form.jvmXms" placeholder="512m">
                <template #prepend>-Xms</template>
              </el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="form.jvmXmx" placeholder="1024m">
                <template #prepend>-Xmx</template>
              </el-input>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.jvmGc" placeholder="GC算法">
                <el-option label="G1GC" value="UseG1GC" />
                <el-option label="ParallelGC" value="UseParallelGC" />
                <el-option label="CMS" value="UseConcMarkSweepGC" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="Spring Profile">
          <el-input v-model="form.springProfile" placeholder="prod" />
        </el-form-item>

        <el-form-item label="应用端口">
          <el-input-number v-model="form.serverPort" :min="1" :max="65535" />
        </el-form-item>

        <el-form-item label="启动超时(秒)">
          <el-input-number v-model="form.startupTimeout" :min="5" :max="300" />
        </el-form-item>
      </template>

      <!-- Node.js 配置 -->
      <template v-if="form.appType === 'nodejs'">
        <el-divider content-position="left">Node.js 配置</el-divider>
        
        <el-form-item label="入口文件" prop="entryFile">
          <el-input v-model="form.entryFile" placeholder="app.js 或 index.js" />
        </el-form-item>

        <el-form-item label="进程管理器">
          <el-radio-group v-model="form.processManager">
            <el-radio value="pm2">PM2</el-radio>
            <el-radio value="node">Node 直接运行</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="实例数量" v-if="form.processManager === 'pm2'">
          <el-input-number v-model="form.instances" :min="1" :max="16" />
        </el-form-item>

        <el-form-item label="最大内存(MB)" v-if="form.processManager === 'pm2'">
          <el-input-number v-model="form.maxMemory" :min="128" :max="4096" :step="128" />
        </el-form-item>

        <el-form-item label="环境变量">
          <el-input v-model="form.nodeEnv" placeholder="production">
            <template #prepend>NODE_ENV</template>
          </el-input>
        </el-form-item>
      </template>

      <!-- Python 配置 -->
      <template v-if="form.appType === 'python'">
        <el-divider content-position="left">Python 配置</el-divider>
        
        <el-form-item label="WSGI 应用" prop="wsgiApp">
          <el-input v-model="form.wsgiApp" placeholder="app:app 或 wsgi:application" />
        </el-form-item>

        <el-form-item label="Web 服务器">
          <el-radio-group v-model="form.webServer">
            <el-radio value="gunicorn">Gunicorn</el-radio>
            <el-radio value="uwsgi">uWSGI</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="Worker 数量">
          <el-input-number v-model="form.workers" :min="1" :max="16" />
        </el-form-item>

        <el-form-item label="绑定地址">
          <el-input v-model="form.bindAddress" placeholder="0.0.0.0:8000" />
        </el-form-item>

        <el-form-item label="虚拟环境路径">
          <el-input v-model="form.venvPath" placeholder="/opt/apps/myapp/venv" />
        </el-form-item>
      </template>

      <!-- Docker 配置 -->
      <template v-if="form.appType === 'docker'">
        <el-divider content-position="left">Docker 配置</el-divider>
        
        <el-form-item label="镜像名称" prop="imageName">
          <el-input v-model="form.imageName" placeholder="myapp:latest" />
        </el-form-item>

        <el-form-item label="容器名称" prop="containerName">
          <el-input v-model="form.containerName" placeholder="myapp-container" />
        </el-form-item>

        <el-form-item label="端口映射">
          <el-input v-model="form.portMapping" placeholder="8080:8080">
            <template #prepend>主机:容器</template>
          </el-input>
        </el-form-item>

        <el-form-item label="环境变量">
          <el-input
            v-model="form.dockerEnv"
            type="textarea"
            :rows="3"
            placeholder="KEY1=value1&#10;KEY2=value2"
          />
        </el-form-item>

        <el-form-item label="重启策略">
          <el-select v-model="form.restartPolicy">
            <el-option label="总是重启" value="always" />
            <el-option label="失败时重启" value="on-failure" />
            <el-option label="除非停止" value="unless-stopped" />
            <el-option label="不重启" value="no" />
          </el-select>
        </el-form-item>
      </template>

      <!-- 通用配置 -->
      <el-divider content-position="left">通用配置</el-divider>
      
      <el-form-item label="部署路径" prop="deployPath">
        <el-input v-model="form.deployPath" placeholder="/opt/apps/myapp" />
      </el-form-item>

      <el-form-item label="日志目录">
        <el-input v-model="form.logDir" placeholder="logs" />
      </el-form-item>

      <el-form-item label="备份旧版本">
        <el-switch v-model="form.backupOld" />
      </el-form-item>

      <el-form-item label="健康检查URL">
        <el-input v-model="form.healthCheckUrl" placeholder="http://localhost:8080/health" />
      </el-form-item>

      <!-- 预览生成的脚本 -->
      <el-divider content-position="left">生成的脚本预览</el-divider>
      
      <el-tabs v-model="activeScriptTab">
        <el-tab-pane label="启动脚本" name="start">
          <el-input
            v-model="generatedStartScript"
            type="textarea"
            :rows="15"
            readonly
            style="font-family: monospace;"
          />
        </el-tab-pane>
        <el-tab-pane label="停止脚本" name="stop">
          <el-input
            v-model="generatedStopScript"
            type="textarea"
            :rows="15"
            readonly
            style="font-family: monospace;"
          />
        </el-tab-pane>
      </el-tabs>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave">保存配置</el-button>
      <el-button type="success" @click="handleSaveAndDeploy">保存并部署</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  appInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'save', 'deploy'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const activeScriptTab = ref('start')

const form = ref({
  appType: 'springboot-jar',
  // Spring Boot
  jarFile: 'app.jar',
  jvmXms: '512m',
  jvmXmx: '1024m',
  jvmGc: 'UseG1GC',
  springProfile: 'prod',
  serverPort: 8080,
  startupTimeout: 60,
  // Node.js
  entryFile: 'app.js',
  processManager: 'pm2',
  instances: 2,
  maxMemory: 1024,
  nodeEnv: 'production',
  // Python
  wsgiApp: 'app:app',
  webServer: 'gunicorn',
  workers: 4,
  bindAddress: '0.0.0.0:8000',
  venvPath: '',
  // Docker
  imageName: '',
  containerName: '',
  portMapping: '8080:8080',
  dockerEnv: '',
  restartPolicy: 'unless-stopped',
  // 通用
  deployPath: '/opt/apps/myapp',
  logDir: 'logs',
  backupOld: true,
  healthCheckUrl: ''
})

const rules = {
  appType: [{ required: true, message: '请选择应用类型', trigger: 'change' }],
  jarFile: [{ required: true, message: '请输入JAR文件名', trigger: 'blur' }],
  entryFile: [{ required: true, message: '请输入入口文件', trigger: 'blur' }],
  wsgiApp: [{ required: true, message: '请输入WSGI应用', trigger: 'blur' }],
  imageName: [{ required: true, message: '请输入镜像名称', trigger: 'blur' }],
  containerName: [{ required: true, message: '请输入容器名称', trigger: 'blur' }],
  deployPath: [{ required: true, message: '请输入部署路径', trigger: 'blur' }]
}

// 生成启动脚本
const generatedStartScript = computed(() => {
  const appName = props.appInfo.appName || 'myapp'
  const config = form.value
  
  switch (config.appType) {
    case 'springboot-jar':
      return generateSpringBootStartScript(appName, config)
    case 'nodejs':
      return generateNodeJsStartScript(appName, config)
    case 'python':
      return generatePythonStartScript(appName, config)
    case 'docker':
      return generateDockerStartScript(appName, config)
    default:
      return '# 请选择应用类型'
  }
})

// 生成停止脚本
const generatedStopScript = computed(() => {
  const appName = props.appInfo.appName || 'myapp'
  const config = form.value
  
  switch (config.appType) {
    case 'springboot-jar':
      return generateSpringBootStopScript(appName, config)
    case 'nodejs':
      return generateNodeJsStopScript(appName, config)
    case 'python':
      return generatePythonStopScript(appName, config)
    case 'docker':
      return generateDockerStopScript(appName, config)
    default:
      return '# 请选择应用类型'
  }
})

function generateSpringBootStartScript(appName, config) {
  const _ = appName; // 使用应用名称作为注释
  return `#!/bin/bash
APP_NAME="${config.jarFile.replace('.jar', '')}"
APP_JAR="${config.jarFile}"
APP_HOME="${config.deployPath}"
LOG_DIR="\${APP_HOME}/${config.logDir}"
PID_FILE="\${APP_HOME}/\${APP_NAME}.pid"

JVM_OPTS="-Xms${config.jvmXms} -Xmx${config.jvmXmx}"
JVM_OPTS="\${JVM_OPTS} -XX:+${config.jvmGc}"
JVM_OPTS="\${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JVM_OPTS="\${JVM_OPTS} -XX:HeapDumpPath=\${LOG_DIR}/heap_dump.hprof"

SPRING_OPTS="-Dspring.profiles.active=${config.springProfile}"
SPRING_OPTS="\${SPRING_OPTS} -Dserver.port=${config.serverPort}"

echo "[INFO] 启动 \${APP_NAME}"
mkdir -p \${LOG_DIR}

${config.backupOld ? `# 备份旧版本
if [ -f "\${APP_HOME}/\${APP_JAR}" ]; then
    mv \${APP_HOME}/\${APP_JAR} \${APP_HOME}/\${APP_JAR}.bak.\$(date '+%Y%m%d_%H%M%S')
fi` : ''}

cd \${APP_HOME}
nohup java \${JVM_OPTS} \${SPRING_OPTS} -jar \${APP_JAR} > \${LOG_DIR}/app.log 2>&1 &
echo $! > \${PID_FILE}

echo "[SUCCESS] 应用启动成功，PID: \$(cat \${PID_FILE})"
${config.healthCheckUrl ? `
# 健康检查
sleep 5
curl -f ${config.healthCheckUrl} && echo "[SUCCESS] 健康检查通过" || echo "[WARN] 健康检查失败"
` : ''}`
}

function generateSpringBootStopScript(appName, config) {
  const _ = appName; // 使用应用名称作为注释
  return `#!/bin/bash
APP_NAME="${config.jarFile.replace('.jar', '')}"
APP_HOME="${config.deployPath}"
PID_FILE="\${APP_HOME}/\${APP_NAME}.pid"

if [ -f \${PID_FILE} ]; then
    PID=\$(cat \${PID_FILE})
    echo "[INFO] 停止应用，PID: \${PID}"
    kill \${PID}
    
    # 等待进程结束
    for i in {1..30}; do
        if ! ps -p \${PID} > /dev/null 2>&1; then
            echo "[SUCCESS] 应用已停止"
            rm -f \${PID_FILE}
            exit 0
        fi
        sleep 1
    done
    
    # 强制停止
    kill -9 \${PID}
    rm -f \${PID_FILE}
    echo "[SUCCESS] 应用已强制停止"
else
    echo "[WARN] PID 文件不存在"
fi`
}

function generateNodeJsStartScript(appName, config) {
  if (config.processManager === 'pm2') {
    return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${config.deployPath}"
ENTRY_FILE="${config.entryFile}"

cd \${APP_HOME}

pm2 start \${ENTRY_FILE} \\
    --name \${APP_NAME} \\
    --instances ${config.instances} \\
    --max-memory-restart ${config.maxMemory}M \\
    --env NODE_ENV=${config.nodeEnv}

pm2 save
echo "[SUCCESS] 应用启动成功"`
  } else {
    return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${config.deployPath}"
ENTRY_FILE="${config.entryFile}"
LOG_DIR="\${APP_HOME}/${config.logDir}"

mkdir -p \${LOG_DIR}
cd \${APP_HOME}

NODE_ENV=${config.nodeEnv} nohup node \${ENTRY_FILE} > \${LOG_DIR}/app.log 2>&1 &
echo $! > \${APP_NAME}.pid

echo "[SUCCESS] 应用启动成功，PID: \$(cat \${APP_NAME}.pid)"`
  }
}

function generateNodeJsStopScript(appName, config) {
  if (config.processManager === 'pm2') {
    return `#!/bin/bash
APP_NAME="${appName}"

pm2 stop \${APP_NAME}
pm2 delete \${APP_NAME}
echo "[SUCCESS] 应用已停止"`
  } else {
    return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${config.deployPath}"

if [ -f \${APP_HOME}/\${APP_NAME}.pid ]; then
    kill \$(cat \${APP_HOME}/\${APP_NAME}.pid)
    rm -f \${APP_HOME}/\${APP_NAME}.pid
    echo "[SUCCESS] 应用已停止"
fi`
  }
}

function generatePythonStartScript(appName, config) {
  return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${config.deployPath}"
VENV="${config.venvPath || '\${APP_HOME}/venv'}"
LOG_DIR="\${APP_HOME}/${config.logDir}"

mkdir -p \${LOG_DIR}
cd \${APP_HOME}

source \${VENV}/bin/activate

${config.webServer} \\
    --bind ${config.bindAddress} \\
    --workers ${config.workers} \\
    --daemon \\
    --pid \${APP_HOME}/\${APP_NAME}.pid \\
    --access-logfile \${LOG_DIR}/access.log \\
    --error-logfile \${LOG_DIR}/error.log \\
    ${config.wsgiApp}

echo "[SUCCESS] 应用启动成功"`
}

function generatePythonStopScript(appName, config) {
  return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${config.deployPath}"

if [ -f \${APP_HOME}/\${APP_NAME}.pid ]; then
    kill \$(cat \${APP_HOME}/\${APP_NAME}.pid)
    rm -f \${APP_HOME}/\${APP_NAME}.pid
    echo "[SUCCESS] 应用已停止"
fi`
}

function generateDockerStartScript(appName, config) {
  const envVars = config.dockerEnv.split('\n').filter(line => line.trim())
    .map(line => `-e ${line.trim()}`).join(' \\\n    ')
  
  return `#!/bin/bash
CONTAINER_NAME="${config.containerName}"
IMAGE_NAME="${config.imageName}"

# 停止并删除旧容器
docker stop \${CONTAINER_NAME} 2>/dev/null || true
docker rm \${CONTAINER_NAME} 2>/dev/null || true

# 拉取最新镜像
docker pull \${IMAGE_NAME}

# 启动容器
docker run -d \\
    --name \${CONTAINER_NAME} \\
    --restart ${config.restartPolicy} \\
    -p ${config.portMapping} \\
    ${envVars ? envVars + ' \\' : ''}
    \${IMAGE_NAME}

echo "[SUCCESS] 容器启动成功"
docker ps | grep \${CONTAINER_NAME}`
}

function generateDockerStopScript(appName, config) {
  return `#!/bin/bash
CONTAINER_NAME="${config.containerName}"

docker stop \${CONTAINER_NAME}
docker rm \${CONTAINER_NAME}
echo "[SUCCESS] 容器已停止并删除"`
}

function handleAppTypeChange() {
  // 应用类型改变时，可以重置相关配置
}

function handleClose() {
  visible.value = false
}

function handleSave() {
  formRef.value.validate((valid) => {
    if (valid) {
      emit('save', {
        config: form.value,
        startScript: generatedStartScript.value,
        stopScript: generatedStopScript.value
      })
      ElMessage.success('配置已保存')
      handleClose()
    }
  })
}

function handleSaveAndDeploy() {
  formRef.value.validate((valid) => {
    if (valid) {
      emit('deploy', {
        config: form.value,
        startScript: generatedStartScript.value,
        stopScript: generatedStopScript.value
      })
      handleClose()
    }
  })
}

// 监听 appInfo 变化，加载已有配置
watch(() => props.appInfo, (newVal) => {
  if (newVal.deployConfig) {
    try {
      const config = JSON.parse(newVal.deployConfig)
      Object.assign(form.value, config)
    } catch (e) {
      console.error('解析配置失败:', e)
    }
  }
}, { immediate: true })
</script>

<style scoped>
.el-divider {
  margin: 20px 0;
}
</style>
