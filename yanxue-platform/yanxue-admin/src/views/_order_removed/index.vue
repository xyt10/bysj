<template>
  <div class="order-page">
    <!-- 搜索区域 -->
    <div class="glass-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="订单号" clearable class="custom-input" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable class="custom-select">
            <el-option label="待支付" value="pending" />
            <el-option label="已支付" value="paid" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
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

    <!-- 列表区域 -->
    <div class="glass-card table-card">
      <div class="card-header">
        <span class="card-title">订单列表</span>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" class="custom-table">
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <span class="order-no">#{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="routeName" label="路线名称" min-width="180" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="travelDate" label="出行日期" width="120">
          <template #default="{ row }">
            <span class="date-text"><el-icon><Calendar /></el-icon> {{ row.travelDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="peopleCount" label="人数" width="80">
          <template #default="{ row }">
            <span class="people-badge">{{ row.peopleCount }} 人</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template #default="{ row }">
            <span class="price-highlight">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-badge" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ row.createdAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'paid'" type="primary" link @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" link @click="handleCancel(row)">取消</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api'
import { Search, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)

const searchForm = reactive({
  orderNo: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const getStatusClass = (status) => {
  const map = {
    pending: 'status-pending',
    paid: 'status-paid',
    completed: 'status-completed',
    cancelled: 'status-cancelled'
  }
  return map[status] || 'status-cancelled'
}

const getStatusText = (status) => {
  const map = {
    pending: '待支付',
    paid: '已支付',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await orderApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    tableData.value = [
      { orderNo: '1736678912345678', routeName: '广州历史文化两日研学', contactName: '张三', contactPhone: '13800138000', travelDate: '2026-02-01', peopleCount: 30, totalPrice: 11400, status: 'paid', createdAt: '2026-01-10 10:30:00' },
      { orderNo: '1736678912345679', routeName: '科技探索一日营', contactName: '李四', contactPhone: '13800138001', travelDate: '2026-02-05', peopleCount: 25, totalPrice: 4950, status: 'pending', createdAt: '2026-01-11 14:20:00' }
    ]
    pagination.total = 2
  }
  loading.value = false
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  loadData()
}

const handleComplete = async (row) => {
  await ElMessageBox.confirm('确定将此订单标记为已完成吗？', '提示')
  await orderApi.complete(row.id)
  ElMessage.success('操作成功')
  loadData()
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要取消此订单吗？', '提示', { type: 'warning' })
  await orderApi.cancel(row.id)
  ElMessage.success('取消成功')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-page {
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

/* Order No */
.order-no {
  font-family: 'Monaco', monospace;
  font-weight: 600;
  color: var(--admin-primary);
}

/* Date Text */
.date-text {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--admin-text-secondary);
}

.date-text .el-icon {
  color: var(--admin-primary);
}

/* People Badge */
.people-badge {
  display: inline-block;
  padding: 4px 12px;
  background: #e0e7ff;
  border-radius: 20px;
  font-size: 12px;
  color: #4338ca;
  font-weight: 500;
}

/* Price Highlight */
.price-highlight {
  color: #ef4444;
  font-weight: 700;
  font-size: 15px;
}

/* Status Badges */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-paid {
  background: #dbeafe;
  color: #1e40af;
}

.status-completed {
  background: #dcfce7;
  color: #166534;
}

.status-cancelled {
  background: #f1f5f9;
  color: #64748b;
}

/* Time Text */
.time-text {
  color: var(--admin-text-muted);
  font-size: 13px;
}

/* Pagination */
.custom-pagination {
  padding: 16px 24px;
  justify-content: flex-end;
}
</style>
