<template>
  <div class="home-page">
    <!-- 顶部 Banner & 搜索 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">探索广阔世界 <span class="highlight">智启未来</span></h1>
        <p class="hero-subtitle">基于 AI 的个性化研学路线推荐</p>
        
        <div class="search-wrapper">
          <div class="search-box">
            <el-icon :size="20" class="search-icon"><Search /></el-icon>
            <input
              v-model="searchKeyword"
              placeholder="搜索目的地、主题、课程..."
              @keyup.enter="goToSearch"
            />
            <button class="search-btn" @click="goToSearch">
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </div>
      </div>
      
      <!-- 装饰背景 -->
      <div class="hero-bg">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
      </div>
    </div>

    <!-- 主题分类 -->
    <div class="section category-section">
      <div class="section-header">
        <h3 class="section-title">探索主题</h3>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.name"
          class="category-item"
          @click="goToTheme(cat.name)"
        >
          <div class="cat-icon-wrapper" :style="{ background: cat.gradient }">
            <el-icon :size="24"><component :is="cat.icon" /></el-icon>
          </div>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- AI智能推荐 -->
    <div class="section">
      <div class="section-header">
        <div class="title-with-icon">
          <div class="icon-box ai-box">
            <el-icon :size="16"><MagicStick /></el-icon>
          </div>
          <h3 class="section-title">AI 精选推荐</h3>
        </div>
        <router-link to="/route/generate" class="see-more-btn">
          定制生成 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      
      <div class="route-scroll" v-loading="recommendLoading">
        <div
          v-for="route in recommendRoutes"
          :key="route.id"
          class="route-card-modern"
          @click="goToDetail(route.id)"
        >
          <div class="card-image">
            <el-image :src="route.coverImage || defaultCover" fit="cover" loading="lazy">
              <template #error>
                <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="card-badges">
              <span class="badge tag-badge" v-if="route.reason">{{ route.reason }}</span>
              <span class="badge duration-badge"><el-icon><Timer /></el-icon> {{ route.days }}天</span>
            </div>
          </div>
          <div class="card-content">
            <h4 class="route-title">{{ route.name }}</h4>
            <div class="route-meta">
              <div class="price-box">
                <span class="currency">¥</span>
                <span class="amount">{{ route.price }}</span>
              </div>
              <span class="view-count" v-if="route.viewCount">{{ formatNumber(route.viewCount) }}人看过</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门路线 -->
    <div class="section">
      <div class="section-header">
        <div class="title-with-icon">
          <div class="icon-box hot-box">
            <el-icon :size="16"><TrendCharts /></el-icon>
          </div>
          <h3 class="section-title">热门路线</h3>
        </div>
        <router-link to="/routes" class="see-more-btn">
          全部路线 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      
      <div class="hot-list" v-loading="hotLoading">
        <div
          v-for="route in hotRoutes"
          :key="route.id"
          class="hot-card-modern"
          @click="goToDetail(route.id)"
        >
          <div class="hot-image">
            <el-image :src="route.coverImage || defaultCover" fit="cover" loading="lazy">
              <template #error>
                <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
          </div>
          <div class="hot-info">
            <h4 class="hot-title">{{ route.name }}</h4>
            <p class="hot-desc">{{ route.description }}</p>
            <div class="hot-footer">
              <div class="price">¥{{ route.price }} <span class="unit">起</span></div>
              <div class="stats">
                <el-icon><View /></el-icon> {{ formatNumber(route.viewCount) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, ArrowRight, Reading, Cpu, Sunrise, Flag, 
  MagicStick, TrendCharts, View, Picture, Timer, Location, Trophy, User
} from '@element-plus/icons-vue'
import { routeApi, aiApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const recommendLoading = ref(false)
const hotLoading = ref(false)
const recommendRoutes = ref([])
const hotRoutes = ref([])

const defaultCover = '/placeholder-route.svg'

const categories = [
  { name: '历史文化', icon: Reading, gradient: 'linear-gradient(135deg, #FF9A9E 0%, #FECFEF 100%)' },
  { name: '科技探索', icon: Cpu, gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' },
  { name: '自然生态', icon: Sunrise, gradient: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)' },
  { name: '红色教育', icon: Flag, gradient: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)' },
  { name: '艺术人文', icon: Trophy, gradient: 'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)' },
  { name: '科学实验', icon: MagicStick, gradient: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)' },
  { name: '户外拓展', icon: Location, gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { name: '社会实践', icon: User, gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
]

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num || 0
}

const loadRecommendRoutes = async () => {
  recommendLoading.value = true
  try {
    const userInfo = userStore.userInfo || {}
    const res = await aiApi.recommend({
      userProfile: {
        grade: userInfo.grade || '初中',
        interests: userInfo.interests || ['历史'],
        budget: '中等',
        preferredDays: 2
      },
      context: '首页推荐'
    })
    if (res.data && res.data.recommendations) {
      recommendRoutes.value = res.data.recommendations.slice(0, 6)
    }
  } catch (e) {
    // Fallback data for demo
    recommendRoutes.value = [
      { id: 1, name: '岭南古建筑探秘之旅', days: 2, price: 380, reason: '历史爱好者' },
      { id: 2, name: '未来科技城AI体验营', days: 1, price: 198, reason: '热门科技' },
      { id: 3, name: '湿地公园生态考察', days: 3, price: 580, reason: '自然探索' },
      { id: 4, name: '博物馆奇妙夜', days: 1, price: 150, reason: '特别推荐' }
    ]
  } finally {
    recommendLoading.value = false
  }
}

const loadHotRoutes = async () => {
  hotLoading.value = true
  try {
    const res = await routeApi.getHot(5)
    hotRoutes.value = res.data || []
  } catch (e) {
    hotRoutes.value = [
      { id: 1, name: '探寻千年商都的记忆', description: '深入广州十三行，了解海上丝绸之路的历史', price: 380, viewCount: 12560 },
      { id: 2, name: '大疆创新无人机研学', description: '亲手操控无人机，学习飞行原理与编程', price: 680, viewCount: 8920 },
      { id: 3, name: '华南植物园自然笔记', description: '认识珍稀植物，制作植物标本', price: 120, viewCount: 5670 }
    ]
  } finally {
    hotLoading.value = false
  }
}

const goToSearch = () => {
  router.push({ path: '/routes', query: { keyword: searchKeyword.value } })
}

const goToTheme = (theme) => {
  router.push({ path: '/routes', query: { theme } })
}

const goToDetail = (id) => {
  if (!id || id <= 0) {
    ElMessage.warning('该路线暂无详情页')
    return
  }
  router.push(`/route/${id}`)
}

onMounted(() => {
  loadRecommendRoutes()
  loadHotRoutes()
})
</script>

<style lang="scss" scoped>
.home-page {
  padding-bottom: 90px;
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 32px 20px 40px;
  overflow: hidden;
  background: transparent;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.hero-title {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px;
  line-height: 1.2;
  
  .highlight {
    background: linear-gradient(120deg, #0ea5e9 0%, #14b8a6 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.hero-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
  
  .orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.4;
  }
  
  .orb-1 {
    width: 200px;
    height: 200px;
    background: #ccfbf1;
    top: -50px;
    left: -50px;
  }
  
  .orb-2 {
    width: 250px;
    height: 250px;
    background: #e0f2fe;
    bottom: -50px;
    right: -80px;
  }
}

/* Search Box */
.search-wrapper {
  max-width: 500px;
  margin: 0 auto;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 50px;
  padding: 6px;
  box-shadow: 0 8px 32px -8px rgba(15, 118, 110, 0.15);
  transition: all 0.3s ease;
  
  &:focus-within {
    background: #fff;
    box-shadow: 0 12px 40px -8px rgba(14, 165, 233, 0.25);
    border-color: rgba(14, 165, 233, 0.3);
  }
  
  .search-icon {
    color: #94a3b8;
    margin-left: 12px;
  }
  
  input {
    flex: 1;
    border: none;
    background: transparent;
    padding: 12px;
    font-size: 15px;
    color: #334155;
    outline: none;
    
    &::placeholder {
      color: #cbd5e1;
    }
  }
  
  .search-btn {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: var(--gradient-ocean);
    border: none;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: transform 0.2s;
    
    &:active {
      transform: scale(0.95);
    }
  }
}

/* Sections */
.section {
  padding: 0 20px;
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .title-with-icon {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .icon-box {
    width: 24px;
    height: 24px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    
    &.ai-box { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
    &.hot-box { background: linear-gradient(135deg, #ef4444, #f87171); }
  }
  
  .section-title {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
  }
  
  .see-more-btn {
    font-size: 13px;
    color: #64748b;
    text-decoration: none;
    display: flex;
    align-items: center;
    gap: 2px;
    
    &:hover {
      color: #0ea5e9;
    }
  }
}

/* Category Grid */
.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  
  .cat-icon-wrapper {
    width: 52px;
    height: 52px;
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  
  &:hover .cat-icon-wrapper {
    transform: scale(1.1) rotate(-3deg);
    box-shadow: 0 8px 16px rgba(0,0,0,0.12);
    border-radius: 14px;
  }
  
  .cat-name {
    font-size: 12px;
    color: #475569;
    font-weight: 500;
  }
}

/* Modern Route Card (Scroll) */
.route-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 4px;
  margin: -4px -20px -4px -20px; /* Bleed out */
  padding-left: 20px;
  padding-right: 20px;
  -webkit-overflow-scrolling: touch;
  
  &::-webkit-scrollbar { display: none; }
}

.route-card-modern {
  flex-shrink: 0;
  width: 200px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: 1px solid rgba(0,0,0,0.03);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0,0,0,0.1);
  }
  
  .card-image {
    height: 120px;
    position: relative;
    
    .el-image { width: 100%; height: 100%; }
    
    .card-badges {
      position: absolute;
      top: 8px;
      left: 8px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    
    .badge {
      background: rgba(0,0,0,0.6);
      backdrop-filter: blur(4px);
      color: white;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 4px;
      display: inline-flex;
      align-items: center;
      gap: 2px;
    }
    
    .tag-badge {
      background: linear-gradient(90deg, #f59e0b, #fbbf24);
      font-weight: 700;
    }
  }
  
  .card-content {
    padding: 12px;
    
    .route-title {
      font-size: 15px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 8px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .route-meta {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      
      .price-box {
        color: #ef4444;
        font-weight: 800;
        .currency { font-size: 12px; margin-right: 1px; }
        .amount { font-size: 18px; }
      }
      
      .view-count {
        font-size: 11px;
        color: #94a3b8;
      }
    }
  }
}

/* Hot List (Vertical) */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hot-card-modern {
  display: flex;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.02);
  transition: transform 0.2s;
  cursor: pointer;
  
  &:active { transform: scale(0.98); }
  
  .hot-image {
    width: 100px;
    height: 100px;
    flex-shrink: 0;
    
    .el-image { width: 100%; height: 100%; }
  }
  
  .hot-info {
    flex: 1;
    padding: 12px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .hot-title {
      font-size: 15px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 4px;
      line-height: 1.3;
    }
    
    .hot-desc {
      font-size: 12px;
      color: #64748b;
      margin: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
    }
    
    .hot-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 8px;
      
      .price {
        color: #ef4444;
        font-weight: 700;
        font-size: 16px;
        .unit { font-size: 11px; font-weight: 400; color: #94a3b8; }
      }
      
      .stats {
        font-size: 11px;
        color: #94a3b8;
        display: flex;
        align-items: center;
        gap: 2px;
      }
    }
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cbd5e1;
  font-size: 24px;
}
</style>
