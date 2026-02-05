import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('user_token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || '未登录')

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('user_token', newToken)
  }

  const login = async (account, password) => {
    const res = await userApi.login({ account, password })
    setToken(res.data.token)
    await fetchUserInfo()
    return res
  }

  const register = async (data) => {
    const res = await userApi.register(data)
    setToken(res.data.token)
    await fetchUserInfo()
    return res
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await userApi.getCurrentUser()
      userInfo.value = res.data
    } catch (error) {
      logout()
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('user_token')
    router.push('/login')
  }

  const init = async () => {
    if (token.value) {
      await fetchUserInfo()
    }
  }

  return {
    token, userInfo, isLoggedIn, nickname,
    login, register, fetchUserInfo, logout, init
  }
})
