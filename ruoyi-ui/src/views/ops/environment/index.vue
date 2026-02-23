<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="环境名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入环境名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="环境代码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入环境代码"
          clearable
          @keyup.enter="handleQuery"
        />
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
          v-hasPermi="['ops:environment:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['ops:environment:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:environment:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="environmentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="环境ID" align="center" prop="id" />
      <el-table-column label="环境名称" align="center" prop="name" />
      <el-table-column label="环境代码" align="center" prop="code" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="审批控制" align="center" prop="needApproval">
        <template #default="scope">
           <el-tag v-if="scope.row.needApproval == '1'" type="warning">需要审批</el-tag>
           <el-tag v-else type="success">无需审批</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批流程Key" align="center" prop="approvalProcessKey" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status == '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['ops:environment:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ops:environment:remove']"
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

    <!-- 添加或修改环境配置对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="environmentRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="环境名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入环境名称" />
        </el-form-item>
        <el-form-item label="环境代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入环境代码(如dev,test,prod)" />
        </el-form-item>
        <el-form-item label="显示排序" prop="sort">
          <el-input-number v-model="form.sort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="是否审批" prop="needApproval">
          <el-radio-group v-model="form.needApproval">
            <el-radio value="0">否</el-radio>
            <el-radio value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="流程Key" prop="approvalProcessKey" v-if="form.needApproval == '1'">
          <el-input v-model="form.approvalProcessKey" placeholder="请输入关联的审批流程Key" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
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

<script setup name="Environment">
import { listEnvironment, getEnvironment, delEnvironment, addEnvironment, updateEnvironment } from "@/api/ops/environment";
import { getCurrentInstance, reactive, ref, toRefs } from 'vue';

const { proxy } = getCurrentInstance();

const environmentList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    code: undefined,
    status: undefined
  },
  rules: {
    name: [
      { required: true, message: "环境名称不能为空", trigger: "blur" }
    ],
    code: [
      { required: true, message: "环境代码不能为空", trigger: "blur" }
    ],
    sort: [
      { required: true, message: "显示排序不能为空", trigger: "blur" }
    ],
    needApproval: [
      { required: true, message: "请选择是否需要审批", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询环境配置列表 */
function getList() {
  loading.value = true;
  listEnvironment(queryParams.value).then(response => {
    environmentList.value = response.rows;
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
    name: undefined,
    code: undefined,
    sort: 0,
    status: "0",
    needApproval: "0",
    approvalProcessKey: undefined,
    remark: undefined
  };
  proxy.resetForm("environmentRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加环境配置";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getEnvironment(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改环境配置";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["environmentRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateEnvironment(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addEnvironment(form.value).then(response => {
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
  const deleteIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除环境配置编号为"' + deleteIds + '"的数据项？').then(function() {
    return delEnvironment(deleteIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
