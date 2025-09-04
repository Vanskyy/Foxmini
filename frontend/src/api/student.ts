import { http } from './http'

// 学生档案返回 (与后端 StudentProfileResponse 对齐)
export interface StudentProfileResponse {
  userId: number
  studentProfileId?: number
  username: string
  grade?: string
  major?: string
  // 新增字段 (后端已扩展)
  realName?: string
  email?: string
  studentId?: string
  className?: string
  updatedAt?: string
}

// 更新请求 (允许这些字段传给后端，后端目前只处理已存在字段)
export interface UpdateStudentProfileRequest {
  realName?: string
  grade?: string
  major?: string
  phone?: string
  email?: string
  studentId?: string
  className?: string
}

// 历史实验记录项 (字段名与后端 StudentExperimentHistoryItem 对齐)
export interface StudentExperimentHistoryItem {
  experimentId: number
  experimentTitle: string
  answerId?: number
  latestScore?: number
  submittedAt: string
  // 新增进度相关
  publishedExperimentId?: number
  totalStages?: number
  finishedStages?: number
}

// 当前未完成实验项（与后端 StudentCurrentExperimentItem 对齐）
export interface StudentCurrentExperimentItem {
  publishedExperimentId: number
  experimentId: number
  experimentTitle: string
  deadline?: string
  totalStages?: number
  finishedStages?: number // 仅统计 final 提交阶段数
}

// 实验执行初始化
export interface ExperimentExecutionInitResponse {
  experiment: ExperimentDetailResponse
  progress: ExperimentProgressResponse
  currentAnswer?: StudentAnswerResponse
  allStageDrafts?: StudentAnswerResponse[]
}
export interface ExperimentDetailResponse {
  experimentId: number
  publishedExperimentId: number
  title: string
  description: string
  startTime?: string
  endTime?: string
  stages: ExperimentStageResponse[]
}
export interface ExperimentStageResponse {
  stageId: number
  name: string
  description?: string
  stageType?: string
  orderNo?: number
  title: string
  sequence: number
  content?: string
  hint?: string
  maxScore?: number
  correctAnswer?: string
  evaluation?: any
}
export interface ExperimentProgressResponse {
  publishedExperimentId: number
  totalStages: number
  finishedStages: number // 已 final 提交阶段数
  finalSubmittedStageIds: number[]
  currentStageId?: number
  lastSavedAt?: string
}
export interface StudentAnswerResponse {
  answerId: number
  stageId: number
  publishedExperimentId: number
  answerContent?: string
  codeContent?: string
  finalSubmit: boolean
  savedAt?: string
  submittedAt?: string
  score?: number
  feedback?: string
  passed?: boolean
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
export function apiGetStudentCurrentExperiments(userId: number) {
  return http.get<StudentCurrentExperimentItem[]>(`/students/${userId}/current-experiments`)
}
export function apiInitExperiment(publishedExperimentId: number) {
  return http.get<ExperimentExecutionInitResponse>(`/student/experiments/${publishedExperimentId}/init`)
}
export function apiGetExperimentDetail(publishedExperimentId: number) {
  return http.get<ExperimentDetailResponse>(`/student/experiments/${publishedExperimentId}/detail`)
}
export function apiGetExperimentProgress(publishedExperimentId: number) {
  return http.get<ExperimentProgressResponse>(`/student/experiments/${publishedExperimentId}/progress`)
}
export function apiGetStageAnswer(publishedExperimentId: number, stageId: number) {
  return http.get<StudentAnswerResponse>(`/student/experiments/${publishedExperimentId}/stages/${stageId}/answer`)
}
export function apiSaveStageAnswer(data: { publishedExperimentId:number; stageId:number; answerContent?:string; codeContent?:string }) {
  return http.post<StudentAnswerResponse>(`/student/experiments/answer/save`, data)
}
export function apiFinalSubmitStageAnswer(data: { publishedExperimentId:number; stageId:number; answerContent?:string; codeContent?:string; confirm?:boolean }) {
  return http.post<StudentAnswerResponse>(`/student/experiments/answer/final-submit`, { ...data, confirm: true })
}
