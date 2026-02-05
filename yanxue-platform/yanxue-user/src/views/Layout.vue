<template>
  <div class="app-layout">
    <!-- 顶部导航 -->
    <header class="app-header">
      <div class="header-inner">
        <router-link to="/" class="brand">
          <div class="brand-icon">
            <el-icon :size="20"><Compass /></el-icon>
          </div>
          <span class="brand-text">研学旅行</span>
        </router-link>
        
        <div class="user-actions">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/user" class="user-btn">
              <el-icon :size="18"><User /></el-icon>
            </router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="login-link">登录</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部导航栏 -->
    <nav class="tab-bar">
      <router-link to="/" class="tab-item" :class="{ active: $route.path === '/' }">
        <div class="tab-icon">
          <el-icon :size="22"><HomeFilled /></el-icon>
        </div>
        <span>首页</span>
      </router-link>
      
      <router-link to="/routes" class="tab-item" :class="{ active: $route.path.startsWith('/route') }">
        <div class="tab-icon">
          <el-icon :size="22"><Guide /></el-icon>
        </div>
        <span>路线</span>
      </router-link>
      
      <router-link to="/community" class="tab-item" :class="{ active: $route.path === '/community' }">
        <div class="tab-icon">
          <el-icon :size="22"><ChatDotRound /></el-icon>
        </div>
        <span>社区</span>
      </router-link>
      
      <router-link to="/user" class="tab-item" :class="{ active: $route.path.startsWith('/user') }">
        <div class="tab-icon">
          <el-icon :size="22"><User /></el-icon>
        </div>
        <span>我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { Compass, User, HomeFilled, Guide, ChatDotRound } from '@element-plus/icons-vue'

const userStore = useUserStore()
</script>

<style lang="scss" scoped>
.app-layout {
  min-height: 100vh;
  padding-bottom: 70px;
}

/* Header */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  
  .brand-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 4px 12px rgba(20, 184, 166, 0.3);
  }
  
  .brand-text {
    font-size: 20px;
    font-weight: 700;
    background: linear-gradient(135deg, #0f766e 0%, #0ea5e9 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    letter-spacing: -0.5px;
  }
}

.user-actions {
  .user-btn {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(20, 184, 166, 0.1);
    border-radius: 50%;
    color: #0f766e;
    text-decoration: none;
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(20, 184, 166, 0.2);
      transform: rotate(15deg);
    }
  }
  
  .login-link {
    padding: 10px 24px;
    background: linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%);
    color: white;
    border-radius: 24px;
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(20, 184, 166, 0.25);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(20, 184, 166, 0.35);
    }
  }
}

/* Main */
.app-main {
  padding-top: 64px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Tab Bar */
.tab-bar {
  position: fixed;
  bottom: 22px;
  left: 16px;
  right: 16px;
  max-width: 420px;
  margin: 0 auto;
  z-index: 100;

  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.64);
  border-radius: 26px;

  box-shadow:
    0 18px 46px -28px rgba(15, 118, 110, 0.45),
    0 10px 30px rgba(15, 118, 110, 0.10);

  display: flex;
  justify-content: space-around;
  padding: 12px 16px;

  transform: translateY(18px);
  opacity: 0;
  animation: tabBarIn 520ms cubic-bezier(0.2, 0.8, 0.2, 1) 80ms forwards;
}

@keyframes tabBarIn {
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.tab-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  text-decoration: none;
  color: var(--ink-500);
  transition: transform var(--transition-base), color var(--transition-base), background var(--transition-base);
  border-radius: 16px;

  .tab-icon {
    transition: transform var(--transition-base), color var(--transition-base);
  }

  span {
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.02em;
  }

  &::after {
    content: '';
    position: absolute;
    left: 50%;
    top: 4px;
    width: 18px;
    height: 4px;
    border-radius: 999px;
    background: transparent;
    transform: translateX(-50%);
    transition: background var(--transition-base);
  }

  &.active {
    color: var(--brand-700);
    background: rgba(204, 251, 241, 0.58); /* brand-100 */

    &::after {
      background: linear-gradient(90deg, rgba(20, 184, 166, 0.0) 0%, rgba(20, 184, 166, 0.75) 50%, rgba(14, 165, 233, 0.0) 100%);
    }

    .tab-icon {
      transform: translateY(-2px) scale(1.06);
      color: var(--brand-600);
    }
  }

  &:hover:not(.active) {
    color: var(--brand-600);
    background: rgba(240, 253, 250, 0.55);
    transform: translateY(-1px);
  }
}

/* Page Transition */
.page-enter-active,
.page-leave-active {
  transition: opacity 250ms ease, transform 250ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* Responsive */
@media (min-width: 768px) {
  .tab-bar {
    display: none;
  }
  
  .app-layout {
    padding-bottom: 0;
  }
}

@media (max-width: 767px) {
  .app-header {
    display: none;
  }
  
  .app-main {
    padding-top: 0;
  }
  
  .tab-bar {
    left: 20px;
    right: 20px;
    bottom: 24px;
    max-width: none;
  }
}
</style>
