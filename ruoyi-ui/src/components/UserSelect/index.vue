<template>
  <div class="user-select">
    <div class="selected-tags" v-if="selectedUsers.length > 0">
      <el-tag
        v-for="user in selectedUsers"
        :key="user.userId"
        closable
        @close="handleRemove(user)"
        style="margin-right: 5px; margin-bottom: 5px;"
      >
        {{ user.nickName }}
      </el-tag>
      <el-button link type="primary" size="small" @click="handleClear">清空</el-button>
    </div>
    
    <el-button type="primary" plain icon="Plus" @click="open = true">选择接收人</el-button>

    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-row :gutter="20">
        <!-- 部门树 -->
        <el-col :span="6" :xs="24">
          <div class="head-container">
            <el-input
              v-model="deptName"
              placeholder="请输入部门名称"
              clearable
              prefix-icon="Search"
              style="margin-bottom: 20px"
            />
          </div>
          <div class="head-container">
            <el-tree
              :data="deptOptions"
              :props="{ label: 'label', children: 'children' }"
              :expand-on-click-node="false"
              :filter-node-method="filterNode"
              ref="deptTreeRef"
              node-key="id"
              highlight-current
              default-expand-all
              @node-click="handleNodeClick"
              show-checkbox
              @check="handleDeptCheck"
            />
          </div>
        </el-col>
        <!-- 用户列表 -->
        <el-col :span="18" :xs="24">
          <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
            <el-form-item label="用户名称" prop="userName">
              <el-input
                v-model="queryParams.userName"
                placeholder="请输入用户名称"
                clearable
                style="width: 240px"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input
                v-model="queryParams.phonenumber"
                placeholder="请输入手机号码"
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

          <el-table 
            v-loading="loading" 
            :data="userList" 
            @selection-change="handleSelectionChange" 
            ref="userTableRef"
            row-key="userId"
          >
            <el-table-column type="selection" width="50" align="center" :reserve-selection="true" />
            <el-table-column label="用户编号" align="center" key="userId" prop="userId" />
            <el-table-column label="用户名称" align="center" key="userName" prop="userName" :show-overflow-tooltip="true" />
            <el-table-column label="用户昵称" align="center" key="nickName" prop="nickName" :show-overflow-tooltip="true" />
            <el-table-column label="部门" align="center" key="deptName" prop="dept.deptName" :show-overflow-tooltip="true" />
            <el-table-column label="手机号码" align="center" key="phonenumber" prop="phonenumber" width="120" />
            <el-table-column label="状态" align="center" key="status">
              <template #default="scope">
                <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
          />
        </el-col>
      </el-row>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitSelect">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="UserSelect">
import { listUser, deptTreeSelect } from "@/api/system/user";
import useUserStore from '@/store/modules/user'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue']);

const { proxy } = getCurrentInstance();

const open = ref(false);
const title = ref("选择接收人");
const deptName = ref("");
const deptOptions = ref(undefined);
const userList = ref([]);
const total = ref(0);
const loading = ref(true);
const showSearch = ref(true);
const selectedUsers = ref([]); // 当前选中的用户列表（对象）
const userTableRef = ref(null);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    deptId: undefined
  }
});

const { queryParams } = toRefs(data);

watch(() => props.modelValue, (val) => {
    // 这里简单处理，如果外部传入IDs，我们可能无法立刻显示Name，除非预先加载或要求传入Object
    // 假设modelValue是IDs，我们在选中时同步更新
    // 如果需要反显，比较麻烦，因为userList是分页的。
    // 简化：这里主要作为输出组件。如果需要记忆，建议外部传入完整User对象列表。
    // 但为了兼容 receiverId (Long) 或 receiverIds (List<Long>), 我们暂时只维护内部selectedUsers
}, { deep: true });

watch(deptName, val => {
  proxy.$refs["deptTreeRef"].filter(val);
});

/** 查询部门下拉树结构 */
function getDeptTree() {
  deptTreeSelect().then(response => {
    deptOptions.value = response.data;
  });
}

/** 过滤节点 */
const filterNode = (value, data) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};

/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.deptId = data.id;
  handleQuery();
}

/** 部门勾选事件（选择整个部门） */
function handleDeptCheck(data, checkedInfo) {
    // 逻辑：如果勾选了部门，应该加载该部门下所有用户并选中
    // 但由于用户量可能很大，且后端API是分页的，直接前端全选比较困难。
    // 方案：提示用户"已选择部门：X，将包含该部门下所有用户"，在提交时处理。
    // 但需求要求"自动包含该部门下所有用户...已选用户需以标签形式展示"。这意味着必须解析出用户。
    // 我们可以调用一个不分页的接口获取该部门用户? listUser不传pageNum?
    
    // 这里做简化处理：只支持"点击部门加载列表，手动全选当前页"。
    // 或者：实现一个特殊的 "Dept" 类型的Tag，发送时后端解析。
    // 根据需求描述 "已选用户需以标签形式展示"，倾向于解析出用户。
    
    // 实际实现：如果check了部门，去后台查该部门所有userId
    if (checkedInfo.checkedKeys.includes(data.id)) {
        listUser({ deptId: data.id, pageNum: 1, pageSize: 9999 }).then(res => {
            if(res.rows) {
                res.rows.forEach(u => {
                    if (!selectedUsers.value.some(item => item.userId === u.userId)) {
                        selectedUsers.value.push(u);
                    }
                });
                // 更新表格选中状态
                updateTableSelection();
            }
        });
    }
}

/** 查询用户列表 */
function getList() {
  loading.value = true;
  listUser(queryParams.value).then(res => {
    loading.value = false;
    userList.value = res.rows;
    total.value = res.total;
    // 列表加载后，恢复选中状态
    nextTick(() => {
        updateTableSelection();
    });
  });
}

function updateTableSelection() {
    if (!userTableRef.value) return;
    userList.value.forEach(row => {
        const isSelected = selectedUsers.value.some(u => u.userId === row.userId);
        userTableRef.value.toggleRowSelection(row, isSelected);
    });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.deptId = undefined;
  proxy.$refs.deptTreeRef.setCurrentKey(null);
  handleQuery();
}

/** 表格多选框选中数据 */
function handleSelectionChange(selection) {
    // 这里的selection是当前页选中的。
    // 我们需要维护全局selectedUsers。
    // 难点：当翻页时，selection会变。Element Plus table设置 reserve-selection="true" + row-key 可以保留跨页选择。
    
    // 但是我们需要同步 selectedUsers 用于显示Tag。
    // 使用 selection 直接覆盖 selectedUsers 可能会丢失其他页的数据（如果 reserve-selection 不生效或逻辑不对）。
    // 正确做法：监听 select / select-all 事件，或者利用 reserve-selection 
    
    // 简单做法：利用 reserve-selection，handleSelectionChange 返回的是所有页已选的数据（只要row-key唯一）。
    selectedUsers.value = selection;
}

function handleRemove(user) {
    const index = selectedUsers.value.findIndex(u => u.userId === user.userId);
    if (index > -1) {
        selectedUsers.value.splice(index, 1);
        updateTableSelection();
    }
}

function handleClear() {
    selectedUsers.value = [];
    if (userTableRef.value) {
        userTableRef.value.clearSelection();
    }
}

function submitSelect() {
    if (selectedUsers.value.length === 0) {
        proxy.$modal.msgError("请至少选择一个接收人");
        return;
    }
    const ids = selectedUsers.value.map(u => u.userId);
    emit('update:modelValue', ids);
    open.value = false;
}

onMounted(() => {
  getDeptTree();
  getList();
});
</script>

<style scoped>
.selected-tags {
    border: 1px solid #dcdfe6;
    padding: 5px;
    border-radius: 4px;
    min-height: 36px;
    margin-bottom: 10px;
}
</style>
