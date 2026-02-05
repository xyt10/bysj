import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'spot',
        name: 'Spot',
        component: () => import('@/views/spot/index.vue'),
        meta: { title: '点位管理', icon: 'Location' }
      },
      {
        path: 'route',
        name: 'Route',
        component: () => import('@/views/route/index.vue'),
        meta: { title: '路线管理', icon: 'Guide' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'ai-config',
        name: 'AIConfig',
        component: () => import('@/views/ai-config/index.vue'),
        meta: { title: 'AI配置', icon: 'MagicStick' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta?.title ? `${to.meta.title} - 研学旅行管理端` : '研学旅行管理端'

  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.path === '/login' && token) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
