<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模板名称" prop="templateName">
        <el-input
          v-model="queryParams.templateName"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="应用类型" prop="appType">
        <el-select v-model="queryParams.appType" placeholder="请选择应用类型" clearable>
          <el-option label="Java" value="Java" />
          <el-option label="Node" value="Node" />
          <el-option label="Python" value="Python" />
          <el-option label="Static" value="Static" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['ops:deployTemplate:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:deployTemplate:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="模板ID" align="center" prop="id" />
      <el-table-column label="模板名称" align="center" prop="templateName" />
      <el-table-column label="应用类型" align="center" prop="appType" />
      <el-table-column label="当前版本" align="center" prop="version" />
      <el-table-column label="内置" align="center" prop="builtIn">
        <template #default="scope">
          <el-tag v-if="scope.row.builtIn == '1'" type="danger">是</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status == '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['ops:deployTemplate:edit']"
          >修改</el-button>
          <el-button
            size="small"
            type="text"
            icon="Time"
            @click="handleVersions(scope.row)"
            v-hasPermi="['ops:deployTemplate:query']"
          >版本</el-button>
          <el-button
            size="small"
            type="text"
            icon="Document"
            @click="handleDoc(scope.row)"
            v-hasPermi="['ops:deployTemplate:query']"
          >文档</el-button>
          <el-button
            size="small"
            type="text"
            icon="VideoPlay"
            @click="handleDeploy(scope.row)"
            v-hasPermi="['ops:deployTemplate:deploy']"
          >部署</el-button>
          <el-button
            size="small"
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:deployTemplate:remove']"
            v-if="scope.row.builtIn != '1'"
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

    <!-- 添加或修改部署模板对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="form.appType" placeholder="请选择应用类型">
            <el-option label="Java" value="Java" />
            <el-option label="Node" value="Node" />
            <el-option label="Python" value="Python" />
            <el-option label="Static" value="Static" />
          </el-select>
        </el-form-item>
        <el-form-item label="脚本内容" prop="scriptContent">
          <div style="margin-bottom: 10px;">
            <el-radio-group v-model="editMode" size="small">
              <el-radio-button label="script">脚本模式</el-radio-button>
              <el-radio-button label="config">配置模式</el-radio-button>
            </el-radio-group>
          </div>
          
          <div v-show="editMode === 'script'">
            <div style="border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; width: 100%;">
              <VueMonacoEditor
                v-if="open"
                v-model:value="form.scriptContent"
                theme="vs-dark"
                language="shell"
                height="400px"
                style="width: 100%;"
                :options="{
                  automaticLayout: true,
                  minimap: { enabled: false },
                  fontSize: 14,
                  scrollBeyondLastLine: false
                }"
              />
            </div>
            <div style="color: #909399; font-size: 12px; margin-top: 5px;">
              支持变量：${app_name}, ${version}, ${deploy_path}, ${package_path}
            </div>
          </div>
          
          <div v-show="editMode === 'config'">
            <div style="border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px;">
              <el-alert title="定义部署参数，部署时将生成表单供用户填写" type="info" :closable="false" show-icon style="margin-bottom: 10px;" />
              <div style="margin-bottom: 10px;">
                 <el-button type="primary" size="small" icon="Plus" @click="addParameter">添加参数</el-button>
                 <el-button type="success" size="small" icon="Refresh" @click="syncParamsFromScript">从脚本提取参数</el-button>
              </div>
              <el-table :data="parameterList" border size="small">
                <el-table-column label="参数名(变量名)" prop="name">
                  <template #default="scope">
                    <el-input v-model="scope.row.name" placeholder="例如: port" />
                  </template>
                </el-table-column>
                <el-table-column label="显示名称" prop="label">
                  <template #default="scope">
                    <el-input v-model="scope.row.label" placeholder="例如: 服务端口" />
                  </template>
                </el-table-column>
                <el-table-column label="默认值" prop="default">
                  <template #default="scope">
                    <el-input v-model="scope.row.default" placeholder="默认值" />
                  </template>
                </el-table-column>
                <el-table-column label="必填" width="80" align="center">
                  <template #default="scope">
                    <el-switch v-model="scope.row.required" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="60" align="center">
                  <template #default="scope">
                    <el-button type="text" icon="Delete" class="text-danger" @click="removeParameter(scope.$index)"></el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          
          <div class="el-form-item__error" v-if="yamlError">{{ yamlError }}</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 版本历史对话框 -->
    <el-dialog title="版本历史" v-model="versionOpen" width="900px" append-to-body>
      <el-table :data="versionList" v-loading="versionLoading">
        <el-table-column label="版本号" align="center" prop="version" />
        <el-table-column label="变更日志" align="center" prop="changeLog" />
        <el-table-column label="创建人" align="center" prop="creator" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              size="small"
              type="text"
              icon="RefreshLeft"
              @click="handleRollback(scope.row)"
              v-if="scope.row.version !== currentVersion"
            >回滚至此</el-button>
            <el-tag v-else type="success">当前版本</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="versionOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 部署对话框 -->
    <el-dialog title="执行部署" v-model="deployOpen" width="780px" append-to-body>
      <el-form :model="deployForm" ref="deployFormRef" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="发布标题" prop="title" :rules="[{ required: true, message: '请输入发布标题', trigger: 'blur' }]">
              <el-input v-model="deployForm.title" placeholder="请输入发布标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version" :rules="[{ required: true, message: '请输入版本号', trigger: 'blur' }]">
              <el-input v-model="deployForm.version" placeholder="请输入版本号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="environment" :rules="[{ required: true, message: '请选择环境', trigger: 'change' }]">
              <el-select v-model="deployForm.environment" placeholder="请选择环境" style="width: 100%">
                <el-option label="生产环境" value="prod" />
                <el-option label="测试环境" value="test" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发布说明" prop="description">
              <el-input v-model="deployForm.description" type="textarea" placeholder="请输入发布说明" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="选择应用" prop="appId" :rules="[{ required: true, message: '请选择应用', trigger: 'change' }]">
          <el-select v-model="deployForm.appId" placeholder="请选择要部署的应用" filterable style="width: 100%" @change="handleAppChange">
            <el-option
              v-for="item in appOptions"
              :key="item.appId"
              :label="item.appName"
              :value="item.appId"
            />
          </el-select>
        </el-form-item>
        
        <!-- Dynamic Parameters Form -->
        <div v-if="deployParameters.length > 0">
          <el-divider content-position="left">部署参数</el-divider>
          <el-form-item
            v-for="(param, index) in deployParameters"
            :key="index"
            :label="param.label || param.name"
            :prop="'params.' + param.name"
            :rules="param.required ? [{ required: true, message: '该项不能为空', trigger: 'blur' }] : []"
          >
            <el-input v-model="deployForm.params[param.name]" :placeholder="param.default ? '默认: ' + param.default : ''" />
          </el-form-item>
        </div>

        <!-- Skip Steps -->
        <div v-if="templateSteps.length > 0">
          <el-divider content-position="left">跳过步骤</el-divider>
          <el-form-item label="选择跳过">
            <el-checkbox-group v-model="deployForm.skippedSteps">
              <el-checkbox v-for="step in templateSteps" :key="step" :label="step">{{ step }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </div>

        <!-- Script name override -->
        <el-divider content-position="left">启动/停止脚本</el-divider>
        <el-alert title="默认上传 start.sh 和 stop.sh 到部署目录；可在此修改文件名。如需携带参数启动，请在文件名后追加参数。" type="warning" :closable="false" show-icon />
        <el-form-item label="启动脚本">
          <el-input v-model="deployForm.params.start_name" placeholder="例如: start.sh 或 start.sh --server.port=8080" />
        </el-form-item>
        <el-form-item label="停止脚本">
          <el-input v-model="deployForm.params.stop_name" placeholder="例如: stop.sh" />
        </el-form-item>

        <!-- Script preview -->
        <el-divider content-position="left">脚本预览</el-divider>
        <div style="margin-bottom: 10px;">
          <el-radio-group v-model="previewType" size="small" @change="updatePreview">
            <el-radio-button label="deploy">部署脚本</el-radio-button>
            <el-radio-button label="start">启动脚本</el-radio-button>
            <el-radio-button label="stop">停止脚本</el-radio-button>
          </el-radio-group>
        </div>
        <div style="border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden;">
          <VueMonacoEditor
            v-if="deployOpen"
            v-model:value="previewScript"
            theme="vs-dark"
            language="shell"
            height="240px"
            :options="{
              automaticLayout: true,
              readOnly: true,
              minimap: { enabled: false },
              fontSize: 12,
              scrollBeyondLastLine: false
            }"
          />
        </div>
        
      </el-form>
      <div class="dialog-footer" style="text-align: right">
        <el-button type="primary" @click="submitDeploy" :loading="deployLoading">发起发布</el-button>
        <el-button @click="deployOpen = false">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 文档管理 -->
    <doc-manager ref="docManager" />
  </div>
</template>

<script setup name="DeployTemplate">
import { listDeployTemplate, getDeployTemplate, delDeployTemplate, addDeployTemplate, updateDeployTemplate, listDeployTemplateVersions, rollbackDeployTemplate } from "@/api/ops/deployTemplate";
import { addRelease } from "@/api/ops/release";
import { listApp } from "@/api/ops/app";
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'
import jsYaml from 'js-yaml'
import { marked } from 'marked'
import DocManager from "./components/DocManager";
import { getCurrentInstance, reactive, ref, toRefs, watch } from 'vue';

const { proxy } = getCurrentInstance();

const templateList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const yamlError = ref("");
const editMode = ref("script");
const parameterList = ref([]);
const deployParameters = ref([]);

// 版本历史相关
const versionOpen = ref(false);
const versionLoading = ref(false);
const versionList = ref([]);
const currentVersion = ref("");
const currentTemplateId = ref(null);

// 部署相关
const deployOpen = ref(false);
const deployLoading = ref(false);
const appOptions = ref([]);
const currentTemplateScript = ref('');
const currentAppStartScript = ref('');
const currentAppStopScript = ref('');
const previewType = ref('start');
const previewScript = ref('');
const deployForm = reactive({
  appId: undefined,
  title: '',
  version: '',
  description: '',
  environment: 'prod',
  params: {},
  skippedSteps: []
});
const templateSteps = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    templateName: undefined,
    appType: undefined,
    status: undefined
  },
  rules: {
    templateName: [
      { required: true, message: "模板名称不能为空", trigger: "blur" }
    ],
    appType: [
      { required: true, message: "应用类型不能为空", trigger: "change" }
    ],
    scriptContent: [
      { required: true, message: "脚本内容不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询模板列表 */
function getList() {
  loading.value = true;
  listDeployTemplate(queryParams.value).then(response => {
    templateList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    templateName: undefined,
    appType: undefined,
    scriptContent: undefined,
    parameterConfig: undefined,
    status: "0",
    remark: undefined
  };
  parameterList.value = [];
  editMode.value = "script";
  proxy.resetForm("formRef");
  yamlError.value = "";
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
  ids.value = selection.map(item => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加部署模板";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getDeployTemplate(id).then(response => {
    form.value = response.data;
    if (form.value.parameterConfig) {
      try {
        parameterList.value = JSON.parse(form.value.parameterConfig);
      } catch (e) {
        parameterList.value = [];
      }
    }
    open.value = true;
    title.value = "修改部署模板";
  });
}

/** 校验YAML内容 */
function validateYaml(content) {
  // 简单校验：禁止 rm -rf /
  if (content && content.includes('rm -rf /')) {
    yamlError.value = "禁止包含高危命令 rm -rf /";
    return false;
  }
  try {
    jsYaml.load(content);
  } catch (e) {
    yamlError.value = "YAML格式错误: " + e.message;
    return false;
  }
  yamlError.value = "";
  return true;
}

/** 从脚本提取参数 */
function syncParamsFromScript() {
  if (!form.value.scriptContent) {
    proxy.$modal.msgWarning("脚本内容为空");
    return;
  }
  const regex = /\$\{([a-zA-Z0-9_]+)\}/g;
  let match;
  const foundParams = new Set();
  // Standard built-in vars
  const builtInVars = ['app_name', 'version', 'deploy_path', 'package_path'];
  
  while ((match = regex.exec(form.value.scriptContent)) !== null) {
    const paramName = match[1];
    if (!builtInVars.includes(paramName)) {
      foundParams.add(paramName);
    }
  }
  
  if (foundParams.size === 0) {
    proxy.$modal.msgInfo("未发现新的自定义参数");
    return;
  }
  
  let addedCount = 0;
  foundParams.forEach(name => {
    const exists = parameterList.value.some(p => p.name === name);
    if (!exists) {
      parameterList.value.push({
        name: name,
        label: name,
        default: "",
        required: true
      });
      addedCount++;
    }
  });
  
  if (addedCount > 0) {
    proxy.$modal.msgSuccess(`已提取 ${addedCount} 个新参数`);
  } else {
    proxy.$modal.msgInfo("所有参数已存在");
  }
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (!validateYaml(form.value.scriptContent)) {
        return;
      }
      form.value.parameterConfig = JSON.stringify(parameterList.value);
      if (form.value.id != null) {
        updateDeployTemplate(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addDeployTemplate(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 添加参数 */
function addParameter() {
  parameterList.value.push({
    name: "",
    label: "",
    default: "",
    required: true
  });
}

/** 移除参数 */
function removeParameter(index) {
  parameterList.value.splice(index, 1);
}

/** 删除按钮操作 */
function handleDelete(row) {
  const templateIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除部署模板编号为"' + templateIds + '"的数据项？').then(function() {
    return delDeployTemplate(templateIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 版本历史按钮操作 */
function handleVersions(row) {
  currentTemplateId.value = row.id;
  currentVersion.value = row.version;
  versionOpen.value = true;
  versionLoading.value = true;
  listDeployTemplateVersions(row.id).then(response => {
    versionList.value = response.rows;
    versionLoading.value = false;
  });
}

/** 回滚按钮操作 */
function handleRollback(row) {
  proxy.$modal.confirm('确认回滚到版本 "' + row.version + '" 吗？').then(function() {
    return rollbackDeployTemplate(currentTemplateId.value, row.id);
  }).then(() => {
    proxy.$modal.msgSuccess("回滚成功");
    versionOpen.value = false;
    getList();
  }).catch(() => {});
}

/** 部署按钮操作 */
function handleDeploy(row) {
  currentTemplateId.value = row.id;
  deployForm.appId = undefined;
  deployForm.params = {};
  deployParameters.value = [];
  currentTemplateScript.value = row.scriptContent || '';
  // default script names
  deployForm.params.start_name = 'start.sh';
  deployForm.params.stop_name = 'stop.sh';
  deployForm.title = row.templateName + ' - 发布 - ' + new Date().toLocaleString();
  deployForm.version = 'v' + new Date().getTime();
  deployForm.description = '';
  deployForm.skippedSteps = [];
  
  // Parse steps from YAML script if possible
  templateSteps.value = [];
  try {
    const doc = jsYaml.load(currentTemplateScript.value);
    if (doc && doc.steps && Array.isArray(doc.steps)) {
      templateSteps.value = doc.steps.map(s => s.name);
    }
  } catch (e) {
    console.warn("Parse steps failed", e);
  }
  
  if (row.parameterConfig) {
    try {
      deployParameters.value = JSON.parse(row.parameterConfig);
      // Init default values
      deployParameters.value.forEach(p => {
        deployForm.params[p.name] = p.default || "";
      });
    } catch (e) {
      console.error("Parse params failed", e);
    }
  }
  
  deployOpen.value = true;
  
  // 加载应用列表
  listApp({ pageSize: 1000 }).then(response => {
    appOptions.value = response.rows;
  });
}

/** 监听应用选择，回显参数 */
function handleAppChange(appId) {
  if (!appId) return;
  const app = appOptions.value.find(item => item.appId === appId);
  if (app) {
    currentAppStartScript.value = app.startScript || '';
    currentAppStopScript.value = app.stopScript || '';
    updatePreview();

    // 自动回填参数
    if (deployParameters.value.length > 0) {
      deployParameters.value.forEach(param => {
        // 回显部署路径
        if (param.name === 'deploy_path' && app.deployPath) {
          deployForm.params[param.name] = app.deployPath;
        }
        // 回显应用包路径
        if (param.name === 'package_path' && app.packagePath) {
          deployForm.params[param.name] = app.packagePath;
        }
        // 回显端口
        if (param.name === 'port' && app.monitorPorts) {
           // 取第一个端口
           const ports = app.monitorPorts.split(',');
           if (ports.length > 0) {
             deployForm.params[param.name] = ports[0];
           }
        }
        // 回显应用名称
        if (param.name === 'app_name' && app.appName) {
          deployForm.params[param.name] = app.appName;
        }
        // 回显新增参数
        if (param.name === 'deploy_timeout' && app.deployTimeout) {
          deployForm.params[param.name] = app.deployTimeout;
        }
        if (param.name === 'retry_count' && app.retryCount !== undefined) {
          deployForm.params[param.name] = app.retryCount;
        }
        if (param.name === 'health_check_url' && app.healthCheckUrl) {
          deployForm.params[param.name] = app.healthCheckUrl;
        }
      });
    }
  }
}

/** 更新预览脚本 */
function updatePreview() {
  let raw = '';
  if (previewType.value === 'deploy') {
    raw = currentTemplateScript.value;
  } else if (previewType.value === 'start') {
    raw = currentAppStartScript.value;
  } else {
    raw = currentAppStopScript.value;
  }

  if (!raw) {
    previewScript.value = '';
    return;
  }
  let content = raw;
  for (const key in deployForm.params) {
    const val = deployForm.params[key];
    if (val !== undefined && val !== null) {
      content = content.split('${' + key + '}').join(val);
    }
  }

  if (deployForm.skippedSteps.length > 0) {
    try {
      const doc = jsYaml.load(content);
      if (doc && doc.steps && Array.isArray(doc.steps)) {
        doc.steps = doc.steps.filter(s => !deployForm.skippedSteps.includes(s.name));
        content = jsYaml.dump(doc);
      }
    } catch (e) {}
  }
  previewScript.value = content;
}

watch(() => [deployForm.params, deployForm.skippedSteps], () => {
  updatePreview();
}, { deep: true });

/** 提交发布 */
function submitDeploy() {
  if (!deployForm.appId) {
    proxy.$modal.msgError("请选择应用");
    return;
  }
  
  if (!deployForm.title || !deployForm.version) {
    proxy.$modal.msgError("请填写发布标题和版本号");
    return;
  }

  // Validate required params
  for (const param of deployParameters.value) {
    if (param.required && !deployForm.params[param.name]) {
      proxy.$modal.msgError(`参数 ${param.label || param.name} 不能为空`);
      return;
    }
  }
  
  deployLoading.value = true;
  
  // Generate final script with skipped steps removed
  let finalScript = previewScript.value;
  // If we are previewing 'deploy' script, we might want to re-generate it from source to ensure we have the latest variable replacements
  // But wait, `previewScript` already has replacements. 
  // However, we need to remove steps from the YAML structure BEFORE replacement or AFTER?
  // Ideally, we take the raw template, remove steps, THEN replace variables.
  // Or take `previewScript` (which is already replaced) and remove steps if it's still valid YAML.
  // Let's try to process `currentTemplateScript` (raw), remove steps, then replace.
  
  try {
    let scriptToProcess = currentTemplateScript.value;
    // Process steps skipping
    if (deployForm.skippedSteps.length > 0) {
      const doc = jsYaml.load(scriptToProcess);
      if (doc && doc.steps && Array.isArray(doc.steps)) {
        doc.steps = doc.steps.filter(s => !deployForm.skippedSteps.includes(s.name));
        scriptToProcess = jsYaml.dump(doc);
      }
    }
    
    // Replace variables
    let content = scriptToProcess;
    const app = appOptions.value.find(item => item.appId === deployForm.appId);
    if (app) {
        content = content.replaceAll('${app_name}', app.appName || '')
                 .replaceAll('${deploy_path}', app.deployPath || '')
                 .replaceAll('${package_path}', app.packagePath || '');
    }
    
    // Replace dynamic vars
    for (const key in deployForm.params) {
      const val = deployForm.params[key];
      if (val !== undefined && val !== null) {
        content = content.split('${' + key + '}').join(val);
      }
    }
    finalScript = content;
  } catch (e) {
    console.error("Script processing failed", e);
    // Fallback to previewScript if parsing fails, but steps won't be skipped
    finalScript = previewScript.value;
  }

  const releaseData = {
    templateId: currentTemplateId.value,
    appId: deployForm.appId,
    title: deployForm.title,
    version: deployForm.version,
    description: deployForm.description,
    environment: deployForm.environment,
    scriptContent: finalScript,
    status: "1" // 待审批
  };
  
  addRelease(releaseData).then(response => {
    proxy.$modal.msgSuccess("发布申请已提交");
    deployOpen.value = false;
    deployLoading.value = false;
  }).catch(() => {
    deployLoading.value = false;
  });
}

/** 文档管理 */
function handleDoc(row) {
  if (proxy.$refs.docManager) {
    proxy.$refs.docManager.show(row.id, row.version);
  } else {
    console.error("DocManager ref not found");
  }
}

getList();
</script>
