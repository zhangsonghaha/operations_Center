<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="发布标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入发布标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="应用名称" prop="appId">
        <el-select v-model="queryParams.appId" placeholder="请选择应用" clearable filterable>
          <el-option
            v-for="item in appOptions"
            :key="item.appId"
            :label="item.appName"
            :value="item.appId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="草稿" value="0" />
          <el-option label="待审批" value="1" />
          <el-option label="已通过" value="2" />
          <el-option label="已驳回" value="3" />
          <el-option label="已发布" value="4" />
          <el-option label="发布失败" value="5" />
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
          v-hasPermi="['ops:release:add']"
        >新增申请</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['ops:release:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:release:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="releaseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请ID" align="center" prop="id" width="80" />
      <el-table-column label="发布标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="应用名称" align="center" prop="appName">
        <template #default="scope">
          {{ getAppName(scope.row.appId) }}
        </template>
      </el-table-column>
      <el-table-column label="版本号" align="center" prop="version" width="100" />
      <el-table-column label="审批进度" align="center" width="120">
        <template #default="scope">
           <span v-if="scope.row.totalSteps > 0">
             第 {{ scope.row.currentStep }} / {{ scope.row.totalSteps }} 步
           </span>
           <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status == '0'" type="info">草稿</el-tag>
          <el-tag v-else-if="scope.row.status == '1'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.status == '2'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.status == '3'" type="danger">已驳回</el-tag>
          <el-tag v-else-if="scope.row.status == '4'" type="primary">已发布</el-tag>
          <el-tag v-else-if="scope.row.status == '5'" type="danger">发布失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划发布时间" align="center" prop="scheduleTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.scheduleTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请人" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            link
            type="primary"
            icon="View"
            @click="handleViewProcess(scope.row)"
            v-if="scope.row.processInstanceId"
          >流程</el-button>
          <el-button
            size="small"
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['ops:release:edit']"
            v-if="scope.row.status == '0' || scope.row.status == '3'"
          >修改</el-button>
          <el-button
            size="small"
            link
            type="primary"
            icon="VideoPlay"
            @click="handleExecute(scope.row)"
            v-hasPermi="['ops:release:deploy']"
            v-if="scope.row.status == '2'"
          >发布</el-button>
          <el-button
            size="small"
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:release:remove']"
            v-if="scope.row.status == '0' || scope.row.status == '3'"
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

    <!-- 添加或修改发布申请对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="发布标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入发布标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="请输入版本号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择应用" prop="appId">
              <el-select v-model="form.appId" placeholder="请选择应用" filterable style="width: 100%">
                <el-option
                  v-for="item in appOptions"
                  :key="item.appId"
                  :label="item.appName"
                  :value="item.appId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署模板" prop="templateId">
              <el-select v-model="form.templateId" placeholder="请选择部署模板" filterable style="width: 100%">
                <el-option
                  v-for="item in templateOptions"
                  :key="item.id"
                  :label="item.templateName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联需求" prop="requirementIds">
              <el-input v-model="form.requirementIds" placeholder="请输入关联需求ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联Bug" prop="bugIds">
              <el-input v-model="form.bugIds" placeholder="请输入关联BugID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划时间" prop="scheduleTime">
              <el-date-picker
                v-model="form.scheduleTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择计划发布时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发布说明" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入发布变更内容说明" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="风险点" prop="risks">
              <el-input v-model="form.risks" type="textarea" placeholder="请输入可能的风险点" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="回滚方案" prop="rollbackPlan">
              <el-input v-model="form.rollbackPlan" type="textarea" placeholder="请输入回滚方案" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 流程追踪对话框 -->
    <el-dialog title="流程追踪" v-model="processOpen" width="1000px" append-to-body destroy-on-close>
      <div v-if="bpmnXml" style="height: 500px; border: 1px solid #dcdfe6;">
        <BpmnViewer :xml="bpmnXml" :activeActivityIds="activeActivityIds" :activeTaskInfo="activeTaskInfo" />
      </div>
      <div v-else style="text-align: center; padding: 20px;">
        暂无流程图数据
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="processOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Release">
import { listRelease, getRelease, delRelease, addRelease, updateRelease, executeRelease } from "@/api/ops/release";
import { listApp } from "@/api/ops/app";
import { listDeployTemplate } from "@/api/ops/deployTemplate";
import { getProcessProgress } from "@/api/ops/workflow";
import BpmnViewer from "@/components/BpmnViewer";
import { getCurrentInstance, reactive, ref, toRefs } from 'vue';

const { proxy } = getCurrentInstance();

const releaseList = ref([]);
const open = ref(false);
const processOpen = ref(false);
const bpmnXml = ref("");
const activeActivityIds = ref([]);
const activeTaskInfo = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const appOptions = ref([]);
const templateOptions = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    appId: undefined,
    status: undefined
  },
  rules: {
    title: [
      { required: true, message: "发布标题不能为空", trigger: "blur" }
    ],
    version: [
      { required: true, message: "版本号不能为空", trigger: "blur" }
    ],
    appId: [
      { required: true, message: "应用不能为空", trigger: "change" }
    ],
    templateId: [
      { required: true, message: "部署模板不能为空", trigger: "change" }
    ],
    description: [
      { required: true, message: "发布说明不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询发布申请列表 */
function getList() {
  loading.value = true;
  listRelease(queryParams.value).then(response => {
    releaseList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 查询应用列表 */
function getAppList() {
  listApp().then(response => {
    appOptions.value = response.rows;
  });
}

/** 查询模板列表 */
function getTemplateList() {
  listDeployTemplate().then(response => {
    templateOptions.value = response.rows;
  });
}

/** 获取应用名称 */
function getAppName(appId) {
  const app = appOptions.value.find(item => item.appId === appId);
  return app ? app.appName : appId;
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
    appId: undefined,
    templateId: undefined,
    version: undefined,
    title: undefined,
    description: undefined,
    risks: undefined,
    rollbackPlan: undefined,
    requirementIds: undefined,
    bugIds: undefined,
    status: "0",
    scheduleTime: undefined
  };
  proxy.resetForm("formRef");
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
  title.value = "发起发布申请";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getRelease(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改发布申请";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 默认新增为待审批状态，如果是草稿状态修改提交，可以改为待审批
      if (form.value.status === '0') {
         // Optionally ask user if they want to submit for audit directly or save as draft
         // For simplicity, let's assume 'Submit' button saves and potentially submits.
         // Let's keep status as is if editing, or set to 1 (Pending Audit) if new?
         // User requirement: "发起发布申请...". Usually implies submitting.
         form.value.status = "1"; 
      }
      
      if (form.value.id != null) {
        updateRelease(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        form.value.status = "1"; // Default to pending audit
        addRelease(form.value).then(response => {
          proxy.$modal.msgSuccess("申请提交成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const deleteIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除发布申请编号为"' + deleteIds + '"的数据项？').then(function() {
    return delRelease(deleteIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 执行发布操作 */
function handleExecute(row) {
  proxy.$modal.confirm('确认立即执行发布任务 "' + row.title + '" 吗？').then(function() {
    return executeRelease(row.id);
  }).then(() => {
    proxy.$modal.msgSuccess("发布指令已下发");
    getList();
  }).catch(() => {});
}

/** 流程追踪 */
function handleViewProcess(row) {
  if (row.processInstanceId) {
    bpmnXml.value = "";
    activeActivityIds.value = [];
    processOpen.value = true;
    getProcessProgress(row.processInstanceId).then(res => {
      bpmnXml.value = res.data.bpmnXml;
      activeActivityIds.value = res.data.activeActivityIds || [];
    }).catch(() => {
      proxy.$modal.msgError("获取流程进度失败");
      processOpen.value = false;
    });
  }
}

// Init
getAppList();
getTemplateList();
getList();
</script>
