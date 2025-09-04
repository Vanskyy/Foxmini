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

// 创建实验请求/响应结构
export interface ExperimentCreateRequest {
  title: string
  description?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  stages?: ExperimentStageCreateRequest[]
  resources?: ExperimentResourceCreateRequest[]
}
export interface ExperimentStageCreateRequest {
  title: string
  description?: string
  type: 'CHOICE' | 'FILL' | 'CODE' | 'TEXT'
  content: string
  hint?: string
  maxScore?: number
  sequence?: number
  evaluation?: EvaluationCriteriaCreateRequest
}
export interface EvaluationCriteriaCreateRequest {
  correctOptions?: string
  fillAnswers?: string
  testCases?: string
}
export interface ExperimentResourceCreateRequest {
  name: string
  type: 'DOCUMENT' | 'VIDEO' | 'CODE' | 'OTHER'
  url: string
  size?: number
}
export interface ExperimentCreatedResponse {
  experimentId: number
  stageIds?: number[]
  resourceIds?: number[]
}

export function apiCreateExperiment(creatorId: number, data: ExperimentCreateRequest) {
  return http.post<ExperimentCreatedResponse>(`/teacher/experiments?creatorId=${creatorId}`, data)
}

// 新增：删除实验
export function apiDeleteExperiment(teacherUserId: number, experimentId: number) {
  return http.del<void>(`/teacher/experiments/${teacherUserId}/${experimentId}`)
}

// 更新实验请求（与后端 ExperimentUpdateRequest 一致）
export interface ExperimentUpdateRequest {
  title?: string
  description?: string
  objective?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  stages?: ExperimentStageCreateRequest[] | null // 不传: 不修改; 传空数组: 清空
  resources?: ExperimentResourceCreateRequest[] | null
}

export function apiUpdateExperiment(teacherUserId: number, experimentId: number, data: ExperimentUpdateRequest) {
  return http.put(`/teacher/experiments/${teacherUserId}/${experimentId}`, data)
}

// 获取单个实验详情的类型与API
export interface ExperimentDetailStageResponse {
  id?: number
  title: string
  description?: string
  type: 'CHOICE' | 'FILL' | 'CODE' | 'TEXT'
  content?: string
  hint?: string
  maxScore?: number
  sequence?: number
  evaluation?: EvaluationCriteriaCreateRequest | null
  correctAnswer?: string // 兼容旧字段
}
export interface ExperimentDetailResourceResponse {
  id?: number
  name: string
  type: 'DOCUMENT' | 'VIDEO' | 'CODE' | 'OTHER'
  url: string
  size?: number
  uploadedAt?: string
}
export interface ExperimentResponse {
  id: number // 与后端 ExperimentResponse.id 对齐
  title: string
  description?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  stages?: ExperimentDetailStageResponse[]
  resources?: ExperimentDetailResourceResponse[]
  createdAt?: string
  updatedAt?: string
}

export function apiGetExperiment(teacherUserId: number, experimentId: number) {
  return http.get<ExperimentResponse>(`/teacher/experiments/${teacherUserId}/${experimentId}`)
}

// 发布实验相关请求与类型
export interface PublishExperimentRequest {
  experimentId: number
  startTime: string // ISO LocalDateTime: yyyy-MM-ddTHH:mm / with seconds
  endTime: string
  allowLateSubmission: boolean
  targets: PublishTargetSpec[]
  initialAnnouncement?: AnnouncementCreateRequest
}
export interface PublishTargetSpec {
  targetType: 'ALL' | 'GROUP' | 'INDIVIDUAL'
  targetId?: number | null
  title?: string
  description?: string
}
export interface AnnouncementCreateRequest {
  title: string
  content: string
  important?: boolean
}
export interface AssignmentDTO {
  id?: number
  targetType: 'ALL' | 'GROUP' | 'INDIVIDUAL'
  targetId?: number | null
}
export interface PublishedExperimentResponse {
  id: number
  experimentId: number
  publisherId?: number
  startTime: string
  endTime: string
  allowLateSubmission: boolean
  publishedAt?: string
  assignments?: AssignmentDTO[]
}
export function apiPublishExperiment(teacherUserId: number, data: PublishExperimentRequest) {
  return http.post<PublishedExperimentResponse>(`/published-experiments?teacherUserId=${teacherUserId}`, data)
}
// 新增：下线（提前结束）已发布实验
export function apiUnpublishExperiment(teacherUserId: number, publishId: number) {
  return http.patch<PublishedExperimentResponse>(`/published-experiments/${publishId}/unpublish?teacherUserId=${teacherUserId}`, {})
}

// 公告管理相关类型与API
export interface AnnouncementResponse {
  id: number
  publishedExperimentId: number
  title: string
  content: string
  important: boolean
  createdAt: string
  createdByUserId?: number
}
export function apiListRunningPublishedExperiments() {
  return http.get<PublishedExperimentResponse[]>(`/published-experiments/running`)
}
export function apiListFinishedPublishedExperiments() {
  return http.get<PublishedExperimentResponse[]>(`/published-experiments/finished`)
}
export function apiListAnnouncements(publishedId: number) {
  return http.get<AnnouncementResponse[]>(`/published-experiments/${publishedId}/announcements`)
}
export function apiCreateAnnouncement(publishedId: number, data: AnnouncementCreateRequest, teacherUserId: number) {
  return http.post<AnnouncementResponse>(`/published-experiments/${publishedId}/announcements?teacherUserId=${teacherUserId}`, data)
}

// 新增：删除已结束发布实例
export function apiDeletePublishedExperiment(teacherUserId: number, publishId: number) {
  return http.del<void>(`/published-experiments/${publishId}?teacherUserId=${teacherUserId}`)
}

// 新增：公告更新与删除 API
export interface AnnouncementUpdateRequest { title?: string; content?: string; important?: boolean }

export function apiUpdateAnnouncement(announcementId: number, data: AnnouncementUpdateRequest, teacherUserId: number){
  return http.put<AnnouncementResponse>(`/published-experiments/announcements/${announcementId}?teacherUserId=${teacherUserId}`, data)
}
export function apiDeleteAnnouncement(announcementId: number, teacherUserId: number){
  return http.del<void>(`/published-experiments/announcements/${announcementId}?teacherUserId=${teacherUserId}`)
}
