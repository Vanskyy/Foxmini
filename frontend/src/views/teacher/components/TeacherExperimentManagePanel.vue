<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import { apiListTeacherExperiments, type TeacherExperimentItem, type PageResult } from '@/api/teacher'
import router from '@/router'

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
}

watch([nameKeyword, statusFilter], ()=>{ page.value = 0; loadList() })

const filtered = computed(()=> experiments.value )

function onPreview(e: ExperimentItemUI){
  console.log('preview', e.id)
  alert('预览实验: ' + e.title + ' (待实现)')
}
function onEdit(e: ExperimentItemUI){
  console.log('edit', e.id)
  alert('编辑实验: ' + e.title + ' (待实现)')
}
function onTogglePublish(e: ExperimentItemUI){
  if(e.status === 'published'){
    if(confirm('确认下线该实验?')){ e.status = 'draft'; e.updatedAt = new Date().toISOString().slice(0,10) }
  } else {
    if(confirm('确认发布该实验?')){ e.status = 'published'; e.updatedAt = new Date().toISOString().slice(0,10) }
  }
}
function onDelete(e: ExperimentItemUI){
  if(confirm('删除后不可恢复，确认删除: ' + e.title + ' ?')){
    experiments.value = experiments.value.filter(x=>x.id!==e.id)
  }
}

// 新增：创建实验前的弹窗表单状态
const showCreateModal = ref(false)
const newTitle = ref('')
const newDesc = ref('')
const createErrors = ref<string[]>([])
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
  const url = router.resolve({ name:'teacher-experiment-create', query:{ title:newTitle.value.trim(), description:newDesc.value.trim() }}).href
  window.open(url, '_blank')
  showCreateModal.value=false
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

loadList()
</script>
<template>
  <section class="panel fade-in">
    <div class="panel-head">
      <!-- 移除内部标题，外层已有紫色主标题 -->
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
              <button class="link" @click="onPreview(e)">预览</button>
              <button class="link" @click="onEdit(e)">编辑</button>
              <button class="link" @click="onTogglePublish(e)">{{ e.status==='PUBLISHED' ? '下线' : '发布' }}</button>
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
      <div class="help-hint">提示：已接入后端 API /teacher/experiments/{teacherUserId}</div>
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
        <button class="btn ghost" @click="cancelCreateDialog">取消</button>
        <button class="btn primary" @click="confirmCreateDialog">确定并进入创建</button>
      </div>
    </div>
  </div>
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
.modal{background:#fff;width:480px;max-width:92%;border-radius:18px;padding:26px 28px 30px;box-shadow:0 10px 40px -4px rgba(0,0,0,.25);display:flex;flex-direction:column;gap:18px;animation:pop .18s ease;}
.modal h3{margin:0;font-size:18px;font-weight:700;color:#1e293b;}
.modal .form{display:flex;flex-direction:column;gap:10px;}
.modal label{font-size:12px;font-weight:600;color:#475569;display:flex;align-items:center;gap:4px;}
.modal .req{color:#dc2626;}
.modal input,.modal textarea{border:1px solid #cbd5e1;border-radius:10px;padding:10px 12px;font-size:14px;resize:vertical;outline:none;}
.modal input:focus,.modal textarea:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.2);}
.modal-actions{display:flex;justify-content:flex-end;gap:12px;margin-top:4px;}
.btn{border:none;cursor:pointer;font-size:13px;font-weight:600;padding:8px 16px;border-radius:8px;letter-spacing:.5px;}
.btn.primary{background:#6366f1;color:#fff;}
.btn.primary:hover{background:#4f46e5;}
.btn.ghost{background:#e2e8f0;color:#1e293b;}
.btn.ghost:hover{background:#cbd5e1;}
.errors{display:flex;flex-direction:column;gap:4px;}
.err{font-size:12px;color:#dc2626;background:#fee2e2;padding:4px 8px;border-radius:6px;}
@keyframes pop{from{transform:scale(.92);opacity:0;}to{transform:scale(1);opacity:1;}}
</style>
