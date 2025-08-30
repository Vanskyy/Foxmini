import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('../views/auth/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('../views/auth/RegisterView.vue') },
  { path: '/student', name: 'student-home', component: () => import('../views/student/StudentHome.vue') },
  { path: '/student/experiment/:id', name: 'student-experiment-detail', component: () => import('../views/student/ExperimentDetail.vue'), props: true },
  { path: '/teacher', name: 'teacher-home', component: () => import('../views/teacher/TeacherHome.vue') },
  { path: '/teacher/experiment/create', name: 'teacher-experiment-create', component: () => import('../views/teacher/TeacherExperimentCreate.vue') },
  { path: '/admin', name: 'admin-home', component: () => import('../views/admin/AdminHome.vue') },
  { path: '/', redirect: '/login' },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
