<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="连接名称" prop="connName">
        <el-input
          v-model="queryParams.connName"
          placeholder="请输入连接名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="数据库名" prop="dbName">
        <el-input
          v-model="queryParams.dbName"
          placeholder="请输入数据库名"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
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
          v-hasPermi="['system:db:conn:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:db:conn:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:db:conn:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="connList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="连接ID" align="center" prop="connId" />
      <el-table-column label="连接名称" align="center" prop="connName" />
      <el-table-column label="数据库类型" align="center" prop="dbType" />
      <el-table-column label="主机地址" align="center" prop="host" />
      <el-table-column label="端口" align="center" prop="port" />
      <el-table-column label="账号" align="center" prop="username" />
      <el-table-column label="数据库名" align="center" prop="dbName" />
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
            v-hasPermi="['system:db:conn:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:db:conn:remove']"
          >删除</el-button>
          <el-button
            link
            type="primary"
            icon="Connection"
            @click="handleTest(scope.row)"
          >测试连接</el-button>
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

    <!-- 添加或修改数据库连接配置对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="connRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="连接名称" prop="connName">
          <el-input v-model="form.connName" placeholder="请输入连接名称" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="dbType">
          <el-select v-model="form.dbType" placeholder="请选择数据库类型">
            <el-option label="MySQL" value="mysql" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="form.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input v-model="form.port" placeholder="请输入端口" />
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="数据库名" prop="dbName">
          <el-input v-model="form.dbName" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="状态">
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
          <el-button type="warning" @click="handleTestForm">测 试</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DbConn">
import { listConn, getConn, delConn, addConn, updateConn, testConn } from "@/api/system/db";

const { proxy } = getCurrentInstance();

const connList = ref([]);
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
    connName: null,
    dbType: null,
    host: null,
    username: null,
    status: null
  },
  rules: {
    connName: [
      { required: true, message: "连接名称不能为空", trigger: "blur" }
    ],
    host: [
      { required: true, message: "主机地址不能为空", trigger: "blur" }
    ],
    port: [
      { required: true, message: "端口不能为空", trigger: "blur" }
    ],
    username: [
      { required: true, message: "账号不能为空", trigger: "blur" }
    ],
    password: [
      { required: true, message: "密码不能为空", trigger: "blur" }
    ],
    dbName: [
      { required: true, message: "数据库名不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询数据库连接配置列表 */
function getList() {
  loading.value = true;
  listConn(queryParams.value).then(response => {
    connList.value = response.rows;
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
  form.value = {
    connId: null,
    connName: null,
    dbType: "mysql",
    host: "localhost",
    port: "3306",
    username: null,
    password: null,
    dbName: null,
    status: "0",
    remark: null
  };
  proxy.resetForm("connRef");
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

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.connId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加数据库连接配置";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const connId = row.connId || ids.value;
  getConn(connId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改数据库连接配置";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["connRef"].validate(valid => {
    if (valid) {
      if (form.value.connId != null) {
        updateConn(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addConn(form.value).then(response => {
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
  const connIds = row.connId || ids.value;
  proxy.$modal.confirm('是否确认删除数据库连接配置编号为"' + connIds + '"的数据项？').then(function() {
    return delConn(connIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 测试连接操作 */
function handleTest(row) {
  testConn(row).then(response => {
    proxy.$modal.msgSuccess("连接成功");
  });
}

/** 表单测试连接 */
function handleTestForm() {
  proxy.$refs["connRef"].validate(valid => {
    if (valid) {
      testConn(form.value).then(response => {
        proxy.$modal.msgSuccess("连接成功");
      });
    }
  });
}

getList();
</script>
