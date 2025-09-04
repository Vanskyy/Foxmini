import { http } from './http'

// 分组基础类型
export interface GroupResponse {
  id: number
  name: string
  description?: string
  memberCount: number
  createdAt?: string
}
export interface GroupMemberResponse {
  userId: number
  username: string
  realName?: string
  joinedAt?: string
}
export interface GroupCreateRequest { name: string; description?: string }
export interface GroupUpdateRequest { name?: string; description?: string }

export interface BatchIdsRequest { userIds: number[] }
export interface BatchAddStudentsResult { successIds: number[]; alreadyInGroupIds: number[]; notFoundIds: number[] }
export interface BatchRemoveStudentsResult { removedIds: number[]; notInGroupIds: number[] }

// CRUD
export function apiListGroups(creatorId: number) {
  return http.get<GroupResponse[]>(`/teacher/groups?creatorId=${creatorId}`)
}
export function apiCreateGroup(creatorId: number, data: GroupCreateRequest) {
  return http.post<GroupResponse>(`/teacher/groups?creatorId=${creatorId}`, data)
}
export function apiUpdateGroup(groupId: number, data: GroupUpdateRequest) {
  return http.put<GroupResponse>(`/teacher/groups/${groupId}`, data)
}
export function apiDeleteGroup(groupId: number) {
  return http.del<void>(`/teacher/groups/${groupId}`)
}
export function apiListGroupMembers(groupId: number) {
  return http.get<GroupMemberResponse[]>(`/teacher/groups/${groupId}/members`)
}
// 单个成员操作
export function apiAddStudentToGroup(groupId: number, userId: number) {
  return http.post<void>(`/teacher/groups/${groupId}/members?userId=${userId}`)
}
export function apiRemoveStudentFromGroup(groupId: number, userId: number) {
  return http.del<void>(`/teacher/groups/${groupId}/members/${userId}`)
}
export function apiMoveStudent(fromGroupId: number, toGroupId: number, userId: number) {
  const qs = `fromGroupId=${fromGroupId}&toGroupId=${toGroupId}&userId=${userId}`
  return http.post<void>(`/teacher/groups/move-simple?${qs}`)
}
// 批量
export function apiBatchAdd(groupId: number, userIds: number[]) {
  return http.post<BatchAddStudentsResult>(`/teacher/groups/${groupId}/members/batch`, { userIds })
}
export function apiBatchRemove(groupId: number, userIds: number[]) {
  return http.post<BatchRemoveStudentsResult>(`/teacher/groups/${groupId}/members/batch-remove`, { userIds })
}
