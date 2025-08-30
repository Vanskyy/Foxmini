import { http } from './http'

// 教师档案返回结构（与后端 TeacherProfileResponse 对齐）
export interface TeacherProfileResponse {
  userId: number
  teacherProfileId?: number
  username: string
  title?: string
  department?: string
}

// 更新请求（与后端 UpdateTeacherProfileRequest 一致）
export interface UpdateTeacherProfileRequest {
  title?: string
  department?: string
}

// ===== 实验创建 / 更新相关类型（依据后端 DTO） =====
// 后端 ExperimentCreateRequest 只有 title/description/status/stages/resources 五部分，不包含 objective。
// objective 仅在 ExperimentResponse 与 ExperimentUpdateRequest 中出现（更新时可修改），因此创建时不应发送 objective。
export type StageType = 'CHOICE' | 'FILL' | 'CODE' | 'TEXT'
export type ResourceType = 'FILE' | 'LINK' | 'VIDEO' | 'IMAGE' | 'DOC' | string

export interface EvaluationCriteriaCreateRequest { [key: string]: any }

export interface ExperimentStageCreateRequest {
  title: string
  description?: string
  type: StageType
  content: string
  hint?: string
  maxScore?: number
  sequence?: number
  evaluation?: EvaluationCriteriaCreateRequest
}

// 补充资源的 type 与 size（字节数或由后端定义单位）
export interface ExperimentResourceCreateRequest { 
  name: string; 
  url?: string; 
  type: ResourceType; 
  size?: number; 
}

export interface ExperimentCreateRequest {
  title: string
  description?: string
  status?: string // 可选：DRAFT / PUBLISHED
  stages: ExperimentStageCreateRequest[]
  resources?: ExperimentResourceCreateRequest[]
}

export interface ExperimentCreatedResponse {
  experimentId: number
  stageIds: number[]
  resourceIds: number[]
}

// ===== 实验详情响应（后端当前 ExperimentResponse 仅包含基础字段） =====
export interface ExperimentStage { 
  id: number
  title: string
  description?: string
  type: StageType
  content: string
  hint?: string
  maxScore?: number
  sequence?: number
  evaluation?: any
}
export interface ExperimentResource { 
  id: number
  name: string
  url?: string
  type: ResourceType
  size?: number
}

export interface ExperimentResponse {
  id: number
  title: string
  description?: string
  objective?: string
  status?: string
  ownerUserId?: number
  // 现在后端若返回阶段与资源，可直接使用强类型；若后端暂未返回，则保持 undefined
  stages?: ExperimentStage[]
  resources?: ExperimentResource[]
  createdAt?: string
  updatedAt?: string
}

// ===== 更新请求 =====
export interface ExperimentStageUpdateRequest extends Omit<ExperimentStageCreateRequest, 'sequence'> {
  id?: number
  sequence?: number
}
export interface ExperimentResourceUpdateRequest extends Omit<ExperimentResourceCreateRequest, 'size'> {
  id?: number
  size?: number
}
export interface ExperimentUpdateRequest {
  title?: string
  description?: string
  objective?: string
  status?: string
  stages?: ExperimentStageUpdateRequest[]
  resources?: ExperimentResourceUpdateRequest[]
}

// ===== 分页列表（教师端批量显示） =====
export interface ExperimentListItem {
  id: number
  title: string
  description?: string
  objective?: string
  status?: string
  ownerUserId?: number
  createdAt?: string
  updatedAt?: string
}
export interface PageResult<T> {
  content: T[]
  total: number
  page: number
  size: number
}
export interface ExperimentListQuery {
  page?: number // 前端 1 基
  size?: number
  status?: string
  keyword?: string
}

// Spring Data Page 结构
interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number // 当前页 0 基
}

export function apiGetTeacherProfile(userId: number) {
  return http.get<TeacherProfileResponse>(`/teacher/profile/${userId}`)
}

export function apiUpdateTeacherProfile(userId: number, data: UpdateTeacherProfileRequest) {
  return http.put<TeacherProfileResponse>(`/teacher/profile/${userId}`, data)
}

// 创建实验（POST /api/teacher/experiments?creatorId=xxx）
export function apiCreateTeacherExperiment(creatorId: number, data: ExperimentCreateRequest){
  return http.post<ExperimentCreatedResponse>(`/teacher/experiments?creatorId=${creatorId}`, data)
}

// 获取单个实验详情（GET /api/teacher/experiments/{teacherUserId}/{experimentId}）
export function apiGetTeacherExperiment(teacherUserId: number, experimentId: number){
  return http.get<ExperimentResponse>(`/teacher/experiments/${teacherUserId}/${experimentId}`)
}

// 更新实验（PUT /api/teacher/experiments/{teacherUserId}/{experimentId}）
export function apiUpdateTeacherExperiment(teacherUserId: number, experimentId: number, data: ExperimentUpdateRequest){
  return http.put<ExperimentResponse>(`/teacher/experiments/${teacherUserId}/${experimentId}`, data)
}

// 分页查询教师的实验列表（GET /api/teacher/experiments/{teacherUserId}?page=&size=&status=&keyword=）
export function apiListTeacherExperiments(teacherUserId: number, query: ExperimentListQuery){
  const params: any = {}
  // Spring Data page 为 0 基，前端使用 1 基需减 1
  if(query.page!=null) params.page = Math.max(0, (query.page||1) - 1)
  if(query.size!=null) params.size = query.size
  if(query.status) params.status = query.status
  if(query.keyword) params.keyword = query.keyword
  return http.get<SpringPage<ExperimentListItem>>(`/teacher/experiments/${teacherUserId}`, { params })
    .then(p => ({ content: p.content, total: p.totalElements, page: p.number + 1, size: p.size } as PageResult<ExperimentListItem>))
}

// 删除实验：后端当前未提供删除端点，若后续增加再补充
export function apiDeleteTeacherExperiment(_teacherUserId: number, _experimentId: number){
  return Promise.reject(new Error('后端未实现删除接口'))
}

// 修改实验状态（调用 update）
export function apiChangeTeacherExperimentStatus(teacherUserId: number, experimentId: number, status: string){
  return apiUpdateTeacherExperiment(teacherUserId, experimentId, { status })
}
