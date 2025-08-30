<script setup lang="ts">
import { ref } from 'vue'
import type { TeacherProfileResponse, UpdateTeacherProfileRequest } from '@/api/teacher'

const props = defineProps<{
  profile: TeacherProfileResponse | null
  loading: boolean
  error: string
}>()
const emit = defineEmits<{
  (e:'save', data: UpdateTeacherProfileRequest):void
  (e:'reload'):void
}>()
interface LocalEditForm extends UpdateTeacherProfileRequest {}
const editing = ref(false)
const saving = ref(false)
const form = ref<LocalEditForm>({})
function startEdit(){
  form.value = { title: props.profile?.title, department: props.profile?.department, teacherId: props.profile?.teacherId, realName: props.profile?.realName, email: props.profile?.email }
  editing.value = true
}
function cancel(){ editing.value = false }
async function save(){
  saving.value = true
  try {
    await emit('save', { title: form.value.title, department: form.value.department, teacherId: form.value.teacherId, realName: form.value.realName, email: form.value.email })
    editing.value = false
  } finally { saving.value = false }
}
// 新增：时间格式化（去秒）
function formatTime(v?: string){
  if(!v) return '-'
  const normalized = v.replace(' ', 'T')
  const d = new Date(normalized)
  if(isNaN(d.getTime())) return v
  const pad = (n:number)=> n<10? '0'+n : ''+n
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>
<template>
  <section class="panel fade-in">
    <div class="panel-head">
      <h3>个人信息</h3>
      <div class="actions" v-if="!editing">
        <button class="text-btn" @click="startEdit" :disabled="loading">编辑</button>
      </div>
      <div class="actions" v-else>
        <button class="primary small" :disabled="saving" @click="save">{{ saving? '保存中...' : '保存' }}</button>
        <button class="outline small" :disabled="saving" @click="cancel">取消</button>
      </div>
    </div>
    <div v-if="loading" class="loading-line">加载中...</div>
    <div v-else-if="error" class="error-line">{{ error }}</div>
    <div v-else-if="!editing" class="profile-box">
      <ul class="profile-list">
        <li><span class="pl-label">用户名</span><span class="pl-value">{{ profile?.username }}</span></li>
        <li><span class="pl-label">姓名</span><span class="pl-value">{{ profile?.realName || '-' }}</span></li>
        <li><span class="pl-label">邮箱</span><span class="pl-value">{{ profile?.email || '-' }}</span></li>
        <li><span class="pl-label">工号</span><span class="pl-value">{{ profile?.teacherId || '-' }}</span></li>
        <li><span class="pl-label">职称</span><span class="pl-value">{{ profile?.title || '-' }}</span></li>
        <li><span class="pl-label">部门</span><span class="pl-value">{{ profile?.department || '-' }}</span></li>
        <li><span class="pl-label">更新时间</span><span class="pl-value">{{ formatTime(profile?.updatedAt) }}</span></li>
      </ul>
      <div v-if="profile?.avatar" class="avatar-box">
        <img :src="profile.avatar" alt="avatar" />
      </div>
    </div>
    <div v-else class="profile-box">
      <form class="profile-form" @submit.prevent="save">
        <div class="form-row">
          <label>姓名</label>
          <input v-model="form.realName" placeholder="真实姓名" />
        </div>
        <div class="form-row">
          <label>邮箱</label>
          <input v-model="form.email" placeholder="邮箱" type="email" />
        </div>
        <div class="form-row">
          <label>工号</label>
          <input v-model="form.teacherId" placeholder="教师工号" />
        </div>
        <div class="form-row">
          <label>职称</label>
          <input v-model="form.title" placeholder="教授/副教授/讲师..." />
        </div>
        <div class="form-row">
          <label>部门</label>
            <input v-model="form.department" placeholder="所属学院或系" />
        </div>
      </form>
    </div>
  </section>
</template>
<style scoped>
.panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:22px 24px 26px; }
.panel-head { display:flex; align-items:center; justify-content:space-between; gap:16px; margin-bottom:10px; }
.profile-box { background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; padding:14px 18px 6px; }
.profile-list { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:10px; }
.profile-list li { display:flex; flex-wrap:wrap; font-size:14px; }
.pl-label { width:90px; color:#64748b; font-size:12px; letter-spacing:.5px; font-weight:600; }
.pl-value { flex:1; color:#1e293b; }
.profile-form { display:flex; flex-direction:column; gap:14px; }
.form-row { display:flex; flex-direction:column; gap:6px; }
.form-row label { font-size:12px; font-weight:600; color:#64748b; letter-spacing:.5px; }
.form-row input { height:38px; border:1px solid #cbd5e1; border-radius:8px; padding:0 12px; font-size:14px; }
.text-btn { background:transparent; border:none; color:#6366f1; cursor:pointer; font-size:14px; font-weight:600; padding:6px 10px; border-radius:6px; }
.text-btn:hover { background:rgba(99,102,241,.1); }
button.primary.small { background:#4f46e5; color:#fff; border:none; border-radius:10px; padding:8px 14px; font-size:13px; cursor:pointer; font-weight:600; }
button.outline.small { background:#fff; color:#475569; border:1px solid #cbd5e1; border-radius:10px; padding:8px 14px; font-size:13px; cursor:pointer; font-weight:600; }
.loading-line { padding:6px 4px; font-size:13px; color:#64748b; }
.error-line { padding:6px 4px; font-size:13px; color:#dc2626; }
.avatar-box { margin:14px 0 8px; }
.avatar-box img { width:100px; height:100px; object-fit:cover; border-radius:12px; border:1px solid #cbd5e1; }
</style>
