import { http } from './http'

// 教师档案返回结构（与后端 TeacherProfileResponse 对齐）
export interface TeacherProfileResponse {
  userId: number
  teacherProfileId?: number
  username: string
  realName?: string
  email?: string
  avatar?: string
  teacherId?: string
  title?: string
  department?: string
  createdAt?: string
  updatedAt?: string
}

// 更新请求（与后端 UpdateTeacherProfileRequest 一致）
export interface UpdateTeacherProfileRequest {
  title?: string
  department?: string
  teacherId?: string
  realName?: string
  email?: string
}

// 仅保留教师个人信息相关 API
export function apiGetTeacherProfile(userId: number) {
  return http.get<TeacherProfileResponse>(`/teacher/profile/${userId}`)
}

export function apiUpdateTeacherProfile(userId: number, data: UpdateTeacherProfileRequest) {
  return http.put<TeacherProfileResponse>(`/teacher/profile/${userId}`, data)
}

// 实验列表返回分页结构
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface TeacherExperimentItem {
  id: number
  title: string
  description?: string
  objective?: string
  status?: string
  ownerUserId?: number
  createdAt?: string
  updatedAt?: string
}

export interface TeacherExperimentQuery {
  page?: number
  size?: number
  keyword?: string
  status?: string
}

export function apiListTeacherExperiments(teacherUserId: number, params: TeacherExperimentQuery) {
  const query = new URLSearchParams()
  if (params.page != null) query.append('page', String(params.page))
  if (params.size != null) query.append('size', String(params.size))
  if (params.keyword) query.append('keyword', params.keyword)
  if (params.status) query.append('status', params.status)
  const qs = query.toString()
  return http.get<PageResult<TeacherExperimentItem>>(
    `/teacher/experiments/${teacherUserId}` + (qs ? `?${qs}` : '')
  )
}
