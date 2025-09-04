import { http } from './http'

export interface StudentNotification {
  id: number
  title: string
  content: string
  type?: string
  relatedId?: number
  createdAt: string
  read: boolean
}

// 列表接口分页返回 Page 结构
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number // 当前页
}

// 获取当前登录学生的通知列表，unread=true 仅取未读
export function apiGetStudentNotifications(params?: { unread?: boolean; page?: number; size?: number }) {
  return http.get<PageResult<StudentNotification>>('/notifications', { params })
}
// 未读数量
export function apiGetUnreadNotificationCount() {
  return http.get<number>('/notifications/unread-count')
}
// 单条标记已读
export function apiMarkNotificationRead(notificationId: number) {
  return http.post<boolean>(`/notifications/${notificationId}/read`)
}
// 全部标记已读
export function apiMarkAllNotificationsRead() {
  return http.post<number>('/notifications/read-all')
}
