import request from './request'

// 用户相关API
export const userApi = {
  login: (data) => request.post('/user/login', data),
  register: (data) => request.post('/user/register', data),
  getCurrentUser: () => request.get('/user/current'),
  update: (id, data) => request.put(`/user/${id}`, data),
  updateProfile: (id, data) => request.post(`/user/${id}/profile`, data),
  getStats: () => request.get('/user/stats')
}

// 路线相关API
export const routeApi = {
  page: (params) => request.get('/route/page', { params }),
  getById: (id) => request.get(`/route/${id}`),
  getHot: (limit = 6) => request.get('/route/hot', { params: { limit } })
}


// AI相关API
export const aiApi = {
  recommend: (data) => request.post('/ai/recommend', data),
  generateRoute: (data) => request.post('/ai/generate-route', data)
}

// 收藏相关API
export const favoriteApi = {
  list: () => request.get('/user/favorites'),
  add: (routeId) => request.post('/user/favorite', { routeId }),
  remove: (id) => request.delete(`/user/favorite/${id}`)
}

// 社区相关API
export const communityApi = {
  list: (params) => request.get('/achievement/list', { params }),
  getById: (id) => request.get(`/achievement/${id}`),
  toggleLike: (id, like) => request.post(`/achievement/${id}/like`, { like })
}
