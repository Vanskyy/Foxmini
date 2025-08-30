<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { apiGetStudentProfile, apiUpdateStudentProfile, apiGetStudentHistory, type StudentProfileResponse, type UpdateStudentProfileRequest, type StudentExperimentHistoryItem } from '@/api/student'
import { useAuthStore } from '@/stores/modules/auth'
import { useRouter } from 'vue-router'

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
  { key: 'current', label: '未完成实验', icon: '🧪' },
  { key: 'history', label: '历史记录', icon: '📜' },
  { key: 'notify', label: '未读通知', icon: '🔔' },
]

const currentTitle = computed(() => active.value === 'welcome' ? '欢迎' : (menus.find(m => m.key === active.value)?.label || ''))

// 用户档案状态及数据（需在 displayName 之前定义）
const profileLoading = ref(false)
const historyLoading = ref(false)
const profileError = ref('')
const historyError = ref('')
const profile = ref<StudentProfileResponse | null>(null)

// 动态显示名称（之前放在 profile 定义前导致引用错误）
const displayName = computed(() => auth.user?.realName || auth.user?.username || '同学')

// 历史实验记录（使用后端数据替换 mock）
const historyList = ref<StudentExperimentHistoryItem[]>([])

// 新增：当前未完成实验（缺失导致模板报错 currentExperiments 未定义，使得点击菜单不生效）
interface CurrentExperiment { id:number; title:string; deadline:string; progress:number }
// TODO: 后续改为从后端获取未完成实验列表
const currentExperiments = ref<CurrentExperiment[]>([])

async function loadProfile() {
  if (!auth.user?.id) return
  profileLoading.value = true
  profileError.value = ''
  try {
    profile.value = await apiGetStudentProfile(Number(auth.user.id))
    // 同步 user 基础显示字段
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

onMounted(() => {
  loadProfile()
  loadHistory()
})

// 个人信息编辑相关（使用 profile 数据，如果不存在 fallback auth.user）
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
  } catch(e:any){
    alert(e.message || '更新失败')
  } finally { savingProfile.value = false }
}

// 未读通知（mock）
interface Notice { id:number; title:string; time:string }
const notices = ref<Notice[]>([
  { id: 1, title: '实验一截止时间临近，请及时提交。', time: '2025-08-29 09:20' },
  { id: 2, title: '平台将于本周日 02:00-04:00 维护。', time: '2025-08-28 18:05' },
])
function markNoticeRead(id:number){
  notices.value = notices.value.filter(n=>n.id!==id)
}
function markAllRead(){
  notices.value = []
}

function openExperiment(id: number) {
  router.push({ name: 'student-experiment-detail', params: { id } })
}

function formatTime(v?: string){
  if(!v) return '-'
  const d = new Date(v)
  if(isNaN(d.getTime())) return v
  const pad = (n:number)=> n<10? '0'+n : ''+n
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
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
        <p class="welcome-tip">请选择左侧菜单查看未完成实验、历史记录或未读通知。</p>
      </section>

      <!-- 个人中心 -->
      <section v-else-if="active==='profile'" class="panel fade-in">
        <div class="panel-head">
          <h3>个人信息</h3>
          <div class="profile-actions" v-if="!editingProfile">
            <button class="text-btn" @click="startEditProfile" :disabled="profileLoading">编辑</button>
          </div>
          <div class="profile-actions" v-else>
            <button class="primary small" :disabled="savingProfile" @click="saveProfile">{{ savingProfile? '保存中...' : '保存' }}</button>
            <button class="outline small" :disabled="savingProfile" @click="cancelEditProfile">取消</button>
          </div>
        </div>
        <div v-if="profileLoading" class="loading-line">加载中...</div>
        <div v-else-if="profileError" class="error-line">{{ profileError }}</div>
        <div class="profile-box" v-else-if="!editingProfile">
          <ul class="profile-list">
            <li><span class="pl-label">用户名</span><span class="pl-value">{{ profile?.username || auth.user?.username }}</span></li>
            <li><span class="pl-label">姓名</span><span class="pl-value">{{ profile?.realName || auth.user?.realName || '-' }}</span></li>
            <li><span class="pl-label">邮箱</span><span class="pl-value">{{ profile?.email || auth.user?.email || '-' }}</span></li>
            <li><span class="pl-label">学号</span><span class="pl-value">{{ profile?.studentId || '-' }}</span></li>
            <li><span class="pl-label">班级</span><span class="pl-value">{{ profile?.className || '-' }}</span></li>
            <li><span class="pl-label">专业</span><span class="pl-value">{{ profile?.major || '-' }}</span></li>
            <li><span class="pl-label">年级</span><span class="pl-value">{{ profile?.grade || '-' }}</span></li>
            <li><span class="pl-label">更新时间</span><span class="pl-value">{{ formatTime(profile?.updatedAt) }}</span></li>
          </ul>
        </div>
        <div class="profile-box" v-else>
          <form class="profile-form" @submit.prevent="saveProfile">
            <div class="form-row">
              <label>姓名</label>
              <input v-model="profileForm.realName" placeholder="真实姓名" />
            </div>
            <div class="form-row">
              <label>邮箱</label>
              <input v-model="profileForm.email" placeholder="邮箱" type="email" />
            </div>
            <div class="form-row">
              <label>学号</label>
              <input v-model="profileForm.studentId" placeholder="学号" />
            </div>
            <div class="form-row">
              <label>班级</label>
              <input v-model="profileForm.className" placeholder="班级" />
            </div>
            <div class="form-row">
              <label>年级</label>
              <input v-model="profileForm.grade" placeholder="如 2023" />
            </div>
            <div class="form-row">
              <label>专业</label>
              <input v-model="profileForm.major" placeholder="请输入专业" />
            </div>
          </form>
        </div>
        <div class="stats single">
          <div class="stat-box compact">
            <div class="num">{{ currentExperiments.length }}</div>
            <div class="desc">未完成实验</div>
          </div>
        </div>
      </section>

      <!-- 未完成实验（原进行中实验） -->
      <section v-else-if="active==='current'" class="panel fade-in">
        <h3>未完成实验</h3>
        <div v-if="!currentExperiments.length" class="empty">暂无未完成实验</div>
        <ul v-else class="exp-list">
          <li v-for="exp in currentExperiments" :key="exp.id" class="exp-item">
            <div class="main">
              <div class="title">{{ exp.title }}</div>
              <div class="meta">截止：{{ exp.deadline }}</div>
              <div class="progress-bar">
                <div class="inner" :style="{width: exp.progress + '%'}"></div>
              </div>
            </div>
            <div class="actions">
              <button class="primary small" @click="openExperiment(exp.id)">进入实验</button>
            </div>
          </li>
        </ul>
      </section>

      <!-- 历史实验记录 -->
      <section v-else-if="active==='history'" class="panel fade-in">
        <h3>历史实验记录</h3>
        <div v-if="historyLoading" class="loading-line">加载中...</div>
        <div v-else-if="historyError" class="error-line">{{ historyError }}</div>
        <div v-else-if="!historyList.length" class="empty">暂无历史记录</div>
        <table v-else class="history-table">
          <thead>
            <tr>
              <th>ID</th><th>实验名称</th><th>成绩</th><th>提交时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in historyList" :key="r.experimentId">
              <td>{{ r.experimentId }}</td>
              <td>{{ r.experimentTitle }}</td>
              <td><span class="score" :class="{ high: (r.latestScore||0) >= 90, mid: (r.latestScore||0) >= 75 && (r.latestScore||0) < 90 }">{{ r.latestScore ?? '-' }}</span></td>
              <td>{{ r.submittedAt }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 未读通知 -->
      <section v-else class="panel fade-in">
        <div class="notify-head">
          <h3>未读通知</h3>
          <button v-if="notices.length" class="text-btn" @click="markAllRead">全部标记已读</button>
        </div>
        <div v-if="!notices.length" class="empty">暂无未读通知</div>
        <ul v-else class="notice-list">
          <li v-for="n in notices" :key="n.id" class="notice-item">
            <div class="n-main">
              <div class="n-title">{{ n.title }}</div>
              <div class="n-time">{{ n.time }}</div>
            </div>
            <button class="outline small" @click="markNoticeRead(n.id)">已读</button>
          </li>
        </ul>
      </section>
    </main>
  </div>
</template>

<style scoped>
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

/* Panels & shared */
.panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:22px 24px 26px; box-shadow:0 4px 18px -6px rgba(0,0,0,.08); }
.fade-in { animation:fade .25s ease; }
@keyframes fade { from { opacity:0; transform:translateY(4px); } to { opacity:1; transform:translateY(0); } }

/* Profile */
.panel-head { display:flex; align-items:center; justify-content:space-between; gap:16px; margin-bottom:10px; }
.profile-box { background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; padding:14px 18px 6px; }
.profile-list { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:10px; }
.profile-list li { display:flex; flex-wrap:wrap; font-size:14px; }
.pl-label { width:80px; color:#64748b; font-size:12px; letter-spacing:.5px; font-weight:600; }
.pl-value { flex:1; color:#1e293b; }
.profile-form { display:flex; flex-direction:column; gap:14px; }
.form-row { display:flex; flex-direction:column; gap:6px; }
.form-row label { font-size:12px; font-weight:600; color:#64748b; letter-spacing:.5px; }
.form-row input { height:38px; border:1px solid #cbd5e1; border-radius:8px; padding:0 12px; font-size:14px; outline:none; background:#fff; transition:.2s; }
.form-row input:focus { border-color:#3b82f6; box-shadow:0 0 0 3px rgba(59,130,246,.15); }
.profile-actions { display:flex; gap:10px; }
.text-btn { background:transparent; border:none; color:#3b82f6; cursor:pointer; font-size:14px; font-weight:600; padding:6px 10px; border-radius:6px; }
.text-btn:hover { background:rgba(59,130,246,.1); }
button.outline.small { background:#fff; color:#475569; border:1px solid #cbd5e1; border-radius:8px; padding:8px 14px; font-size:13px; cursor:pointer; font-weight:600; letter-spacing:.5px; transition:.25s; }
button.outline.small:hover { background:#f1f5f9; }
.stats { display:flex; gap:16px; margin-top:20px; flex-wrap:wrap; }
.stats.single { justify-content:flex-start; }
.stat-box { flex:1; min-width:150px; background:#3b82f6; color:#fff; border-radius:14px; padding:16px 18px; display:flex; flex-direction:column; gap:6px; position:relative; overflow:hidden; box-shadow:0 4px 12px -4px rgba(59,130,246,.45); }
.stat-box.compact { padding:12px 16px; min-width:120px; flex:0 0 50%; max-width:20%; }
.stat-box .num { font-size:28px; font-weight:600; line-height:1; }
.stat-box.compact .num { font-size:20px; }
.stat-box .desc { font-size:13px; opacity:.85; }
.stat-box.compact .desc { font-size:12px; }
.tips { margin-top:18px; font-size:12px; color:#64748b; }

/* Current experiments */
.exp-list { list-style:none; padding:0; margin:16px 0 0; display:flex; flex-direction:column; gap:16px; }
.exp-item { display:flex; gap:24px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; padding:16px 18px; align-items:center; }
.exp-item .main { flex:1; display:flex; flex-direction:column; gap:8px; }
.exp-item .title { font-size:15px; font-weight:600; color:#1e293b; }
.exp-item .meta { font-size:12px; color:#64748b; }
.progress-bar { height:8px; background:#e2e8f0; border-radius:4px; overflow:hidden; position:relative; }
.progress-bar .inner { height:100%; background:#3b82f6; transition:width .3s; }
.actions { display:flex; align-items:center; }
button.primary.small { background:#2563eb; color:#fff; border:none; border-radius:8px; padding:8px 14px; font-size:13px; cursor:pointer; font-weight:600; letter-spacing:.5px; transition:.25s; }
button.primary.small:hover { background:#1d4ed8; box-shadow:0 4px 12px -4px rgba(37,99,235,.45); }

/* History */
.history-table { width:100%; border-collapse:collapse; margin-top:14px; font-size:14px; }
.history-table th, .history-table td { padding:10px 12px; text-align:left; border-bottom:1px solid #e2e8f0; }
.history-table th { background:#f1f5f9; font-weight:600; font-size:12px; letter-spacing:.5px; color:#475569; text-transform:uppercase; }
.history-table tbody tr:hover { background:#f8fafc; }
.score { font-weight:600; }
.score.high { color:#16a34a; }
.score.mid { color:#2563eb; }
.empty { padding:26px 8px; text-align:center; color:#64748b; font-size:14px; }

/* 通知样式 */
.notify-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:4px; }
.notice-list { list-style:none; padding:0; margin:12px 0 0; display:flex; flex-direction:column; gap:12px; }
.notice-item { display:flex; align-items:flex-start; gap:16px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:14px 16px; }
.n-main { flex:1; display:flex; flex-direction:column; gap:6px; }
.n-title { font-size:14px; font-weight:600; color:#1e293b; line-height:1.4; }
.n-time { font-size:12px; color:#64748b; }

/* 欢迎页样式 */
.welcome-panel { text-align:center; padding:60px 40px; display:flex; flex-direction:column; gap:18px; }
.welcome-title { margin:0; font-size:28px; font-weight:600; background:linear-gradient(90deg,#3b82f6,#2563eb); -webkit-background-clip:text; background-clip: text; color:transparent; }
.welcome-tip { margin:0; font-size:15px; color:#475569; }
.welcome-sub { margin:0; font-size:13px; color:#64748b; }

/* 响应式 */
@media (max-width: 900px){
  .sidebar { position:fixed; left:0; top:0; bottom:0; z-index:40; box-shadow:4px 0 14px -6px rgba(0,0,0,.35); }
  .content-area { padding:24px 20px 34px; margin-left:var(--ml); transition:margin-left .25s; }
  :root { --ml: 220px; }
  .sidebar.collapsed ~ .content-area { margin-left:68px; }
  .sidebar:not(.collapsed) ~ .content-area { margin-left:220px; }
}
@media (max-width: 640px){
  .sidebar { transform:translateX(0); }
  .sidebar.collapsed { width:54px; }
  .page-title { font-size:20px; margin-bottom:18px; }
  .exp-item { flex-direction:column; align-items:flex-start; gap:14px; }
  button.primary.small { width:100%; justify-content:center; }
  .welcome-panel { padding:42px 24px; }
  .welcome-title { font-size:22px; }
  .welcome-tip { font-size:14px; }
}
.loading-line { padding:6px 4px; font-size:13px; color:#64748b; }
.error-line { padding:6px 4px; font-size:13px; color:#dc2626; }
</style>
