<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="应用名称" prop="appName">
        <el-input
          v-model="queryParams.appName"
          placeholder="请输入应用名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="应用类型" prop="appType">
        <el-select v-model="queryParams.appType" placeholder="请选择应用类型" clearable>
          <el-option
            v-for="dict in sys_ops_app_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['ops:app:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['ops:app:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:app:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['ops:app:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="appList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="应用ID" align="center" prop="appId" />
      <el-table-column label="应用名称" align="center" prop="appName" />
      <el-table-column label="应用类型" align="center" prop="appType">
        <template #default="scope">
          <dict-tag :options="sys_ops_app_type" :value="scope.row.appType"/>
        </template>
      </el-table-column>
      <el-table-column label="部署路径" align="center" prop="deployPath" :show-overflow-tooltip="true" />
      <el-table-column label="启动脚本" align="center" prop="startScript" :show-overflow-tooltip="true" />
      <el-table-column label="监控端口" align="center" prop="monitorPorts" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template #default="scope">
          <el-dropdown @command="(cmd) => handleDeploy(scope.row, cmd)" v-hasPermi="['ops:app:deploy']">
            <el-button link type="success" icon="VideoPlay">
              部署 <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="deploy" icon="Upload">部署应用</el-dropdown-item>
                <el-dropdown-item command="start" icon="VideoPlay">启动应用</el-dropdown-item>
                <el-dropdown-item command="stop" icon="VideoPause">停止应用</el-dropdown-item>
                <el-dropdown-item command="restart" icon="RefreshRight">重启应用</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['ops:app:edit']"
          >修改</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:app:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 部署服务器选择对话框 -->
    <el-dialog 
      v-model="deployDialogVisible" 
      title="选择部署服务器"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="应用名称">
          <el-input v-model="currentApp.appName" disabled />
        </el-form-item>
        <el-form-item label="部署类型">
          <el-tag :type="getDeployTypeTag(deployType)">{{ getDeployTypeName(deployType) }}</el-tag>
        </el-form-item>
        <el-form-item label="选择服务器" required>
          <el-select 
            v-model="selectedServerId" 
            placeholder="请选择服务器"
            style="width: 100%"
          >
            <el-option
              v-for="server in currentServerOptions"
              :key="server.serverId"
              :label="`${server.serverName} (${server.serverIp})`"
              :value="server.serverId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deployDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDeploy" :loading="deploying">
          {{ deploying ? '部署中...' : '开始部署' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 实时日志查看对话框 -->
    <el-dialog 
      v-model="logDialogVisible" 
      :title="`部署日志 - ${currentApp.appName}`"
      width="85%"
      top="5vh"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <DeployLogViewer v-if="logDialogVisible" :logId="currentLogId" />
    </el-dialog>

    <!-- 添加或修改应用注册对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="基本信息" name="basic">
            <el-row>
              <el-col :span="12">
                <el-form-item label="应用名称" prop="appName">
                  <el-input v-model="form.appName" placeholder="请输入应用名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="应用类型" prop="appType">
                  <el-select v-model="form.appType" placeholder="请选择应用类型">
                    <el-option
                      v-for="dict in sys_ops_app_type"
                      :key="dict.value"
                      :label="dict.label"
                      :value="dict.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="负责人" prop="owner">
                  <el-select v-model="form.owner" placeholder="请选择负责人" clearable filterable>
                    <el-option
                      v-for="user in userOptions"
                      :key="user.userName"
                      :label="user.nickName"
                      :value="user.userName"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属部门" prop="deptId">
                  <el-tree-select
                    v-model="form.deptId"
                    :data="deptOptions"
                    :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
                    value-key="deptId"
                    placeholder="请选择所属部门"
                    check-strictly
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="应用描述" prop="description">
                  <el-input v-model="form.description" type="textarea" placeholder="请输入应用描述" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio value="0">正常</el-radio>
                    <el-radio value="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注" prop="remark">
                  <el-input v-model="form.remark" type="textarea" placeholder="请输入备注内容" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="部署配置" name="config">
            <el-row>
              <el-col :span="24">
                <el-form-item label="应用包" prop="packagePath">
                  <el-upload
                    class="upload-demo"
                    :action="uploadUrl"
                    :headers="headers"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    :limit="1"
                    :show-file-list="true"
                    :file-list="fileList"
                  >
                    <el-button size="small" type="primary" icon="Upload">点击上传应用包</el-button>
                    <template #tip>
                      <div class="el-upload__tip">支持 zip/tar/jar 等格式，大小不超过 500MB</div>
                    </template>
                  </el-upload>
                  <el-input v-model="form.packagePath" placeholder="上传后自动回填，也可手动输入路径" style="margin-top: 5px;"/>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="关联服务器" prop="serverIds">
                  <el-select
                    v-model="form.serverIds"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    placeholder="请选择关联服务器"
                    clearable
                    filterable
                    style="width: 100%"
                  >
                    <el-option
                      v-for="server in serverOptions"
                      :key="server.serverId"
                      :label="server.serverName + ' (' + server.serverIp + ')'"
                      :value="server.serverId"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="技术栈" prop="techStack">
                  <el-transfer
                    v-model="techStackValue"
                    :data="techStackData"
                    :titles="['可选技术', '已选技术']"
                    filterable
                    @change="handleTechStackChange"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="监控端口" prop="monitorPorts">
                  <el-input v-model="form.monitorPorts" placeholder="例如：8080,8081" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="部署路径" prop="deployPath">
                  <el-input v-model="form.deployPath" placeholder="请输入绝对路径" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="配置文件路径" prop="configFilePath">
                  <el-input v-model="form.configFilePath" placeholder="例如：/home/config/ 或 /home/config/application.yml（可选）">
                    <template #prepend>
                      <el-icon><Document /></el-icon>
                    </template>
                  </el-input>
                  <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                    可指定配置目录或配置文件，系统会自动添加 --spring.config.additional-location 参数。支持目录（推荐）或具体文件路径
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="Spring Boot 参数" prop="springParams">
                  <el-input 
                    v-model="form.springParams" 
                    type="textarea" 
                    :rows="2"
                    placeholder="例如：--spring.profiles.active=prod --server.port=8080"
                  />
                  <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                    Spring Boot 应用参数，多个参数用空格分隔。留空则不添加额外参数
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="健康检查" prop="healthCheckUrl">
                   <el-input v-model="form.healthCheckUrl" placeholder="例如：http://localhost:8080/health" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="超时时间" prop="deployTimeout">
                   <el-input-number v-model="form.deployTimeout" :min="1" :max="3600" /> 秒
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="重试次数" prop="retryCount">
                   <el-input-number v-model="form.retryCount" :min="0" :max="10" /> 次
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="脚本管理" name="script">
            <el-alert
              title="提示"
              type="info"
              :closable="false"
              style="margin-bottom: 15px;"
            >
              可以使用可视化配置快速生成脚本,或手动编辑脚本内容
            </el-alert>
            
            <!-- 可视化配置区域 -->
            <el-card shadow="never" style="margin-bottom: 20px;">
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span>可视化配置</span>
                  <el-button 
                    size="small" 
                    type="primary" 
                    @click="showVisualConfig = !showVisualConfig"
                    :icon="showVisualConfig ? 'ArrowUp' : 'ArrowDown'"
                  >
                    {{ showVisualConfig ? '收起' : '展开' }}
                  </el-button>
                </div>
              </template>
              
              <div v-show="showVisualConfig">
                <el-form label-width="120px" size="small">
                  <el-form-item label="应用类型">
                    <el-select v-model="visualConfig.appType" placeholder="选择应用类型" @change="handleVisualConfigChange">
                      <el-option label="Spring Boot JAR" value="springboot-jar" />
                      <el-option label="Spring Boot WAR" value="springboot-war" />
                      <el-option label="Node.js" value="nodejs" />
                      <el-option label="Python" value="python" />
                      <el-option label="Docker" value="docker" />
                      <el-option label="静态网站" value="static" />
                      <el-option label="手动编辑" value="custom" />
                    </el-select>
                  </el-form-item>

                  <!-- Spring Boot JAR 配置 -->
                  <template v-if="visualConfig.appType === 'springboot-jar'">
                    <el-form-item label="JAR 文件名">
                      <el-input v-model="visualConfig.jarFile" placeholder="从上传的文件自动获取" disabled>
                        <template #append>
                          <el-button @click="autoFillJarName">自动填充</el-button>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item label="JVM 参数">
                      <el-row :gutter="10">
                        <el-col :span="8">
                          <el-input v-model="visualConfig.jvmXms" placeholder="512m">
                            <template #prepend>-Xms</template>
                          </el-input>
                        </el-col>
                        <el-col :span="8">
                          <el-input v-model="visualConfig.jvmXmx" placeholder="1024m">
                            <template #prepend>-Xmx</template>
                          </el-input>
                        </el-col>
                        <el-col :span="8">
                          <el-select v-model="visualConfig.jvmGc" placeholder="GC算法">
                            <el-option label="G1GC" value="UseG1GC" />
                            <el-option label="ParallelGC" value="UseParallelGC" />
                            <el-option label="CMS" value="UseConcMarkSweepGC" />
                          </el-select>
                        </el-col>
                      </el-row>
                    </el-form-item>
                    <el-form-item label="Spring Profile">
                      <el-input v-model="visualConfig.springProfile" placeholder="prod" />
                    </el-form-item>
                    <el-form-item label="应用端口">
                      <el-input-number v-model="visualConfig.serverPort" :min="1" :max="65535" />
                    </el-form-item>
                  </template>

                  <!-- Node.js 配置 -->
                  <template v-if="visualConfig.appType === 'nodejs'">
                    <el-form-item label="入口文件">
                      <el-input v-model="visualConfig.entryFile" placeholder="app.js 或 index.js" />
                    </el-form-item>
                    <el-form-item label="进程管理器">
                      <el-radio-group v-model="visualConfig.processManager">
                        <el-radio value="pm2">PM2</el-radio>
                        <el-radio value="node">Node 直接运行</el-radio>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item label="实例数量" v-if="visualConfig.processManager === 'pm2'">
                      <el-input-number v-model="visualConfig.instances" :min="1" :max="16" />
                    </el-form-item>
                  </template>

                  <!-- Python 配置 -->
                  <template v-if="visualConfig.appType === 'python'">
                    <el-form-item label="WSGI 应用">
                      <el-input v-model="visualConfig.wsgiApp" placeholder="app:app" />
                    </el-form-item>
                    <el-form-item label="Web 服务器">
                      <el-radio-group v-model="visualConfig.webServer">
                        <el-radio value="gunicorn">Gunicorn</el-radio>
                        <el-radio value="uwsgi">uWSGI</el-radio>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item label="Worker 数量">
                      <el-input-number v-model="visualConfig.workers" :min="1" :max="16" />
                    </el-form-item>
                  </template>

                  <!-- Docker 配置 -->
                  <template v-if="visualConfig.appType === 'docker'">
                    <el-form-item label="镜像名称">
                      <el-input v-model="visualConfig.imageName" placeholder="myapp:latest" />
                    </el-form-item>
                    <el-form-item label="容器名称">
                      <el-input v-model="visualConfig.containerName" placeholder="myapp-container" />
                    </el-form-item>
                    <el-form-item label="端口映射">
                      <el-input v-model="visualConfig.portMapping" placeholder="8080:8080" />
                    </el-form-item>
                  </template>

                  <el-form-item>
                    <el-button type="primary" @click="generateScriptsFromConfig">生成脚本</el-button>
                    <el-button @click="resetVisualConfig">重置配置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-card>

            <!-- 脚本编辑区域 -->
            <el-form-item label="启动脚本" prop="startScript">
              <div style="margin-bottom: 5px;">
                <el-upload
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  :on-change="(file) => handleScriptUpload(file, 'start')">
                  <el-button size="small" type="primary" icon="Upload">导入脚本文件</el-button>
                  <span style="margin-left: 10px; color: #909399; font-size: 12px;">支持上传 .sh 脚本，上传后可在线编辑</span>
                </el-upload>
              </div>
              <div style="border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; width: 100%;">
                <VueMonacoEditor
                  v-if="open"
                  v-model:value="form.startScript"
                  theme="vs-dark"
                  language="shell"
                  height="400px"
                  style="width: 100%;"
                  :options="{
                    automaticLayout: true,
                    minimap: { enabled: false },
                    fontSize: 12,
                    scrollBeyondLastLine: false
                  }"
                />
              </div>
            </el-form-item>
            <el-form-item label="停止脚本" prop="stopScript">
              <div style="margin-bottom: 5px;">
                <el-upload
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  :on-change="(file) => handleScriptUpload(file, 'stop')">
                  <el-button size="small" type="primary" icon="Upload">导入脚本文件</el-button>
                  <span style="margin-left: 10px; color: #909399; font-size: 12px;">支持上传 .sh 脚本，上传后可在线编辑</span>
                </el-upload>
              </div>
              <div style="border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; width: 100%;">
                <VueMonacoEditor
                  v-if="open"
                  v-model:value="form.stopScript"
                  theme="vs-dark"
                  language="shell"
                  height="400px"
                  style="width: 100%;"
                  :options="{
                    automaticLayout: true,
                    minimap: { enabled: false },
                    fontSize: 12,
                    scrollBeyondLastLine: false
                  }"
                />
              </div>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="App">
import request from '@/utils/request'
import { listApp, getApp, delApp, addApp, updateApp, uploadAppPackage } from "@/api/ops/app";
import { startDeploy } from "@/api/ops/deployLog";
import { listDept } from "@/api/system/dept";
import { listUser } from "@/api/system/user";
import { handleTree } from "@/utils/ruoyi";
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'
import { getCurrentInstance, reactive, ref, toRefs, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import DeployLogViewer from '@/components/DeployLogViewer/index.vue';

import { getToken } from "@/utils/auth";

// 获取服务器列表
const listServer = (query) => {
  return request({
    url: '/system/server/list',
    method: 'get',
    params: query
  })
}

const { proxy } = getCurrentInstance();
const { sys_ops_app_type, sys_tech_stack } = proxy.useDict('sys_ops_app_type', 'sys_tech_stack');

const appList = ref([]);
const open = ref(false);
const activeTab = ref("basic");
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const deptOptions = ref([]);
const userOptions = ref([]);
const techStackValue = ref([]);
const techStackData = ref([]);
const serverOptions = ref([]);

// 部署相关
const deployDialogVisible = ref(false);
const logDialogVisible = ref(false);
const currentApp = ref({});
const deployType = ref('deploy');
const selectedServerId = ref(null);
const currentServerOptions = ref([]);
const deploying = ref(false);
const currentLogId = ref(null);

// 可视化配置
const showVisualConfig = ref(false);
const visualConfig = ref({
  appType: 'custom',
  jarFile: '',
  jvmXms: '512m',
  jvmXmx: '1024m',
  jvmGc: 'UseG1GC',
  springProfile: 'prod',
  serverPort: 8080,
  entryFile: 'app.js',
  processManager: 'pm2',
  instances: 2,
  wsgiApp: 'app:app',
  webServer: 'gunicorn',
  workers: 4,
  imageName: '',
  containerName: '',
  portMapping: '8080:8080'
});

// 上传相关
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + "/system/app/upload");
const headers = ref({ Authorization: "Bearer " + getToken() });
const fileList = ref([]);

// 上传前校检
const beforeUpload = (file) => {
  const isLt500M = file.size / 1024 / 1024 < 500;
  if (!isLt500M) {
    proxy.$modal.msgError('上传文件大小不能超过 500MB!');
  }
  return isLt500M;
};

// 上传成功处理
const handleUploadSuccess = (res, file) => {
  if (res.code === 200) {
    form.value.packagePath = res.msg; // Assuming msg contains filePath from backend
    proxy.$modal.msgSuccess("上传成功");
  } else {
    proxy.$modal.msgError(res.msg);
  }
};

// 上传失败处理
const handleUploadError = () => {
  proxy.$modal.msgError("上传失败");
};

// 脚本文件上传处理
const handleScriptUpload = (file, type) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    if (type === 'start') {
      form.value.startScript = e.target.result;
    } else {
      form.value.stopScript = e.target.result;
    }
  };
  reader.readAsText(file.raw);
};

// 监听字典数据变化，初始化穿梭框数据源
watch(sys_tech_stack, (val) => {
  if (val && val.length > 0) {
    techStackData.value = val.map(item => ({
      key: item.value,
      label: item.label,
      disabled: false
    }));
  }
}, { immediate: true });

// 穿梭框变化事件
const handleTechStackChange = (value) => {
  form.value.techStack = value.join(',');
};

// 路径验证
const validatePath = (rule, value, callback) => {
  // 简单判断绝对路径：Linux以/开头，Windows以盘符开头
  const isLinuxPath = /^\/.*$/;
  const isWinPath = /^[a-zA-Z]:\\.*$/;
  if (!isLinuxPath.test(value) && !isWinPath.test(value)) {
    callback(new Error('请输入正确的绝对路径'));
  } else {
    callback();
  }
};
// 端口验证
const validatePort = (rule, value, callback) => {
  const ports = value.split(/,|，/);
  for (let port of ports) {
    const p = parseInt(port);
    if (isNaN(p) || p < 1 || p > 65535) {
      callback(new Error('端口必须在1-65535之间'));
      return;
    }
  }
  callback();
};

const data = reactive({
  form: {
    appId: undefined,
    appName: undefined,
    appType: undefined,
    deployPath: undefined,
    configFilePath: undefined,
    springParams: undefined,
    startScript: undefined,
    stopScript: undefined,
    monitorPorts: undefined,
    healthCheckUrl: undefined,
    deployTimeout: 60,
    retryCount: 0,
    owner: undefined,
    techStack: undefined,
    deptId: undefined,
    description: undefined,
    serverIds: [],
    packagePath: undefined,
    status: "0",
    remark: undefined
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    appName: undefined,
    appType: undefined,
    status: undefined,
  },
  rules: {
    appName: [
      { required: true, message: "应用名称不能为空", trigger: "blur" },
      { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
      { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/, message: "只能包含中英文、数字和下划线", trigger: "blur" }
    ],
    appType: [
      { required: true, message: "应用类型不能为空", trigger: "change" }
    ],
    deployPath: [
      { required: true, message: "部署路径不能为空", trigger: "blur" },
      { validator: validatePath, trigger: "blur" }
    ],
    startScript: [
      { required: true, message: "启动脚本不能为空", trigger: "blur" }
    ],
    monitorPorts: [
      { required: true, message: "监控端口不能为空", trigger: "blur" },
      { validator: validatePort, trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询应用注册列表 */
function getList() {
  loading.value = true;
  listApp(queryParams.value).then(response => {
    appList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  activeTab.value = "basic";
  form.value = {
    appId: undefined,
    appName: undefined,
    appType: undefined,
    deployPath: undefined,
    configFilePath: undefined,
    springParams: undefined,
    startScript: undefined,
    stopScript: undefined,
    monitorPorts: undefined,
    healthCheckUrl: undefined,
    deployTimeout: 60,
    retryCount: 0,
    owner: undefined,
    techStack: undefined,
    deptId: undefined,
    description: undefined,
    serverIds: [],
    status: "0",
    remark: undefined
  };
  techStackValue.value = [];
  proxy.resetForm("formRef");
}

/** 查询用户列表 */
function getUserList() {
  listUser().then(response => {
    userOptions.value = response.rows;
  });
}

/** 查询服务器列表 */
function getServerList() {
  return listServer().then(response => {
    // 假设返回的数据结构是 response.rows，包含 serverId, serverName, serverIp 等字段
    if (response.rows) {
      serverOptions.value = response.rows.map(item => ({
        serverId: item.serverId,
        serverName: item.serverName || '未知服务器',
        serverIp: item.publicIp || item.serverIp || '未知IP'
      }));
    } else if (response.data) {
       // 兼容另一种返回格式
       const data = Array.isArray(response.data) ? response.data : [response.data];
       serverOptions.value = data.map(item => ({
        serverId: item.serverId,
        serverName: item.serverName || '未知服务器',
        serverIp: item.publicIp || item.serverIp || '未知IP'
      }));
    }
    return serverOptions.value;
  });
}

/** 查询部门下拉树结构 */
function getDeptTree() {
  listDept().then(response => {
    deptOptions.value = handleTree(response.data, "deptId");
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryForm");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.appId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  getDeptTree();
  getUserList();
  getServerList();
  open.value = true;
  title.value = "添加应用注册";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  getDeptTree();
  getUserList();
  getServerList();
  const appId = row.appId || ids.value;
  getApp(appId).then(response => {
    form.value = response.data;
    if (form.value.techStack) {
      techStackValue.value = form.value.techStack.split(',');
    }
    // 处理服务器ID回显
    if (form.value.serverIds && typeof form.value.serverIds === 'string') {
       form.value.serverIds = form.value.serverIds.split(',').map(Number);
    } else if (!form.value.serverIds) {
       form.value.serverIds = [];
    }
    
    // 加载可视化配置
    if (form.value.deployConfig) {
      try {
        const config = JSON.parse(form.value.deployConfig);
        Object.assign(visualConfig.value, config);
      } catch (e) {
        console.error('解析配置失败:', e);
      }
    }
    
    // 自动从 packagePath 提取 JAR 文件名
    autoFillJarName();
    
    open.value = true;
    title.value = "修改应用注册";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 提交前处理 serverIds 数组转字符串
      const submitForm = { ...form.value };
      if (Array.isArray(submitForm.serverIds)) {
        submitForm.serverIds = submitForm.serverIds.join(',');
      }
      
      // 保存可视化配置
      submitForm.deployConfig = JSON.stringify(visualConfig.value);
      
      if (submitForm.appId != null) {
        updateApp(submitForm).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addApp(submitForm).then(response => {
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
  const appIds = row.appId || ids.value;
  proxy.$modal.confirm('是否确认删除应用注册编号为"' + appIds + '"的数据项？').then(function() {
    return delApp(appIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/app/export', {
    ...queryParams.value
  }, `app_${new Date().getTime()}.xlsx`)
}

/** 部署操作 */
function handleDeploy(row, type) {
  currentApp.value = row;
  deployType.value = type;
  
  // 解析服务器ID
  let serverIds = [];
  if (row.serverIds) {
    if (typeof row.serverIds === 'string') {
      serverIds = row.serverIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
    } else if (Array.isArray(row.serverIds)) {
      serverIds = row.serverIds;
    }
  }
  
  if (serverIds.length === 0) {
    ElMessage.warning('该应用未关联服务器，请先配置服务器');
    return;
  }
  
  // 获取服务器详细信息
  getServerList().then(() => {
    currentServerOptions.value = serverOptions.value.filter(server => 
      serverIds.includes(server.serverId)
    );
    
    if (currentServerOptions.value.length === 0) {
      ElMessage.warning('未找到关联的服务器信息');
      return;
    }
    
    // 如果只有一个服务器，直接选中
    if (currentServerOptions.value.length === 1) {
      selectedServerId.value = currentServerOptions.value[0].serverId;
    } else {
      selectedServerId.value = null;
    }
    
    deployDialogVisible.value = true;
  });
}

/** 确认部署 */
function confirmDeploy() {
  if (!selectedServerId.value) {
    ElMessage.warning('请选择部署服务器');
    return;
  }
  
  deploying.value = true;
  
  startDeploy({
    appId: currentApp.value.appId,
    serverId: selectedServerId.value,
    deployType: deployType.value
  }).then(response => {
    deploying.value = false;
    deployDialogVisible.value = false;
    
    if (response.code === 200) {
      currentLogId.value = response.data;
      logDialogVisible.value = true;
      ElMessage.success('部署任务已启动，正在查看实时日志');
    } else {
      ElMessage.error(response.msg || '启动部署失败');
    }
  }).catch(error => {
    deploying.value = false;
    ElMessage.error('启动部署失败: ' + error.message);
  });
}

/** 获取部署类型名称 */
function getDeployTypeName(type) {
  const map = {
    'deploy': '部署应用',
    'start': '启动应用',
    'stop': '停止应用',
    'restart': '重启应用'
  };
  return map[type] || type;
}

/** 获取部署类型标签 */
function getDeployTypeTag(type) {
  const map = {
    'deploy': 'primary',
    'start': 'success',
    'stop': 'warning',
    'restart': 'info'
  };
  return map[type] || '';
}

/** 自动填充 JAR 文件名 */
function autoFillJarName() {
  if (form.value.packagePath) {
    const fileName = form.value.packagePath.split('/').pop();
    visualConfig.value.jarFile = fileName;
  }
}

/** 可视化配置变化 */
function handleVisualConfigChange() {
  // 配置类型变化时可以做一些初始化
}

/** 重置可视化配置 */
function resetVisualConfig() {
  visualConfig.value = {
    appType: 'custom',
    jarFile: '',
    jvmXms: '512m',
    jvmXmx: '1024m',
    jvmGc: 'UseG1GC',
    springProfile: 'prod',
    serverPort: 8080,
    entryFile: 'app.js',
    processManager: 'pm2',
    instances: 2,
    wsgiApp: 'app:app',
    webServer: 'gunicorn',
    workers: 4,
    imageName: '',
    containerName: '',
    portMapping: '8080:8080'
  };
}

/** 从可视化配置生成脚本 */
function generateScriptsFromConfig() {
  const config = visualConfig.value;
  const appName = form.value.appName || 'myapp';
  const deployPath = form.value.deployPath || '/opt/apps/myapp';
  
  if (config.appType === 'custom') {
    ElMessage.info('请选择应用类型或手动编辑脚本');
    return;
  }
  
  // 生成启动脚本
  form.value.startScript = generateStartScript(appName, deployPath, config);
  // 生成停止脚本
  form.value.stopScript = generateStopScript(appName, deployPath, config);
  
  ElMessage.success('脚本已生成,请查看下方编辑器');
  showVisualConfig.value = false;
}

/** 生成启动脚本 */
function generateStartScript(appName, deployPath, config) {
  switch (config.appType) {
    case 'springboot-jar':
      // 确保 jarFile 有值
      const jarFile = config.jarFile || 'app.jar';
      const jarName = jarFile.replace('.jar', '');
      
      // 使用数组拼接，避免模板字符串中的特殊字符被转义
      const lines = [
        '#!/bin/bash',
        '# Spring Boot 应用启动脚本（带进程验证）',
        '',
        `APP_NAME="${jarName}"`,
        `APP_JAR="${jarFile}"`,
        `APP_HOME="${deployPath}"`,
        'LOG_DIR="${APP_HOME}/logs"',
        'PID_FILE="${APP_HOME}/${APP_NAME}.pid"',
        `PORT=${config.serverPort}`,
        '',
        '# 启用详细输出',
        'set -x',
        'exec 2>&1',
        '',
        'echo "[INFO] ========== 开始启动应用 =========="',
        'echo "[INFO] 应用名称: ${APP_NAME}"',
        'echo "[INFO] JAR 文件: ${APP_JAR}"',
        'echo "[INFO] 应用路径: ${APP_HOME}"',
        'echo "[INFO] 当前时间: $(date +%Y-%m-%d\\ %H:%M:%S)"',
        '',
        '# 创建日志目录',
        'mkdir -p ${LOG_DIR}',
        'echo "[INFO] 日志目录: ${LOG_DIR}"',
        '',
        '# 切换到应用目录',
        'cd ${APP_HOME}',
        'echo "[INFO] 当前目录: $(pwd)"',
        '',
        '# 检查 JAR 文件',
        'if [ ! -f "${APP_JAR}" ]; then',
        '    echo "[ERROR] JAR 文件不存在: ${APP_HOME}/${APP_JAR}"',
        '    echo "[ERROR] 目录内容:"',
        '    ls -lh ${APP_HOME}',
        '    exit 1',
        'fi',
        'echo "[INFO] JAR 文件存在: ${APP_JAR}"',
        '',
        '# 检查并停止占用端口的进程',
        'echo "[INFO] 检查端口 ${PORT} 是否被占用..."',
        'PORT_PID=$(lsof -ti:${PORT} 2>/dev/null)',
        'if [ -z "${PORT_PID}" ]; then',
        '    PORT_PID=$(netstat -tulnp 2>/dev/null | grep ":${PORT} " | grep LISTEN | head -1 | awk '"'"'{print $7}'"'"' | cut -d/ -f1)',
        'fi',
        'if [ ! -z "${PORT_PID}" ]; then',
        '    echo "[WARN] 端口 ${PORT} 已被进程 ${PORT_PID} 占用"',
        '    echo "[INFO] 停止占用端口的进程..."',
        '    kill ${PORT_PID} 2>/dev/null || true',
        '    sleep 2',
        '    if ps -p ${PORT_PID} 1>/dev/null 2>&1; then',
        '        echo "[WARN] 强制停止进程 ${PORT_PID}"',
        '        kill -9 ${PORT_PID} 2>/dev/null || true',
        '        sleep 1',
        '    fi',
        '    echo "[SUCCESS] 已释放端口 ${PORT}"',
        'else',
        '    echo "[INFO] 端口 ${PORT} 未被占用"',
        'fi',
        '',
        '# 停止旧进程',
        'if [ -f ${PID_FILE} ]; then',
        '    OLD_PID=$(cat ${PID_FILE})',
        '    echo "[INFO] 发现旧进程 PID: ${OLD_PID}"',
        '    if ps -p ${OLD_PID} 1>/dev/null 2>&1; then',
        '        echo "[INFO] 停止旧进程..."',
        '        kill ${OLD_PID}',
        '        sleep 3',
        '        if ps -p ${OLD_PID} 1>/dev/null 2>&1; then',
        '            echo "[WARN] 强制停止旧进程"',
        '            kill -9 ${OLD_PID}',
        '            sleep 1',
        '        fi',
        '        echo "[SUCCESS] 旧进程已停止"',
        '    else',
        '        echo "[INFO] 旧进程已不存在"',
        '    fi',
        '    rm -f ${PID_FILE}',
        'fi',
        '',
        '# 启动应用',
        'echo "[INFO] 启动应用..."',
        '# 查找 Java 路径',
        'JAVA_CMD=$(which java 2>/dev/null || echo "/usr/bin/java")',
        'if [ ! -x "${JAVA_CMD}" ]; then',
        '    # 尝试常见的 Java 安装路径',
        '    for java_path in /usr/bin/java /usr/local/bin/java /opt/java/bin/java; do',
        '        if [ -x "${java_path}" ]; then',
        '            JAVA_CMD="${java_path}"',
        '            break',
        '        fi',
        '    done',
        'fi',
        'echo "[INFO] Java 路径: ${JAVA_CMD}"',
        '${JAVA_CMD} -version 2>&1 | head -n 1',
        '',
        `nohup \${JAVA_CMD} -Xms${config.jvmXms} -Xmx${config.jvmXmx} \\`,
        `    -XX:+${config.jvmGc} \\`,
        '    -XX:+HeapDumpOnOutOfMemoryError \\',
        '    -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof \\',
        '    -jar ${APP_JAR} \\',
        '    1>${LOG_DIR}/app.log 2>&1 &',
        '',
        'NEW_PID=$!',
        'echo ${NEW_PID} 1>${PID_FILE}',
        'echo "[INFO] 应用已后台启动，PID: ${NEW_PID}"',
        '',
        '# 等待 2 秒，让应用初始化',
        'sleep 2',
        '',
        '# 第一次检查：进程是否还存活',
        'if ! ps -p ${NEW_PID} 1>/dev/null 2>&1; then',
        '    echo "[ERROR] 应用进程已退出（启动失败）"',
        '    echo "[ERROR] 最后 50 行日志:"',
        '    tail -n 50 ${LOG_DIR}/app.log',
        '    rm -f ${PID_FILE}',
        '    exit 1',
        'fi',
        'echo "[INFO] 初步检查通过，进程仍在运行"',
        '',
        '# 持续检查 15 秒',
        'echo "[INFO] 开始 15 秒稳定性检查..."',
        'for i in {1..15}; do',
        '    echo "[INFO] 检查进度: ${i}/15 秒"',
        '    ',
        '    if ! ps -p ${NEW_PID} 1>/dev/null 2>&1; then',
        '        echo "[ERROR] 应用进程在第 ${i} 秒时退出"',
        '        echo "[ERROR] 最后 50 行日志:"',
        '        tail -n 50 ${LOG_DIR}/app.log',
        '        rm -f ${PID_FILE}',
        '        exit 1',
        '    fi',
        '    ',
        '    if [ $((i % 5)) -eq 0 ]; then',
        '        echo "[INFO] 应用日志最后 5 行:"',
        '        tail -n 5 ${LOG_DIR}/app.log | sed "s/^/  /"',
        '    fi',
        '    ',
        '    sleep 1',
        'done',
        '',
        '# 最终验证',
        'echo "[INFO] ========== 执行最终验证 =========="',
        '',
        'if ! ps -p ${NEW_PID} 1>/dev/null 2>&1; then',
        '    echo "[ERROR] 最终检查失败：进程不存在"',
        '    echo "[ERROR] 完整日志:"',
        '    cat ${LOG_DIR}/app.log',
        '    rm -f ${PID_FILE}',
        '    exit 1',
        'fi',
        'echo "[SUCCESS] 进程存活检查通过"',
        '',
        'sleep 2',
        'if netstat -tuln | grep -q ":${PORT} "; then',
        '    echo "[SUCCESS] 端口 ${PORT} 监听检查通过"',
        'else',
        '    echo "[WARN] 端口 ${PORT} 未监听，应用可能还在启动中"',
        'fi',
        '',
        'echo "[INFO] 进程详细信息:"',
        'ps -p ${NEW_PID} -o pid,ppid,cmd,%cpu,%mem,etime | sed "s/^/  /"',
        '',
        'echo "[INFO] 应用日志最后 20 行:"',
        'tail -n 20 ${LOG_DIR}/app.log | sed "s/^/  /"',
        '',
        'echo "[SUCCESS] ========== 应用启动成功 =========="',
        'echo "[INFO] PID: ${NEW_PID}"',
        'echo "[INFO] 端口: ${PORT}"',
        'echo "[INFO] 日志文件: ${LOG_DIR}/app.log"',
        'echo "[INFO] PID 文件: ${PID_FILE}"',
        ''
      ];
      
      return lines.join('\n');

    case 'nodejs':
      if (config.processManager === 'pm2') {
        return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${deployPath}"
ENTRY_FILE="${config.entryFile}"

cd \${APP_HOME}

pm2 start \${ENTRY_FILE} \\
    --name \${APP_NAME} \\
    --instances ${config.instances}

pm2 save
echo "[SUCCESS] 应用启动成功"
`;
      } else {
        return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${deployPath}"
ENTRY_FILE="${config.entryFile}"
LOG_DIR="\${APP_HOME}/logs"

mkdir -p \${LOG_DIR}
cd \${APP_HOME}

nohup node \${ENTRY_FILE} > \${LOG_DIR}/app.log 2>&1 &
echo $! > \${APP_NAME}.pid

echo "[SUCCESS] 应用启动成功，PID: \$(cat \${APP_NAME}.pid)"
`;
      }

    case 'python':
      return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${deployPath}"
LOG_DIR="\${APP_HOME}/logs"

mkdir -p \${LOG_DIR}
cd \${APP_HOME}

${config.webServer} \\
    --bind 0.0.0.0:8000 \\
    --workers ${config.workers} \\
    --daemon \\
    --pid \${APP_HOME}/\${APP_NAME}.pid \\
    --access-logfile \${LOG_DIR}/access.log \\
    --error-logfile \${LOG_DIR}/error.log \\
    ${config.wsgiApp}

echo "[SUCCESS] 应用启动成功"
`;

    case 'docker':
      return `#!/bin/bash
CONTAINER_NAME="${config.containerName}"
IMAGE_NAME="${config.imageName}"

docker stop \${CONTAINER_NAME} 2>/dev/null || true
docker rm \${CONTAINER_NAME} 2>/dev/null || true

docker pull \${IMAGE_NAME}

docker run -d \\
    --name \${CONTAINER_NAME} \\
    --restart unless-stopped \\
    -p ${config.portMapping} \\
    \${IMAGE_NAME}

echo "[SUCCESS] 容器启动成功"
docker ps | grep \${CONTAINER_NAME}
`;

    default:
      return '#!/bin/bash\necho "请配置启动脚本"';
  }
}

/** 生成停止脚本 */
function generateStopScript(appName, deployPath, config) {
  switch (config.appType) {
    case 'springboot-jar':
      return `#!/bin/bash
APP_NAME="${config.jarFile.replace('.jar', '')}"
APP_HOME="${deployPath}"
PID_FILE="\${APP_HOME}/\${APP_NAME}.pid"

if [ -f \${PID_FILE} ]; then
    PID=\$(cat \${PID_FILE})
    echo "[INFO] 停止应用，PID: \${PID}"
    kill \${PID}
    
    for i in {1..30}; do
        if ! ps -p \${PID} > /dev/null 2>&1; then
            echo "[SUCCESS] 应用已停止"
            rm -f \${PID_FILE}
            exit 0
        fi
        sleep 1
    done
    
    kill -9 \${PID}
    rm -f \${PID_FILE}
    echo "[SUCCESS] 应用已强制停止"
else
    echo "[WARN] PID 文件不存在"
fi
`;

    case 'nodejs':
      if (config.processManager === 'pm2') {
        return `#!/bin/bash
APP_NAME="${appName}"

pm2 stop \${APP_NAME}
pm2 delete \${APP_NAME}
echo "[SUCCESS] 应用已停止"
`;
      } else {
        return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${deployPath}"

if [ -f \${APP_HOME}/\${APP_NAME}.pid ]; then
    kill \$(cat \${APP_HOME}/\${APP_NAME}.pid)
    rm -f \${APP_HOME}/\${APP_NAME}.pid
    echo "[SUCCESS] 应用已停止"
fi
`;
      }

    case 'python':
      return `#!/bin/bash
APP_NAME="${appName}"
APP_HOME="${deployPath}"

if [ -f \${APP_HOME}/\${APP_NAME}.pid ]; then
    kill \$(cat \${APP_HOME}/\${APP_NAME}.pid)
    rm -f \${APP_HOME}/\${APP_NAME}.pid
    echo "[SUCCESS] 应用已停止"
fi
`;

    case 'docker':
      return `#!/bin/bash
CONTAINER_NAME="${config.containerName}"

docker stop \${CONTAINER_NAME}
docker rm \${CONTAINER_NAME}
echo "[SUCCESS] 容器已停止并删除"
`;

    default:
      return '#!/bin/bash\necho "请配置停止脚本"';
  }
}

getList();
</script>
