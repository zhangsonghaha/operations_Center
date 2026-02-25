<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="策略名称" prop="strategyName">
        <el-input v-model="queryParams.strategyName" placeholder="请输入策略名称" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="数据库类型" prop="dbType">
        <el-select v-model="queryParams.dbType" placeholder="请选择" clearable style="width: 150px">
          <el-option v-for="dict in dbTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="enabled">
        <el-select v-model="queryParams.enabled" placeholder="请选择" clearable style="width: 150px">
          <el-option label="启用" value="0" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:db:backupStrategy:add']">新增策略</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:db:backupStrategy:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="strategyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="策略ID" align="center" prop="strategyId" width="80" />
      <el-table-column label="策略名称" align="center" prop="strategyName" show-overflow-tooltip />
      <el-table-column label="数据库类型" align="center" prop="dbType" width="120">
        <template #default="scope">
          <dict-tag :options="dbTypeOptions" :value="scope.row.dbType" />
        </template>
      </el-table-column>
      <el-table-column label="备份方式" align="center" prop="backupMode" width="100">
        <template #default="scope">
          <dict-tag :options="backupModeOptions" :value="scope.row.backupMode" />
        </template>
      </el-table-column>
      <el-table-column label="备份级别" align="center" prop="backupLevel" width="100">
        <template #default="scope">
          <dict-tag :options="backupLevelOptions" :value="scope.row.backupLevel" />
        </template>
      </el-table-column>
      <el-table-column label="定时表达式" align="center" prop="cronExpression" width="150" />
      <el-table-column label="保留策略" align="center" width="150">
        <template #default="scope">
          <span v-if="scope.row.retentionDays">{{ scope.row.retentionDays }}天</span>
          <span v-else-if="scope.row.retentionCount">{{ scope.row.retentionCount }}个</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="enabled" width="80">
        <template #default="scope">
          <el-switch v-model="scope.row.enabled" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:db:backupStrategy:edit']">修改</el-button>
          <el-button link type="primary" icon="VideoPlay" @click="handleExecute(scope.row)" v-hasPermi="['system:db:backupStrategy:add']">立即执行</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:db:backupStrategy:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="strategyRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="策略名称" prop="strategyName">
              <el-input v-model="form.strategyName" placeholder="请输入策略名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择连接" prop="connId">
              <el-select v-model="form.connId" placeholder="请选择数据库连接" style="width: 100%">
                <el-option v-for="item in connList" :key="item.connId" :label="item.connName" :value="item.connId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据库类型" prop="dbType">
              <el-select v-model="form.dbType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in dbTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备份方式" prop="backupMode">
              <el-select v-model="form.backupMode" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in backupModeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="备份级别" prop="backupLevel">
              <el-select v-model="form.backupLevel" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in backupLevelOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备份目标" prop="targetName">
              <el-input v-model="form.targetName" placeholder="数据库名/表名，多个用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>定时配置</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Cron表达式" prop="cronExpression">
              <el-input v-model="form.cronExpression" placeholder="如: 0 0 2 * * ?">
                <template #append>
                  <el-button @click="showCron = true">生成</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="策略状态">
              <el-radio-group v-model="form.enabled">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>保留策略</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保留天数">
              <el-input-number v-model="form.retentionDays" :min="1" :max="365" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保留数量">
              <el-input-number v-model="form.retentionCount" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>高级选项</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用压缩">
              <el-switch v-model="form.compressEnabled" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="压缩类型" v-if="form.compressEnabled === '1'">
              <el-select v-model="form.compressType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in compressTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存储类型" prop="storageType">
              <el-select v-model="form.storageType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in storageTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用告警">
              <el-switch v-model="form.alertEnabled" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- Cron表达式生成器 -->
    <el-dialog title="Cron表达式生成器" v-model="showCron" width="600px" append-to-body>
      <div class="cron-generator">
        <el-tabs v-model="cronTab">
          <el-tab-pane label="秒" name="second">
            <el-radio-group v-model="cron.second">
              <el-radio label="*">每秒</el-radio>
              <el-radio label="0/5">每5秒</el-radio>
              <el-radio label="0/10">每10秒</el-radio>
              <el-radio label="0/30">每30秒</el-radio>
            </el-radio-group>
          </el-tab-pane>
          <el-tab-pane label="分钟" name="minute">
            <el-radio-group v-model="cron.minute">
              <el-radio label="*">每分钟</el-radio>
              <el-radio label="0">整点</el-radio>
              <el-radio label="0/5">每5分钟</el-radio>
              <el-radio label="0/10">每10分钟</el-radio>
              <el-radio label="0/30">每30分钟</el-radio>
            </el-radio-group>
          </el-tab-pane>
          <el-tab-pane label="小时" name="hour">
            <el-radio-group v-model="cron.hour">
              <el-radio label="*">每小时</el-radio>
              <el-radio label="0">0点</el-radio>
              <el-radio label="2">2点</el-radio>
              <el-radio label="0,12">0点和12点</el-radio>
            </el-radio-group>
          </el-tab-pane>
          <el-tab-pane label="日" name="day">
            <el-radio-group v-model="cron.day">
              <el-radio label="*">每天</el-radio>
              <el-radio label="?">不指定</el-radio>
            </el-radio-group>
          </el-tab-pane>
        </el-tabs>
        <div class="cron-preview">
          <el-input v-model="generatedCron" readonly>
            <template #prepend>生成的表达式</template>
          </el-input>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCron = false">取 消</el-button>
        <el-button type="primary" @click="applyCron">应 用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DbBackupStrategy">
import { listStrategy, getStrategy, delStrategy, addStrategy, updateStrategy, executeStrategy } from "@/api/system/dbBackup";
import { listConn } from "@/api/system/db";

const { proxy } = getCurrentInstance();

const strategyList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const open = ref(false);
const connList = ref([]);
const showCron = ref(false);
const cronTab = ref("second");

// 字典选项
const dbTypeOptions = ref([
  { label: "MySQL/MariaDB", value: "mysql" },
  { label: "PostgreSQL", value: "postgresql" },
  { label: "MongoDB", value: "mongodb" },
  { label: "Redis", value: "redis" }
]);
const backupModeOptions = ref([
  { label: "全量备份", value: "full" },
  { label: "增量备份", value: "incremental" },
  { label: "差异备份", value: "differential" }
]);
const backupLevelOptions = ref([
  { label: "实例级", value: "instance" },
  { label: "数据库级", value: "database" },
  { label: "表级", value: "table" }
]);
const storageTypeOptions = ref([
  { label: "本地存储", value: "local" },
  { label: "FTP服务器", value: "ftp" },
  { label: "SFTP服务器", value: "sftp" },
  { label: "阿里云OSS", value: "aliyun_oss" },
  { label: "腾讯云COS", value: "tencent_cos" }
]);
const compressTypeOptions = ref([
  { label: "Gzip", value: "gzip" },
  { label: "Bzip2", value: "bzip2" },
  { label: "Zip", value: "zip" }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    strategyName: null,
    dbType: null,
    enabled: null
  },
  rules: {
    strategyName: [{ required: true, message: "策略名称不能为空", trigger: "blur" }],
    connId: [{ required: true, message: "请选择数据库连接", trigger: "change" }],
    dbType: [{ required: true, message: "请选择数据库类型", trigger: "change" }],
    backupMode: [{ required: true, message: "请选择备份方式", trigger: "change" }],
    backupLevel: [{ required: true, message: "请选择备份级别", trigger: "change" }],
    storageType: [{ required: true, message: "请选择存储类型", trigger: "change" }]
  },
  cron: {
    second: "0",
    minute: "0",
    hour: "2",
    day: "*",
    month: "*",
    week: "?"
  }
});

const { queryParams, form, rules, cron } = toRefs(data);

const generatedCron = computed(() => {
  return `${cron.value.second} ${cron.value.minute} ${cron.value.hour} ${cron.value.day} ${cron.value.month} ${cron.value.week}`;
});

function getList() {
  loading.value = true;
  listStrategy(queryParams.value).then(response => {
    strategyList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getConnList() {
  listConn().then(response => {
    connList.value = response.rows;
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    strategyId: null,
    strategyName: null,
    connId: null,
    dbType: "mysql",
    backupMode: "full",
    backupLevel: "database",
    targetName: null,
    cronExpression: "0 0 2 * * ?",
    enabled: "0",
    retentionDays: 7,
    retentionCount: 10,
    compressEnabled: "1",
    compressType: "gzip",
    encryptEnabled: "0",
    storageType: "local",
    storageConfig: null,
    alertEnabled: "0",
    alertConfig: null,
    remark: null
  };
  proxy.resetForm("strategyRef");
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
  ids.value = selection.map(item => item.strategyId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增备份策略";
}

function handleUpdate(row) {
  reset();
  const strategyId = row.strategyId || ids.value;
  getStrategy(strategyId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改备份策略";
  });
}

function handleDelete(row) {
  const strategyIds = row.strategyId || ids.value;
  proxy.$modal.confirm('是否确认删除备份策略编号为"' + strategyIds + '"的数据项？').then(function() {
    return delStrategy(strategyIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleStatusChange(row) {
  updateStrategy(row).then(() => {
    proxy.$modal.msgSuccess("状态更新成功");
  });
}

function handleExecute(row) {
  proxy.$modal.confirm('确定要立即执行策略"' + row.strategyName + '"的备份任务吗？').then(function() {
    return executeStrategy(row.strategyId);
  }).then(() => {
    proxy.$modal.msgSuccess("备份任务已启动，请稍后查看备份记录");
  }).catch(() => {});
}

function submitForm() {
  proxy.$refs["strategyRef"].validate(valid => {
    if (valid) {
      if (form.value.strategyId != null) {
        updateStrategy(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addStrategy(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function applyCron() {
  form.value.cronExpression = generatedCron.value;
  showCron.value = false;
}

getList();
getConnList();
</script>

<style scoped>
.cron-generator {
  padding: 20px;
}
.cron-preview {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #dcdfe6;
}
</style>
