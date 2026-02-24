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
      <el-form-item label="执行语句" prop="sqlContent">
        <el-input
          v-model="queryParams.sqlContent"
          placeholder="请输入执行语句"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
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
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:db:log:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="日志ID" align="center" prop="logId" />
      <el-table-column label="连接名称" align="center" prop="connId" :formatter="connNameFormat" />
      <el-table-column label="执行语句" align="center" prop="sqlContent" show-overflow-tooltip />
      <el-table-column label="耗时(ms)" align="center" prop="costTime" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误信息" align="center" prop="errorMsg" show-overflow-tooltip />
      <el-table-column label="操作者" align="center" prop="createBy" />
      <el-table-column label="操作时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
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
  </div>
</template>

<script setup name="DbLog">
import { listLog, listConn } from "@/api/system/db";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const logList = ref([]);
const connList = ref([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    connId: null,
    sqlContent: null,
    status: null
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listLog(queryParams.value).then(response => {
    logList.value = response.rows;
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

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleExport() {
  proxy.download('system/db/log/export', {
    ...queryParams.value
  }, `db_log_${new Date().getTime()}.xlsx`)
}

getList();
getConnList();
</script>
