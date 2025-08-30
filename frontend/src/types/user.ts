// 用户与鉴权相关类型定义
export enum Role {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN',
}

export interface LoginRequest { usernameOrEmail: string; password: string }
export interface RegisterRequest {
  username: string
  password: string
  email: string
  realName: string
  role: Role
  studentId?: string
  className?: string
  major?: string
  grade?: string
  teacherId?: string
  department?: string
  title?: string
}

// 与后端 UserResponse 对齐（当前无 token 字段）
export interface UserResponse {
  id?: number
  username: string
  email?: string
  realName?: string
  role: Role
  avatar?: string
  studentId?: string
  teacherId?: string
}
