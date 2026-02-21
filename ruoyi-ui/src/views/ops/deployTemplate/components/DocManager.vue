<template>
  <el-dialog :title="title" v-model="open" width="900px" append-to-body>
    <div class="doc-manager">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="headers"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :data="uploadData"
            multiple
            :limit="3"
          >
            <el-button type="primary" icon="Upload" size="small">上传文档</el-button>
          </el-upload>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            size="small"
            :disabled="multiple"
            @click="handleDelete"
          >删除</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="docList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="文档名称" align="center" prop="docName" :show-overflow-tooltip="true" />
        <el-table-column label="版本" align="center" prop="version" width="100" />
        <el-table-column label="类型" align="center" prop="docType" width="80">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.docType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" align="center" prop="fileSize" width="100">
          <template #default="scope">
            <span>{{ (scope.row.fileSize / 1024).toFixed(2) }} KB</span>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              size="small"
              type="text"
              icon="View"
              @click="handlePreview(scope.row)"
            >预览</el-button>
            <el-button
              size="small"
              type="text"
              icon="Download"
              @click="handleDownload(scope.row)"
            >下载</el-button>
            <el-button
              size="small"
              type="text"
              icon="Delete"
              class="text-danger"
              @click="handleDelete(scope.row)"
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
    </div>
    
    <!-- Preview Dialog -->
    <doc-preview ref="preview" />
  </el-dialog>
</template>

<script>
import { listDoc, delDoc } from "@/api/ops/deployDoc";
import { getToken } from "@/utils/auth";
import DocPreview from "./DocPreview";

export default {
  name: "DocManager",
  components: { DocPreview },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 文档表格数据
      docList: [],
      // 弹出层标题
      title: "文档管理",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        templateId: null,
        version: null,
        docName: null,
        docType: null
      },
      // Upload params
      uploadUrl: import.meta.env.VITE_APP_BASE_API + "/ops/deployDoc/upload",
      headers: { Authorization: "Bearer " + getToken() },
      uploadData: {
        templateId: null,
        version: null
      }
    };
  },
  methods: {
    /** 打开弹窗 */
    show(templateId, version) {
      this.queryParams.templateId = templateId;
      this.queryParams.version = version;
      this.uploadData.templateId = templateId;
      this.uploadData.version = version;
      this.open = true;
      this.getList();
    },
    /** 查询文档列表 */
    getList() {
      this.loading = true;
      listDoc(this.queryParams).then(response => {
        this.docList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.docId)
      this.multiple = !selection.length
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const docIds = row.docId || this.ids;
      this.$modal.confirm('是否确认删除文档编号为"' + docIds + '"的数据项？').then(function() {
        return delDoc(docIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 上传前校检 */
    beforeUpload(file) {
      const isLt50M = file.size / 1024 / 1024 < 50;
      if (!isLt50M) {
        this.$message.error('上传文件大小不能超过 50MB!');
      }
      return isLt50M;
    },
    /** 上传成功处理 */
    handleUploadSuccess(res, file) {
      if (res.code === 200) {
        this.$modal.msgSuccess("上传成功");
        this.getList();
      } else {
        this.$modal.msgError(res.msg);
      }
    },
    /** 上传失败处理 */
    handleUploadError() {
      this.$modal.msgError("上传失败");
    },
    /** 预览按钮操作 */
    handlePreview(row) {
      this.$refs.preview.preview(row);
    },
    /** 下载按钮操作 */
    handleDownload(row) {
      this.$download.resource(row.docPath);
    }
  }
};
</script>

<style scoped>
.doc-manager {
  padding: 10px;
}
</style>
