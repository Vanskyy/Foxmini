import axios, { AxiosError } from 'axios'
import type { AxiosRequestConfig } from 'axios'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    ;(config.headers as any)['Authorization'] = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (res) => res.data,
  (error: AxiosError<any>) => {
    let message = '请求失败'
    if (error.response) {
      const data: any = error.response.data
      message = data?.message || data?.error || error.message || message
    } else if (error.request) {
      message = '网络无响应'
    } else {
      message = error.message
    }
    return Promise.reject(new Error(message))
  }
)

// 通过指定第二泛型参数为返回类型，直接得到 T 而不是 AxiosResponse<T>
export function get<T = any>(url: string, config?: AxiosRequestConfig) { return instance.get<T, T>(url, config) }
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) { return instance.post<T, T>(url, data, config) }
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) { return instance.put<T, T>(url, data, config) }
export function del<T = any>(url: string, config?: AxiosRequestConfig) { return instance.delete<T, T>(url, config) }

export const http = { get, post, put, del, instance }
