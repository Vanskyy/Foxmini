<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/modules/auth'
import { Role } from '../../types/user'
import { useAutoScale } from '../../composables/useAutoScale'

const auth = useAuthStore()
const router = useRouter()

const form = ref({
  username: '',
  password: '',
  email: '',
  realName: '',
  role: Role.STUDENT as Role,
  // 学生
  studentId: '', className: '', major: '', grade: '',
  // 教师
  teacherId: '', department: '', title: '',
  // 管理员无需额外邀请码
})

const submitting = ref(false)
const error = ref('')

const isStudent = computed(() => form.value.role === Role.STUDENT)
const isTeacher = computed(() => form.value.role === Role.TEACHER)
const isAdmin = computed(() => form.value.role === Role.ADMIN)

// 自动缩放：基准宽 1280 高 900，注册表单更高一点；最小 0.65
const { style: scaleStyle } = useAutoScale(1280, 900, 0.65, 1)
const finalStyle = computed(() => {
  if (window.innerWidth < 520) return { transform: 'none' }
  return scaleStyle.value
})

async function submit() {
  error.value = ''
  if (!form.value.username || !form.value.password || !form.value.role) {
    error.value = '请填写必填字段'
    return
  }
  // 组装最小必要 payload，避免无关空字段
  const payload: any = {
    username: form.value.username,
    password: form.value.password,
    email: form.value.email,
    realName: form.value.realName,
    role: form.value.role,
  }
  if (isStudent.value) Object.assign(payload, {
    studentId: form.value.studentId,
    className: form.value.className,
    major: form.value.major,
    grade: form.value.grade,
  })
  if (isTeacher.value) Object.assign(payload, {
    teacherId: form.value.teacherId,
    department: form.value.department,
    title: form.value.title,
  })
  submitting.value = true
  try {
    await auth.register(payload)
    if (auth.role === Role.TEACHER) router.push('/teacher')
    else if (auth.role === Role.STUDENT) router.push('/student')
    else if (auth.role === Role.ADMIN) router.push('/admin')
    else router.push('/')
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-bg">
    <div class="register-card" :style="finalStyle">
      <h2 class="title">账号注册</h2>
      <form class="reg-form" @submit.prevent="submit">
        <!-- 顶部角色切换：仅学生 / 教师 -->
        <div class="role-switch" data-span v-if="!isAdmin">
          <span class="role-label">选择角色</span>
          <div class="role-buttons">
            <button type="button" :class="{ active: form.role===Role.STUDENT }" @click="form.role=Role.STUDENT">学生</button>
            <button type="button" :class="{ active: form.role===Role.TEACHER }" @click="form.role=Role.TEACHER">教师</button>
          </div>
        </div>
        <!-- 管理员模式提示（无需邀请码） -->
        <div class="admin-role-banner" v-else>
          正在注册管理员账号
          <button type="button" class="exit-admin" @click="form.role=Role.STUDENT">返回普通注册</button>
        </div>
        <!-- 用户名等其余字段 -->
        <div class="field">
          <label>用户名</label>
          <input v-model.trim="form.username" placeholder="用户名" />
        </div>
        <div class="field">
          <label>密码</label>
          <input type="password" v-model="form.password" placeholder="密码" />
        </div>
        <div class="field">
          <label>邮箱</label>
          <input v-model.trim="form.email" type="email" placeholder="邮箱" />
        </div>
        <div class="field">
          <label>姓名</label>
          <input v-model.trim="form.realName" placeholder="真实姓名" />
        </div>
        <!-- 学生字段 -->
        <template v-if="isStudent">
          <div class="field"><label>学号</label><input v-model="form.studentId" /></div>
          <div class="field"><label>班级</label><input v-model="form.className" /></div>
          <div class="field"><label>专业</label><input v-model="form.major" /></div>
          <div class="field"><label>年级</label><input v-model="form.grade" /></div>
        </template>
        <!-- 教师字段 -->
        <template v-else-if="isTeacher">
          <div class="field"><label>教师编号</label><input v-model="form.teacherId" /></div>
          <div class="field"><label>院系</label><input v-model="form.department" /></div>
          <div class="field"><label>职称</label><input v-model="form.title" /></div>
        </template>
        <!-- 管理员无需额外字段 -->
        <button class="primary" type="submit" :disabled="submitting">{{ submitting ? '注册中...' : '注册' }}</button>
        <p v-if="error" class="error">{{ error }}</p>
        <p class="goto-login">已有账号？<router-link to="/login">去登录</router-link></p>
        <!-- 管理员切换入口（放在最底部，小字体） -->
        <p class="admin-link" v-if="!isAdmin">
          <button type="button" class="as-text" @click="form.role=Role.ADMIN">我是管理员</button>
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.auth-bg { min-height:100vh; display:flex; align-items:center; justify-content:center; background: linear-gradient(135deg,#2563eb,#1e3a8a); padding:24px; box-sizing:border-box; overflow:hidden; }
.register-card { width:100%; max-width:520px; background:#fff; border-radius:22px; padding:36px 38px 30px; box-shadow:0 12px 36px -6px rgba(0,0,0,.18); display:flex; flex-direction:column; gap:24px; transition:transform .25s ease; }
.title { margin:0; font-size:26px; font-weight:600; text-align:center; background:linear-gradient(90deg,#1d4ed8,#2563eb); -webkit-background-clip:text; color:transparent; }
.reg-form { display:flex; flex-direction:column; gap:18px; }
.field { display:flex; flex-direction:column; gap:6px; width:100%; }
.field label { font-size:13px; font-weight:600; letter-spacing:.5px; color:#374151; }
input, select { padding:10px 12px; border:1px solid #d1d5db; border-radius:10px; font-size:14px; outline:none; background:#f8fafc; transition:.2s; }
input:focus, select:focus { border-color:#2563eb; background:#fff; box-shadow:0 0 0 3px rgba(37,99,235,.15); }
button.primary { background:#2563eb; color:#fff; border:none; padding:12px 16px; font-size:15px; border-radius:10px; cursor:pointer; font-weight:600; letter-spacing:.5px; display:inline-flex; align-items:center; justify-content:center; transition:.25s; }
button.primary:hover:not(:disabled) { background:#1d4ed8; box-shadow:0 6px 16px -4px rgba(37,99,235,.45); transform:translateY(-1px); }
button.primary:active:not(:disabled) { transform:translateY(0); box-shadow:0 2px 8px -2px rgba(37,99,235,.4); }
button.primary:disabled { opacity:.6; cursor:default; }
.error { color:#dc2626; font-size:13px; margin:0; text-align:center; }
.goto-login { font-size:13px; margin:0; text-align:center; }
.goto-login a { color:#2563eb; text-decoration:none; font-weight:600; }
.goto-login a:hover { text-decoration:underline; }
.role-switch { display:flex; flex-direction:column; gap:8px; padding:12px 14px; border:1px solid #d1d5db; border-radius:12px; background:#f1f5f9; width:100%; }
.role-label { font-size:12px; font-weight:600; letter-spacing:.5px; color:#475569; text-transform:uppercase; }
.role-buttons { display:flex; gap:10px; }
.role-buttons button { flex:1; background:#fff; border:1px solid #cbd5e1; border-radius:8px; padding:10px 12px; font-size:14px; cursor:pointer; font-weight:500; transition:.2s; position:relative; }
.role-buttons button.active { background:#2563eb; color:#fff; border-color:#2563eb; box-shadow:0 2px 8px -2px rgba(37,99,235,.4); }
.role-buttons button:not(.active):hover { border-color:#2563eb; color:#2563eb; }
.admin-role-banner { width:100%; padding:10px 14px; background:#fef3c7; border:1px solid #fcd34d; border-radius:10px; font-size:13px; color:#92400e; display:flex; justify-content:space-between; align-items:center; gap:12px; }
.exit-admin { background:transparent; border:none; color:#b45309; cursor:pointer; font-size:12px; text-decoration:underline; padding:4px 6px; }
.exit-admin:hover { color:#92400e; }
.admin-link { margin:4px 0 0; font-size:12px; text-align:center; color:#64748b; }
.admin-link .as-text { background:none; border:none; padding:0; font:inherit; color:#2563eb; cursor:pointer; text-decoration:underline; }
.admin-link .as-text:hover { color:#1d4ed8; }
@media (max-width:520px){
  .register-card { transform:none !important; max-width:100%; padding:30px 24px 26px; }
}
</style>
