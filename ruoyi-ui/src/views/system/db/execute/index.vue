<template>
  <div class="app-container">
    <splitpanes class="default-theme" style="height: 100%">
      <!-- 左侧数据库资源树 -->
      <pane :size="sidebarSize" min-size="0" v-if="sidebarVisible">
        <el-card class="box-card db-explorer-card" style="height: 100%; display: flex; flex-direction: column;">
          <template #header>
            <div class="db-explorer-header">
              <div class="db-explorer-title">
                <el-icon><Collection /></el-icon>
                <span>数据库资源</span>
              </div>
              <el-select v-model="connId" placeholder="请选择连接" size="small" class="conn-select" @change="handleConnChange">
                <el-option
                  v-for="item in connList"
                  :key="item.connId"
                  :label="item.connName"
                  :value="item.connId"
                />
              </el-select>
            </div>
          </template>
          <div v-loading="tableLoading" class="db-tree-container">
            <el-input
              v-model="filterText"
              placeholder="搜索资源..."
              size="small"
              clearable
              prefix-icon="Search"
              class="db-search-input"
            />
            <el-tree
              :data="dbTreeData"
              :props="defaultProps"
              :filter-node-method="filterNode"
              ref="treeRef"
              highlight-current
              :expand-on-click-node="false"
              @node-click="handleNodeClick"
              @node-dblclick="handleNodeDblClick"
              class="db-resource-tree"
            >
              <template #default="{ node, data }">
                <span class="custom-tree-node" :class="data.type" @contextmenu.prevent="handleContextMenu($event, data)">
                  <el-icon v-if="data.type === 'database'" class="node-icon db-icon"><Coin /></el-icon>
                  <el-icon v-else-if="data.type === 'category'" class="node-icon folder-icon"><Folder /></el-icon>
                  <el-icon v-else-if="data.type === 'TABLE'" class="node-icon table-icon"><Grid /></el-icon>
                  <el-icon v-else-if="data.type === 'VIEW'" class="node-icon view-icon"><View /></el-icon>
                  <el-icon v-else-if="data.type === 'PROCEDURE'" class="node-icon proc-icon"><SetUp /></el-icon>
                  <el-icon v-else-if="data.type === 'FUNCTION'" class="node-icon func-icon"><Operation /></el-icon>
                  <el-icon v-else class="node-icon"><Document /></el-icon>
                  <span class="node-label" :title="data.label">{{ data.label }}</span>
                  <span v-if="data.count !== undefined" class="node-count">({{ data.count }})</span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-card>
      </pane>
      
      <!-- 右侧SQL执行 -->
      <pane :size="100 - (sidebarVisible ? sidebarSize : 0)">
        <div class="right-pane" :class="{ 'fullscreen-mode': isFullscreen }">
          
          <!-- 全屏模式下的布局：Splitpanes 上下分割 -->
          <splitpanes horizontal class="default-theme" style="height: 100%" v-if="isFullscreen">
            <!-- 上部分：SQL编辑器 -->
            <pane size="50">
              <el-card class="box-card editor-card" style="height: 100%; border: none;">
                <template #header>
                  <div class="clearfix">
                    <el-button type="text" icon="Operation" @click="toggleSidebar" v-if="!isFullscreen" style="margin-right: 10px; font-size: 16px;">
                      {{ sidebarVisible ? '收起左侧' : '展开左侧' }}
                    </el-button>
                    <span>SQL编辑器</span>
                    <div style="float: right;">
                      <el-button type="primary" link icon="VideoPlay" @click="handleRun">执行 (F9)</el-button>
                      <el-button type="info" link icon="Brush" @click="handleFormat">格式化</el-button>
                      <el-button type="warning" link icon="FullScreen" @click="toggleFullscreen">{{ isFullscreen ? '退出全屏' : '全屏' }}</el-button>
                    </div>
                  </div>
                </template>
                <div style="height: 100%; border: 1px solid #dcdfe6; position: relative;">
                  <vue-monaco-editor
                    v-model:value="sqlContent"
                    theme="vs"
                    language="sql"
                    :options="editorOptions"
                    @mount="handleEditorMount"
                  />
                </div>
              </el-card>
            </pane>
            
            <!-- 下部分：执行结果 -->
            <pane size="50">
              <el-card class="box-card result-card" style="height: 100%; display: flex; flex-direction: column; overflow: hidden; border: none; border-top: 1px solid #eee;">
                <template #header>
                  <div class="clearfix">
                    <span>执行结果</span>
                    <span v-if="costTime" style="margin-left: 20px; font-size: 12px; color: #909399;">耗时: {{ costTime }} ms</span>
                    <span v-if="tableData.length > 0" style="margin-left: 20px; font-size: 12px; color: #909399;">共 {{ total }} 条记录</span>
                  </div>
                </template>
                <div style="flex: 1; overflow: hidden; display: flex; flex-direction: column;">
                  <el-table
                    v-loading="executeLoading"
                    :data="pagedTableData"
                    border
                    height="100%"
                    style="width: 100%"
                    highlight-current-row
                    :header-cell-style="{background:'#f8f8f9'}"
                  >
                    <el-table-column
                      v-for="key in tableHeader"
                      :key="key"
                      :prop="key"
                      :label="key"
                      sortable
                      show-overflow-tooltip
                      min-width="150"
                    >
                      <template #default="scope">
                        <span @dblclick="handleCellDblClick(scope.row, key)">{{ scope.row[key] }}</span>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div style="margin-top: 10px; text-align: right;">
                    <el-pagination
                      v-show="total > 0"
                      v-model:current-page="currentPage"
                      v-model:page-size="pageSize"
                      :page-sizes="[10, 20, 50, 100, 500]"
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="total"
                      @size-change="handleSizeChange"
                      @current-change="handleCurrentChange"
                    />
                  </div>
                </div>
              </el-card>
            </pane>
          </splitpanes>

          <!-- 非全屏模式下的布局：固定高度编辑器 + 结果表格 -->
          <div v-else style="height: 100%; display: flex; flex-direction: column; padding-left: 10px;">
            <!-- SQL编辑器区域 -->
            <el-card class="box-card" style="margin-bottom: 10px; flex-shrink: 0; height: 300px;">
              <template #header>
                <div class="clearfix">
                  <el-button type="text" icon="Operation" @click="toggleSidebar" style="margin-right: 10px; font-size: 16px;">
                    {{ sidebarVisible ? '收起左侧' : '展开左侧' }}
                  </el-button>
                  <span>SQL编辑器</span>
                  <div style="float: right;">
                    <el-button type="primary" link icon="VideoPlay" @click="handleRun">执行 (F9)</el-button>
                    <el-button type="info" link icon="Brush" @click="handleFormat">格式化</el-button>
                    <el-button type="warning" link icon="FullScreen" @click="toggleFullscreen">全屏</el-button>
                  </div>
                </div>
              </template>
              <div style="height: 100%; border: 1px solid #dcdfe6; position: relative;">
                <vue-monaco-editor
                  v-model:value="sqlContent"
                  theme="vs"
                  language="sql"
                  :options="editorOptions"
                  @mount="handleEditorMount"
                />
              </div>
            </el-card>

            <!-- 执行结果区域 -->
            <el-card class="box-card" style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
              <template #header>
                <div class="clearfix">
                  <span>执行结果</span>
                  <span v-if="costTime" style="margin-left: 20px; font-size: 12px; color: #909399;">耗时: {{ costTime }} ms</span>
                  <span v-if="tableData.length > 0" style="margin-left: 20px; font-size: 12px; color: #909399;">共 {{ total }} 条记录</span>
                </div>
              </template>
              
              <div style="flex: 1; overflow: hidden; display: flex; flex-direction: column;">
                <el-table
                  v-loading="executeLoading"
                  :data="pagedTableData"
                  border
                  height="100%"
                  style="width: 100%"
                  highlight-current-row
                  :header-cell-style="{background:'#f8f8f9'}"
                >
                  <el-table-column
                    v-for="key in tableHeader"
                    :key="key"
                    :prop="key"
                    :label="key"
                    sortable
                    show-overflow-tooltip
                    min-width="150"
                  >
                    <template #default="scope">
                      <span @dblclick="handleCellDblClick(scope.row, key)">{{ scope.row[key] }}</span>
                    </template>
                  </el-table-column>
                </el-table>
                
                <div style="margin-top: 10px; text-align: right;">
                  <el-pagination
                    v-show="total > 0"
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100, 500]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                  />
                </div>
              </div>
            </el-card>
          </div>

        </div>
      </pane>
    </splitpanes>

    <!-- 单元格编辑对话框 (仅展示，不支持保存回DB) -->
    <el-dialog title="单元格内容" v-model="cellDialogVisible" width="500px">
      <el-input
        v-model="currentCellValue"
        type="textarea"
        :rows="5"
        readonly
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cellDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 结构查看面板 -->
    <el-drawer
      v-model="structurePanelVisible"
      :title="getStructureTitle()"
      direction="rtl"
      size="60%"
      :before-close="closeStructurePanel"
    >
      <template #header>
        <div class="structure-header">
          <span>{{ getStructureTitle() }}</span>
          <div class="structure-actions" v-if="structureType === 'procedure' || structureType === 'function'">
            <template v-if="!isEditMode">
              <el-button type="primary" link :icon="Edit" @click="startEdit">编辑</el-button>
            </template>
            <template v-else>
              <el-button type="success" link :icon="Check" @click="saveEdit">保存</el-button>
              <el-button type="info" link :icon="Close" @click="cancelEdit">取消</el-button>
            </template>
          </div>
        </div>
      </template>
      
      <div v-loading="structureLoading" class="structure-panel">
        <!-- 表结构 -->
        <div v-if="structureType === 'table'" class="table-structure">
          <div class="structure-section" v-if="structureData.tableComment">
            <div class="section-title">表注释</div>
            <div class="section-content comment-content">{{ structureData.tableComment }}</div>
          </div>
          
          <div class="structure-section">
            <div class="section-title">列信息 <el-tag size="small" type="info">{{ structureData.columns?.length || 0 }} 列</el-tag></div>
            <el-table :data="structureData.columns" border size="small" style="width: 100%">
              <el-table-column prop="position" label="#" width="50" />
              <el-table-column prop="name" label="列名" min-width="120" />
              <el-table-column prop="type" label="数据类型" min-width="100" />
              <el-table-column prop="size" label="长度" width="70" />
              <el-table-column label="可空" width="60" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.nullable" size="small" type="info">是</el-tag>
                  <el-tag v-else size="small" type="success">否</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="defaultValue" label="默认值" min-width="100" />
              <el-table-column prop="remarks" label="注释" min-width="150" />
            </el-table>
          </div>

          <div class="structure-section" v-if="structureData.primaryKeys && structureData.primaryKeys.length > 0">
            <div class="section-title">主键 <el-tag size="small" type="success">{{ structureData.primaryKeys.length }} 个</el-tag></div>
            <el-tag 
              v-for="pk in structureData.primaryKeys" 
              :key="pk"
              type="success"
              style="margin-right: 8px; margin-bottom: 5px;"
            >
              {{ pk }}
            </el-tag>
          </div>

          <div class="structure-section" v-if="structureData.indexes && structureData.indexes.length > 0">
            <div class="section-title">索引 <el-tag size="small" type="warning">{{ structureData.indexes.length }} 个</el-tag></div>
            <el-table :data="structureData.indexes" border size="small" style="width: 100%">
              <el-table-column prop="name" label="索引名" min-width="120" />
              <el-table-column label="唯一" width="60" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.unique" size="small" type="success">是</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column label="列">
                <template #default="scope">
                  <el-tag 
                    v-for="col in scope.row.columns" 
                    :key="col"
                    size="small"
                    style="margin-right: 5px;"
                  >
                    {{ col }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 存储过程定义 -->
        <div v-else-if="structureType === 'procedure'" class="procedure-definition">
          <div class="structure-section">
            <div class="section-title">存储过程: {{ structureData.name }}</div>
            <div class="meta-info">
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="SQL模式">{{ structureData.sqlMode }}</el-descriptions-item>
                <el-descriptions-item label="字符集">{{ structureData.characterSetClient }}</el-descriptions-item>
                <el-descriptions-item label="连接排序">{{ structureData.collationConnection }}</el-descriptions-item>
                <el-descriptions-item label="数据库排序">{{ structureData.databaseCollation }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          
          <div class="structure-section">
            <div class="section-title">定义</div>
            <div v-if="!isEditMode" class="code-content">
              <pre><code>{{ structureData.definition }}</code></pre>
            </div>
            <div v-else class="editor-container">
              <vue-monaco-editor
                v-model:value="editedDefinition"
                theme="vs-dark"
                language="sql"
                :options="{
                  automaticLayout: true,
                  minimap: { enabled: false },
                  fontSize: 13,
                  scrollBeyondLastLine: false
                }"
                @mount="handleStructureEditorMount"
              />
            </div>
          </div>
        </div>

        <!-- 函数定义 -->
        <div v-else-if="structureType === 'function'" class="function-definition">
          <div class="structure-section">
            <div class="section-title">函数: {{ structureData.name }}</div>
            <div class="meta-info">
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="SQL模式">{{ structureData.sqlMode }}</el-descriptions-item>
                <el-descriptions-item label="字符集">{{ structureData.characterSetClient }}</el-descriptions-item>
                <el-descriptions-item label="连接排序">{{ structureData.collationConnection }}</el-descriptions-item>
                <el-descriptions-item label="数据库排序">{{ structureData.databaseCollation }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          
          <div class="structure-section">
            <div class="section-title">定义</div>
            <div v-if="!isEditMode" class="code-content">
              <pre><code>{{ structureData.definition }}</code></pre>
            </div>
            <div v-else class="editor-container">
              <vue-monaco-editor
                v-model:value="editedDefinition"
                theme="vs-dark"
                language="sql"
                :options="{
                  automaticLayout: true,
                  minimap: { enabled: false },
                  fontSize: 13,
                  scrollBeyondLastLine: false
                }"
                @mount="handleStructureEditorMount"
              />
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 右键菜单 -->
    <div 
      v-if="contextMenuVisible" 
      class="context-menu"
      :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
      @click.stop
    >
      <div class="context-menu-item" @click="handleViewStructure">
        <el-icon><View /></el-icon>
        <span>查看结构</span>
      </div>
      <div class="context-menu-item" @click="handleGenerateSelect">
        <el-icon><VideoPlay /></el-icon>
        <span>生成SELECT语句</span>
      </div>
    </div>
  </div>
</template>

<script setup name="DbExecute">
import { listConn, getSchema, getMetadata, executeSelect, executeUpdate, getTableStructure, getProcedureDefinition, getFunctionDefinition, updateProcedure, updateFunction } from "@/api/system/db";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import { format } from 'sql-formatter';
import * as monaco from 'monaco-editor';
import { Coin, Folder, Grid, View, SetUp, Operation, Document, Collection, Edit, Check, Close } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

// 侧边栏
const sidebarVisible = ref(true);
const sidebarSize = ref(22);

// 全屏
const isFullscreen = ref(false);

// 连接与数据库资源
const connId = ref(null);
const connList = ref([]);
const tableLoading = ref(false);
const dbTreeData = ref([]);
const dbMetadata = ref({ tables: [], functions: [] });
const filterText = ref("");
const treeRef = ref(null);
const defaultProps = ref({
  children: "children",
  label: "label"
});

// SQL编辑器
const sqlContent = ref("");
const editorOptions = ref({
  automaticLayout: true,
  formatOnType: true,
  formatOnPaste: true,
  minimap: { enabled: false },
  fontSize: 14,
  scrollBeyondLastLine: false
});
let monacoInstance = null;
let monacoRef = null; // 保存 monaco 实例
let completionProvider = null;

// 执行结果
const executeLoading = ref(false);
const tableData = ref([]); // 所有数据
const tableHeader = ref([]);
const costTime = ref(null);

// 分页
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

// 单元格查看
const cellDialogVisible = ref(false);
const currentCellValue = ref("");

// 结构查看
const structurePanelVisible = ref(false);
const structureLoading = ref(false);
const structureData = ref({});
const structureType = ref(""); // 'table', 'procedure', 'function'

// 编辑模式
const isEditMode = ref(false);
const editedDefinition = ref("");
let structureEditor = null;
let structureEditorRef = null;

// 右键菜单
const contextMenuVisible = ref(false);
const contextMenuPosition = ref({ x: 0, y: 0 });
const contextMenuData = ref(null);

// 计算当前页数据
const pagedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end);
});

// 监听过滤文本
watch(filterText, (val) => {
  treeRef.value.filter(val);
});

function toggleSidebar() {
  sidebarVisible.value = !sidebarVisible.value;
}

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value;
}

function getConnList() {
  listConn().then(response => {
    connList.value = response.rows;
    if (connList.value.length > 0) {
      connId.value = connList.value[0].connId;
      handleConnChange();
    }
  });
}

function handleConnChange() {
  if (!connId.value) return;
  tableLoading.value = true;
  
  // 并行获取数据库结构和元数据
  Promise.all([
    getSchema(connId.value),
    getMetadata(connId.value)
  ]).then(([schemaRes, metadataRes]) => {
    const schema = schemaRes.data;
    
    // 构建树形数据
    dbTreeData.value = buildDbTreeData(schema);
    
    // 处理元数据（编辑器提示）
    dbMetadata.value = metadataRes.data;
    
    tableLoading.value = false;
    registerAutocomplete();
  }).catch((err) => {
    console.error("加载数据失败", err);
    tableLoading.value = false;
    registerAutocomplete();
  });
}

// 构建数据库资源树形数据
function buildDbTreeData(schema) {
  const treeData = [];
  
  // 根节点：数据库
  const dbNode = {
    label: schema.dbName || '数据库',
    type: 'database',
    children: []
  };
  
  // 表分类
  if (schema.tables && schema.tables.length > 0) {
    const tablesNode = {
      label: '表',
      type: 'category',
      count: schema.tables.length,
      children: schema.tables.map(t => ({
        label: t.name,
        type: 'TABLE',
        remarks: t.remarks,
        icon: 'table'
      }))
    };
    dbNode.children.push(tablesNode);
  }
  
  // 视图分类
  if (schema.views && schema.views.length > 0) {
    const viewsNode = {
      label: '视图',
      type: 'category',
      count: schema.views.length,
      children: schema.views.map(v => ({
        label: v.name,
        type: 'VIEW',
        remarks: v.remarks,
        icon: 'view'
      }))
    };
    dbNode.children.push(viewsNode);
  }
  
  // 存储过程分类
  if (schema.procedures && schema.procedures.length > 0) {
    const procsNode = {
      label: '存储过程',
      type: 'category',
      count: schema.procedures.length,
      children: schema.procedures.map(p => ({
        label: p.name,
        type: 'PROCEDURE',
        remarks: p.remarks,
        icon: 'procedure'
      }))
    };
    dbNode.children.push(procsNode);
  }
  
  // 函数分类
  if (schema.functions && schema.functions.length > 0) {
    const funcsNode = {
      label: '函数',
      type: 'category',
      count: schema.functions.length,
      children: schema.functions.map(f => ({
        label: f.name,
        type: 'FUNCTION',
        remarks: f.remarks,
        icon: 'function'
      }))
    };
    dbNode.children.push(funcsNode);
  }
  
  treeData.push(dbNode);
  return treeData;
}

function registerAutocomplete() {
  // 必须要有 monaco 实例才能注册
  if (!monacoRef) return;

  // 如果之前注册过，先销毁
  if (completionProvider) {
    completionProvider.dispose();
  }

  completionProvider = monacoRef.languages.registerCompletionItemProvider('sql', {
    triggerCharacters: ['.', ' '],
    provideCompletionItems: (model, position) => {
      const suggestions = [];
      const { tables, functions } = dbMetadata.value || { tables: [], functions: [] };
      
      const word = model.getWordUntilPosition(position);
      const range = {
        startLineNumber: position.lineNumber,
        endLineNumber: position.lineNumber,
        startColumn: word.startColumn,
        endColumn: word.endColumn
      };

      const textUntilPosition = model.getValueInRange({
        startLineNumber: 1,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column
      });

      // 简单解析上下文
      const currentLine = model.getLineContent(position.lineNumber);
      const beforeCursor = currentLine.substring(0, position.column - 1);
      const lastChar = beforeCursor.charAt(beforeCursor.length - 1);

      // 如果是 . 触发，尝试解析别名或表名
      if (lastChar === '.') {
        const match = beforeCursor.match(/([a-zA-Z0-9_]+)\.$/);
        if (match) {
          const identifier = match[1];
          let targetTableName = identifier;

          // 1. 检查是否是别名
          // 简单的正则匹配 FROM table alias 或 JOIN table alias
          // 注意：这只是非常基础的匹配，不支持复杂的嵌套查询
          const aliasRegex = new RegExp(`\\b([a-zA-Z0-9_]+)\\s+(?:AS\\s+)?\\b${identifier}\\b`, 'gi');
          const aliasMatch = aliasRegex.exec(textUntilPosition);
          
          if (aliasMatch) {
            targetTableName = aliasMatch[1];
          }

          // 查找对应的表元数据
          const targetTable = tables.find(t => t.name.toLowerCase() === targetTableName.toLowerCase());
          
          if (targetTable && targetTable.columns) {
            targetTable.columns.forEach(col => {
              suggestions.push({
                label: col.name,
                kind: monacoRef.languages.CompletionItemKind.Field,
                insertText: col.name,
                detail: `Column (${targetTable.name})`,
                documentation: `Type: ${col.type}`,
                range: range
              });
            });
            return { suggestions: suggestions };
          }
        }
      }

      // 默认提示（非 . 触发，或者无法解析上下文）
      
      // 1. 添加表名和列名提示
      if (tables) {
        tables.forEach(table => {
          // 表名
          suggestions.push({
            label: table.name,
            kind: monacoRef.languages.CompletionItemKind.Class,
            insertText: table.name,
            detail: 'Table',
            range: range
          });
          
          // 列名
          if (table.columns) {
            table.columns.forEach(col => {
              suggestions.push({
                label: col.name,
                kind: monacoRef.languages.CompletionItemKind.Field,
                insertText: col.name,
                detail: `Column (${table.name})`,
                documentation: `Type: ${col.type}`,
                range: range
              });
            });
          }
        });
      }

      // 2. 添加函数/存储过程提示
      if (functions) {
        functions.forEach(func => {
          suggestions.push({
            label: func,
            kind: monacoRef.languages.CompletionItemKind.Function,
            insertText: func,
            detail: 'Function',
            range: range
          });
        });
      }

      // 3. 添加常用关键字提示
      const keywords = [
        'SELECT', 'FROM', 'WHERE', 'UPDATE', 'DELETE', 'INSERT', 'INTO', 'VALUES', 
        'GROUP BY', 'ORDER BY', 'LIMIT', 'JOIN', 'LEFT JOIN', 'RIGHT JOIN', 'INNER JOIN', 
        'ON', 'AND', 'OR', 'NOT', 'NULL', 'IS', 'AS', 'DISTINCT', 'COUNT', 'SUM', 'AVG', 
        'MAX', 'MIN', 'LIKE', 'BETWEEN', 'HAVING', 'CASE', 'WHEN', 'THEN', 'ELSE', 'END',
        'CREATE', 'DROP', 'ALTER', 'TABLE', 'INDEX', 'VIEW', 'TRIGGER', 'PROCEDURE', 'FUNCTION',
        'desc', 'asc', 'primary', 'key', 'foreign', 'default', 'null', 'constraint',
        'union', 'all', 'exec', 'exists', 'in'
      ];
      
      keywords.forEach(kw => {
        suggestions.push({
          label: kw,
          kind: monacoRef.languages.CompletionItemKind.Keyword,
          insertText: kw,
          detail: 'Keyword',
          range: range
        });
      });

      return { suggestions: suggestions };
    }
  });
}

function filterNode(value, data) {
  if (!value) return true;
  // 递归搜索：如果当前节点匹配，或者子节点中有匹配的，则显示
  const matchCurrent = data.label.toLowerCase().includes(value.toLowerCase());
  if (matchCurrent) return true;
  
  // 如果有子节点，检查子节点
  if (data.children && data.children.length > 0) {
    return data.children.some(child => filterNode(value, child));
  }
  return false;
}

function handleNodeClick(data) {
  // 只有点击表或视图时才生成SQL
  if (data.type === 'TABLE' || data.type === 'VIEW') {
    sqlContent.value = "SELECT * FROM `" + data.label + "` LIMIT 1000";
    handleRun();
  } else if (data.type === 'PROCEDURE') {
    sqlContent.value = "CALL `" + data.label + "`()";
  } else if (data.type === 'FUNCTION') {
    sqlContent.value = "SELECT `" + data.label + "`()";
  }
}

function handleNodeDblClick(data) {
  // 双击查看结构
  if (data.type === 'TABLE' || data.type === 'VIEW') {
    showTableStructure(data.label);
  } else if (data.type === 'PROCEDURE') {
    showProcedureDefinition(data.label);
  } else if (data.type === 'FUNCTION') {
    showFunctionDefinition(data.label);
  }
}

function handleContextMenu(event, data) {
  if (!data.type || data.type === 'database' || data.type === 'category') {
    return;
  }
  
  contextMenuVisible.value = true;
  contextMenuPosition.value = { x: event.clientX, y: event.clientY };
  contextMenuData.value = data;
}

// 关闭右键菜单
function closeContextMenu() {
  contextMenuVisible.value = false;
}

// 查看表结构
function showTableStructure(tableName) {
  if (!connId.value) return;
  
  structureLoading.value = true;
  structurePanelVisible.value = true;
  structureType.value = 'table';
  
  getTableStructure(connId.value, tableName).then(response => {
    structureData.value = response.data;
    structureLoading.value = false;
  }).catch(err => {
    proxy.$modal.msgError("获取表结构失败: " + err.message);
    structureLoading.value = false;
  });
}

// 查看存储过程定义
function showProcedureDefinition(procName) {
  if (!connId.value) return;
  
  structureLoading.value = true;
  structurePanelVisible.value = true;
  structureType.value = 'procedure';
  
  getProcedureDefinition(connId.value, procName).then(response => {
    structureData.value = response.data;
    structureLoading.value = false;
  }).catch(err => {
    proxy.$modal.msgError("获取存储过程定义失败: " + err.message);
    structureLoading.value = false;
  });
}

// 查看函数定义
function showFunctionDefinition(funcName) {
  if (!connId.value) return;
  
  structureLoading.value = true;
  structurePanelVisible.value = true;
  structureType.value = 'function';
  
  getFunctionDefinition(connId.value, funcName).then(response => {
    structureData.value = response.data;
    structureLoading.value = false;
  }).catch(err => {
    proxy.$modal.msgError("获取函数定义失败: " + err.message);
    structureLoading.value = false;
  });
}

function handleEditorMount(editor, monaco) {
  monacoInstance = editor;
  monacoRef = monaco;
  
  // 绑定快捷键 F9 执行
  editor.addCommand(monaco.KeyCode.F9, () => {
    handleRun();
  });
  
  // 注册自动补全
  registerAutocomplete();
}

function handleFormat() {
  if (!sqlContent.value) return;
  try {
    sqlContent.value = format(sqlContent.value);
  } catch (e) {
    console.error("Format error", e);
  }
}

function handleRun() {
  if (!connId.value || !sqlContent.value) {
    proxy.$modal.msgError("请选择连接并输入SQL");
    return;
  }
  executeLoading.value = true;
  tableData.value = [];
  tableHeader.value = [];
  costTime.value = null;
  total.value = 0;
  currentPage.value = 1;
  
  const startTime = new Date().getTime();
  
  const sql = sqlContent.value.trim().toUpperCase();
  // 简单的正则判断，实际可能更复杂
  const isSelect = /^(SELECT|SHOW|DESC|EXPLAIN)/i.test(sql);
  
  if (isSelect) {
    executeSelect({ connId: connId.value, sql: sqlContent.value }).then(response => {
      tableData.value = response.data;
      if (tableData.value.length > 0) {
        tableHeader.value = Object.keys(tableData.value[0]);
      } else {
        tableHeader.value = [];
      }
      total.value = tableData.value.length;
      costTime.value = new Date().getTime() - startTime;
      executeLoading.value = false;
      proxy.$modal.msgSuccess(`查询成功，共 ${total.value} 条记录`);
    }).catch((err) => {
      console.error(err);
      executeLoading.value = false;
    });
  } else {
    executeUpdate({ connId: connId.value, sql: sqlContent.value }).then(response => {
      proxy.$modal.msgSuccess(response.msg);
      costTime.value = new Date().getTime() - startTime;
      executeLoading.value = false;
    }).catch(() => {
      executeLoading.value = false;
    });
  }
}

function handleSizeChange(val) {
  pageSize.value = val;
}

function handleCurrentChange(val) {
  currentPage.value = val;
}

function handleCellDblClick(row, key) {
  currentCellValue.value = String(row[key]);
  cellDialogVisible.value = true;
}

function handleStructureEditorMount(editor, monaco) {
  structureEditor = editor;
  structureEditorRef = monaco;
}

function getStructureTitle() {
  if (!structureData.value) return '结构查看';
  
  switch (structureType.value) {
    case 'table':
      return '表结构: ' + (structureData.value.tableName || '');
    case 'procedure':
      return '存储过程: ' + (structureData.value.name || '');
    case 'function':
      return '函数: ' + (structureData.value.name || '');
    default:
      return '结构查看';
  }
}

function closeStructurePanel() {
  structurePanelVisible.value = false;
  structureData.value = {};
  structureType.value = '';
  isEditMode.value = false;
  editedDefinition.value = "";
  if (structureEditor) {
    structureEditor.dispose();
    structureEditor = null;
  }
}

function startEdit() {
  isEditMode.value = true;
  editedDefinition.value = structureData.value.definition || "";
  
  // 等待编辑器挂载
  setTimeout(() => {
    if (structureEditor) {
      structureEditor.focus();
    }
  }, 100);
}

function cancelEdit() {
  isEditMode.value = false;
  editedDefinition.value = "";
}

function saveEdit() {
  if (!connId.value || !structureData.value.name) {
    proxy.$modal.msgError("参数错误");
    return;
  }
  
  if (!editedDefinition.value.trim()) {
    proxy.$modal.msgError("定义不能为空");
    return;
  }
  
  // 确认保存
  proxy.$modal.confirm('确定要更新' + (structureType.value === 'procedure' ? '存储过程' : '函数') + '吗？此操作不可撤销。').then(() => {
    structureLoading.value = true;
    
    const data = {
      connId: connId.value,
      procName: structureType.value === 'procedure' ? structureData.value.name : null,
      funcName: structureType.value === 'function' ? structureData.value.name : null,
      definition: editedDefinition.value
    };
    
    const updateApi = structureType.value === 'procedure' ? updateProcedure : updateFunction;
    
    updateApi(data).then(response => {
      proxy.$modal.msgSuccess('更新成功');
      structureLoading.value = false;
      isEditMode.value = false;
      
      // 刷新结构数据
      if (structureType.value === 'procedure') {
        showProcedureDefinition(structureData.value.name);
      } else {
        showFunctionDefinition(structureData.value.name);
      }
    }).catch(err => {
      proxy.$modal.msgError('更新失败: ' + err.message);
      structureLoading.value = false;
    });
  }).catch(() => {
    // 取消
  });
}

// 右键菜单 - 查看结构
function handleViewStructure() {
  if (!contextMenuData.value) return;
  
  const data = contextMenuData.value;
  if (data.type === 'TABLE' || data.type === 'VIEW') {
    showTableStructure(data.label);
  } else if (data.type === 'PROCEDURE') {
    showProcedureDefinition(data.label);
  } else if (data.type === 'FUNCTION') {
    showFunctionDefinition(data.label);
  }
  
  closeContextMenu();
}

// 右键菜单 - 生成SELECT语句
function handleGenerateSelect() {
  if (!contextMenuData.value) return;
  
  const data = contextMenuData.value;
  if (data.type === 'TABLE' || data.type === 'VIEW') {
    sqlContent.value = "SELECT * FROM `" + data.label + "` LIMIT 1000";
    handleRun();
  }
  
  closeContextMenu();
}

// 监听右键菜单外部点击
watch(contextMenuVisible, (val) => {
  if (val) {
    document.addEventListener('click', closeContextMenu);
  } else {
    document.removeEventListener('click', closeContextMenu);
  }
});

// 组件卸载时销毁 provider
onBeforeUnmount(() => {
  if (completionProvider) {
    completionProvider.dispose();
  }
  document.removeEventListener('click', closeContextMenu);
});

getConnList();
</script>

<style scoped>
.app-container {
  height: calc(100vh - 84px);
  padding: 10px;
}
::deep(.el-card__body) {
  padding: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 数据库资源浏览器样式 */
.db-explorer-card {
  background: linear-gradient(145deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
}

.db-explorer-card ::v-deep(.el-card__header) {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  padding: 12px 15px;
  border-bottom: none;
}

.db-explorer-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.db-explorer-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f8fafc;
  font-size: 14px;
  font-weight: 600;
}

.db-explorer-title .el-icon {
  font-size: 16px;
  color: #60a5fa;
}

.conn-select {
  width: 100%;
}

.conn-select ::v-deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.1);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.conn-select ::v-deep(.el-input__inner) {
  color: #f8fafc;
}

.conn-select ::v-deep(.el-input__inner::placeholder) {
  color: rgba(248, 250, 252, 0.6);
}

.db-tree-container {
  flex: 1;
  overflow-y: auto;
  padding: 5px;
}

.db-search-input {
  margin-bottom: 12px;
}

.db-search-input ::v-deep(.el-input__wrapper) {
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border-radius: 6px;
}

/* 资源树样式 */
.db-resource-tree {
  background: transparent;
}

.db-resource-tree ::v-deep(.el-tree-node__content) {
  height: 32px;
  border-radius: 6px;
  margin: 2px 0;
  transition: all 0.2s ease;
}

.db-resource-tree ::v-deep(.el-tree-node__content:hover) {
  background-color: #e0f2fe;
}

.db-resource-tree ::v-deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #dbeafe;
  border-left: 3px solid #3b82f6;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  font-size: 13px;
}

.node-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.db-icon {
  color: #f59e0b;
  font-size: 16px;
}

.folder-icon {
  color: #3b82f6;
}

.table-icon {
  color: #10b981;
}

.view-icon {
  color: #8b5cf6;
}

.proc-icon {
  color: #f97316;
}

.func-icon {
  color: #06b6d4;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-count {
  color: #94a3b8;
  font-size: 11px;
  margin-left: 4px;
}

/* 节点类型样式 */
.custom-tree-node.database {
  font-weight: 600;
  color: #1e293b;
}

.custom-tree-node.category {
  font-weight: 500;
  color: #475569;
}

.custom-tree-node.TABLE,
.custom-tree-node.VIEW,
.custom-tree-node.PROCEDURE,
.custom-tree-node.FUNCTION {
  color: #334155;
  padding-left: 4px;
}

.right-pane {
  height: 100%; 
  display: flex; 
  flex-direction: column; 
  padding-left: 10px;
}

/* 全屏模式下的容器样式 */
.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh !important;
  background-color: #fff;
  z-index: 2000;
  padding: 10px;
}

/* 结构面板样式 */
.structure-panel {
  padding: 0 20px 20px 20px;
  height: 100%;
  overflow-y: auto;
}

.structure-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.structure-actions {
  display: flex;
  gap: 8px;
}

.structure-section {
  margin-bottom: 25px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-content {
  background-color: #f8fafc;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #3b82f6;
}

.comment-content {
  font-style: italic;
  color: #475569;
}

.meta-info {
  margin-bottom: 20px;
}

.code-content {
  background-color: #1e293b;
  border-radius: 8px;
  padding: 15px;
  overflow-x: auto;
}

.code-content pre {
  margin: 0;
  color: #e2e8f0;
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.editor-container {
  height: 400px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 3000;
  padding: 5px 0;
  min-width: 180px;
}

.context-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  transition: background-color 0.2s;
}

.context-menu-item:hover {
  background-color: #f1f5f9;
}

.context-menu-item .el-icon {
  font-size: 14px;
  color: #3b82f6;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .db-explorer-card {
    font-size: 12px;
  }
  
  .custom-tree-node {
    font-size: 12px;
  }
  
  .node-icon {
    font-size: 12px;
  }
}
</style>
