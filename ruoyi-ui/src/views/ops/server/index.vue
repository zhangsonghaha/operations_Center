<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="服务器名称" prop="serverName" label-width="100px">
        <el-input
          v-model="queryParams.serverName"
          placeholder="请输入服务器名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="公网IP" prop="publicIp">
        <el-input
          v-model="queryParams.publicIp"
          placeholder="请输入公网IP"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="内网IP" prop="privateIp">
        <el-input
          v-model="queryParams.privateIp"
          placeholder="请输入内网IP"
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
          v-hasPermi="['ops:server:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['ops:server:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ops:server:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['ops:server:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="serverList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="服务器ID" align="center" prop="serverId" />
      <el-table-column label="服务器名称" align="center" prop="serverName" />
      <el-table-column label="公网IP" align="center" prop="publicIp" />
      <el-table-column label="内网IP" align="center" prop="privateIp" />
      <el-table-column label="SSH端口" align="center" prop="serverPort" />
      <el-table-column label="所属机房" align="center" prop="dataCenter" />
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
          <el-button link type="primary" icon="Monitor" @click="handleTerminal(scope.row)" v-hasPermi="['ops:server:query']">终端</el-button>
          <el-button link type="primary" icon="Switch" @click="handleCheck(scope.row)" v-hasPermi="['ops:server:query']">检测</el-button>
          <el-dropdown size="small" @command="(command) => handleCommand(command, scope.row)">
            <el-button link type="primary" icon="DArrowRight">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="handleUpdate" icon="Edit" v-hasPermi="['ops:server:edit']">修改</el-dropdown-item>
                <el-dropdown-item command="handleDelete" icon="Delete" v-hasPermi="['ops:server:remove']">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

    <!-- 添加或修改服务器资产对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="serverRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="服务器名称" prop="serverName">
          <el-input v-model="form.serverName" placeholder="请输入服务器名称" />
        </el-form-item>
        <el-form-item label="公网IP" prop="publicIp">
          <el-input v-model="form.publicIp" placeholder="请输入公网IP" />
        </el-form-item>
        <el-form-item label="内网IP" prop="privateIp">
          <el-input v-model="form.privateIp" placeholder="请输入内网IP" />
        </el-form-item>
        <el-form-item label="SSH端口" prop="serverPort">
          <el-input-number v-model="form.serverPort" :min="1" :max="65535" placeholder="请输入SSH端口" />
        </el-form-item>
        <el-form-item label="所属机房" prop="dataCenter">
          <el-input v-model="form.dataCenter" placeholder="请输入所属机房" />
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-radio-group v-model="form.authType">
            <el-radio label="0">密码</el-radio>
            <el-radio label="1">密钥</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="form.authType == '0'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="SSH密钥" prop="privateKey" v-if="form.authType == '1'">
          <el-input v-model="form.privateKey" type="textarea" :rows="4" placeholder="请输入SSH密钥内容" />
          <div class="mt10">
            <el-upload
              action="#"
              :auto-upload="false"
              :on-change="handleFileChange"
              :show-file-list="false"
              accept=".pem,.key,.ppk"
            >
              <el-button type="primary" link icon="Upload">从文件导入</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
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

<script setup name="Server">
import { listServer, getServer, delServer, addServer, updateServer, checkConnection } from "@/api/ops/server";
import { Search, Refresh, Plus, Edit, Delete, Download, Switch, Upload, Monitor, DArrowRight } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus'

const { proxy } = getCurrentInstance();
const router = useRouter();

const serverList = ref([]);
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
    serverName: null,
    publicIp: null,
    privateIp: null,
    status: null
  },
  rules: {
    serverName: [
      { required: true, message: "服务器名称不能为空", trigger: "blur" }
    ],
    publicIp: [
      { required: true, message: "公网IP不能为空", trigger: "blur" }
    ],
    username: [
      { required: true, message: "账号不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询服务器资产列表 */
function getList() {
  loading.value = true;
  listServer(queryParams.value).then(response => {
    serverList.value = response.rows;
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
    serverId: null,
    serverName: null,
    publicIp: null,
    privateIp: null,
    serverPort: 22,
    username: "root",
    password: null,
    authType: "0",
    privateKey: null,
    dataCenter: null,
    status: "0",
    remark: null
  };
  proxy.resetForm("serverRef");
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
  ids.value = selection.map(item => item.serverId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加服务器资产";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const serverId = row.serverId || ids.value
  getServer(serverId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改服务器资产";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["serverRef"].validate(valid => {
    if (valid) {
      if (form.value.serverId != null) {
        updateServer(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addServer(form.value).then(response => {
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
  const serverIds = row.serverId || ids.value;
  proxy.$modal.confirm('是否确认删除服务器资产编号为"' + serverIds + '"的数据项？').then(function() {
    return delServer(serverIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/server/export', {
    ...queryParams.value
  }, `server_${new Date().getTime()}.xlsx`)
}

/** 终端连接 */
function handleTerminal(row) {
  const routeUrl = router.resolve({
    path: `/ops/terminal/index/${row.serverId}`
  });
  window.open(routeUrl.href, '_blank');
}

/** 更多操作 */
function handleCommand(command, row) {
  switch (command) {
    case "handleUpdate":
      handleUpdate(row);
      break;
    case "handleDelete":
      handleDelete(row);
      break;
    default:
      break;
  }
}

/** 检测连接 */
function handleCheck(row) {
  const loading = proxy.$loading({
    lock: true,
    text: '正在进行网络诊断与SSH验证...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  checkConnection(row.serverId).then(response => {
    loading.close();
    const result = response.msg || response.data || ""; // 兼容不同返回格式
    
    // 成功状态判断逻辑优化
    // 只要包含成功标识（内网或公网），即视为成功
    if (result.indexOf("🟢") !== -1 || result.indexOf("🔵") !== -1) {
      ElMessageBox.alert(result, "连接成功", {
        dangerouslyUseHTMLString: true,
        type: 'success',
        confirmButtonText: '确定'
      });
    } else {
      // 失败/警告状态
      const isError = result.indexOf("🔴") !== -1;
      ElMessageBox.alert(result, "连接诊断报告", {
        dangerouslyUseHTMLString: true,
        type: isError ? 'error' : 'warning',
        confirmButtonText: '确定'
      });
    }
  }).catch(() => {
    loading.close();
  });
}

/** 文件上传处理 */
function handleFileChange(file) {
  const reader = new FileReader();
  reader.onload = (e) => {
    form.value.privateKey = e.target.result;
  };
  reader.readAsText(file.raw);
}

getList();
</script>

<style scoped>
.mt10 {
  margin-top: 10px;
}
</style>

