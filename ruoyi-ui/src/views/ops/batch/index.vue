<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 批量执行 -->
      <el-tab-pane label="批量执行" name="execute">
        <el-row :gutter="20">
          <!-- 左侧：服务器选择 -->
          <el-col :span="12">
            <el-card shadow="never" class="box-card">
              <div slot="header" class="clearfix">
                <span>目标服务器 (已选 {{ selection.length }})</span>
                <el-button style="float: right; padding: 3px 0" type="text" @click="handleSelectAll">全选/反选</el-button>
              </div>
              <el-input v-model="serverQuery.serverName" placeholder="搜索服务器名称/IP" prefix-icon="el-icon-search" style="margin-bottom: 10px;" @input="getList" />
              <el-table
                ref="serverTable"
                :data="serverList"
                @selection-change="handleSelectionChange"
                height="500"
                v-loading="loadingServer"
              >
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column label="服务器名称" prop="serverName" />
                <el-table-column label="IP地址" prop="publicIp">
                  <template #default="scope">
                    {{ scope.row.publicIp || scope.row.privateIp }}
                  </template>
                </el-table-column>
                <el-table-column label="分组" prop="groupId" />
              </el-table>
            </el-card>
          </el-col>

          <!-- 右侧：任务配置 -->
          <el-col :span="12">
            <el-card shadow="never">
              <div slot="header">
                <span>任务配置</span>
              </div>
              <el-form ref="taskForm" :model="taskForm" :rules="taskRules" label-width="80px">
                <el-form-item label="任务名称" prop="taskName">
                  <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
                </el-form-item>
                <el-form-item label="任务类型" prop="taskType">
                  <el-radio-group v-model="taskForm.taskType">
                    <el-radio label="1">命令执行</el-radio>
                    <el-radio label="2">文件分发</el-radio>
                  </el-radio-group>
                </el-form-item>

                <!-- 命令执行配置 -->
                <div v-if="taskForm.taskType === '1'">
                  <el-form-item label="命令内容" prop="commandContent">
                    <div style="margin-bottom: 5px;">
                      <el-select v-model="selectedTemplate" placeholder="选择常用模板" @change="handleTemplateChange" size="small">
                        <el-option
                          v-for="item in templateList"
                          :key="item.templateId"
                          :label="item.templateName"
                          :value="item.templateId"
                        />
                      </el-select>
                    </div>
                    <el-input
                      type="textarea"
                      :rows="10"
                      placeholder="请输入Shell命令"
                      v-model="taskForm.commandContent"
                    />
                  </el-form-item>
                </div>

                <!-- 文件分发配置 -->
                <div v-if="taskForm.taskType === '2'">
                  <el-form-item label="源文件" prop="sourceFile">
                    <el-upload
                      class="upload-demo"
                      :action="uploadUrl"
                      :headers="headers"
                      :on-success="handleUploadSuccess"
                      :on-change="handleUploadChange"
                      :on-error="handleError"
                      :on-remove="handleRemove"
                      :limit="1"
                      v-model:file-list="fileList"
                      :show-file-list="true"
                    >
                      <el-button size="small" type="primary">点击上传</el-button>
                      <template #tip>
                        <div class="el-upload__tip">文件将先上传至中转服务器</div>
                      </template>
                    </el-upload>
                    <!-- 隐藏的输入框用于表单校验 -->
                    <el-input v-model="taskForm.sourceFile" style="display:none" />
                  </el-form-item>
                  <el-form-item label="目标路径" prop="destPath">
                    <el-input v-model="taskForm.destPath" placeholder="例如: /tmp/file.txt" />
                  </el-form-item>
                </div>

                <el-form-item>
                  <el-button type="primary" @click="submitTask" :loading="submitting">立即执行</el-button>
                  <el-button @click="resetTaskForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- Tab 2: 任务记录 -->
      <el-tab-pane label="任务记录" name="history">
        <el-form :inline="true" :model="historyQuery" size="small">
          <el-form-item label="任务名称">
            <el-input v-model="historyQuery.taskName" placeholder="请输入任务名称" clearable @keyup.enter.native="getHistoryList" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getHistoryList">搜索</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loadingHistory" :data="historyList">
          <el-table-column label="任务ID" prop="taskId" width="80" />
          <el-table-column label="任务名称" prop="taskName" />
          <el-table-column label="类型" prop="taskType">
            <template #default="scope">
              <el-tag v-if="scope.row.taskType === '1'">命令</el-tag>
              <el-tag v-else type="success">文件</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="总主机" prop="totalHost" width="80" />
          <el-table-column label="成功/失败" width="140" align="center">
            <template #default="scope">
              <el-tag type="success" effect="plain" size="small">{{ scope.row.successHost }}</el-tag>
              <span style="margin: 0 5px; color: #dcdfe6">/</span>
              <el-tag type="danger" effect="plain" size="small">{{ scope.row.failHost }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status">
            <template #default="scope">
              <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="160" />
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="historyTotal>0"
          :total="historyTotal"
          v-model:page="historyQuery.pageNum"
          v-model:limit="historyQuery.pageSize"
          @pagination="getHistoryList"
        />
      </el-tab-pane>

      <!-- Tab 3: 模板管理 -->
      <el-tab-pane label="模板管理" name="template">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAddTemplate">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multipleTemplate" @click="handleDeleteTemplate">删除</el-button>
          </el-col>
        </el-row>

        <el-table v-loading="loadingTemplate" :data="templateList" @selection-change="handleTemplateSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="模板名称" prop="templateName" />
          <el-table-column label="内容" prop="commandContent" :show-overflow-tooltip="true" />
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEditTemplate(scope.row)">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteTemplate(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 任务详情对话框（Master-Detail 布局优化） -->
    <el-dialog title="任务执行详情" v-model="openDetail" width="900px" append-to-body custom-class="task-detail-dialog">
      <div class="detail-container">
        <!-- 左侧：服务器列表 -->
        <div class="detail-sidebar">
          <div class="sidebar-header">
            <span>服务器列表 ({{ detailList.length }})</span>
          </div>
          <ul class="server-list">
            <li 
              v-for="(item, index) in detailList" 
              :key="index"
              :class="{ 'active': currentDetailIndex === index }"
              @click="handleSelectServer(index)"
            >
              <div class="server-item">
                <span class="server-name" :title="item.serverName">{{ item.serverName }}</span>
                <el-tag size="small" :type="statusType(item.status)" effect="dark" class="status-tag">
                  <i :class="statusIcon(item.status)"></i>
                </el-tag>
              </div>
              <div class="server-ip">{{ item.serverIp }}</div>
            </li>
          </ul>
        </div>

        <!-- 右侧：执行日志 -->
        <div class="detail-content">
          <div class="content-header">
            <span>执行日志</span>
            <span v-if="currentDetail && currentDetail.startTime && currentDetail.endTime" class="exec-time">
              耗时: {{ (new Date(currentDetail.endTime) - new Date(currentDetail.startTime)) / 1000 }}s
            </span>
          </div>
          <div class="log-console">
            <pre v-if="currentDetail && currentDetail.executionLog">{{ currentDetail.executionLog }}</pre>
            <div v-else class="empty-log">暂无日志</div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 模板新增/修改对话框 -->
    <el-dialog :title="templateTitle" v-model="openTemplate" width="500px" append-to-body>
      <el-form ref="templateForm" :model="templateForm" :rules="templateRules" label-width="80px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="命令内容" prop="commandContent">
          <el-input v-model="templateForm.commandContent" type="textarea" :rows="5" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitTemplate">确 定</el-button>
        <el-button @click="openTemplate = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listServer } from "@/api/ops/server";
import { listTemplate, addTemplate, updateTemplate, delTemplate, listTask, getTask, listTaskDetail, executeTask } from "@/api/ops/batch";
import { getToken } from "@/utils/auth";

export default {
  name: "BatchOps",
  data() {
    return {
      activeTab: "execute",
      // 服务器数据
      loadingServer: false,
      serverList: [],
      serverQuery: { serverName: "" },
      selection: [],
      
      // 任务配置
      taskForm: {
        taskName: "",
        taskType: "1",
        commandContent: "",
        sourceFile: "",
        destPath: ""
      },
      taskRules: {
        taskName: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
        commandContent: [{ required: true, message: "命令内容不能为空", trigger: "blur" }],
        sourceFile: [{ required: true, message: "请上传文件", trigger: "change" }],
        destPath: [{ required: true, message: "目标路径不能为空", trigger: "blur" }]
      },
      submitting: false,
      selectedTemplate: null,
      fileList: [],
      
      // 文件上传
      uploadUrl: import.meta.env.VITE_APP_BASE_API + "/common/upload",
      headers: { Authorization: "Bearer " + getToken() },

      // 任务历史
      loadingHistory: false,
      historyList: [],
      historyTotal: 0,
      historyQuery: {
        pageNum: 1,
        pageSize: 10,
        taskName: undefined
      },

      // 任务详情
      openDetail: false,
      loadingDetail: false,
      detailList: [],
      currentDetailIndex: 0,
      
      // 模板管理
      loadingTemplate: false,
      templateList: [],
      multipleTemplate: true,
      ids: [],
      openTemplate: false,
      templateTitle: "",
      templateForm: {},
      templateRules: {
        templateName: [{ required: true, message: "名称不能为空", trigger: "blur" }],
        commandContent: [{ required: true, message: "内容不能为空", trigger: "blur" }]
      }
    };
  },
  computed: {
    currentDetail() {
      if (this.detailList && this.detailList.length > 0) {
        return this.detailList[this.currentDetailIndex];
      }
      return null;
    }
  },
  created() {
    this.getList();
    this.getTemplateList();
    this.getHistoryList();
  },
  methods: {
    // --- 服务器列表 ---
    getList() {
      this.loadingServer = true;
      listServer(this.serverQuery).then(response => {
        this.serverList = response.rows;
        this.loadingServer = false;
      });
    },
    handleSelectionChange(selection) {
      this.selection = selection;
    },
    handleSelectAll() {
      this.$refs.serverTable.toggleAllSelection();
    },

    // --- 任务配置 ---
    handleTemplateChange(val) {
      const template = this.templateList.find(item => item.templateId === val);
      if (template) {
        this.taskForm.commandContent = template.commandContent;
      }
    },
    handleUploadChange(file, fileList) {
      this.fileList = fileList.slice();
    },
    handleUploadSuccess(res, file, fileList) {
      const isOk = Number(res.code) === 200;
      if (isOk) {
        file.status = "success";
        this.taskForm.sourceFile = res.fileName;
        this.$message.success("上传成功: " + (res.originalFilename || file.name));
        this.fileList = fileList.slice();
        return;
      }
      this.$message.error(res.msg || "文件上传失败");
      this.fileList = fileList.filter(item => item.uid !== file.uid);
    },
    handleError(err, file, fileList) {
      this.$message.error("文件上传失败");
      this.fileList = fileList.filter(item => item.uid !== file.uid);
    },
    handleRemove(file, fileList) {
      this.taskForm.sourceFile = "";
      this.fileList = fileList.slice();
    },
    submitTask() {
      if (this.selection.length === 0) {
        this.$message.warning("请至少选择一台服务器");
        return;
      }
      this.$refs.taskForm.validate(valid => {
        if (valid) {
          this.submitting = true;
          const serverIds = this.selection.map(item => item.serverId);
          const data = {
            ...this.taskForm,
            serverIds: serverIds
          };
          executeTask(data).then(response => {
            this.$message.success("任务已提交");
            this.submitting = false;
            this.activeTab = "history";
            this.getHistoryList();
            this.resetTaskForm();
          }).catch(() => {
            this.submitting = false;
          });
        }
      });
    },
    resetTaskForm() {
      this.taskForm = {
        taskName: "",
        taskType: "1",
        commandContent: "",
        sourceFile: "",
        destPath: ""
      };
      this.fileList = [];
      this.$refs.serverTable.clearSelection();
    },

    // --- 任务历史 ---
    getHistoryList() {
      this.loadingHistory = true;
      listTask(this.historyQuery).then(response => {
        this.historyList = response.rows;
        this.historyTotal = response.total;
        this.loadingHistory = false;
      });
    },
    statusType(status) {
      if (status === '0') return 'info';
      if (status === '1') return 'warning';
      if (status === '2') return 'success';
      return 'danger';
    },
    statusText(status) {
      const map = { '0': '等待', '1': '执行中', '2': '成功', '3': '失败', '4': '部分成功' };
      return map[status];
    },
    statusIcon(status) {
      const map = { '0': 'el-icon-time', '1': 'el-icon-loading', '2': 'el-icon-check', '3': 'el-icon-close' };
      return map[status] || 'el-icon-question';
    },
    handleDetail(row) {
      this.openDetail = true;
      this.loadingDetail = true;
      this.currentDetailIndex = 0;
      listTaskDetail({ taskId: row.taskId }).then(response => {
        this.detailList = response.rows;
        this.loadingDetail = false;
      });
    },
    handleSelectServer(index) {
      this.currentDetailIndex = index;
    },
    // viewLog(row) { ... } // Removed


    // --- 模板管理 ---
    getTemplateList() {
      this.loadingTemplate = true;
      listTemplate().then(response => {
        this.templateList = response.rows;
        this.loadingTemplate = false;
      });
    },
    handleTemplateSelectionChange(selection) {
      this.ids = selection.map(item => item.templateId);
      this.multipleTemplate = !selection.length;
    },
    handleAddTemplate() {
      this.templateForm = {};
      this.openTemplate = true;
      this.templateTitle = "新增模板";
    },
    handleEditTemplate(row) {
      this.templateForm = { ...row };
      this.openTemplate = true;
      this.templateTitle = "修改模板";
    },
    handleDeleteTemplate(row) {
      const ids = row.templateId || this.ids;
      this.$confirm('是否确认删除?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return delTemplate(ids);
      }).then(() => {
        this.getTemplateList();
        this.$message.success("删除成功");
      });
    },
    submitTemplate() {
      this.$refs.templateForm.validate(valid => {
        if (valid) {
          if (this.templateForm.templateId) {
            updateTemplate(this.templateForm).then(() => {
              this.$message.success("修改成功");
              this.openTemplate = false;
              this.getTemplateList();
            });
          } else {
            addTemplate(this.templateForm).then(() => {
              this.$message.success("新增成功");
              this.openTemplate = false;
              this.getTemplateList();
            });
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}

/* 任务详情弹窗样式优化 */
.task-detail-dialog .el-dialog__body {
  padding: 0 !important;
}

.detail-container {
  display: flex;
  height: 500px;
  border-top: 1px solid #eee;
}

.detail-sidebar {
  width: 280px;
  border-right: 1px solid #eee;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 12px 15px;
  font-weight: bold;
  border-bottom: 1px solid #eee;
  background-color: #fff;
  color: #606266;
}

.server-list {
  flex: 1;
  overflow-y: auto;
  margin: 0;
  padding: 0;
  list-style: none;
}

.server-list li {
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.server-list li:hover {
  background-color: #ecf5ff;
}

.server-list li.active {
  background-color: #fff;
  border-left: 3px solid #409EFF;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.server-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.server-name {
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.server-ip {
  font-size: 12px;
  color: #909399;
}

.detail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #1e1e1e; /* Dark theme for console */
}

.content-header {
  padding: 10px 20px;
  background-color: #2d2d2d;
  color: #ccc;
  font-size: 13px;
  border-bottom: 1px solid #444;
  display: flex;
  justify-content: space-between;
}

.log-console {
  flex: 1;
  overflow: auto;
  padding: 15px;
  color: #47c032; /* Console green */
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.5;
}

.log-console pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.empty-log {
  color: #666;
  text-align: center;
  margin-top: 100px;
}
</style>
