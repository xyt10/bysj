import axios from 'axios'
import { ElMessage } from 'element-plus'
import 'element-plus/es/components/message/style/index'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 90000  // AI调用需要更长时间,设置为90秒
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('user_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res?.code !== 200) {
      const message = res?.message || '请求失败'
      // 延迟显示错误消息，避免阻塞页面加载
      setTimeout(() => {
        try {
          ElMessage.error(message)
        } catch (e) {
          console.error('Message error:', message)
        }
      }, 100)
      if (res?.code === 401) {
        localStorage.removeItem('user_token')
        router.push('/login')
      }
      return Promise.reject(new Error(message))
    }
    return res
  },
  error => {
    const status = error?.response?.status
    const backendMsg = error?.response?.data?.message

    // 500 错误时静默处理，让页面的 fallback 逻辑生效
    if (status === 500) {
      console.error('Server error:', backendMsg || 'Internal Server Error')
      return Promise.reject(error)
    }

    const msg =
      (status === 401 && '登录已过期，请重新登录') ||
      (error?.code === 'ECONNABORTED' && '请求超时，请稍后重试') ||
      backendMsg ||
      error?.message ||
      '网络错误'

    // 延迟显示错误消息
    setTimeout(() => {
      try {
        ElMessage.error(msg)
      } catch (e) {
        console.error('Error:', msg)
      }
    }, 100)

    if (status === 401) {
      localStorage.removeItem('user_token')
      router.push('/login')
    }

    return Promise.reject(error)
  }
)

export default request
