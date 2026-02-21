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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['ops:app:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
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
import { listDept } from "@/api/system/dept";
import { listUser } from "@/api/system/user";
import { handleTree } from "@/utils/ruoyi";
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'
import { getCurrentInstance, reactive, ref, toRefs, watch } from 'vue';

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
  listServer().then(response => {
    // 假设返回的数据结构是 response.rows，包含 serverId, serverName, serverIp 等字段
    if (response.rows) {
      serverOptions.value = response.rows.map(item => ({
        serverId: item.serverId,
        serverName: item.serverName || '未知服务器',
        serverIp: item.publicIp || '未知IP'
      }));
    } else if (response.data) {
       // 兼容另一种返回格式
       const data = Array.isArray(response.data) ? response.data : [response.data];
       serverOptions.value = data.map(item => ({
        serverId: item.serverId,
        serverName: item.serverName || '未知服务器',
        serverIp: item.publicIp || '未知IP'
      }));
    }
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
    // 处理服务器ID回显，假设后端返回的是逗号分隔字符串
    if (form.value.serverIds && typeof form.value.serverIds === 'string') {
       form.value.serverIds = form.value.serverIds.split(',').map(Number);
    } else if (!form.value.serverIds) {
       form.value.serverIds = [];
    }
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

getList();
</script>
