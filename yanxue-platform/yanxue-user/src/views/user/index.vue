<template>
  <div class="profile-page">
    <!-- 用户信息头部 -->
    <div class="profile-header">
      <div class="header-bg"></div>
      <div class="user-info">
        <div class="avatar-wrapper">
          <div class="avatar-ring"></div>
          <el-avatar 
            :size="90" 
            :src="userStore.userInfo?.avatar || '/placeholder-avatar.svg'"
            class="user-avatar"
          >
            <el-icon :size="40"><User /></el-icon>
          </el-avatar>
        </div>
        <div class="user-name">{{ userStore.nickname || '游客用户' }}</div>
        <div class="user-tags">
          <span v-if="userStore.userInfo?.grade" class="tag grade-tag">
            {{ userStore.userInfo.grade }}
          </span>
          <span v-if="userStore.userInfo?.school" class="tag school-tag">
            <el-icon><School /></el-icon> {{ userStore.userInfo.school }}
          </span>
        </div>
        <button class="edit-btn" @click="goToProfile">
          <el-icon><Edit /></el-icon> 编辑资料
        </button>
      </div>
    </div>

    <!-- 统计数据卡片 -->
    <div class="stats-section">
      <div class="stat-card" @click="goToOrders" v-if="false">
        <div class="stat-icon blue">
          <el-icon :size="22"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">0</div>
          <div class="stat-label">我的订单</div>
        </div>
      </div>
      <div class="stat-card" @click="goToFavorites">
        <div class="stat-icon orange">
          <el-icon :size="22"><Star /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.favoriteCount }}</div>
          <div class="stat-label">我的收藏</div>
        </div>
      </div>
      <div class="stat-card" @click="goToCommunity">
        <div class="stat-icon green">
          <el-icon :size="22"><Trophy /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.achievementCount }}</div>
          <div class="stat-label">研学成果</div>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-title">研学服务</div>
      
      <div class="menu-list">
        <div class="menu-item" @click="goToOrders" v-if="false">
          <div class="item-left">
            <div class="item-icon blue">
              <el-icon :size="20"><Document /></el-icon>
            </div>
            <span class="item-text">我的订单</span>
          </div>
          <el-icon class="item-arrow"><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="goToFavorites">
          <div class="item-left">
            <div class="item-icon orange">
              <el-icon :size="20"><Star /></el-icon>
            </div>
            <span class="item-text">我的收藏</span>
          </div>
          <el-icon class="item-arrow"><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="goToCommunity">
          <div class="item-left">
            <div class="item-icon green">
              <el-icon :size="20"><Trophy /></el-icon>
            </div>
            <span class="item-text">研学成果</span>
          </div>
          <el-icon class="item-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-title">设置</div>
      
      <div class="menu-list">
        <div class="menu-item" @click="goToProfile">
          <div class="item-left">
            <div class="item-icon purple">
              <el-icon :size="20"><UserFilled /></el-icon>
            </div>
            <span class="item-text">个人资料</span>
          </div>
          <el-icon class="item-arrow"><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="handleAbout">
          <div class="item-left">
            <div class="item-icon gray">
              <el-icon :size="20"><InfoFilled /></el-icon>
            </div>
            <span class="item-text">关于我们</span>
          </div>
          <el-icon class="item-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <button class="logout-btn" @click="handleLogout">
      <el-icon><SwitchButton /></el-icon>
      退出登录
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/es/components/message/style/index'
import 'element-plus/es/components/message-box/style/index'
import { 
  User, ArrowRight, Document, Star, Trophy, 
  UserFilled, InfoFilled, School, Edit, SwitchButton 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
    favoriteCount: 0,
  achievementCount: 0
})

const loadStats = async () => {
  try {
    const res = await userApi.getStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (e) {
    stats.value = {
      favoriteCount: 0,
      achievementCount: 0
    }
  }
}

const goToOrders = () => ElMessage.info('订单功能已移除')
const goToFavorites = () => router.push('/user/favorites')
const goToCommunity = () => router.push('/community')

const goToProfile = () => {
  router.push('/user/profile')
}

const handleAbout = () => {
  ElMessageBox.alert(
    '<div style="text-align:center;padding:20px 0;">' +
    '<div style="width:60px;height:60px;margin:0 auto 16px;background:linear-gradient(135deg,#6366f1,#8b5cf6);border-radius:16px;display:flex;align-items:center;justify-content:center;color:white;font-size:28px;">🏕️</div>' +
    '<h3 style="margin:0 0 12px;color:#1e293b;font-size:18px;">研学旅行平台</h3>' +
    '<p style="color:#64748b;font-size:14px;line-height:1.6;margin:0;">v1.0.0<br>探索世界，发现美好</p>' +
    '</div>',
    '',
    {
      confirmButtonText: '确定',
      dangerouslyUseHTMLString: true,
      customClass: 'about-dialog',
      showClose: false
    }
  )
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {}
}

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 0 16px 40px;
  max-width: 600px;
  margin: 0 auto;
}

/* Profile Header */
.profile-header {
  position: relative;
  margin: 0 -16px 20px;
  padding: 40px 16px 60px;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%);
  border-radius: 0 0 32px 32px;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(circle at 20% 30%, rgba(255,255,255,0.2) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(255,255,255,0.15) 0%, transparent 40%);
  }
}

.user-info {
  position: relative;
  z-index: 1;
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.avatar-ring {
  position: absolute;
  inset: -6px;
  border: 3px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.05); opacity: 0.8; }
}

.user-avatar {
  border: 4px solid white;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  background: linear-gradient(135deg, #818cf8 0%, #c084fc 100%);
  color: white;
}

.user-name {
  font-size: 22px;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.user-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  font-size: 13px;
  color: white;
  border: 1px solid rgba(255,255,255,0.3);
}

.edit-btn {
  padding: 10px 24px;
  background: rgba(255,255,255,0.9);
  border: none;
  border-radius: 50px;
  font-size: 14px;
  font-weight: 600;
  color: #6366f1;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.15);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0,0,0,0.2);
  }
}

/* Stats Section */
.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
  margin-top: -40px;
  position: relative;
  z-index: 2;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 16px 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0,0,0,0.12);
  }
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  color: white;
  
  &.blue {
    background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  }
  
  &.orange {
    background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  }
  
  &.green {
    background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  }
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
}

/* Menu Section */
.menu-section {
  margin-bottom: 20px;
}

.menu-title {
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  margin-bottom: 12px;
  padding-left: 4px;
}

.menu-list {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f1f5f9;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: #f8fafc;
    
    .item-icon {
      transform: scale(1.1);
    }
    
    .item-arrow {
      color: #6366f1;
      transform: translateX(4px);
    }
  }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: transform 0.2s ease;
  
  &.blue {
    background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  }
  
  &.orange {
    background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  }
  
  &.green {
    background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  }
  
  &.purple {
    background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  }
  
  &.gray {
    background: linear-gradient(135deg, #64748b 0%, #94a3b8 100%);
  }
}

.item-text {
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
}

.item-arrow {
  color: #cbd5e1;
  transition: all 0.2s ease;
}

/* Logout Button */
.logout-btn {
  width: 100%;
  padding: 14px;
  margin-top: 20px;
  background: white;
  border: 1px solid #fecaca;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  color: #ef4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #fef2f2;
    border-color: #ef4444;
  }
}

/* Responsive */
@media (max-width: 380px) {
  .stats-section {
    gap: 8px;
  }
  
  .stat-card {
    padding: 12px 8px;
  }
  
  .stat-icon {
    width: 38px;
    height: 38px;
  }
  
  .stat-value {
    font-size: 18px;
  }
}
</style>
