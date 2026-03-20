import axios from 'axios'
import type { AxiosError } from 'axios'
import type { ApiResponse } from '@/types'

const http = axios.create({
  baseURL: '',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('ai-boot-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>
    if (payload && typeof payload.success === 'boolean' && !payload.success) {
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return response
  },
  (error: AxiosError<{ message?: string }>) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('ai-boot-token')
      localStorage.removeItem('ai-boot-user')
      if (!location.hash.includes('/login') && location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || '网络异常'))
  },
)

export async function request<T>(config: Parameters<typeof http.request>[0]): Promise<T> {
  const response = await http.request<ApiResponse<T>>(config)
  return response.data.data
}

export default http
