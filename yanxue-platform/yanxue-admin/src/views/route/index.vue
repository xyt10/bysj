<template>
  <div class="route-page">
    <!-- 搜索区域 -->
    <div class="glass-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索路线名称" clearable class="custom-input" />
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="searchForm.theme" placeholder="主题" clearable class="custom-input" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" class="btn-gradient">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表区域 -->
    <div class="glass-card table-card">
      <div class="card-header">
        <span class="card-title">路线列表</span>
        <el-button type="primary" @click="handleAdd" class="btn-gradient">
          <el-icon><Plus /></el-icon> 新增路线
        </el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="路线名称" min-width="200">
          <template #default="{ row }">
            <div class="route-info">
              <div class="route-icon"><el-icon><Guide /></el-icon></div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="days" label="天数" width="80">
          <template #default="{ row }">
            <span class="days-badge">{{ row.days }} 天</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-tag">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{ row }">
            <span class="source-badge" :class="row.source === 'ai' ? 'ai-badge' : 'manual-badge'">
              <el-icon v-if="row.source === 'ai'"><MagicStick /></el-icon>
              {{ row.source === 'ai' ? 'AI生成' : '人工' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100">
          <template #default="{ row }">
            <div class="metric">
              <el-icon><View /></el-icon>
              <span>{{ row.viewCount }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status === 1 ? 'status-active' : 'status-inactive'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        class="custom-pagination"
      >
      </el-pagination>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" class="custom-dialog"
      :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="路线名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入路线名称" class="custom-input" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="天数" prop="days">
              <el-input-number v-model="form.days" :min="1" :max="30" class="custom-input-number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参考价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" class="custom-input-number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适合学段">
          <el-checkbox-group v-model="selectedGrades">
            <el-checkbox label="小学">小学</el-checkbox>
            <el-checkbox label="初中">初中</el-checkbox>
            <el-checkbox label="高中">高中</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="主题标签">
          <el-input v-model="form.themes" placeholder="多个主题用逗号分隔，如：历史,文化" class="custom-input" />
        </el-form-item>
        <el-form-item label="路线简介">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="路线简介" class="custom-textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="btn-gradient">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { routeApi } from '@/api'
import { Plus, Search, Guide, MagicStick, View } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增路线')
const formRef = ref()
const selectedGrades = ref([])

const searchForm = reactive({
  keyword: '',
  theme: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const form = reactive({
  id: null,
  name: '',
  days: 1,
  price: 0,
  suitableGrades: '',
  themes: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  days: [{ required: true, message: '请输入天数', trigger: 'blur' }]
}

watch(selectedGrades, (val) => {
  form.suitableGrades = JSON.stringify(val)
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await routeApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    tableData.value = [
      { id: 1, name: '广州历史文化两日研学', days: 2, price: 380, source: 'manual', viewCount: 1256, status: 1 },
      { id: 2, name: '科技探索一日营', days: 1, price: 198, source: 'manual', viewCount: 892, status: 1 },
      { id: 3, name: 'AI智能生成路线', days: 2, price: 320, source: 'ai', viewCount: 156, status: 1 }
    ]
    pagination.total = 3
  }
  loading.value = false
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.theme = ''
  loadData()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    days: 1,
    price: 0,
    suitableGrades: '',
    themes: '',
    description: ''
  })
  selectedGrades.value = []
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增路线'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  try {
    selectedGrades.value = JSON.parse(row.suitableGrades || '[]')
  } catch (e) {
    selectedGrades.value = []
  }
  dialogTitle.value = '编辑路线'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该路线吗？', '提示', { type: 'warning' })
  await routeApi.delete(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await routeApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await routeApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.route-page {
  padding: 0;
}

/* Glass Card */
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 24px;
  margin-bottom: 20px;
}

.search-card {
  padding: 20px 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--admin-text);
}

/* Custom Inputs */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.custom-input-number {
  width: 100%;
}

.custom-input-number :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
}

/* Button Gradient */
.btn-gradient {
  background: linear-gradient(135deg, var(--admin-primary) 0%, var(--admin-secondary) 100%);
  border: none;
}

.btn-gradient:hover {
  opacity: 0.96;
  box-shadow: 0 18px 38px -22px rgba(2, 132, 199, 0.75);
}

/* Table Styles */
.table-card {
  padding: 0;
  overflow: hidden;
}

.table-card .card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 0;
}

.custom-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f1f5f9;
}

.custom-table :deep(.el-table__header) {
  th {
    font-weight: 600;
    color: var(--admin-text-secondary);
    height: 48px;
  }
}

/* Route Info */
.route-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.route-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--gradient-ocean);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 10px 26px -20px rgba(2, 132, 199, 0.6);
}

/* Days Badge */
.days-badge {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(20, 184, 166, 0.12);
  border-radius: 20px;
  font-size: 12px;
  color: #0f766e;
  font-weight: 600;
}

/* Price Tag */
.price-tag {
  color: #ef4444;
  font-weight: 600;
  font-size: 15px;
}

/* Source Badge */
.source-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.ai-badge {
  background: #fef3c7;
  color: #92400e;
}

.manual-badge {
  background: #f1f5f9;
  color: #475569;
}

.source-badge .el-icon {
  font-size: 12px;
}

/* Metric */
.metric {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--admin-text-muted);
  font-size: 13px;
}

.metric .el-icon {
  font-size: 14px;
}

/* Status Badge */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-inactive {
  background: #f1f5f9;
  color: #64748b;
}

/* Pagination */
.custom-pagination {
  padding: 16px 24px;
  justify-content: flex-end;
}

/* Dialog */
:deep(.custom-dialog) {
  .el-dialog {
    border-radius: 20px;
    overflow: hidden;
  }
  
  .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid #f1f5f9;
    margin-right: 0;
  }
  
  .el-dialog__title {
    font-weight: 600;
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #f1f5f9;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ================== 移动端响应式 ================== */
@media (max-width: 768px) {
  .glass-card {
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 12px;
  }
  
  .search-card {
    padding: 12px 16px;
  }
  
  .search-card :deep(.el-form) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .search-card :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 0;
    width: 100%;
  }
  
  .search-card :deep(.el-form-item__label) {
    width: 60px !important;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .card-header .btn-gradient {
    width: 100%;
  }
  
  /* 表格移动端 - 水平滚动 */
  .table-card {
    overflow-x: auto;
  }
  
  .table-card .card-header {
    padding: 16px;
  }
  
  .custom-table {
    min-width: 800px;
  }
  
  .route-info {
    min-width: 180px;
  }
  
  .route-icon {
    width: 32px;
    height: 32px;
  }
  
  .custom-pagination {
    padding: 12px 16px;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .custom-pagination :deep(.el-pagination__sizes) {
    display: none;
  }
  
  /* 弹窗移动端 */
  :deep(.custom-dialog .el-dialog) {
    width: 95vw !important;
    max-width: 95vw !important;
    margin: 16px auto !important;
  }
  
  :deep(.custom-dialog .el-dialog__body) {
    padding: 16px;
    max-height: 60vh;
    overflow-y: auto;
  }
  
  :deep(.el-row) {
    flex-direction: column;
  }
  
  :deep(.el-col) {
    max-width: 100%;
  }
}
</style>
