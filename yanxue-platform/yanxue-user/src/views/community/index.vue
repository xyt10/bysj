<template>
  <div class="community-page">
    <div class="card header-card">
      <h2 class="page-title">研学社区</h2>
      <p class="page-desc">分享你的研学成果，与同学们交流学习心得</p>
    </div>

    <div v-loading="loading" class="achievement-list">
      <div v-for="item in achievements" :key="item.id" class="achievement-card card">
        <!-- 用户信息 -->
        <div class="user-header">
          <el-avatar :size="40" :src="item.userAvatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="user-info">
            <div class="user-name">{{ item.userName }}</div>
            <div class="post-time">{{ item.createdAt }}</div>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="content-area">
          <h3 class="title">{{ item.title }}</h3>
          <p class="content">{{ item.content }}</p>

          <!-- 图片展示 -->
          <div v-if="item.images && item.images.length" class="image-grid">
            <el-image
              v-for="(img, index) in item.images.slice(0, 4)"
              :key="index"
              :src="img"
              fit="cover"
              :preview-src-list="item.images"
              :initial-index="index"
            />
          </div>

          <!-- 关联路线 -->
          <div v-if="item.routeName" class="route-tag" @click="goToRoute(item.routeId)">
            <el-icon><Location /></el-icon>
            {{ item.routeName }}
          </div>
        </div>

        <!-- 互动区域 -->
        <div class="action-area">
          <div class="action-item" @click="handleLike(item)">
            <el-icon :color="item.isLiked ? '#ff3b30' : ''">
              <component :is="item.isLiked ? StarFilled : Star" />
            </el-icon>
            <span>{{ item.likeCount }}</span>
          </div>
          <div class="action-item">
            <el-icon><View /></el-icon>
            <span>{{ item.viewCount }}</span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && achievements.length === 0" description="暂无研学成果">
        <p class="empty-hint">快去参加研学活动，分享你的精彩时刻吧</p>
      </el-empty>

      <!-- 加载更多 -->
      <div v-if="hasMore && achievements.length > 0" class="load-more">
        <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Location, Star, StarFilled, View } from '@element-plus/icons-vue'
import { communityApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const loadingMore = ref(false)
const achievements = ref([])
const page = ref(1)
const hasMore = ref(true)

const loadAchievements = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const res = await communityApi.list({
      page: page.value,
      pageSize: 10
    })
    const newList = res.data?.records || []

    if (isLoadMore) {
      achievements.value = [...achievements.value, ...newList]
    } else {
      achievements.value = newList
    }

    hasMore.value = newList.length >= 10
  } catch (e) {
    console.log('加载成果失败', e)
    // 模拟数据
    const mockData = [
      {
        id: 1,
        userId: 1,
        userName: '小明同学',
        userAvatar: null,
        title: '广州博物馆研学之旅',
        content: '今天参观了广东省博物馆，了解了很多岭南文化的历史，特别是看到了南越王墓出土的文物，感受到了古人的智慧。导游老师讲解得很详细，让我对历史产生了浓厚的兴趣。',
        images: ['https://via.placeholder.com/200?text=研学照片1', 'https://via.placeholder.com/200?text=研学照片2'],
        routeId: 1,
        routeName: '广州历史文化两日研学',
        likeCount: 56,
        viewCount: 328,
        isLiked: false,
        createdAt: '2024-01-15'
      },
      {
        id: 2,
        userId: 2,
        userName: '小红同学',
        userAvatar: null,
        title: '科技探索营收获满满',
        content: '在广州科学中心体验了很多有趣的科学实验，特别是VR体验区让我仿佛置身于太空中。科技真的太神奇了，以后我也想成为一名科学家！',
        images: ['https://via.placeholder.com/200?text=科技体验'],
        routeId: 2,
        routeName: '科技探索一日营',
        likeCount: 42,
        viewCount: 215,
        isLiked: true,
        createdAt: '2024-01-14'
      },
      {
        id: 3,
        userId: 3,
        userName: '小华同学',
        userAvatar: null,
        title: '红色教育让我更懂感恩',
        content: '参观黄埔军校旧址，了解了革命先辈们的英勇事迹，深深感受到今天幸福生活的来之不易。我们要珍惜现在，努力学习，为祖国的未来贡献力量。',
        images: [],
        routeId: 3,
        routeName: '红色教育主题研学',
        likeCount: 38,
        viewCount: 186,
        isLiked: false,
        createdAt: '2024-01-12'
      }
    ]

    if (!isLoadMore) {
      achievements.value = mockData
    }
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  page.value++
  loadAchievements(true)
}

const handleLike = async (item) => {
  try {
    await communityApi.toggleLike(item.id, !item.isLiked)
    item.isLiked = !item.isLiked
    item.likeCount += item.isLiked ? 1 : -1
  } catch (e) {
    // 模拟
    item.isLiked = !item.isLiked
    item.likeCount += item.isLiked ? 1 : -1
  }
}

const goToRoute = (routeId) => {
  if (routeId) {
    router.push(`/route/${routeId}`)
  }
}

onMounted(() => {
  loadAchievements()
})
</script>

<style lang="scss" scoped>
.community-page {
  padding: 20px 0;
  animation: communityIn 420ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes communityIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card {
  background: var(--surface-strong);
  border-radius: var(--r-lg);
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid rgba(15, 118, 110, 0.06);
  box-shadow: var(--shadow-sm);
}

.header-card {
  text-align: center;
  background: var(--gradient-mint);
  color: #fff;

  .page-title {
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 8px;
  }

  .page-desc {
    font-size: 14px;
    opacity: 0.9;
    margin: 0;
  }
}

.achievement-list {
  min-height: 300px;
}

.achievement-card {
  .user-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .user-info {
      .user-name {
        font-size: 15px;
        font-weight: 500;
        color: #333;
      }

      .post-time {
        font-size: 12px;
        color: #999;
        margin-top: 2px;
      }
    }
  }

  .content-area {
    .title {
      font-size: 17px;
      font-weight: 600;
      color: #333;
      margin: 0 0 12px;
    }

    .content {
      font-size: 14px;
      color: #666;
      line-height: 1.8;
      margin: 0 0 16px;
    }

    .image-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
      margin-bottom: 16px;

      .el-image {
        width: 100%;
        height: 120px;
        border-radius: 8px;
      }
    }

    .route-tag {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 7px 12px;
      background: rgba(204, 251, 241, 0.55);
      color: var(--brand-700);
      border-radius: 999px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      border: 1px solid rgba(20, 184, 166, 0.18);
      transition: transform var(--transition-base), background var(--transition-base), box-shadow var(--transition-base);

      &:hover {
        transform: translateY(-1px);
        background: rgba(240, 253, 250, 0.7);
        box-shadow: 0 16px 34px -26px rgba(14, 165, 233, 0.6);
      }
    }
  }

  .action-area {
    display: flex;
    gap: 24px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;

    .action-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #999;
      cursor: pointer;

      &:hover {
        color: #666;
      }
    }
  }
}

.empty-hint {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}
</style>
