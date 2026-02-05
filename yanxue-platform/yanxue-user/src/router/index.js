import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录', requireAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册', requireAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', tabIndex: 0 }
      },
      {
        path: 'routes',
        name: 'RouteList',
        component: () => import('@/views/route/list.vue'),
        meta: { title: '研学路线', tabIndex: 1 }
      },
      {
        path: 'route/:id',
        name: 'RouteDetail',
        component: () => import('@/views/route/detail.vue'),
        meta: { title: '路线详情' }
      },
      {
        path: 'route/generate',
        name: 'RouteGenerate',
        component: () => import('@/views/route/generate.vue'),
        meta: { title: 'AI生成路线', requireAuth: true }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/index.vue'),
        meta: { title: '研学社区', tabIndex: 2 }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '个人中心', tabIndex: 3, requireAuth: true }
      },
      {
        path: 'user/favorites',
        name: 'MyFavorites',
        component: () => import('@/views/user/favorites.vue'),
        meta: { title: '我的收藏', requireAuth: true }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/profile.vue'),
        meta: { title: '个人资料', requireAuth: true }
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 研学旅行` : '研学旅行'

  const token = localStorage.getItem('user_token')
  if (to.meta.requireAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
