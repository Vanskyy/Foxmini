<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import { apiListTeacherExperiments, type TeacherExperimentItem, type PageResult, apiCreateExperiment, type ExperimentCreateRequest, apiDeleteExperiment, apiPublishExperiment, type PublishExperimentRequest, apiUnpublishExperiment, apiListRunningPublishedExperiments, apiListFinishedPublishedExperiments, type PublishedExperimentResponse, apiDeletePublishedExperiment } from '@/api/teacher'
import router from '@/router'
import { apiListGroups, type GroupResponse } from '@/api/group'

// 加载状态 & 错误
const loading = ref(false)
const error = ref('')

// 名称筛选
const nameKeyword = ref('')
const statusFilter = ref<string>('')

// 数据分页
const page = ref(0)
const size = ref(10)
const total = ref(0)
const totalPages = ref(0)

// 模拟数据（后续替换为 API）
const experiments = ref<ExperimentItemUI[]>([])
const auth = useAuthStore()

interface ExperimentItemUI extends TeacherExperimentItem {
  // 扩展 UI 字段（若后端后续提供 createdAt / updatedAt 可直接替换）
  createdAt?: string
  updatedAt?: string
}

// 已发布实例列表（运行中 / 已结束）
const runningPublished = ref<PublishedExperimentResponse[]>([])
const finishedPublished = ref<PublishedExperimentResponse[]>([])

const experimentTitleMap = computed(()=>{
  const m: Record<number,string> = {}
  experiments.value.forEach(e=>{ if(e.id) m[e.id]=e.title })
  return m
})

async function loadPublished(){
  try {
    const [r,f] = await Promise.all([
      apiListRunningPublishedExperiments(),
      apiListFinishedPublishedExperiments()
    ])
    runningPublished.value = r
    finishedPublished.value = f
  } catch {/* ignore */}
}

async function loadList(){
  if(!auth.user?.id){ return }
  loading.value = true; error.value=''
  try {
    const res: PageResult<TeacherExperimentItem> = await apiListTeacherExperiments(Number(auth.user.id), {
      page: page.value,
      size: size.value,
      keyword: nameKeyword.value || undefined,
      status: statusFilter.value || undefined,
    })
    experiments.value = res.content.map(e=> ({...e}))
    total.value = res.totalElements
    totalPages.value = res.totalPages
  } catch(e:any){ error.value = e.message || '加载失败' } finally { loading.value = false }
  loadPublished()
}

watch([nameKeyword, statusFilter], ()=>{ page.value = 0; loadList() })

const filtered = computed(()=> experiments.value )

function onEdit(e: ExperimentItemUI){
  if(!e.id) return
  const routeLoc = router.resolve({ name:'teacher-experiment-create', query:{ id:e.id } })
  window.open(routeLoc.href, '_blank')
}
// 只负责打开发布弹窗，不再承担下线逻辑
function onPublish(e: ExperimentItemUI){
  openPublishDialog(e)
}
function onDelete(e: ExperimentItemUI){
  if(!auth.user?.id) return
  if(confirm('删除后不可恢复，确认删除: ' + e.title + ' ?')){
    const tid = Number(auth.user.id)
    apiDeleteExperiment(tid, e.id)
      .then(()=>{
        experiments.value = experiments.value.filter(x=>x.id!==e.id)
        total.value = Math.max(0, total.value - 1)
        if(experiments.value.length===0 && page.value>0){ page.value -= 1; loadList() } // 若当前页删空回退一页
      })
      .catch(err=> alert('删除失败: '+ err.message))
  }
}

// 新增：创建实验前的弹窗表单状态
const showCreateModal = ref(false)
const newTitle = ref('')
const newDesc = ref('')
const createErrors = ref<string[]>([])
const creating = ref(false)
function openCreateDialog(){
  newTitle.value=''
  newDesc.value=''
  createErrors.value=[]
  showCreateModal.value = true
}
function cancelCreateDialog(){ showCreateModal.value=false }
function confirmCreateDialog(){
  createErrors.value = []
  if(!newTitle.value.trim()) createErrors.value.push('标题必填')
  if(!newDesc.value.trim()) createErrors.value.push('描述必填')
  if(createErrors.value.length) return
  // 调后端创建：只提交标题+描述，初始为空阶段与资源（后续在创建页继续编辑保存）
  if(!auth.user?.id){ createErrors.value.push('未登录'); return }
  creating.value = true
  const payload: ExperimentCreateRequest = { title: newTitle.value.trim(), description: newDesc.value.trim(), status: 'DRAFT' }
  apiCreateExperiment(Number(auth.user.id), payload)
    .then(res=>{
      const url = router.resolve({ name:'teacher-experiment-create', query:{ id:res.experimentId, title:newTitle.value.trim(), description:newDesc.value.trim() }}).href
      window.open(url, '_blank')
      showCreateModal.value=false
      // 重新加载列表
      loadList()
    })
    .catch(err=>{ createErrors.value.push(err.message||'创建失败') })
    .finally(()=> creating.value=false)
}

function onCreate(){
  // 弹出输入标题/描述对话框
  openCreateDialog()
}

function formatTime(v?: string){
  if(!v) return '-'
  // 兼容 LocalDateTime (无Z) 与 ISO 带毫秒
  const normalized = v.replace(' ', 'T')
  const d = new Date(normalized)
  if(isNaN(d.getTime())) return v
  const pad = (n:number)=> n<10? '0'+n : ''+n
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ========== 发布实验 弹窗逻辑 ==========
const showPublishModal = ref(false)
const publishExperimentId = ref<number | null>(null)
const publishStart = ref<string>('')
const publishEnd = ref<string>('')
const allowLate = ref(false)

// 目标类型： ALL / GROUP / INDIVIDUAL
const targetMode = ref<'ALL' | 'GROUP' | 'INDIVIDUAL'>('ALL')
const targetGroupIds = ref<number[]>([]) // 可多选组
const targetUserIdsText = ref('') // 文本输入多个用户

// 初始公告
const annTitle = ref('')
const annContent = ref('')
const annImportant = ref(false)

const publishErrors = ref<string[]>([])
const publishing = ref(false)

// 供选择的组
const groupOptions = ref<GroupResponse[]>([])
async function loadGroupsForPublish(){
  if(!auth.user?.id) return
  try { groupOptions.value = await apiListGroups(Number(auth.user.id)) } catch{/* ignore */}
}

function openPublishDialog(e: ExperimentItemUI){
  publishExperimentId.value = e.id
  publishStart.value = new Date(Date.now()+5*60*1000).toISOString().slice(0,16) // 默认5分钟后
  publishEnd.value = new Date(Date.now()+2*60*60*1000).toISOString().slice(0,16) // 2小时
  allowLate.value = false
  targetMode.value = 'ALL'
  targetGroupIds.value = []
  targetUserIdsText.value = ''
  annTitle.value = ''
  annContent.value = ''
  annImportant.value = false
  publishErrors.value = []
  showPublishModal.value = true
  loadGroupsForPublish()
}
function cancelPublish(){ showPublishModal.value=false }

function parseIds(text: string): number[]{
  return text.split(/[,\s;；，]+/).map(s=>s.trim()).filter(Boolean).map(Number).filter(n=>!isNaN(n))
}

async function confirmPublish(){
  publishErrors.value = []
  if(!auth.user?.id){ publishErrors.value.push('未登录'); return }
  if(!publishExperimentId.value){ publishErrors.value.push('缺少实验ID'); return }
  if(!publishStart.value) publishErrors.value.push('开始时间必填')
  if(!publishEnd.value) publishErrors.value.push('结束时间必填')
  if(publishStart.value && publishEnd.value && publishStart.value >= publishEnd.value) publishErrors.value.push('开始时间需早于结束时间')

  const targets: PublishExperimentRequest['targets'] = []
  if(targetMode.value === 'ALL'){
    targets.push({ targetType:'ALL' })
  } else if(targetMode.value === 'GROUP'){
    if(!targetGroupIds.value.length) publishErrors.value.push('请选择至少一个分组')
    targetGroupIds.value.forEach(gid=> targets.push({ targetType:'GROUP', targetId: gid }))
  } else if(targetMode.value === 'INDIVIDUAL'){
    const ids = parseIds(targetUserIdsText.value)
    if(!ids.length) publishErrors.value.push('请输入至少一个用户ID')
    ids.forEach(uid=> targets.push({ targetType:'INDIVIDUAL', targetId: uid }))
  }

  let initialAnnouncement: PublishExperimentRequest['initialAnnouncement'] | undefined
  if(annTitle.value.trim() || annContent.value.trim()){
    if(!annTitle.value.trim()) publishErrors.value.push('公告标题必填或清空内容')
    if(!annContent.value.trim()) publishErrors.value.push('公告内容必填或清空标题')
    if(!publishErrors.value.length){
      initialAnnouncement = { title: annTitle.value.trim(), content: annContent.value.trim(), important: annImportant.value }
    }
  }
  if(publishErrors.value.length) return

  const payload: PublishExperimentRequest = {
    experimentId: publishExperimentId.value,
    startTime: publishStart.value + ':00',
    endTime: publishEnd.value + ':00',
    allowLateSubmission: allowLate.value,
    targets,
    initialAnnouncement
  }
  publishing.value = true
  apiPublishExperiment(Number(auth.user.id), payload)
    .then(()=>{
      alert('发布成功')
      showPublishModal.value = false
      loadList()
    })
    .catch(err=> publishErrors.value.push(err.message||'发布失败'))
    .finally(()=> publishing.value=false)
}

function unpublishInstance(p: PublishedExperimentResponse){
  if(!auth.user?.id) return
  if(!confirm('确认下线该已发布实例?')) return
  apiUnpublishExperiment(Number(auth.user.id), p.id)
    .then(()=>{ alert('下线成功'); loadPublished() })
    .catch(err=> alert('下线失败: '+ err.message))
}

function deleteFinishedInstance(p: PublishedExperimentResponse){
  if(!auth.user?.id) return
  if(!confirm('确认删除该已结束发布实例 (ID '+p.id+') ?\n此操作不可恢复且要求无学生作答记录。')) return
  apiDeletePublishedExperiment(Number(auth.user.id), p.id)
    .then(()=>{ alert('删除成功'); loadPublished() })
    .catch(err=> alert('删除失败: '+ err.message))
}

loadList()
</script>
<template>
  <section class="panel fade-in">
    <div class="panel-head">
      <div class="actions">
        <input class="filter-input" v-model="nameKeyword" placeholder="按名称筛选..." />
        <select class="filter-select" v-model="statusFilter">
          <option value="">全部状态</option>
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">已发布</option>
          <option value="ARCHIVED">已归档</option>
        </select>
        <button class="primary" @click="onCreate">新建实验</button>
      </div>
    </div>
    <div v-if="loading" class="state-line">加载中...</div>
    <div v-else-if="error" class="state-line error">{{ error }}</div>
    <div v-else>
      <table class="exp-table">
        <thead>
          <tr>
            <th style="width:46px;">ID</th>
            <th>名称</th>
            <th style="width:90px;">状态</th>
            <th style="width:120px;">创建时间</th>
            <th style="width:120px;">更新时间</th>
            <th style="width:210px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!filtered.length">
            <td colspan="6" class="empty-cell">暂无实验</td>
          </tr>
          <tr v-for="e in filtered" :key="e.id">
            <td>{{ e.id }}</td>
            <td class="name">{{ e.title }}</td>
            <td>
              <span :class="['badge', (e.status||'').toLowerCase()]">{{ e.status==='PUBLISHED' ? '已发布' : (e.status==='ARCHIVED' ? '已归档' : '草稿') }}</span>
            </td>
            <td>{{ formatTime(e.createdAt) }}</td>
            <td>{{ formatTime(e.updatedAt) }}</td>
            <td class="ops">
              <button class="link" @click="onEdit(e)">编辑</button>
              <button class="link" @click="onPublish(e)">发布</button>
              <button class="link danger" @click="onDelete(e)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="totalPages>1">
        <button :disabled="page===0" @click="page=0; loadList()">«</button>
        <button :disabled="page===0" @click="page--; loadList()">‹</button>
        <span class="pg-info">第 {{ page+1 }}/{{ totalPages }} 页 (共 {{ total }} 条)</span>
        <button :disabled="page>=totalPages-1" @click="page++; loadList()">›</button>
        <button :disabled="page>=totalPages-1" @click="page=totalPages-1; loadList()">»</button>
      </div>
      <div class="help-hint">提示：已完成</div>
    </div>
  </section>
  <!-- 新增创建实验弹窗 -->
  <div v-if="showCreateModal" class="modal-mask">
    <div class="modal">
      <h3>新建实验</h3>
      <div class="form">
        <label>标题<span class="req">*</span></label>
        <input v-model="newTitle" placeholder="请输入实验标题" />
        <label>描述<span class="req">*</span></label>
        <textarea v-model="newDesc" rows="4" placeholder="请输入实验描述"></textarea>
        <div v-if="createErrors.length" class="errors">
          <div class="err" v-for="(er,i) in createErrors" :key="i">{{ er }}</div>
        </div>
      </div>
      <div class="modal-actions">
        <button class="btn ghost" @click="cancelCreateDialog" :disabled="creating">取消</button>
        <button class="btn primary" @click="confirmCreateDialog" :disabled="creating">{{ creating? '创建中...' : '确定并进入创建' }}</button>
      </div>
    </div>
  </div>

  <!-- 发布实验弹窗 -->
  <div v-if="showPublishModal" class="modal-mask">
    <div class="modal publish">
      <h3>发布实验</h3>
      <div class="form grid">
        <div class="field col2">
          <label>实验ID</label>
          <input :value="publishExperimentId||''" disabled />
        </div>
        <div class="field">
          <label>开始时间<span class="req">*</span></label>
          <input type="datetime-local" v-model="publishStart" />
        </div>
        <div class="field">
          <label>结束时间<span class="req">*</span></label>
          <input type="datetime-local" v-model="publishEnd" />
        </div>
        <div class="field col2 inline">
          <label><input type="checkbox" v-model="allowLate" /> 允许迟交</label>
        </div>
        <div class="field col2">
          <label>分配对象模式</label>
          <select v-model="targetMode">
            <option value="ALL">全体学生</option>
            <option value="GROUP">按分组</option>
            <option value="INDIVIDUAL">按指定学生</option>
          </select>
        </div>
        <template v-if="targetMode==='GROUP'">
          <div class="field col2">
            <label>选择分组 (可多选)</label>
            <div class="chips-box">
              <label v-for="g in groupOptions" :key="g.id" class="chip">
                <input type="checkbox" :value="g.id" v-model="targetGroupIds" /> {{ g.name }}
              </label>
            </div>
          </div>
        </template>
        <template v-else-if="targetMode==='INDIVIDUAL'">
          <div class="field col2">
            <label>学生用户ID列表</label>
            <textarea v-model="targetUserIdsText" placeholder="多个ID用逗号/空格"></textarea>
          </div>
        </template>
        <div class="divider col2"></div>
        <div class="field col2">
          <label>首条公告（可选）</label>
          <input v-model="annTitle" placeholder="公告标题" />
          <textarea v-model="annContent" rows="3" placeholder="公告内容"></textarea>
          <label class="inline"><input type="checkbox" v-model="annImportant" /> 重要</label>
        </div>
        <div v-if="publishErrors.length" class="errors col2">
          <div class="err" v-for="(er,i) in publishErrors" :key="i">{{ er }}</div>
        </div>
      </div>
      <div class="modal-actions">
        <button class="btn ghost" @click="cancelPublish" :disabled="publishing">取消</button>
        <button class="btn primary" @click="confirmPublish" :disabled="publishing">{{ publishing? '发布中...' : '确认发布' }}</button>
      </div>
    </div>
  </div>

  <section class="panel fade-in published-panel">
    <h4 class="pub-title">已发布实验实例</h4>
    <div class="pub-section">
      <h5>运行中</h5>
      <table class="exp-table mini">
        <thead>
          <tr>
            <th style="width:60px;">发布ID</th>
            <th style="width:60px;">实验ID</th>
            <th>实验名称</th>
            <th style="width:140px;">开始</th>
            <th style="width:140px;">结束</th>
            <th style="width:90px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!runningPublished.length"><td colspan="6" class="empty-cell">暂无运行中实例</td></tr>
          <tr v-for="p in runningPublished" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.experimentId }}</td>
            <td>{{ experimentTitleMap[p.experimentId] || '-' }}</td>
            <td>{{ formatTime(p.startTime) }}</td>
            <td>{{ formatTime(p.endTime) }}</td>
            <td class="ops"><button class="link" @click="unpublishInstance(p)">下线</button></td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="pub-section">
      <h5>已结束</h5>
      <table class="exp-table mini">
        <thead>
          <tr>
            <th style="width:60px;">发布ID</th>
            <th style="width:60px;">实验ID</th>
            <th>实验名称</th>
            <th style="width:140px;">开始</th>
            <th style="width:140px;">结束</th>
            <th style="width:80px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!finishedPublished.length"><td colspan="6" class="empty-cell">暂无已结束实例</td></tr>
            <tr v-for="p in finishedPublished" :key="p.id">
              <td>{{ p.id }}</td>
              <td>{{ p.experimentId }}</td>
              <td>{{ experimentTitleMap[p.experimentId] || '-' }}</td>
              <td>{{ formatTime(p.startTime) }}</td>
              <td>{{ formatTime(p.endTime) }}</td>
              <td class="ops"><button class="link danger" @click="deleteFinishedInstance(p)">删除</button></td>
            </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
<style scoped>
.panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:22px 24px 26px; }
.panel-head { display:flex; align-items:center; justify-content:space-between; gap:16px; margin-bottom:14px; flex-wrap:wrap; }
.actions { display:flex; gap:12px; align-items:center; flex-wrap:wrap; }
button.primary { background:#6366f1; color:#fff; border:none; padding:8px 16px; border-radius:10px; font-size:14px; font-weight:600; cursor:pointer; opacity:1; }
button.primary:hover { background:#4f46e5; }
.filter-input { height:38px; border:1px solid #cbd5e1; border-radius:10px; padding:0 12px; font-size:14px; min-width:220px; }
.filter-select { height:38px; border:1px solid #cbd5e1; border-radius:10px; padding:0 10px; font-size:14px; background:#fff; }
.state-line { padding:6px 4px; font-size:13px; color:#64748b; }
.state-line.error { color:#dc2626; }
.exp-table { width:100%; border-collapse:separate; border-spacing:0 8px; }
.exp-table thead th { text-align:left; font-size:12px; font-weight:600; color:#64748b; padding:0 12px 6px; }
.exp-table tbody tr { background:#f8fafc; box-shadow:0 1px 2px rgba(0,0,0,.04); transition:.18s; }
.exp-table tbody tr:hover { background:#eef2ff; }
.exp-table tbody td { padding:14px 12px; font-size:13px; color:#334155; border-top:1px solid #e2e8f0; border-bottom:1px solid #e2e8f0; }
.exp-table tbody td:first-child { border-left:1px solid #e2e8f0; border-top-left-radius:10px; border-bottom-left-radius:10px; }
.exp-table tbody td:last-child { border-right:1px solid #e2e8f0; border-top-right-radius:10px; border-bottom-right-radius:10px; }
.exp-table .name { font-weight:600; }
.badge { display:inline-block; padding:4px 10px; border-radius:999px; font-size:12px; font-weight:600; letter-spacing:.5px; }
.badge.published { background:#d1fae5; color:#047857; }
.badge.draft { background:#e2e8f0; color:#475569; }
.badge.archived { background:#fde68a; color:#92400e; }
.ops { display:flex; gap:6px; flex-wrap:wrap; }
button.link { background:transparent; border:none; padding:4px 6px; font-size:12px; font-weight:600; color:#6366f1; cursor:pointer; border-radius:6px; }
button.link:hover { background:rgba(99,102,241,.12); }
button.link.danger { color:#dc2626; }
button.link.danger:hover { background:rgba(220,38,38,.12); }
.empty-cell { text-align:center; padding:38px 0; font-size:13px; color:#64748b; }
.pagination { margin-top:14px; display:flex; align-items:center; gap:8px; font-size:12px; }
.pagination button { background:#fff; border:1px solid #cbd5e1; padding:4px 8px; border-radius:6px; cursor:pointer; font-size:12px; }
.pagination button:disabled { opacity:.4; cursor:not-allowed; }
.pg-info { color:#475569; }
.help-hint { margin-top:10px; font-size:12px; color:#64748b; }
.fade-in { animation:fade .25s ease; }
@keyframes fade { from { opacity:0; transform:translateY(4px); } to { opacity:1; transform:translateY(0); } }

.modal-mask{position:fixed;inset:0;background:rgba(15,23,42,.45);display:flex;align-items:center;justify-content:center;z-index:3000;}
.modal{background:#fff;width:540px;max-width:94%;border-radius:18px;padding:26px 28px 30px;box-shadow:0 10px 40px -4px rgba(0,0,0,.25);display:flex;flex-direction:column;gap:18px;animation:pop .18s ease;}
.modal.publish{width:680px;}
.modal h3{margin:0;font-size:18px;font-weight:700;color:#1e293b;}
.modal .form{display:flex;flex-direction:column;gap:12px;}
.modal .form.grid{display:grid;grid-template-columns:repeat(2,1fr);column-gap:18px;row-gap:14px;}
.field{display:flex;flex-direction:column;gap:6px;}
.field.inline{flex-direction:row;align-items:center;gap:10px;}
.field label{font-size:12px;font-weight:600;color:#475569;}
.field textarea{resize:vertical;min-height:70px;}
.field input,.field select,.field textarea{border:1px solid #cbd5e1;border-radius:10px;padding:10px 12px;font-size:14px;outline:none;font-family:inherit;}
.field input:focus,.field select:focus,.field textarea:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.2);}
.col2{grid-column:span 2;}
.chips-box{display:flex;flex-wrap:wrap;gap:8px;}
.chip{background:#f1f5f9;padding:6px 10px;border-radius:20px;font-size:12px;display:flex;align-items:center;gap:4px;}
.divider{height:1px;background:#e2e8f0;margin:4px 0;}
.modal-actions{display:flex;justify-content:flex-end;gap:12px;margin-top:4px;}
.btn{border:none;cursor:pointer;font-size:13px;font-weight:600;padding:8px 16px;border-radius:8px;letter-spacing:.5px;}
.btn.primary{background:#6366f1;color:#fff;}
.btn.primary:hover{background:#4f46e5;}
.btn.ghost{background:#e2e8f0;color:#1e293b;}
.btn.ghost:hover{background:#cbd5e1;}
.errors{display:flex;flex-direction:column;gap:4px;}
.err{font-size:12px;color:#dc2626;background:#fee2e2;padding:4px 8px;border-radius:6px;}
.req{color:#dc2626;margin-left:4px;}
@keyframes pop{from{transform:scale(.92);opacity:0;}to{transform:scale(1);opacity:1;}}
.published-panel{margin-top:18px;}
.pub-title{margin:0 0 10px;font-size:16px;font-weight:700;color:#1e293b;}
.pub-section{margin-top:12px;}
.exp-table.mini tbody td{font-size:12px;padding:10px 10px;}
</style>
