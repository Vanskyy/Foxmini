import { http } from './http'
import type { LoginRequest, RegisterRequest, UserResponse } from '../types/user'

export function apiLogin(data: LoginRequest) {
  return http.post<UserResponse>('/users/login', data)
}

export function apiRegister(data: RegisterRequest) {
  return http.post<UserResponse>('/users/register', data)
}
