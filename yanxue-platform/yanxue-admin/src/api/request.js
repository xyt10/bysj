import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res?.code !== 200) {
      const message = res?.message || '请求失败'
      ElMessage.error(message)
      if (res?.code === 401) {
        localStorage.removeItem('admin_token')
        router.push('/login')
      }
      return Promise.reject(new Error(message))
    }
    return res
  },
  error => {
    const status = error?.response?.status
    const backendMsg = error?.response?.data?.message
    const msg =
      (status === 401 && '登录已过期，请重新登录') ||
      (error?.code === 'ECONNABORTED' && '请求超时，请稍后重试') ||
      backendMsg ||
      error?.message ||
      '网络错误'

    ElMessage.error(msg)

    if (status === 401) {
      localStorage.removeItem('admin_token')
      router.push('/login')
    }

    return Promise.reject(error)
  }
)

export default request
