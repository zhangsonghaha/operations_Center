<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="连接ID" prop="connId">
        <el-input
          v-model="queryParams.connId"
          placeholder="请输入连接ID"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件名" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
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
          v-hasPermi="['system:db:backup:add']"
        >新建备份</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:db:backup:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="backupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备份ID" align="center" prop="backupId" />
      <el-table-column label="连接名称" align="center" prop="connId" :formatter="connNameFormat" />
      <el-table-column label="文件名" align="center" prop="fileName" show-overflow-tooltip />
      <el-table-column label="备份类型" align="center" prop="backupType">
        <template #default="scope">
          <span>{{ scope.row.backupType === '0' ? '手动' : '自动' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '成功' : '失败' }}
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
            icon="Download"
            @click="handleDownload(scope.row)"
            v-if="scope.row.status === '0'"
          >下载</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:db:backup:remove']"
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

    <!-- 新建备份对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="backupRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="选择连接" prop="connId">
          <el-select v-model="form.connId" placeholder="请选择连接">
            <el-option
              v-for="item in connList"
              :key="item.connId"
              :label="item.connName"
              :value="item.connId"
            />
          </el-select>
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

<script setup name="DbBackup">
import { listBackup, delBackup, backup, listConn } from "@/api/system/db";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const backupList = ref([]);
const connList = ref([]);
const title = ref("");
const open = ref(false);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    connId: null,
    fileName: null,
  },
  rules: {
    connId: [
      { required: true, message: "请选择连接", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listBackup(queryParams.value).then(response => {
    backupList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getConnList() {
  listConn().then(response => {
    connList.value = response.rows;
  });
}

function connNameFormat(row) {
  const conn = connList.value.find(c => c.connId === row.connId);
  return conn ? conn.connName : row.connId;
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    connId: null
  };
  proxy.resetForm("backupRef");
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.backupId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新建备份";
}

function submitForm() {
  proxy.$refs["backupRef"].validate(valid => {
    if (valid) {
      proxy.$modal.loading("正在备份中，请稍候...");
      backup(form.value.connId).then(response => {
        proxy.$modal.closeLoading();
        proxy.$modal.msgSuccess("备份成功");
        open.value = false;
        getList();
      }).catch(() => {
        proxy.$modal.closeLoading();
      });
    }
  });
}

function handleDelete(row) {
  const backupIds = row.backupId || ids.value;
  proxy.$modal.confirm('是否确认删除备份记录编号为"' + backupIds + '"的数据项？').then(function() {
    return delBackup(backupIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleDownload(row) {
  const resource = "/backup/" + row.fileName;
  proxy.$download.resource(resource);
}

getList();
getConnList();
</script>
