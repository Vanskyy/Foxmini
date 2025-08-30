import { http } from './http'

// 学生档案返回 (与后端 StudentProfileResponse 对齐)
export interface StudentProfileResponse {
  userId: number
  studentProfileId?: number
  username: string
  grade?: string
  major?: string
  // 后端当前返回中未包含 realName/phone/email，如后端补充再添加
}

// 更新请求 (允许这些字段传给后端，后端目前只处理已存在字段)
export interface UpdateStudentProfileRequest {
  realName?: string
  grade?: string
  major?: string
  phone?: string
  email?: string
}

// 历史实验记录项 (字段名与后端 StudentExperimentHistoryItem 对齐)
export interface StudentExperimentHistoryItem {
  experimentId: number
  experimentTitle: string
  answerId?: number
  latestScore?: number
  submittedAt: string
}

export function apiGetStudentProfile(userId: number) {
  return http.get<StudentProfileResponse>(`/students/${userId}/profile`)
}
export function apiUpdateStudentProfile(userId: number, data: UpdateStudentProfileRequest) {
  return http.put<StudentProfileResponse>(`/students/${userId}/profile`, data)
}
export function apiGetStudentHistory(userId: number) {
  return http.get<StudentExperimentHistoryItem[]>(`/students/${userId}/history`)
}
