<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 消息分类菜单 -->
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            @select="handleMenuSelect"
          >
            <el-menu-item index="inbox">
              <el-icon><Message /></el-icon>
              <span>收件箱</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="item" />
            </el-menu-item>
            <el-menu-item index="sent">
              <el-icon><Position /></el-icon>
              <span>已发送</span>
            </el-menu-item>
            <!-- 暂未实现草稿箱和回收站逻辑，预留 -->
            <!-- <el-menu-item index="draft">
              <el-icon><EditPen /></el-icon>
              <span>草稿箱</span>
            </el-menu-item>
            <el-menu-item index="trash">
              <el-icon><Delete /></el-icon>
              <span>回收站</span>
            </el-menu-item> -->
          </el-menu>
        </div>
      </el-col>

      <!-- 消息列表 -->
      <el-col :span="20" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="queryParams.title"
              placeholder="请输入标题"
              clearable
              style="width: 240px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="类型" prop="messageType">
            <el-select v-model="queryParams.messageType" placeholder="消息类型" clearable style="width: 240px">
              <el-option label="通知" value="1" />
              <el-option label="待办" value="2" />
              <el-option label="催办" value="3" />
              <el-option label="完结" value="4" />
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
              v-hasPermi="['system:message:add']"
            >发送消息</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['system:message:remove']"
            >删除</el-button>
          </el-col>
          <el-col :span="1.5">
             <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
              v-hasPermi="['system:message:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <!-- <el-table-column label="消息ID" align="center" prop="messageId" /> -->
          <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true">
            <template #default="scope">
              <el-link @click="handleDetail(scope.row)" :type="scope.row.readStatus === '0' && activeMenu === 'inbox' ? 'primary' : 'default'" :style="scope.row.readStatus === '0' && activeMenu === 'inbox' ? 'font-weight:bold' : ''">
                {{ scope.row.title }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="发送人" align="center" prop="senderId" width="120" />
          <el-table-column label="类型" align="center" prop="messageType" width="100">
             <template #default="scope">
               <el-tag v-if="scope.row.messageType === '1'">通知</el-tag>
               <el-tag v-else-if="scope.row.messageType === '2'" type="warning">待办</el-tag>
               <el-tag v-else-if="scope.row.messageType === '3'" type="danger">催办</el-tag>
               <el-tag v-else-if="scope.row.messageType === '4'" type="success">完结</el-tag>
             </template>
          </el-table-column>
          <el-table-column label="时间" align="center" prop="createTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="readStatus" width="100" v-if="activeMenu === 'inbox'">
             <template #default="scope">
               <el-tag :type="scope.row.readStatus === '1' ? 'info' : 'danger'">{{ scope.row.readStatus === '1' ? '已读' : '未读' }}</el-tag>
             </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:message:remove']">删除</el-button>
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
      </el-col>
    </el-row>

    <!-- 发送消息对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="messageRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收件人" prop="receiverIds">
           <UserSelect v-model="form.receiverIds" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="messageType">
            <el-radio-group v-model="form.messageType">
              <el-radio label="1">通知</el-radio>
              <el-radio label="2">待办</el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item label="内容">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 消息详情对话框 -->
    <el-dialog title="消息详情" v-model="detailOpen" width="800px" append-to-body custom-class="message-detail-dialog">
        <div class="message-detail-container" v-loading="detailLoading">
            <div class="detail-header">
                <h2 class="title">{{ detailForm.title }}</h2>
                <div class="meta-info">
                    <el-tag size="small" :type="getMessageTypeType(detailForm.messageType)">{{ getMessageTypeLabel(detailForm.messageType) }}</el-tag>
                    <span class="meta-item"><el-icon><User /></el-icon> {{ detailForm.senderId }}</span>
                    <span class="meta-item"><el-icon><Clock /></el-icon> {{ parseTime(detailForm.createTime) }}</span>
                </div>
            </div>
            
            <el-divider />
            
            <div class="detail-content" v-html="processedContent" @click="handleContentClick"></div>
            
            <!-- 附件列表 -->
            <div v-if="attachmentList.length > 0" class="attachment-section">
                <div class="section-title"><el-icon><Paperclip /></el-icon> 附件列表 ({{ attachmentList.length }})</div>
                <div class="attachment-list">
                    <div v-for="(file, index) in attachmentList" :key="index" class="attachment-item">
                        <div class="file-icon">
                            <el-icon><Document /></el-icon>
                        </div>
                        <div class="file-info">
                            <div class="file-name" :title="file.name">{{ file.name }}</div>
                            <div class="file-actions">
                                <el-link type="primary" :underline="false" :href="file.url" target="_blank" download>
                                    <el-icon><Download /></el-icon> 下载
                                </el-link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="detailForm.messageType === '2' || detailForm.messageType === '3'" class="action-footer">
                 <el-divider />
                 <el-button type="primary" size="large" @click="handleProcess(detailForm)" style="width: 200px;">
                    <el-icon class="el-icon--left"><Promotion /></el-icon> 前往处理
                 </el-button>
            </div>
        </div>
        
        <!-- 图片预览 -->
        <el-image-viewer
            v-if="showViewer"
            @close="showViewer = false"
            :url-list="previewSrcList"
            :initial-index="initialIndex"
        />
    </el-dialog>
  </div>
</template>

<script setup name="SystemMessage">
import { listMessage, getMessage, delMessage, addMessage, updateMessage, getUnreadCount } from "@/api/system/message";
import UserSelect from "@/components/UserSelect";
import useUserStore from '@/store/modules/user'
import { ElImageViewer } from 'element-plus'

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const messageList = ref([]);
const open = ref(false);
const detailOpen = ref(false);
const loading = ref(true);
const detailLoading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const unreadCount = ref(0);
const activeMenu = ref('inbox');

// 详情页相关状态
const processedContent = ref("");
const attachmentList = ref([]);
const previewSrcList = ref([]);
const showViewer = ref(false);
const initialIndex = ref(0);

const data = reactive({
  form: {},
  detailForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: null,
    messageType: null,
    receiverId: null, 
    senderId: null
  },
  rules: {
    receiverIds: [
      { required: true, message: "接收人不能为空", trigger: "blur", type: "array" }
    ],
    title: [
      { required: true, message: "标题不能为空", trigger: "blur" }
    ],
    content: [
      { required: true, message: "内容不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, detailForm, rules } = toRefs(data);

function getMessageTypeLabel(type) {
    const map = { '1': '通知', '2': '待办', '3': '催办', '4': '完结' };
    return map[type] || '未知';
}

function getMessageTypeType(type) {
    const map = { '1': '', '2': 'warning', '3': 'danger', '4': 'success' };
    return map[type] || 'info';
}

/** 处理消息内容：分离图片和附件 */
function processMessageContent(content) {
    if (!content) return "";
    
    const div = document.createElement('div');
    div.innerHTML = content;
    
    // 1. 提取图片
    const imgs = div.querySelectorAll('img');
    const images = [];
    imgs.forEach((img, index) => {
        images.push(img.src);
        img.setAttribute('data-index', index);
        img.classList.add('clickable-image');
        img.style.cursor = 'zoom-in';
        img.style.maxWidth = '100%';
    });
    previewSrcList.value = images;
    
    // 2. 提取附件 (所有链接暂视为附件，或者根据特定类名/特征)
    // 编辑器生成的链接通常是 <a href="...">name</a>
    const links = div.querySelectorAll('a');
    const files = [];
    links.forEach(a => {
        // 简单过滤：如果href存在
        if (a.href) {
            files.push({
                name: a.innerText || '未知文件',
                url: a.href
            });
            // 从正文中移除文件链接，避免重复显示
            a.remove();
        }
    });
    attachmentList.value = files;
    
    return div.innerHTML;
}

/** 监听内容点击，触发图片预览 */
function handleContentClick(e) {
    if (e.target.tagName === 'IMG' && e.target.classList.contains('clickable-image')) {
        const index = parseInt(e.target.getAttribute('data-index'));
        if (!isNaN(index)) {
            initialIndex.value = index;
            showViewer.value = true;
        }
    }
}

/** 查询站内信列表 */
function getList() {
  loading.value = true;
  // 根据菜单过滤
  if (activeMenu.value === 'inbox') {
      queryParams.value.receiverId = userStore.id; 
      queryParams.value.senderId = null;
  } else if (activeMenu.value === 'sent') {
      queryParams.value.receiverId = null;
      queryParams.value.senderId = userStore.id;
  }

  listMessage(queryParams.value).then(response => {
    messageList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
  
  getUnreadCount().then(res => {
      unreadCount.value = res.data || 0;
  });
}

/** 菜单切换 */
function handleMenuSelect(index) {
    activeMenu.value = index;
    handleQuery();
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    messageId: null,
    title: null,
    content: null,
    messageType: "1",
    receiverIds: []
  };
  proxy.resetForm("messageRef");
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
  ids.value = selection.map(item => item.messageId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "发送消息";
}

/** 详细按钮操作 */
function handleDetail(row) {
    detailLoading.value = true;
    detailOpen.value = true;
    processedContent.value = "";
    attachmentList.value = [];
    previewSrcList.value = [];
    
    getMessage(row.messageId).then(response => {
        detailForm.value = response.data;
        // 处理内容
        processedContent.value = processMessageContent(response.data.content);
        
        detailLoading.value = false;
        
        if (row.readStatus === '0' && activeMenu.value === 'inbox') {
            row.readStatus = '1';
            if(unreadCount.value > 0) unreadCount.value--;
        }
    });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["messageRef"].validate(valid => {
    if (valid) {
       addMessage(form.value).then(response => {
          proxy.$modal.msgSuccess("发送成功");
          open.value = false;
          if (activeMenu.value === 'sent') {
              getList();
          }
       });
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const messageIds = row.messageId || ids.value;
  proxy.$modal.confirm('是否确认删除站内信编号为"' + messageIds + '"的数据项？').then(function() {
    return delMessage(messageIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/message/export', {
    ...queryParams.value
  }, `message_${new Date().getTime()}.xlsx`)
}

/** 待办处理跳转 */
function handleProcess(msg) {
    if (msg.businessId) {
        proxy.$modal.msgSuccess("跳转到流程: " + msg.businessId);
    }
}

onMounted(() => {
    getList();
});
</script>

<style scoped>
.el-badge {
    margin-left: 10px;
}
.message-detail-container {
    padding: 0 20px 20px;
}
.detail-header {
    text-align: center;
    margin-bottom: 20px;
}
.detail-header .title {
    font-size: 22px;
    color: #303133;
    margin: 10px 0;
}
.meta-info {
    color: #909399;
    font-size: 13px;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
}
.meta-item {
    display: flex;
    align-items: center;
    gap: 5px;
}
.detail-content {
    font-size: 14px;
    line-height: 1.8;
    color: #606266;
    margin: 20px 0;
    min-height: 100px;
    word-wrap: break-word;
}
.attachment-section {
    background-color: #f8f9fa;
    border-radius: 4px;
    padding: 15px;
    margin-top: 20px;
}
.section-title {
    font-size: 14px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    gap: 5px;
}
.attachment-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}
.attachment-item {
    display: flex;
    align-items: center;
    background: #fff;
    border: 1px solid #ebeef5;
    padding: 10px;
    border-radius: 4px;
    transition: all 0.3s;
}
.attachment-item:hover {
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
    border-color: #c0c4cc;
}
.file-icon {
    font-size: 24px;
    color: #409EFF;
    margin-right: 12px;
    display: flex;
    align-items: center;
}
.file-info {
    flex: 1;
    overflow: hidden;
}
.file-name {
    font-size: 14px;
    color: #606266;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.action-footer {
    margin-top: 30px;
    text-align: center;
}
</style>
