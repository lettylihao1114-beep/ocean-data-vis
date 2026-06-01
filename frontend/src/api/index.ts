import request from './request'
import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types'

// 认证
export const authAPI = {
  login: (data: LoginRequest) => request.post<any, LoginResponse>('/api/auth/login', data),
  register: (data: RegisterRequest) => request.post('/api/auth/register', data),
}

// 海洋数据
export const oceanDataAPI = {
  page: (params: any) => request.post('/api/ocean-data/page', params?.query, { params: { pageNum: params?.pageNum || 1, pageSize: params?.pageSize || 20 } }),
  list: (query?: any) => request.post('/api/ocean-data/list', query),
  overview: () => request.get('/api/ocean-data/overview'),
  trend: (params: any) => request.get('/api/ocean-data/trend', { params }),
}

// 监测点位
export const monitorAPI = {
  list: (params?: any) => request.get('/api/monitor-points', { params }),
  getById: (id: number) => request.get(`/api/monitor-points/${id}`),
  add: (data: any) => request.post('/api/monitor-points', data),
  update: (id: number, data: any) => request.put(`/api/monitor-points/${id}`, data),
  delete: (id: number) => request.delete(`/api/monitor-points/${id}`),
}

// 预警
export const alertAPI = {
  active: () => request.get('/api/alerts/active'),
  page: (params?: any) => request.get('/api/alerts', { params }),
  getById: (id: number) => request.get(`/api/alerts/${id}`),
  add: (data: any) => request.post('/api/alerts', data),
  resolve: (id: number) => request.put(`/api/alerts/${id}/resolve`),
}

// 科普知识
export const knowledgeAPI = {
  publicList: (params?: any) => request.get('/api/knowledge/public', { params }),
  detail: (id: number) => request.get(`/api/knowledge/public/${id}`),
  adminList: (params?: any) => request.get('/api/knowledge/admin', { params }),
  add: (data: any) => request.post('/api/knowledge', data),
  update: (id: number, data: any) => request.put(`/api/knowledge/${id}`, data),
  delete: (id: number) => request.delete(`/api/knowledge/${id}`),
}

// 导出
export const exportAPI = {
  excel: (query?: any) => request.post('/api/export/excel', query, { responseType: 'blob' }),
  csv: (query?: any) => request.post('/api/export/csv', query, { responseType: 'blob' }),
}
