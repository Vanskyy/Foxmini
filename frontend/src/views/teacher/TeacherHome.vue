<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import { apiGetTeacherProfile, apiUpdateTeacherProfile, type TeacherProfileResponse, type UpdateTeacherProfileRequest } from '@/api/teacher'
import TeacherProfilePanel from './components/TeacherProfilePanel.vue'
import TeacherExperimentManagePanel from './components/TeacherExperimentManagePanel.vue'

// 仅保留个人信息
 type SectionKey = 'profile' | 'experiments'
const active = ref<SectionKey>('profile')
const collapsed = ref(false)
const menus = [
  { key: 'profile', label: '个人中心', icon: '👤' },
  { key: 'experiments', label: '实验管理', icon: '🧪' },
]

const auth = useAuthStore()
// 显示名
const displayName = computed(() => auth.user?.realName || auth.user?.username || '教师')

// 教师档案
const loading = ref(false)
const error = ref('')
const teacherProfile = ref<TeacherProfileResponse | null>(null)
async function loadProfile(){
  if(!auth.user?.id) return
  loading.value = true
  error.value = ''
  try { teacherProfile.value = await apiGetTeacherProfile(Number(auth.user.id)) } catch(e:any){ error.value = e.message || '加载失败' } finally { loading.value = false }
}
async function onProfileSave(data: UpdateTeacherProfileRequest){
  if(!auth.user?.id) return
  const updated = await apiUpdateTeacherProfile(Number(auth.user.id), data)
  teacherProfile.value = { ...(teacherProfile.value||{}), ...updated }
  alert('已保存')
}

onMounted(()=>{ loadProfile() })
</script>
<template>
  <div class="teacher-layout">
    <!-- 侧边栏 -->
    <aside :class="['sidebar', { collapsed }]">
      <div class="sb-header">
        <span class="logo" v-if="!collapsed">教师中心</span>
        <button class="collapse-btn" :title="collapsed ? '展开' : '收起'" @click="collapsed=!collapsed">{{ collapsed ? '›' : '‹' }}</button>
      </div>
      <nav class="menu">
        <button v-for="m in menus" :key="m.key" :class="['menu-item',{active:active===m.key}]" @click="active=m.key as SectionKey">
          <span class="icon">{{ m.icon }}</span>
            <span class="text" v-if="!collapsed">{{ m.label }}</span>
        </button>
      </nav>
    </aside>

    <!-- 主内容 -->
    <main class="content-area">
      <template v-if="active==='profile'">
        <h1 class="page-title">个人中心</h1>
        <TeacherProfilePanel :profile="teacherProfile" :loading="loading" :error="error" @save="onProfileSave" />
      </template>
      <template v-else-if="active==='experiments'">
        <h1 class="page-title">实验管理</h1>
        <TeacherExperimentManagePanel />
      </template>
    </main>
  </div>
</template>

<style scoped>
/* 保留原样式，删除未用部分 */
.teacher-layout { display:flex; min-height:calc(100vh - 0px); background:#f1f5f9; }
.sidebar { width:220px; background:#6366f1; color:#fff; display:flex; flex-direction:column; padding:14px 12px 20px; box-sizing:border-box; gap:12px; position:sticky; top:0; height:100vh; transition:width .25s; }
.sidebar.collapsed { width:68px; }
.sb-header { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.logo { font-size:16px; font-weight:600; letter-spacing:.5px; white-space:nowrap; }
.collapse-btn { background:rgba(255,255,255,.15); border:none; color:#fff; width:32px; height:32px; border-radius:8px; cursor:pointer; font-size:16px; line-height:1; display:flex; align-items:center; justify-content:center; transition:.25s; }
.collapse-btn:hover { background:rgba(255,255,255,.28); }
.menu { display:flex; flex-direction:column; gap:6px; margin-top:4px; }
.menu-item { background:transparent; border:none; color:#e0e7ff; display:flex; align-items:center; gap:12px; padding:10px 12px; border-radius:10px; cursor:pointer; font-size:14px; font-weight:500; text-align:left; transition:.2s; position:relative; }
.sidebar.collapsed .menu-item { justify-content:center; padding:10px 0; }
.menu-item .icon { font-size:18px; width:20px; text-align:center; }
.menu-item.active { background:rgba(255,255,255,.18); color:#fff; box-shadow:0 2px 6px -2px rgba(0,0,0,.4); }
.menu-item:not(.active):hover { background:rgba(255,255,255,.12); color:#fff; }
.content-area { flex:1; padding:28px 34px 40px; box-sizing:border-box; overflow:auto; }
.page-title { font-size:24px; font-weight:600; margin:0 0 22px; color:#6366f1; }
.panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:22px 24px 26px; box-shadow:0 4px 18px -6px rgba(0,0,0,.08); }
.fade-in { animation:fade .25s ease; }
@keyframes fade { from { opacity:0; transform:translateY(4px); } to { opacity:1; transform:translateY(0); } }
.placeholder { padding:30px 10px; text-align:center; font-size:14px; color:#64748b; }
</style>
