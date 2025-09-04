<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { apiGetStudentProfile, apiUpdateStudentProfile, apiGetStudentHistory, apiGetStudentCurrentExperiments, type StudentProfileResponse, type UpdateStudentProfileRequest, type StudentExperimentHistoryItem, type StudentCurrentExperimentItem } from '@/api/student'
// 新增引入通知 API
import { apiGetStudentNotifications, apiMarkNotificationRead, apiMarkAllNotificationsRead, type StudentNotification, type PageResult } from '@/api/notification'
import { useAuthStore } from '@/stores/modules/auth'
import { useRouter } from 'vue-router'
import CurrentExperiments, { type ExperimentItem } from './components/CurrentExperiments.vue'
import ProfilePanel from './components/ProfilePanel.vue'
import HistoryTable from './components/HistoryTable.vue'
import NotificationsPanel from './components/NotificationsPanel.vue'

// 菜单 key 类型
// 增加 welcome 初始欢迎页，不在菜单中
type SectionKey = 'welcome' | 'profile' | 'current' | 'history' | 'notify'

const auth = useAuthStore()
const router = useRouter()

// 当前激活栏目，初始为欢迎页
const active = ref<SectionKey>('welcome')

// 侧边栏折叠
const collapsed = ref(false)

// 菜单定义
const menus: { key: SectionKey; label: string; icon: string }[] = [
  { key: 'profile', label: '个人中心', icon: '👤' },
  { key: 'current', label: '我的实验', icon: '🧪' },
  { key: 'history', label: '历史记录', icon: '📜' },
  { key: 'notify', label: '我的通知', icon: '🔔' },
]

const currentTitle = computed(() => active.value === 'welcome' ? '欢迎' : (menus.find(m => m.key === active.value)?.label || ''))

// 用户档案状态及数据
const profileLoading = ref(false)
const historyLoading = ref(false)
const profileError = ref('')
const historyError = ref('')
const profile = ref<StudentProfileResponse | null>(null)

// 动态显示名称
const displayName = computed(() => auth.user?.realName || auth.user?.username || '同学')

// 历史实验记录
const historyList = ref<StudentExperimentHistoryItem[]>([])

// 当前实验（带阶段统计）
interface CurrentExperiment { id:number; title:string; deadline:string; totalStages:number; finishedStages:number; progress:number }
const currentExperiments = ref<CurrentExperiment[]>([])
const currentLoading = ref(false)
const currentError = ref('')
async function loadCurrent(){
  if(!auth.user?.id || currentLoading.value) return
  currentLoading.value = true
  currentError.value = ''
  try {
    const list = await apiGetStudentCurrentExperiments(Number(auth.user.id))
    currentExperiments.value = list.map(e => {
      const total = e.totalStages || 0
      const finished = e.finishedStages || 0 // 后端：仅 final 提交阶段
      return {
        id: e.publishedExperimentId || e.experimentId,
        title: e.experimentTitle,
        deadline: e.deadline || '-',
        totalStages: total,
        finishedStages: finished,
        progress: total ? Math.round((finished / total) * 100) : 0
      }
    })
  } catch(e:any){ currentError.value = e.message || '加载失败' } finally { currentLoading.value = false }
}

async function loadProfile() {
  if (!auth.user?.id) return
  profileLoading.value = true
  profileError.value = ''
  try {
    profile.value = await apiGetStudentProfile(Number(auth.user.id))
    if (profile.value?.realName) auth.user.realName = profile.value.realName
  } catch (e:any) {
    profileError.value = e.message || '加载失败'
  } finally { profileLoading.value = false }
}

async function loadHistory() {
  if (!auth.user?.id) return
  historyLoading.value = true
  historyError.value = ''
  try {
    historyList.value = await apiGetStudentHistory(Number(auth.user.id))
  } catch (e:any) {
    historyError.value = e.message || '加载失败'
  } finally { historyLoading.value = false }
}

onMounted(() => { loadProfile(); loadHistory() })
watch(active, v=>{ if(v==='current' && !currentExperiments.value.length) loadCurrent() })

// Profile 编辑
const editingProfile = ref(false)
const profileForm = ref<UpdateStudentProfileRequest>({})
function startEditProfile(){
  profileForm.value = {
    realName: profile.value?.realName || auth.user?.realName,
    email: profile.value?.email || auth.user?.email,
    studentId: profile.value?.studentId,
    className: profile.value?.className,
    grade: profile.value?.grade,
    major: profile.value?.major,
    phone: undefined,
  }
  editingProfile.value = true
}
function cancelEditProfile(){ editingProfile.value = false }

const savingProfile = ref(false)
async function saveProfile(){
  if(!auth.user?.id) return
  savingProfile.value = true
  try {
    await apiUpdateStudentProfile(Number(auth.user.id), profileForm.value)
    editingProfile.value = false
    loadProfile()
    alert('已保存')
  } catch(e:any){ alert(e.message || '更新失败') } finally { savingProfile.value = false }
}

// 通知
interface Notice { id:number; title:string; time:string; read?:boolean }
const notices = ref<Notice[]>([])
const noticeLoading = ref(false)
const noticeError = ref('')
const noticePage = ref(0)
const noticeFinished = ref(false)
async function loadNotices(loadMore=false){
  if(noticeLoading.value || noticeFinished.value && loadMore) return
  noticeLoading.value = true
  noticeError.value = ''
  try {
    if(!loadMore){ noticePage.value = 0; noticeFinished.value = false; notices.value = [] }
    const pageRes = await apiGetStudentNotifications({ page: noticePage.value, size: 20 })
    const mapped = pageRes.content.map(n=>({ id:n.id, title:n.title, time:n.createdAt }))
    notices.value = loadMore ? [...notices.value, ...mapped] : mapped
    if(pageRes.number >= pageRes.totalPages - 1) noticeFinished.value = true
    else noticePage.value += 1
  } catch(e:any){ noticeError.value = e.message || '加载失败' } finally { noticeLoading.value = false }
}
async function markNoticeRead(id:number){
  try { await apiMarkNotificationRead(id); notices.value = notices.value.filter(n=>n.id!==id) } catch(e:any){ alert(e.message || '操作失败') }
}
async function markAllRead(){
  try { await apiMarkAllNotificationsRead(); notices.value = [] } catch(e:any){ alert(e.message || '操作失败') }
}
watch(active, v=>{ if(v==='notify' && !notices.value.length) loadNotices() })

// 打开实验详情
function openExperiment(id: number) { router.push({ name: 'student-experiment-detail', params: { id } }) }
</script>

<template>
  <div class="student-layout">
    <!-- 侧边栏 -->
    <aside :class="['sidebar', { collapsed }]">
      <div class="sb-header">
        <span class="logo" v-if="!collapsed">学生中心</span>
        <button class="collapse-btn" :title="collapsed ? '展开' : '收起'" @click="collapsed=!collapsed">{{ collapsed ? '›' : '‹' }}</button>
      </div>
      <nav class="menu">
        <button v-for="m in menus" :key="m.key" :class="['menu-item', {active: active===m.key}]" @click="active=m.key as SectionKey">
          <span class="icon" aria-hidden="true">{{ m.icon }}</span>
          <span class="text" v-if="!collapsed">{{ m.label }}</span>
        </button>
      </nav>
    </aside>

    <!-- 主内容 -->
    <main class="content-area">
      <h1 class="page-title">{{ currentTitle }}</h1>

      <!-- 欢迎页 -->
      <section v-if="active==='welcome'" class="panel fade-in welcome-panel">
        <h2 class="welcome-title">欢迎，{{ displayName }} 同学</h2>
        <p class="welcome-tip">请选择左侧菜单查看实验、历史记录或通知。</p>
      </section>

      <!-- 个人中心 -->
      <ProfilePanel
        v-else-if="active==='profile'"
        :profile="profile"
        :loading="profileLoading"
        :error="profileError"
        :editing="editingProfile"
        :saving="savingProfile"
        :current-count="currentExperiments.length"
        :form="profileForm"
        @start-edit="startEditProfile"
        @cancel-edit="cancelEditProfile"
        @save="saveProfile"
      />

      <!-- 未完成实验（原进行中实验） -->
      <CurrentExperiments
        v-else-if="active==='current'"
        :list="currentExperiments as ExperimentItem[]"
        :loading="currentLoading"
        :error="currentError"
        @refresh="loadCurrent"
        @open="openExperiment"
      />

      <!-- 历史实验记录 -->
      <HistoryTable
        v-else-if="active==='history'"
        :list="historyList"
        :loading="historyLoading"
        :error="historyError"
      />

      <!-- 我的通知 -->
      <NotificationsPanel
        v-else
        :notices="notices"
        @read="markNoticeRead"
        @read-all="markAllRead"
      />
    </main>
  </div>
</template>

<style scoped>
/* 保留整体布局与公共样式，细分组件内部样式放入各自文件 */
.student-layout { display:flex; min-height:calc(100vh - 0px); background:#f1f5f9; }
.sidebar { width:220px; background:#3b82f6; color:#fff; display:flex; flex-direction:column; padding:14px 12px 20px; box-sizing:border-box; gap:12px; position:sticky; top:0; height:100vh; transition:width .25s; }
.sidebar.collapsed { width:68px; }
.sb-header { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.logo { font-size:16px; font-weight:600; letter-spacing:.5px; white-space:nowrap; }
.collapse-btn { background:rgba(255,255,255,.15); border:none; color:#fff; width:32px; height:32px; border-radius:8px; cursor:pointer; font-size:16px; line-height:1; display:flex; align-items:center; justify-content:center; transition:.25s; }
.collapse-btn:hover { background:rgba(255,255,255,.28); }
.menu { display:flex; flex-direction:column; gap:6px; margin-top:4px; }
.menu-item { background:transparent; border:none; color:#cbd5e1; display:flex; align-items:center; gap:12px; padding:10px 12px; border-radius:10px; cursor:pointer; font-size:14px; font-weight:500; text-align:left; transition:.2s; position:relative; }
.sidebar.collapsed .menu-item { justify-content:center; padding:10px 0; }
.menu-item .icon { font-size:18px; line-height:1; width:20px; text-align:center; }
.menu-item.active { background:rgba(255,255,255,.18); color:#fff; box-shadow:0 2px 6px -2px rgba(0,0,0,.4); }
.menu-item:not(.active):hover { background:rgba(255,255,255,.12); color:#fff; }
.content-area { flex:1; padding:28px 34px 40px; box-sizing:border-box; overflow:auto; }
.page-title { font-size:24px; font-weight:600; margin:0 0 22px; color:#3b82f6; }
.panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:22px 24px 26px; box-shadow:0 4px 18px -6px rgba(0,0,0,.08); }
.fade-in { animation:fade .25s ease; }
@keyframes fade { from { opacity:0; transform:translateY(4px); } to { opacity:1; transform:translateY(0); } }

/* 欢迎页 */
.welcome-panel { text-align:center; padding:60px 40px; display:flex; flex-direction:column; gap:18px; }
.welcome-title { margin:0; font-size:28px; font-weight:600; background:linear-gradient(90deg,#3b82f6,#2563eb); -webkit-background-clip:text; background-clip: text; color:transparent; }
.welcome-tip { margin:0; font-size:15px; color:#475569; }
</style>

<!-- 全局共享样式：供子组件使用（不加 scoped） -->
<style>
/* Panel 与动画（子组件引用 .panel .fade-in） */
.panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:22px 24px 26px; box-shadow:0 4px 18px -6px rgba(0,0,0,.08); }
.fade-in { animation:fade .25s ease; }
@keyframes fade { from { opacity:0; transform:translateY(4px);} to { opacity:1; transform:translateY(0);} }

/* 通用按钮 */
.text-btn { background:transparent; border:none; color:#3b82f6; cursor:pointer; font-size:14px; font-weight:600; padding:6px 10px; border-radius:6px; }
.text-btn:hover { background:rgba(59,130,246,.1); }
button.primary.small { background:#2563eb; color:#fff; border:none; border-radius:8px; padding:8px 14px; font-size:13px; cursor:pointer; font-weight:600; letter-spacing:.5px; transition:.25s; }
button.primary.small:hover { background:#1d4ed8; box-shadow:0 4px 12px -4px rgba(37,99,235,.45); }
button.outline.small { background:#fff; color:#475569; border:1px solid #cbd5e1; border-radius:8px; padding:8px 14px; font-size:13px; cursor:pointer; font-weight:600; letter-spacing:.5px; transition:.25s; }
button.outline.small:hover { background:#f1f5f9; }

/* 通用状态类 */
.loading-line { padding:6px 4px; font-size:13px; color:#64748b; }
.error-line { padding:6px 4px; font-size:13px; color:#dc2626; }
.empty { padding:26px 8px; text-align:center; color:#64748b; font-size:14px; }

/* 进度条分段（给子组件备用，如未在子组件内部定义） */
.progress-wrapper{display:flex;flex-direction:column;gap:6px;}
.progress-text{font-size:12px;color:#475569;font-weight:500;}
.progress-bar{height:8px;background:#e2e8f0;border-radius:4px;overflow:hidden;position:relative;}
.progress-bar.segmented{display:flex;gap:4px;background:transparent;height:auto;}
.progress-bar.segmented .seg{flex:1;height:8px;background:#e2e8f0;border-radius:4px;position:relative;overflow:hidden;transition:background .25s,opacity .25s;opacity:.55;}
.progress-bar.segmented .seg.done{background:linear-gradient(90deg,#3b82f6,#2563eb);box-shadow:0 0 0 1px #1d4ed8 inset;opacity:1;}
.progress-bar.segmented .seg.done::after{content:"";position:absolute;inset:0;background:linear-gradient(180deg,rgba(255,255,255,.4),rgba(255,255,255,0));mix-blend-mode:overlay;pointer-events:none;}
</style>
