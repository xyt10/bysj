<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="glass-card stat-card">
          <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #ef4444 0%, #f87171 100%)">
            <el-icon :size="24"><Location /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">点位总数</div>
            <div class="stat-value">{{ formatNumber(stats.spotCount) }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="8">
        <div class="glass-card chart-card">
          <div class="card-header">
            <span class="card-title">路线主题分布</span>
          </div>
          <div ref="themeChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 热门路线 -->
    <div class="glass-card table-card">
      <div class="card-header">
        <span class="card-title">热门路线</span>
        <el-button type="primary" text>查看全部</el-button>
      </div>
      <el-table :data="hotRoutes" stripe class="custom-table">
        <el-table-column prop="name" label="路线名称">
          <template #default="{ row }">
            <div class="route-name">
              <div class="route-avatar">{{ row.name?.charAt(0) }}</div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="days" label="天数" width="100">
          <template #default="{ row }">
            <span class="tag-days">{{ row.days }} 天</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            <span class="price-tag">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="120">
          <template #default="{ row }">
            <div class="metric">
              <el-icon><View /></el-icon>
              <span>{{ formatNumber(row.viewCount) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="120">
          <template #default="{ row }">
            <div class="metric likes">
              <el-icon><Star /></el-icon>
              <span>{{ formatNumber(row.likeCount) }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { routeApi, statisticsApi } from '@/api'
import { User, Guide, Location, View, Star } from '@element-plus/icons-vue'

const themeChartRef = ref(null)

const stats = reactive({
  userCount: 0,
  routeCount: 0,
  spotCount: 0
})

const hotRoutes = ref([])

let themeChart = null
const onResize = () => themeChart?.resize()

const formatNumber = (num) => {
  const n = Number(num) || 0
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  return n.toLocaleString()
}

const initThemeChart = () => {
  if (!themeChartRef.value) return
  themeChart = echarts.init(themeChartRef.value)
  themeChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#1e293b' }
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#64748b', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: []
    }]
  })
}

const loadDashboardStats = async () => {
  try {
    const res = await statisticsApi.dashboard()
    const data = res?.data
    if (!data) return

    stats.userCount = data.userCount || 0
    stats.routeCount = data.routeCount || 0
    stats.spotCount = data.spotCount || 0

    if (themeChart && Array.isArray(data.themeDistribution)) {
      themeChart.setOption({
        series: [{
          data: data.themeDistribution.map((item) => ({
            value: item.value,
            name: item.name
          }))
        }]
      })
    }
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

const loadHotRoutes = async () => {
  try {
    const res = await routeApi.hot(5)
    const data = res?.data
    if (Array.isArray(data) && data.length > 0) {
      hotRoutes.value = data
      return
    }
  } catch (e) {
    console.error('加载热门路线失败:', e)
  }

  hotRoutes.value = [
    { name: '北京历史文化研学', days: 5, price: 3280, viewCount: 12580, likeCount: 892 },
    { name: '西安古都探秘之旅', days: 4, price: 2880, viewCount: 10234, likeCount: 756 },
    { name: '上海科技创新研学', days: 3, price: 2680, viewCount: 9876, likeCount: 634 },
    { name: '成都熊猫生态研学', days: 4, price: 2980, viewCount: 8654, likeCount: 723 },
    { name: '敦煌丝绸之路研学', days: 6, price: 4280, viewCount: 7654, likeCount: 567 }
  ]
}

onMounted(async () => {
  initThemeChart()
  window.addEventListener('resize', onResize)
  await loadDashboardStats()
  await loadHotRoutes()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  themeChart?.dispose()
  themeChart = null
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

/* Glass Card Base */
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 24px;
  transition: all 0.3s ease;
}

.glass-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

/* Stat Cards */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--admin-text-muted);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--admin-text);
  line-height: 1;
}

/* Chart Cards */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
  display: flex;
  flex-direction: column;
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

.chart-container {
  flex: 1;
  min-height: 0;
}

/* Table Card */
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
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: #f8fafc;
}

.custom-table :deep(.el-table__header) {
  th {
    font-weight: 600;
    color: var(--admin-text-secondary);
    background: #f8fafc;
  }
}

.route-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.route-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--admin-primary) 0%, var(--admin-secondary) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.tag-days {
  display: inline-block;
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 13px;
  color: var(--admin-text-secondary);
}

.price-tag {
  font-weight: 600;
  color: #ef4444;
  font-size: 15px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--admin-text-muted);
  font-size: 13px;
}

.metric.likes {
  color: #f59e0b;
}

.metric .el-icon {
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .stat-cards .el-col {
    margin-bottom: 12px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon-wrapper {
    width: 48px;
    height: 48px;
    border-radius: 12px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .chart-row .el-col {
    margin-bottom: 12px;
  }
  
  .chart-card {
    height: 280px;
    padding: 16px;
  }
  
  .glass-card {
    padding: 16px;
    border-radius: 12px;
  }
  
  .card-header {
    margin-bottom: 12px;
  }
  
  .card-title {
    font-size: 15px;
  }
  
  /* 表格移动端适配 - 水平滚动 */
  .table-card {
    overflow-x: auto;
  }
  
  .table-card .card-header {
    padding: 16px;
  }
  
  .custom-table {
    min-width: 600px;
  }
  
  .route-name {
    min-width: 150px;
  }
  
  .route-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }
}
</style>
