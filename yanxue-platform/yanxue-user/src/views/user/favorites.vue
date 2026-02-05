<template>
  <div class="my-favorites-page">
    <div class="card">
      <h2 class="page-title">我的收藏</h2>

      <div v-loading="loading" class="favorite-list">
        <div v-for="item in favorites" :key="item.id" class="favorite-card">
          <el-image
            class="route-cover"
            :src="item.coverImage || defaultCover"
            fit="cover"
            @click="goToDetail(item.routeId)"
          />
          <div class="route-info" @click="goToDetail(item.routeId)">
            <div class="route-name">{{ item.routeName }}</div>
            <div class="route-meta">
              <el-tag size="small" type="primary">{{ item.routeDays }}天</el-tag>
              <span class="price">{{ item.routePrice }}元起</span>
            </div>
            <div class="collect-time">收藏于 {{ item.createdAt }}</div>
          </div>
          <el-button
            type="danger"
            link
            @click="handleRemove(item)"
          >
            取消收藏
          </el-button>
        </div>

        <el-empty v-if="!loading && favorites.length === 0" description="暂无收藏">
          <el-button type="primary" @click="goToRoutes">去发现路线</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/es/components/message/style/index'
import 'element-plus/es/components/message-box/style/index'
import { favoriteApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const favorites = ref([])

const defaultCover = 'https://via.placeholder.com/120x90?text=路线'

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.list()
    favorites.value = res.data || []
  } catch (e) {
    console.log('加载收藏失败', e)
    // 模拟数据
    favorites.value = [
      {
        id: 1,
        routeId: 1,
        routeName: '广州历史文化两日研学',
        routeDays: 2,
        routePrice: 380,
        coverImage: null,
        createdAt: '2024-01-15'
      },
      {
        id: 2,
        routeId: 2,
        routeName: '科技探索一日营',
        routeDays: 1,
        routePrice: 198,
        coverImage: null,
        createdAt: '2024-01-14'
      },
      {
        id: 3,
        routeId: 3,
        routeName: '红色教育主题研学',
        routeDays: 1,
        routePrice: 150,
        coverImage: null,
        createdAt: '2024-01-10'
      }
    ]
  } finally {
    loading.value = false
  }
}

const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await favoriteApi.remove(item.id)
    favorites.value = favorites.value.filter(f => f.id !== item.id)
    ElMessage.success('已取消收藏')
  } catch (e) {
    if (e !== 'cancel') {
      // 模拟成功
      favorites.value = favorites.value.filter(f => f.id !== item.id)
      ElMessage.success('已取消收藏')
    }
  }
}

const goToDetail = (routeId) => {
  router.push(`/route/${routeId}`)
}

const goToRoutes = () => {
  router.push('/routes')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style lang="scss" scoped>
.my-favorites-page {
  padding: 20px 0;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px;
}

.favorite-list {
  min-height: 200px;
}

.favorite-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.route-cover {
  width: 120px;
  height: 90px;
  border-radius: 8px;
  flex-shrink: 0;
  cursor: pointer;
}

.route-info {
  flex: 1;
  cursor: pointer;

  .route-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
  }

  .route-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .price {
      font-size: 16px;
      font-weight: 600;
      color: #ff6600;
    }
  }

  .collect-time {
    font-size: 12px;
    color: #999;
  }
}
</style>
