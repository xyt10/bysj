<template>
  <div class="spot-page">
    <!-- 搜索区域 -->
    <div class="glass-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索点位名称" clearable class="custom-input" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="searchForm.city" placeholder="城市" clearable class="custom-input" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable class="custom-select">
            <el-option label="博物馆" value="博物馆" />
            <el-option label="科技馆" value="科技馆" />
            <el-option label="景区" value="景区" />
            <el-option label="基地" value="基地" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" class="btn-gradient">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作区域 -->
    <div class="glass-card table-card">
      <div class="card-header">
        <span class="card-title">点位列表</span>
        <el-button type="primary" @click="handleAdd" class="btn-gradient">
          <el-icon><Plus /></el-icon> 新增点位
        </el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="150">
          <template #default="{ row }">
            <div class="name-cell">
              <div class="name-avatar" :style="{ background: getAvatarColor(row.type) }">{{ row.name?.charAt(0) }}</div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <span class="type-tag" :style="{ background: getTypeColor(row.type) }">{{ row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="ticketPrice" label="门票" width="100">
          <template #default="{ row }">
            <span :class="row.ticketPrice > 0 ? 'price-tag' : 'free-tag'">
              {{ row.ticketPrice > 0 ? '¥' + row.ticketPrice : '免费' }}
            </span>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" class="custom-dialog"
      :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入点位名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" class="custom-select">
            <el-option label="博物馆" value="博物馆" />
            <el-option label="科技馆" value="科技馆" />
            <el-option label="景区" value="景区" />
            <el-option label="基地" value="基地" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="省份" prop="province">
              <el-input v-model="form.province" placeholder="省份" class="custom-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="城市" class="custom-input" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="详细地址" class="custom-input" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input-number v-model="form.longitude" :precision="7" controls-position="right" class="custom-input-number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input-number v-model="form.latitude" :precision="7" controls-position="right" class="custom-input-number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="门票价格">
          <el-input-number v-model="form.ticketPrice" :min="0" :precision="2" class="custom-input-number" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-input v-model="form.openTime" placeholder="如：09:00-17:00" class="custom-input" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="点位简介" class="custom-textarea" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { spotApi } from '@/api'
import { Plus, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增点位')
const formRef = ref()

const searchForm = reactive({
  keyword: '',
  city: '',
  type: ''
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
  type: '',
  province: '',
  city: '',
  address: '',
  longitude: null,
  latitude: null,
  ticketPrice: 0,
  openTime: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }]
}

const typeColors = {
  '博物馆': '#0ea5e9',
  '科技馆': '#38bdf8',
  '景区': '#10b981',
  '基地': '#f59e0b'
}

const getTypeColor = (type) => typeColors[type] || '#64748b'
const getAvatarColor = (type) => {
  const colors = ['#0ea5e9', '#14b8a6', '#38bdf8', '#10b981', '#f59e0b', '#ef4444']
  return colors[type?.length % colors.length] || colors[0]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await spotApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    tableData.value = [
      { id: 1, name: '广东省博物馆', type: '博物馆', city: '广州市', ticketPrice: 0, status: 1 },
      { id: 2, name: '广州科学中心', type: '科技馆', city: '广州市', ticketPrice: 60, status: 1 }
    ]
    pagination.total = 2
  }
  loading.value = false
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.city = ''
  searchForm.type = ''
  loadData()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    type: '',
    province: '',
    city: '',
    address: '',
    longitude: null,
    latitude: null,
    ticketPrice: 0,
    openTime: '',
    description: ''
  })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增点位'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogTitle.value = '编辑点位'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该点位吗？', '提示', { type: 'warning' })
  await spotApi.delete(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await spotApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await spotApi.create(form)
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
.spot-page {
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

.custom-select {
  width: 160px;
}

.custom-select :deep(.el-input__wrapper) {
  border-radius: 10px;
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

/* Name Cell */
.name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

/* Type Tag */
.type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: white;
  font-weight: 500;
}

/* Price Tags */
.price-tag {
  color: #ef4444;
  font-weight: 600;
}

.free-tag {
  color: #10b981;
  font-weight: 600;
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
  
  .search-card :deep(.el-form-item__content) {
    flex: 1;
  }
  
  .custom-select {
    width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .card-header .btn-gradient {
    width: 100%;
  }
  
  /* 表格移动端适配 - 水平滚动 */
  .table-card {
    overflow-x: auto;
  }
  
  .table-card .card-header {
    padding: 16px;
  }
  
  .custom-table {
    min-width: 700px;
  }
  
  .name-cell {
    min-width: 140px;
  }
  
  .name-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }
  
  .custom-pagination {
    padding: 12px 16px;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .custom-pagination :deep(.el-pagination__sizes) {
    display: none;
  }
  
  /* 弹窗移动端适配 */
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
  
  :deep(.el-form-item__label) {
    width: 80px !important;
  }
  
  :deep(.el-row) {
    flex-direction: column;
  }
  
  :deep(.el-col) {
    max-width: 100%;
  }
}
</style>
