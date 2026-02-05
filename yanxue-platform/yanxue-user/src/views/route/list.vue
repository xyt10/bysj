<template>
  <div class="route-list-page">
    <!-- 搜索筛选栏 -->
    <div class="card filter-section">
      <div class="filter-header">
        <h3 class="filter-title"><el-icon><Search /></el-icon> 筛选路线</h3>
        <el-button link @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
      </div>
      
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索路线名称"
            clearable
            @keyup.enter="handleSearch"
            class="filter-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="主题">
          <el-select v-model="searchForm.theme" placeholder="全部主题" clearable class="filter-select">
            <el-option label="历史文化" value="历史文化" >
              <span class="theme-dot" style="background: var(--color-primary)"></span> 历史文化
            </el-option>
            <el-option label="科技探索" value="科技探索">
              <span class="theme-dot" style="background: var(--color-success)"></span> 科技探索
            </el-option>
            <el-option label="自然生态" value="自然生态">
              <span class="theme-dot" style="background: var(--color-warning)"></span> 自然生态
            </el-option>
            <el-option label="红色教育" value="红色教育">
              <span class="theme-dot" style="background: var(--color-danger)"></span> 红色教育
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="search-btn">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 路线列表 -->
    <div class="card route-section">
      <div class="section-header">
        <h3 class="section-title">研学路线</h3>
        <span class="total-count">共 <strong>{{ total }}</strong> 条路线</span>
      </div>

      <div v-loading="loading" class="route-grid">
        <div
          v-for="route in routes"
          :key="route.id"
          class="route-card"
          @click="goToDetail(route.id)"
        >
          <div class="route-image-wrapper">
            <el-image
              class="route-cover"
              :src="route.coverImage || defaultCover"
              fit="cover"
              loading="lazy"
            >
              <template #placeholder>
                <div class="image-placeholder"><el-icon :size="32"><Picture /></el-icon></div>
              </template>
              <template #error>
                <img class="route-cover" :src="defaultCover" alt="placeholder" />
              </template>
            </el-image>
            <div v-if="route.source === 'ai'" class="ai-badge">
              <el-icon><MagicStick /></el-icon> AI生成
            </div>
          </div>
          
          <div class="route-content">
            <h4 class="route-name">{{ route.name }}</h4>
            <p class="route-desc">{{ route.description }}</p>
            
            <div class="route-tags">
              <el-tag size="small" type="primary" effect="light">{{ route.days }}天</el-tag>
              <el-tag 
                v-for="theme in parseThemes(route.themes)" 
                :key="theme" 
                size="small"
                effect="light"
              >
                {{ theme }}
              </el-tag>
            </div>
            
            <div class="route-footer">
              <div class="price-area">
                <span class="price-symbol">¥</span>
                <span class="price">{{ route.price }}</span>
                <span class="price-unit">起/人</span>
              </div>
              <div class="stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon> {{ formatNumber(route.viewCount) }}
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon> {{ formatNumber(route.likeCount) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && routes.length === 0" class="empty-state">
        <el-icon :size="64" class="empty-icon"><Document /></el-icon>
        <p class="empty-title">暂无路线数据</p>
        <p class="empty-desc">试试其他搜索条件</p>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { View, Star, Search, Refresh, Picture, Document, MagicStick } from '@element-plus/icons-vue'
import { routeApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const routes = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const defaultCover = '/placeholder-route.svg'

const searchForm = reactive({
  keyword: '',
  theme: ''
})

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num || 0
}

const parseThemes = (themes) => {
  if (!themes) return []
  if (Array.isArray(themes)) return themes.slice(0, 2)
  try {
    const parsed = JSON.parse(themes)
    return Array.isArray(parsed) ? parsed.slice(0, 2) : []
  } catch {
    return []
  }
}

const loadRoutes = async () => {
  loading.value = true
  try {
    const res = await routeApi.page({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword || undefined,
      theme: searchForm.theme || undefined
    })
    routes.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.log('加载路线失败', e)
    routes.value = [
      { id: 1, name: '广州历史文化两日研学', description: '深入了解广州千年历史文化，探访陈家祠、南越王博物院等历史名胜', days: 2, price: 380, viewCount: 1256, themes: '["历史文化"]' },
      { id: 2, name: '科技探索一日营', description: '走进广州科学中心，体验前沿科技的魅力', days: 1, price: 198, viewCount: 892, source: 'ai', themes: '["科技探索"]' },
      { id: 3, name: '红色教育主题研学', description: '参观黄埔军校旧址，学习革命历史', days: 1, price: 150, viewCount: 568, themes: '["红色教育"]' }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadRoutes()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.theme = ''
  pageNum.value = 1
  loadRoutes()
}

const handleSizeChange = () => {
  pageNum.value = 1
  loadRoutes()
}

const handlePageChange = () => {
  loadRoutes()
}

const goToDetail = (id) => {
  const n = Number(id)
  if (!Number.isFinite(n) || n <= 0) {
    ElMessage.warning('该路线暂无详情页')
    console.log('无效路线ID', id)
    return
  }
  router.push(`/route/${n}`)
}

onMounted(() => {
  if (route.query.keyword) {
    searchForm.keyword = route.query.keyword
  }
  if (route.query.theme) {
    searchForm.theme = route.query.theme
  }
  loadRoutes()
})
</script>

<style lang="scss" scoped>
.route-list-page {
  padding: var(--space-md) 0;
  animation: listIn 420ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes listIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ============================================
// Filter Section
// ============================================
.filter-section {
  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--space-md);
    padding-bottom: var(--space-md);
    border-bottom: 1px solid var(--border-light);
  }
  
  .filter-title {
    font-size: var(--text-md);
    font-weight: var(--font-semibold);
    color: var(--text-primary);
    margin: 0;
    display: flex;
    align-items: center;
    gap: var(--space-sm);
  }
  
  .filter-form {
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: var(--space-md);
      
      &:last-child {
        margin-right: 0;
      }
    }
    
    .filter-input,
    .filter-select {
      width: 220px;
    }
    
    .search-btn {
      font-weight: var(--font-semibold);
    }
  }
}

.theme-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: var(--space-sm);
}

// ============================================
// Route Grid
// ============================================
.route-section {
  .section-header {
    margin-bottom: var(--space-lg);
  }
  
  .total-count {
    font-size: var(--text-sm);
    color: var(--text-tertiary);
    
    strong {
      color: var(--color-primary);
      font-weight: var(--font-bold);
    }
  }
}

.route-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-lg);
  min-height: 200px;
}

.route-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base), border-color var(--transition-base);
  border: 1px solid rgba(15, 118, 110, 0.06);

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-4px);
    border-color: rgba(14, 165, 233, 0.18);

    .route-cover {
      transform: scale(1.05);
    }

    .route-name {
      color: var(--brand-700);
    }
  }
}

.route-image-wrapper {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.route-cover {
  width: 100%;
  height: 100%;
  transition: transform var(--transition-slow);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  color: var(--text-muted);
}

.ai-badge {
  position: absolute;
  top: var(--space-sm);
  right: var(--space-sm);
  background: var(--gradient-mint);
  color: #fff;
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 14px 30px -22px rgba(16, 185, 129, 0.75);
}

.route-content {
  padding: var(--space-md);
}

.route-name {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--transition-fast);
}

.route-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0 0 var(--space-md);
  min-height: 40px;
}

.route-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  margin-bottom: var(--space-md);
}

.route-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-light);
}

.price-area {
  display: flex;
  align-items: baseline;
  
  .price-symbol {
    font-size: var(--text-sm);
    color: var(--color-danger);
    font-weight: var(--font-normal);
  }
  
  .price {
    font-size: var(--text-2xl);
    font-weight: var(--font-bold);
    color: var(--color-danger);
    margin: 0 2px;
  }
  
  .price-unit {
    font-size: var(--text-xs);
    color: var(--text-tertiary);
  }
}

.stats {
  display: flex;
  gap: var(--space-md);
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: var(--space-xs);
    font-size: var(--text-sm);
    color: var(--text-tertiary);
  }
}

// ============================================
// Empty State
// ============================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl);
  
  .empty-icon {
    color: var(--border-medium);
    margin-bottom: var(--space-md);
  }
  
  .empty-title {
    font-size: var(--text-lg);
    font-weight: var(--font-semibold);
    color: var(--text-secondary);
    margin: 0 0 var(--space-xs);
  }
  
  .empty-desc {
    font-size: var(--text-sm);
    color: var(--text-tertiary);
    margin: 0;
  }
}

// ============================================
// Pagination
// ============================================
.pagination-wrapper {
  margin-top: var(--space-xl);
  display: flex;
  justify-content: center;
}

// ============================================
// Responsive
// ============================================
@media (max-width: 768px) {
  .filter-section {
    .filter-form {
      flex-direction: column;
      
      :deep(.el-form-item) {
        margin-right: 0;
        margin-bottom: var(--space-sm);
        
        &:last-child {
          margin-bottom: 0;
        }
      }
      
      .filter-input,
      .filter-select {
        width: 100%;
      }
    }
  }
  
  .route-grid {
    grid-template-columns: 1fr;
    gap: var(--space-md);
  }
  
  .route-image-wrapper {
    height: 200px;
  }
}
</style>
