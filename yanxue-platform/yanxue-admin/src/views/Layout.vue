<template>
  <el-container class="layout-container">
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && sidebarVisible" 
      class="mobile-overlay" 
      @click="closeSidebar"
    ></div>

    <!-- 侧边栏：深色磨砂玻璃风格 -->
    <el-aside 
      :width="sidebarWidth" 
      class="layout-aside"
      :class="{ 'mobile-sidebar': isMobile, 'sidebar-open': sidebarVisible }"
    >
      <div class="logo-container">
        <div class="logo-wrapper">
          <el-icon :size="24" class="logo-icon"><Compass /></el-icon>
        </div>
        <span v-if="!isCollapse || isMobile" class="logo-text">研学平台</span>
        <!-- 移动端关闭按钮 -->
        <div v-if="isMobile" class="close-btn" @click="closeSidebar">
          <el-icon><Close /></el-icon>
        </div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse && !isMobile"
        router
        class="admin-menu"
        :collapse-transition="false"
        unique-opened
        @select="handleMenuSelect"
      >
        <el-menu-item index="/dashboard" class="menu-item">
          <el-icon><DataAnalysis /></el-icon>
          <template #title><span>数据看板</span></template>
        </el-menu-item>
        
        <el-menu-item index="/spot" class="menu-item">
          <el-icon><Location /></el-icon>
          <template #title><span>点位管理</span></template>
        </el-menu-item>
        
        <el-menu-item index="/route" class="menu-item">
          <el-icon><Guide /></el-icon>
          <template #title><span>路线管理</span></template>
        </el-menu-item>

        <el-menu-item index="/user" class="menu-item">
          <el-icon><User /></el-icon>
          <template #title><span>用户管理</span></template>
        </el-menu-item>
        
        <el-menu-item index="/ai-config" class="menu-item">
          <el-icon><MagicStick /></el-icon>
          <template #title><span>AI配置</span></template>
        </el-menu-item>
      </el-menu>
      
      <!-- 底部折叠按钮 (仅桌面端显示) -->
      <div v-if="!isMobile" class="sidebar-footer">
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :class="{ 'is-collapsed': isCollapse }"><Fold /></el-icon>
        </div>
      </div>
    </el-aside>

    <el-container class="main-content-wrapper">
      <!-- 顶部 Header -->
      <el-header class="layout-header">
        <div class="header-left">
          <!-- 移动端汉堡菜单 -->
          <div v-if="isMobile" class="hamburger-btn" @click="toggleSidebar">
            <el-icon :size="24"><Operation /></el-icon>
          </div>
          <h2 class="page-title">{{ currentRouteName }}</h2>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click" class="user-dropdown-trigger">
            <div class="user-card">
              <el-avatar :size="32" class="user-avatar"><el-icon><UserFilled /></el-icon></el-avatar>
              <span class="username" v-if="!isMobile">管理员</span>
              <el-icon class="dropdown-arrow"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item>
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Compass, DataAnalysis, Location, Guide, 
  User, MagicStick, SwitchButton, Fold, CaretBottom, UserFilled,
  Operation, Close
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const isMobile = ref(false)
const sidebarVisible = ref(false)

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta?.title || 'Dashboard')
const sidebarWidth = computed(() => {
  if (isMobile.value) return '280px'
  return isCollapse.value ? '64px' : '260px'
})

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    sidebarVisible.value = false
  }
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const toggleSidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
}

const closeSidebar = () => {
  sidebarVisible.value = false
}

const handleMenuSelect = () => {
  if (isMobile.value) {
    closeSidebar()
  }
}

const handleLogout = () => {
  localStorage.removeItem('admin_token')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
/* 布局容器 */
.layout-container {
  height: 100vh;
  width: 100%;
  background-color: var(--admin-bg);
  overflow: hidden;
}

/* 移动端遮罩层 */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ================== Sidebar ================== */
.layout-aside {
  background: var(--sidebar-bg, rgba(15, 23, 42, 0.95));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s ease;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.2);
  z-index: 20;
}

/* 移动端侧边栏 */
.layout-aside.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  transform: translateX(-100%);
  z-index: 999;
}

.layout-aside.mobile-sidebar.sidebar-open {
  transform: translateX(0);
}

.logo-container {
  height: 70px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
}

.logo-wrapper {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(14, 184, 166, 0.3);
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(to right, #fff, #94a3b8);
  -webkit-background-clip: text;
  color: transparent;
  white-space: nowrap;
}

.close-btn {
  margin-left: auto;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.admin-menu {
  border-right: none;
  background: transparent;
  padding: 10px;
  flex: 1;
  overflow-y: auto;
}

.menu-item {
  margin: 4px 0;
  border-radius: 8px;
  height: 50px;
  color: #94a3b8;
  transition: all 0.2s;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu-item.is-active {
  background-color: rgba(14, 165, 233, 0.15);
  color: #38bdf8;
  font-weight: 500;
  position: relative;
}

.menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  background: #38bdf8;
  border-radius: 0 4px 4px 0;
  box-shadow: 0 0 8px #38bdf8;
}

.menu-item .el-icon {
  font-size: 18px;
  margin-right: 12px;
  vertical-align: middle;
}

/* Sidebar Footer */
.sidebar-footer {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.collapse-btn .el-icon {
  font-size: 20px;
  transition: transform 0.3s;
}

.collapse-btn .el-icon.is-collapsed {
  transform: rotate(180deg);
}

/* ================== Header ================== */
.main-content-wrapper {
  flex-direction: column;
  background-color: var(--admin-bg, #f8fafc);
  min-width: 0;
}

.layout-header {
  height: 70px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.hamburger-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  color: #334155;
  transition: all 0.2s;
  flex-shrink: 0;
}

.hamburger-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 30px;
  background: #fff;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.user-avatar {
  background: #ccfbf1;
  color: #0d9488;
  border: 2px solid #fff;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.dropdown-arrow {
  font-size: 12px;
  color: #94a3b8;
}

/* ================== Main ================== */
.layout-main {
  padding: 16px;
  overflow-y: auto;
  position: relative;
}

/* Transitions */
.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* ================== 响应式适配 ================== */
@media (min-width: 768px) {
  .layout-header {
    padding: 0 32px;
  }
  
  .layout-main {
    padding: 24px 32px;
  }
  
  .page-title {
    font-size: 20px;
  }
}

@media (max-width: 767px) {
  .layout-aside:not(.mobile-sidebar) {
    display: none;
  }
}
</style>
