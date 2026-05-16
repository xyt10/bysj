<template>
  <div class="route-detail-page" v-loading="loading">
    <template v-if="route">
      <!-- 封面图 -->
      <div class="cover-section">
        <el-image
          class="cover-image"
          :src="route.coverImage || defaultCover"
          fit="cover"
        >
          <template #error>
            <img class="cover-image" :src="defaultCover" alt="placeholder" />
          </template>
        </el-image>
      </div>

      <!-- 基本信息 -->
      <div class="card info-card">
        <h1 class="route-name">{{ route.name }}</h1>
        <div class="route-meta">
          <el-tag type="primary">{{ route.days }}天</el-tag>
          <el-tag v-if="route.source === 'ai'" type="success">AI生成</el-tag>
          <span class="view-count"><el-icon><View /></el-icon> {{ route.viewCount || 0 }}人浏览</span>
        </div>
        <p class="route-desc">{{ route.description }}</p>
        <div class="route-info-row">
          <div class="info-item">
            <span class="label">适合学段</span>
            <span class="value">{{ formatGrades(route.suitableGrades) }}</span>
          </div>
          <div class="info-item">
            <span class="label">主题标签</span>
            <span class="value">
              <el-tag v-for="theme in parseThemes(route.themes)" :key="theme" size="small">{{ theme }}</el-tag>
            </span>
          </div>
        </div>
        <div class="route-price">
          <span class="label">参考价格：</span>
          <span class="price">{{ route.price }}</span>
          <span class="unit">元起/人</span>
        </div>
      </div>

      <!-- 行程安排 -->
      <div class="card schedule-card">
        <h2 class="section-title">行程安排</h2>
        <div v-if="groupedSchedules.length > 0" class="schedule-list">
          <div v-for="day in groupedSchedules" :key="day.dayNum" class="schedule-day">
            <div class="day-header">
              <el-tag type="primary" effect="dark">第{{ day.dayNum }}天</el-tag>
            </div>
            <div v-for="spot in day.spots" :key="spot.id" class="spot-item">
              <div class="spot-time">
                <el-icon><Clock /></el-icon>
                {{ spot.startTime }} - {{ spot.endTime }}
              </div>
              <div class="spot-name">{{ spot.spotName || '研学点位' }}</div>
              <div v-if="spot.activities" class="spot-activity">
                <el-icon><Tickets /></el-icon>
                <span>{{ spot.activities }}</span>
              </div>
              <div v-if="spot.tips" class="spot-tips">
                <el-icon><InfoFilled /></el-icon>
                <span>{{ spot.tips }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无行程安排" />
      </div>

      <!-- 底部操作栏 -->
      <div class="bottom-bar">
        <div class="left-area">
          <div class="price-area">
            <span class="price">{{ route.price }}</span>
            <span class="unit">元起/人</span>
          </div>
        </div>
        <div class="right-area">
          <el-button @click="toggleFavorite">
            <el-icon v-if="isFavorite"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
            {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
          <el-button type="primary" size="large" @click="goToBook">立即预订</el-button>
        </div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="路线不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Clock, Tickets, InfoFilled, Star, StarFilled } from '@element-plus/icons-vue'
import { routeApi, favoriteApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const routeInfo = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const route = ref(null)
const schedules = ref([])
const isFavorite = ref(false)
const favoriteRecordId = ref(null)

const defaultCover = '/placeholder-route.svg'

// 按天分组行程
const groupedSchedules = computed(() => {
  const groups = {}
  schedules.value.forEach(item => {
    const day = item.dayNum
    if (!groups[day]) {
      groups[day] = []
    }
    groups[day].push(item)
  })
  return Object.keys(groups).sort((a, b) => a - b).map(day => ({
    dayNum: parseInt(day),
    spots: groups[day]
  }))
})

const formatGrades = (grades) => {
  if (!grades) return '全部学段'
  if (Array.isArray(grades)) return grades.join('、')
  try {
    const parsed = JSON.parse(grades)
    return Array.isArray(parsed) ? parsed.join('、') : '全部学段'
  } catch {
    return grades
  }
}

const parseThemes = (themes) => {
  if (!themes) return []
  if (Array.isArray(themes)) return themes
  try {
    const parsed = JSON.parse(themes)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const loadDetail = async () => {
  const rawId = routeInfo.params.id
  const id = Number(rawId)
  if (!Number.isFinite(id) || id <= 0) {
    ElMessage.error('路线ID无效')
    route.value = null
    schedules.value = []
    return
  }

  loading.value = true
  try {
    const res = await routeApi.getById(id)
    // Backend may return either { route, schedules } or route directly
    route.value = res.data?.route || res.data
    schedules.value = res.data?.schedules || []
  } catch (e) {
    console.log('加载详情失败', e)
    // 模拟数据
    route.value = {
      id: id,
      name: '广州历史文化两日研学',
      description: '深入了解广州千年历史文化，探访陈家祠、南越王博物院等历史名胜，感受岭南文化的独特魅力。通过实地参观和互动体验，让学生在游学中增长见识，培养对历史文化的兴趣。',
      days: 2,
      price: 380,
      viewCount: 1256,
      suitableGrades: '["初中","高中"]',
      themes: '["历史文化","岭南文化"]',
      coverImage: null
    }
    schedules.value = [
      { id: 1, dayNum: 1, startTime: '09:00', endTime: '11:30', spotName: '陈家祠', activities: '参观岭南建筑艺术，了解木雕、砖雕、石雕等传统工艺', tips: '请穿着舒适的鞋子' },
      { id: 2, dayNum: 1, startTime: '14:00', endTime: '17:00', spotName: '南越王博物院', activities: '探索南越国历史，观看出土文物', tips: '可携带笔记本记录' },
      { id: 3, dayNum: 2, startTime: '09:00', endTime: '12:00', spotName: '广东省博物馆', activities: '参观自然、历史、艺术展览', tips: '需提前预约' },
      { id: 4, dayNum: 2, startTime: '14:00', endTime: '16:00', spotName: '珠江夜游', activities: '乘船游览珠江两岸风光', tips: '注意防晒' }
    ]
  } finally {
    loading.value = false
  }
}

const loadFavoriteStatus = async () => {
  if (!userStore.isLoggedIn || !route.value?.id) {
    isFavorite.value = false
    favoriteRecordId.value = null
    return
  }

  try {
    const res = await favoriteApi.list()
    const list = res.data || []
    const hit = list.find((x) => String(x.routeId) === String(route.value.id))
    if (hit) {
      isFavorite.value = true
      favoriteRecordId.value = hit.id
    } else {
      isFavorite.value = false
      favoriteRecordId.value = null
    }
  } catch (e) {
    // If favorite API fails, do not block detail page
    console.log('加载收藏状态失败', e)
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: routeInfo.fullPath } })
    return
  }

  if (!route.value?.id) return

  try {
    if (isFavorite.value) {
      // API deletes by favorite record id; if we only have routeId, resolve it by listing first
      if (!favoriteRecordId.value) {
        await loadFavoriteStatus()
      }
      if (favoriteRecordId.value) {
        await favoriteApi.remove(favoriteRecordId.value)
      }
      isFavorite.value = false
      favoriteRecordId.value = null
      ElMessage.success('已取消收藏')
    } else {
      const res = await favoriteApi.add(route.value.id)
      isFavorite.value = true
      favoriteRecordId.value = res.data?.id || favoriteRecordId.value
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    console.log('收藏操作失败', e)
  }
}

const goToBook = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: routeInfo.fullPath } })
    return
  }
  // 订单功能已移除
  ElMessage.info('订单功能已移除')
}

onMounted(async () => {
  await loadDetail()
  await loadFavoriteStatus()
})
</script>

<style lang="scss" scoped>
.route-detail-page {
  padding-bottom: 100px;
  animation: detailIn 420ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes detailIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cover-section {
  margin: -20px -16px 20px;
}

.cover-image {
  width: 100%;
  height: 300px;
}

.card {
  background: var(--surface-strong);
  border-radius: var(--r-lg);
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(15, 118, 110, 0.06);
  box-shadow: var(--shadow-sm);
}

.info-card {
  .route-name {
    font-size: 24px;
    font-weight: 800;
    color: var(--ink-900);
    margin: 0 0 12px;
    letter-spacing: -0.02em;
  }

  .route-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .view-count {
      font-size: 14px;
      color: #999;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .route-desc {
    font-size: 15px;
    color: #666;
    line-height: 1.8;
    margin: 0 0 20px;
  }

  .route-info-row {
    display: flex;
    gap: 40px;
    margin-bottom: 20px;
    padding: 16px;
    background: #f9f9f9;
    border-radius: 8px;

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .label {
        font-size: 13px;
        color: #999;
      }

      .value {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }

  .route-price {
    display: flex;
    align-items: baseline;

    .label {
      font-size: 14px;
      color: var(--ink-600);
    }

    .price {
      font-size: 28px;
      font-weight: 900;
      color: #ef4444;
      margin: 0 4px;
    }

    .unit {
      font-size: 14px;
      color: var(--ink-500);
    }
  }
}

.schedule-card {
  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 20px;
  }
}

.schedule-list {
  .schedule-day {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .day-header {
    margin-bottom: 16px;
  }

  .spot-item {
    padding-left: 16px;
    border-left: 3px solid rgba(20, 184, 166, 0.8);

    .spot-time {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: var(--brand-700);
      margin-bottom: 8px;
    }

    .spot-name {
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin-bottom: 8px;
    }

    .spot-activity {
      display: flex;
      align-items: flex-start;
      gap: 6px;
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      margin-bottom: 8px;

      .el-icon {
        margin-top: 3px;
        flex-shrink: 0;
      }
    }

    .spot-tips {
      display: flex;
      align-items: flex-start;
      gap: 6px;
      font-size: 13px;
      color: #ff9500;
      background: #fff8f0;
      padding: 8px 12px;
      border-radius: 6px;

      .el-icon {
        margin-top: 2px;
        flex-shrink: 0;
      }
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow:
    0 -10px 26px -22px rgba(15, 118, 110, 0.40),
    0 -2px 12px rgba(0, 0, 0, 0.06);
  border-top: 1px solid rgba(255, 255, 255, 0.7);
  z-index: 100;

  .price-area {
    .price {
      font-size: 28px;
      font-weight: 900;
      color: #ef4444;
    }

    .unit {
      font-size: 14px;
      color: var(--ink-500);
      margin-left: 4px;
    }
  }

  .right-area {
    display: flex;
    gap: 12px;
  }
}
</style>
