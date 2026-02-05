import request from './request'

// 点位相关接口
export const spotApi = {
  page: (params) => request.get('/spot/page', { params }),
  getById: (id) => request.get(`/spot/${id}`),
  create: (data) => request.post('/spot', data),
  update: (id, data) => request.put(`/spot/${id}`, data),
  delete: (id) => request.delete(`/spot/${id}`),
  list: (params) => request.get('/spot/list', { params })
}

// 路线相关接口
export const routeApi = {
  page: (params) => request.get('/route/page', { params }),
  getById: (id) => request.get(`/route/${id}`),
  create: (data) => request.post('/route', data),
  update: (id, data) => request.put(`/route/${id}`, data),
  delete: (id) => request.delete(`/route/${id}`),
  saveSchedule: (id, data) => request.post(`/route/${id}/schedule`, data),
  hot: (limit = 6) => request.get('/route/hot', { params: { limit } })
}


// AI相关接口
export const aiApi = {
  recommend: (data) => request.post('/ai/recommend', data),
  generateRoute: (data) => request.post('/ai/generate-route', data)
}

// AI配置管理接口
export const aiConfigApi = {
  getConfig: () => request.get('/ai-config'),
  saveConfig: (data) => request.post('/ai-config', data),
  testConnection: (provider) => request.post('/ai-config/test', { provider }),
  getLogs: (params) => request.get('/ai-config/logs', { params }),
  getStats: () => request.get('/ai-config/stats')
}

// 用户相关接口
export const userApi = {
  page: (params) => request.get('/user/page', { params }),
  getById: (id) => request.get(`/user/${id}`)
}

// 统计相关接口
export const statisticsApi = {
  dashboard: () => request.get('/statistics/dashboard')
}

// 登录（临时复用用户登录接口；如需区分管理员权限需后端提供 admin login）
export const authApi = {
  login: (data) => request.post('/user/login', data)
}
