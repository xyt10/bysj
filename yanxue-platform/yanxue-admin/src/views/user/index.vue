<template>
  <div class="user-page">
    <!-- 搜索区域 -->
    <div class="glass-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索用户名/昵称" clearable class="custom-input" />
        </el-form-item>
        <el-form-item label="学段">
          <el-select v-model="searchForm.grade" placeholder="选择学段" clearable class="custom-select">
            <el-option label="小学" value="小学" />
            <el-option label="初中" value="初中" />
            <el-option label="高中" value="高中" />
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

    <div class="glass-card table-card">
      <div class="card-header">
        <span class="card-title">用户列表</span>
        <span class="user-count">共 {{ pagination.total }} 位用户</span>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="用户" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-avatar">{{ (row.nickname || row.username || '?').charAt(0) }}</div>
              <div class="user-detail">
                <span class="nickname">{{ row.nickname || row.username || '未设置' }}</span>
                <span class="username" v-if="row.username">@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            <span>{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="学段" width="100">
          <template #default="{ row }">
            <span class="grade-badge" :class="getGradeClass(row.grade)" v-if="row.grade">{{ row.grade }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="school" label="学校" min-width="180">
          <template #default="{ row }">
            <span>{{ row.school || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="interests" label="兴趣标签" min-width="180">
          <template #default="{ row }">
            <template v-if="row.interests && parseInterests(row.interests).length > 0">
              <span v-for="tag in parseInterests(row.interests)" :key="tag" class="interest-tag">
                {{ tag }}
              </span>
            </template>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170">
          <template #default="{ row }">
            <span class="time-text"><el-icon><Clock /></el-icon> {{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        class="custom-pagination"
      >
      </el-pagination>
    </div>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px" class="custom-dialog">
      <div class="user-detail-card" v-if="currentUser">
        <div class="detail-header">
          <div class="big-avatar">{{ (currentUser.nickname || currentUser.username || '?').charAt(0) }}</div>
          <div class="detail-info">
            <h3>{{ currentUser.nickname || currentUser.username || '未设置' }}</h3>
            <p v-if="currentUser.username">@{{ currentUser.username }}</p>
          </div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学段">{{ currentUser.grade || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ currentUser.school || '-' }}</el-descriptions-item>
          <el-descriptions-item label="兴趣">
            <template v-if="currentUser.interests && parseInterests(currentUser.interests).length > 0">
              <el-tag v-for="tag in parseInterests(currentUser.interests)" :key="tag" class="mr-2" size="small">
                {{ tag }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(currentUser.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Clock, Search } from '@element-plus/icons-vue'
import { userApi } from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const detailVisible = ref(false)
const currentUser = ref(null)

const searchForm = reactive({
  keyword: '',
  grade: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const parseInterests = (interests) => {
  if (!interests) return []
  try {
    if (Array.isArray(interests)) return interests
    return JSON.parse(interests)
  } catch (e) {
    // 如果不是 JSON，尝试按逗号分割
    return interests.split(',').map(s => s.trim()).filter(Boolean)
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getGradeClass = (grade) => {
  const map = {
    '小学': 'grade-primary',
    '初中': 'grade-middle',
    '高中': 'grade-high'
  }
  return map[grade] || 'grade-default'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await userApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      grade: searchForm.grade || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error('加载用户列表失败:', e)
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.grade = ''
  pagination.pageNum = 1
  loadData()
}

const viewDetail = (row) => {
  currentUser.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-page {
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

.table-card {
  padding: 0;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--admin-text);
}

.user-count {
  font-size: 13px;
  color: #64748b;
}

/* Custom Inputs */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.custom-select {
  width: 140px;
}

.custom-select :deep(.el-input__wrapper) {
  border-radius: 10px;
}

/* Button Gradient */
.btn-gradient {
  background: linear-gradient(135deg, #0ea5e9 0%, #14b8a6 100%);
  border: none;
}

/* Table Styles */
.custom-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f1f5f9;
}

.custom-table :deep(.el-table__header th) {
  font-weight: 600;
  color: #475569;
  height: 48px;
}

/* User Info */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9 0%, #14b8a6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nickname {
  font-weight: 500;
  color: #1e293b;
}

.username {
  font-size: 12px;
  color: #94a3b8;
}

/* Grade Badge */
.grade-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.grade-primary {
  background: #dcfce7;
  color: #166534;
}

.grade-middle {
  background: #dbeafe;
  color: #1e40af;
}

.grade-high {
  background: #fef3c7;
  color: #92400e;
}

.grade-default {
  background: #f1f5f9;
  color: #64748b;
}

/* Interest Tag */
.interest-tag {
  display: inline-block;
  padding: 4px 10px;
  background: #e0e7ff;
  border-radius: 20px;
  font-size: 12px;
  color: #4338ca;
  margin-right: 6px;
  margin-bottom: 4px;
}

/* Time Text */
.time-text {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
}

.time-text .el-icon {
  font-size: 14px;
}

.text-muted {
  color: #94a3b8;
}

/* Pagination */
.custom-pagination {
  padding: 16px 24px;
  justify-content: flex-end;
}

/* Detail Dialog */
.user-detail-card {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.big-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9 0%, #14b8a6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 24px;
}

.detail-info h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #1e293b;
}

.detail-info p {
  margin: 0;
  color: #94a3b8;
  font-size: 14px;
}

.mr-2 {
  margin-right: 8px;
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
  
  .custom-select {
    width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
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
  
  .user-info {
    min-width: 140px;
  }
  
  .user-avatar {
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
  
  :deep(.el-dialog) {
    width: 95vw !important;
    margin: 16px auto !important;
  }
}
</style>
