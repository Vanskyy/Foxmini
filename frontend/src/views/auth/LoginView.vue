<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/modules/auth'
import { useAutoScale } from '../../composables/useAutoScale'

const auth = useAuthStore()
const router = useRouter()

const form = ref({ usernameOrEmail: '', password: '' })
const submitting = ref(false)
const error = ref('')

// 自动缩放：基准宽 1280 高 800，可按需调整
const { scale, style: scaleStyle } = useAutoScale(1280, 800, 0.7, 1)
// 当屏幕特别窄（<500）时，直接用 1，避免输入框过小
const finalStyle = computed(() => {
  if (window.innerWidth < 500) return { transform: 'none' }
  return scaleStyle.value
})

async function submit() {
  error.value = ''
  if (!form.value.usernameOrEmail || !form.value.password) {
    error.value = '请输入账号与密码'
    return
  }
  submitting.value = true
  try {
    await auth.login({ ...form.value })
    // 根据角色跳转
    if (auth.role === 'TEACHER') router.push('/teacher')
    else if (auth.role === 'STUDENT') router.push('/student')
    else router.push('/')
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-bg">
    <div class="login-card" :style="finalStyle">
      <h2 class="title">平台登录</h2>
      <form class="form" @submit.prevent="submit">
        <div class="field">
          <label>用户名 / 邮箱</label>
          <input v-model.trim="form.usernameOrEmail" autocomplete="username" placeholder="输入用户名或邮箱" />
        </div>
        <div class="field">
          <label>密码</label>
            <input type="password" v-model="form.password" autocomplete="current-password" placeholder="输入密码" />
        </div>
        <button class="primary" type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录' }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
        <p class="goto-register">还没有账号？<router-link to="/register">马上注册</router-link></p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.auth-bg { min-height:100vh; display:flex; align-items:center; justify-content:center; background: linear-gradient(135deg,#2563eb,#1e3a8a); padding:24px; box-sizing:border-box; overflow:hidden; }
.login-card { width:100%; max-width:400px; background:#fff; border-radius:18px; padding:40px 36px 32px; box-shadow:0 10px 30px -5px rgba(0,0,0,.15); display:flex; flex-direction:column; gap:20px; position:relative; transition:transform .25s ease; }
/* 在较大屏幕下为保持清晰居中，限制最大缩放 1，父容器已居中无需 translate */
.title { margin:0; font-size:24px; font-weight:600; text-align:center; background:linear-gradient(90deg,#1d4ed8,#2563eb); -webkit-background-clip:text; color:transparent; }
.form { display:flex; flex-direction:column; gap:18px; }
.field { display:flex; flex-direction:column; gap:6px; }
.field label { font-size:13px; font-weight:600; letter-spacing:.5px; color:#374151; }
input { padding:10px 12px; border:1px solid #d1d5db; border-radius:10px; font-size:14px; outline:none; transition:.2s; background:#f8fafc; }
input:focus { border-color:#2563eb; background:#fff; box-shadow:0 0 0 3px rgba(37,99,235,.15); }
button.primary { background:#2563eb; color:#fff; border:none; padding:12px 16px; font-size:15px; border-radius:10px; cursor:pointer; font-weight:600; letter-spacing:.5px; display:inline-flex; align-items:center; justify-content:center; transition:.25s; }
button.primary:hover:not(:disabled) { background:#1d4ed8; box-shadow:0 6px 16px -4px rgba(37,99,235,.45); transform:translateY(-1px); }
button.primary:active:not(:disabled) { transform:translateY(0); box-shadow:0 2px 8px -2px rgba(37,99,235,.4); }
button.primary:disabled { opacity:.6; cursor:default; }
.error { color:#dc2626; font-size:13px; margin:0; text-align:center; }
.goto-register { font-size:13px; margin:0; text-align:center; }
.goto-register a { color:#2563eb; text-decoration:none; font-weight:600; }
.goto-register a:hover { text-decoration:underline; }

@media (max-width:500px){
  .login-card { transform:none !important; max-width:100%; padding:32px 26px 28px; }
}
</style>
