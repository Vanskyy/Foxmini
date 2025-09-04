<script setup lang="ts">
import type { StudentProfileResponse, UpdateStudentProfileRequest } from '@/api/student'

const props = defineProps<{ profile:StudentProfileResponse|null; loading:boolean; error:string; editing:boolean; saving:boolean; currentCount:number; form:UpdateStudentProfileRequest }>()
const emit = defineEmits<{(e:'start-edit'):void;(e:'cancel-edit'):void;(e:'save'):void}>()
function formatTime(v?:string){
  if(!v) return '-'
  const d=new Date(v); if(isNaN(d.getTime())) return v
  const p=(n:number)=> n<10? '0'+n: ''+n
  return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}
</script>
<template>
  <section class="panel fade-in">
    <div class="panel-head">
      <h3>个人信息</h3>
      <div class="profile-actions" v-if="!editing">
        <button class="text-btn" @click="emit('start-edit')" :disabled="loading">编辑</button>
      </div>
      <div class="profile-actions" v-else>
        <button class="primary small" :disabled="saving" @click="emit('save')">{{ saving? '保存中...' : '保存' }}</button>
        <button class="outline small" :disabled="saving" @click="emit('cancel-edit')">取消</button>
      </div>
    </div>
    <div v-if="loading" class="loading-line">加载中...</div>
    <div v-else-if="error" class="error-line">{{ error }}</div>
    <div class="profile-box" v-else-if="!editing">
      <ul class="profile-list">
        <li><span class="pl-label">用户名</span><span class="pl-value">{{ profile?.username }}</span></li>
        <li><span class="pl-label">姓名</span><span class="pl-value">{{ profile?.realName || '-' }}</span></li>
        <li><span class="pl-label">邮箱</span><span class="pl-value">{{ profile?.email || '-' }}</span></li>
        <li><span class="pl-label">学号</span><span class="pl-value">{{ profile?.studentId || '-' }}</span></li>
        <li><span class="pl-label">班级</span><span class="pl-value">{{ profile?.className || '-' }}</span></li>
        <li><span class="pl-label">专业</span><span class="pl-value">{{ profile?.major || '-' }}</span></li>
        <li><span class="pl-label">年级</span><span class="pl-value">{{ profile?.grade || '-' }}</span></li>
        <li><span class="pl-label">更新时间</span><span class="pl-value">{{ formatTime(profile?.updatedAt) }}</span></li>
      </ul>
    </div>
    <div class="profile-box" v-else>
      <form class="profile-form" @submit.prevent>
        <div class="form-row"><label>姓名</label><input v-model="(form as any).realName" placeholder="真实姓名"/></div>
        <div class="form-row"><label>邮箱</label><input v-model="(form as any).email" placeholder="邮箱" type="email"/></div>
        <div class="form-row"><label>学号</label><input v-model="(form as any).studentId" placeholder="学号"/></div>
        <div class="form-row"><label>班级</label><input v-model="(form as any).className" placeholder="班级"/></div>
        <div class="form-row"><label>年级</label><input v-model="(form as any).grade" placeholder="如 2023"/></div>
        <div class="form-row"><label>专业</label><input v-model="(form as any).major" placeholder="请输入专业"/></div>
      </form>
    </div>
    <div class="stats single">
      <div class="stat-box compact">
        <div class="num">{{ currentCount }}</div>
        <div class="desc">未完成实验</div>
      </div>
    </div>
  </section>
</template>
<style scoped>
/* 头部布局：按钮靠右 */
.panel-head{display:flex;align-items:center;gap:16px;margin-bottom:10px;}
.panel-head h3{margin:0;font-size:18px;font-weight:600;}
.profile-actions{margin-left:auto;display:flex;gap:10px;}
.profile-box{background:#f8fafc;border:1px solid #e2e8f0;border-radius:14px;padding:14px 18px 6px;}
.profile-list{list-style:none;padding:0;margin:0;display:flex;flex-direction:column;gap:10px;}
.profile-list li{display:flex;flex-wrap:wrap;font-size:14px;}
.pl-label{width:80px;color:#64748b;font-size:12px;letter-spacing:.5px;font-weight:600;}
.pl-value{flex:1;color:#1e293b;}
.profile-form{display:flex;flex-direction:column;gap:14px;}
.form-row{display:flex;flex-direction:column;gap:6px;}
.form-row label{font-size:12px;font-weight:600;color:#64748b;letter-spacing:.5px;}
.form-row input{height:38px;border:1px solid #cbd5e1;border-radius:8px;padding:0 12px;font-size:14px;outline:none;background:#fff;transition:.2s;}
.form-row input:focus{border-color:#3b82f6;box-shadow:0 0 0 3px rgba(59,130,246,.15);}
.profile-actions{display:flex;gap:10px;}
.stats{display:flex;gap:16px;margin-top:20px;flex-wrap:wrap;}
.stat-box{flex:1;min-width:150px;background:#3b82f6;color:#fff;border-radius:14px;padding:16px 18px;display:flex;flex-direction:column;gap:6px;position:relative;overflow:hidden;box-shadow:0 4px 12px -4px rgba(59,130,246,.45);}
.stat-box.compact{padding:12px 16px;min-width:120px;flex:0 0 50%;max-width:20%;}
.stat-box .num{font-size:28px;font-weight:600;line-height:1;}
.stat-box.compact .num{font-size:20px;}
.stat-box .desc{font-size:13px;opacity:.85;}
.stat-box.compact .desc{font-size:12px;}
/* 新增：通用按钮样式（从父组件抽出避免 scoped 丢失） */
.text-btn{background:transparent;border:none;color:#3b82f6;cursor:pointer;font-size:14px;font-weight:600;padding:6px 10px;border-radius:6px;}
.text-btn:hover{background:rgba(59,130,246,.1);}
button.primary.small{background:#2563eb;color:#fff;border:none;border-radius:8px;padding:8px 14px;font-size:13px;cursor:pointer;font-weight:600;letter-spacing:.5px;transition:.25s;}
button.primary.small:hover{background:#1d4ed8;box-shadow:0 4px 12px -4px rgba(37,99,235,.45);}
button.outline.small{background:#fff;color:#475569;border:1px solid #cbd5e1;border-radius:8px;padding:8px 14px;font-size:13px;cursor:pointer;font-weight:600;letter-spacing:.5px;transition:.25s;}
button.outline.small:hover{background:#f1f5f9;}
</style>
