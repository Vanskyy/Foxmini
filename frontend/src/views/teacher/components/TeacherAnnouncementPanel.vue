<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import { apiListRunningPublishedExperiments, apiListFinishedPublishedExperiments, apiListAnnouncements, apiCreateAnnouncement, apiUpdateAnnouncement, apiDeleteAnnouncement, type PublishedExperimentResponse, type AnnouncementResponse, type AnnouncementCreateRequest, type AnnouncementUpdateRequest } from '@/api/teacher'

const auth = useAuthStore()

const loadingPub = ref(false)
const runningList = ref<PublishedExperimentResponse[]>([])
const finishedList = ref<PublishedExperimentResponse[]>([])
// 移除 showFinished，改为同时展示

const selectedPublishId = ref<number | null>(null)
const loadingAnnouncements = ref(false)
const announcements = ref<AnnouncementResponse[]>([])

// 新公告表单
const showCreate = ref(false)
const annTitle = ref('')
const annContent = ref('')
const annImportant = ref(false)
const creating = ref(false)
const formErrors = ref<string[]>([])

// 编辑公告表单
const editingId = ref<number | null>(null)
const editTitle = ref('')
const editContent = ref('')
const editImportant = ref(false)
const savingEdit = ref(false)

async function loadPublished(){
  loadingPub.value = true
  try {
    const [running, finished] = await Promise.all([
      apiListRunningPublishedExperiments(),
      apiListFinishedPublishedExperiments(),
    ])
    runningList.value = running
    finishedList.value = finished
    if(!selectedPublishId.value){
      selectedPublishId.value = running[0]?.id || finished[0]?.id || null
      if(selectedPublishId.value) loadAnnouncements(selectedPublishId.value)
    }
  } finally { loadingPub.value = false }
}

async function loadAnnouncements(pid: number){
  loadingAnnouncements.value = true
  try { announcements.value = await apiListAnnouncements(pid) } finally { loadingAnnouncements.value = false }
}

function selectPublish(id: number){
  selectedPublishId.value = id
  loadAnnouncements(id)
}

function openCreate(){
  if(!selectedPublishId.value){ alert('请选择一个已发布实验'); return }
  annTitle.value=''
  annContent.value=''
  annImportant.value=false
  formErrors.value=[]
  showCreate.value = true
}
function cancelCreate(){ showCreate.value=false }
async function confirmCreate(){
  formErrors.value=[]
  if(!selectedPublishId.value) { formErrors.value.push('未选择实验'); return }
  if(!annTitle.value.trim()) formErrors.value.push('标题必填')
  if(!annContent.value.trim()) formErrors.value.push('内容必填')
  if(!auth.user?.id) formErrors.value.push('未登录')
  if(formErrors.value.length) return
  creating.value = true
  const payload: AnnouncementCreateRequest = { title: annTitle.value.trim(), content: annContent.value.trim(), important: annImportant.value }
  try {
    await apiCreateAnnouncement(selectedPublishId.value, payload, Number(auth.user!.id))
    showCreate.value=false
    await loadAnnouncements(selectedPublishId.value)
  } catch(e:any){ formErrors.value.push(e.message||'创建失败') } finally { creating.value=false }
}

function startEdit(a: AnnouncementResponse){
  editingId.value = a.id
  editTitle.value = a.title
  editContent.value = a.content
  editImportant.value = a.important
}
function cancelEdit(){ editingId.value=null }
async function confirmEdit(){
  if(!editingId.value || !auth.user?.id) return
  if(!editTitle.value.trim() || !editContent.value.trim()) return
  savingEdit.value = true
  const payload: AnnouncementUpdateRequest = { title: editTitle.value.trim(), content: editContent.value.trim(), important: editImportant.value }
  try {
    await apiUpdateAnnouncement(editingId.value, payload, Number(auth.user.id))
    if(selectedPublishId.value) await loadAnnouncements(selectedPublishId.value)
    editingId.value = null
  } catch(e:any){ alert(e.message||'更新失败') } finally { savingEdit.value=false }
}
async function deleteAnnouncement(a: AnnouncementResponse){
  if(!auth.user?.id) return
  if(!confirm('确认删除公告: ' + a.title + ' ?')) return
  try {
    await apiDeleteAnnouncement(a.id, Number(auth.user.id))
    if(selectedPublishId.value) await loadAnnouncements(selectedPublishId.value)
  } catch(e:any){ alert(e.message||'删除失败') }
}

const currentAnnouncements = computed(()=> announcements.value )

function formatTime(v?: string){
  if(!v) return ''
  const d = new Date(v.replace(' ','T'))
  if(Number.isNaN(d.getTime())) return v
  return d.toLocaleString()
}

loadPublished()
</script>
<template>
  <section class="panel fade-in announcement-panel">
    <div class="header-row">
      <h3>实验公告管理</h3>
      <div class="right">
        <button class="btn primary" @click="openCreate">新建公告</button>
      </div>
    </div>
    <div class="pub-sections" v-if="!loadingPub">
      <div class="pub-box">
        <div class="box-head">运行中实例 <span class="count">{{ runningList.length }}</span></div>
        <div class="items">
          <button v-for="p in runningList" :key="p.id" :class="['pub-item',{active:p.id===selectedPublishId}]" @click="selectPublish(p.id)">
            <div class="row1"><span class="pid">#{{ p.id }}</span><span class="exp">Exp {{ p.experimentId }}</span></div>
            <div class="time">{{ p.startTime?.slice(5,16).replace('T',' ') }} ~ {{ p.endTime?.slice(5,16).replace('T',' ') }}</div>
          </button>
          <div v-if="!runningList.length" class="empty">暂无运行中实例</div>
        </div>
      </div>
      <div class="pub-box">
        <div class="box-head">已结束实例 <span class="count">{{ finishedList.length }}</span></div>
        <div class="items">
          <button v-for="p in finishedList" :key="p.id" :class="['pub-item finished',{active:p.id===selectedPublishId}]" @click="selectPublish(p.id)">
            <div class="row1"><span class="pid">#{{ p.id }}</span><span class="exp">Exp {{ p.experimentId }}</span></div>
            <div class="time">{{ p.startTime?.slice(5,16).replace('T',' ') }} ~ {{ p.endTime?.slice(5,16).replace('T',' ') }}</div>
          </button>
          <div v-if="!finishedList.length" class="empty">暂无已结束实例</div>
        </div>
      </div>
    </div>
    <div v-else class="loading">加载发布列表...</div>

    <div class="ann-box" v-if="selectedPublishId">
      <div class="ann-head">公告列表 (发布ID {{ selectedPublishId }})</div>
      <div class="ann-list" v-if="!loadingAnnouncements">
        <div v-for="a in currentAnnouncements" :key="a.id" class="ann-item" :class="{important:a.important, editing: a.id===editingId}">
          <template v-if="editingId===a.id">
            <div class="edit-fields">
              <input v-model="editTitle" class="edit-input" />
              <textarea v-model="editContent" rows="3" class="edit-textarea" />
              <label class="inline small"><input type="checkbox" v-model="editImportant" /> 重要</label>
              <div class="edit-actions">
                <button class="btn mini ghost" @click="cancelEdit" :disabled="savingEdit">取消</button>
                <button class="btn mini primary" @click="confirmEdit" :disabled="savingEdit">{{ savingEdit? '保存中...' : '保存' }}</button>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="ann-line">
              <span class="ann-title">{{ a.title }}</span>
              <span class="ann-time">{{ formatTime(a.createdAt) }}</span>
            </div>
            <div class="ann-content">{{ a.content }}</div>
            <div class="ann-ops">
              <button class="link" @click="startEdit(a)">编辑</button>
              <button class="link danger" @click="deleteAnnouncement(a)">删除</button>
            </div>
          </template>
        </div>
        <div v-if="!currentAnnouncements.length" class="empty">暂无公告</div>
      </div>
      <div v-else class="loading">加载公告...</div>
    </div>
  </section>

  <!-- 创建公告弹窗 -->
  <div v-if="showCreate" class="modal-mask" @click.self="cancelCreate">
    <div class="modal">
      <h3>新建公告</h3>
      <div class="form">
        <label>标题<span class="req">*</span></label>
        <input v-model="annTitle" />
        <label>内容<span class="req">*</span></label>
        <textarea v-model="annContent" rows="4" />
        <label class="inline"><input type="checkbox" v-model="annImportant" /> 重要</label>
        <div v-if="formErrors.length" class="errors">
          <div class="err" v-for="(er,i) in formErrors" :key="i">{{ er }}</div>
        </div>
      </div>
      <div class="modal-actions">
        <button class="btn ghost" @click="cancelCreate" :disabled="creating">取消</button>
        <button class="btn primary" @click="confirmCreate" :disabled="creating">{{ creating? '提交中...' : '提交' }}</button>
      </div>
    </div>
  </div>
</template>
<style scoped>
.announcement-panel{display:flex;flex-direction:column;gap:18px;}
.header-row{display:flex;justify-content:space-between;align-items:center;}
.header-row h3{margin:0;font-size:18px;color:#334155;font-weight:600;}
.pub-sections{display:flex;flex-direction:column;gap:14px;}
.pub-box{background:#fff;border:1px solid #e2e8f0;border-radius:14px;padding:14px 16px;display:flex;flex-direction:column;gap:12px;}
.box-head{font-size:14px;font-weight:600;color:#334155;display:flex;align-items:center;gap:6px;}
.count{font-size:12px;color:#6366f1;font-weight:500;}
.items{display:flex;flex-wrap:wrap;gap:10px;}
.pub-item{background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:10px 12px;display:flex;flex-direction:column;gap:4px;min-width:170px;cursor:pointer;align-items:flex-start;position:relative;}
.pub-item.finished{opacity:.78;}
.pub-item.active{border-color:#6366f1;box-shadow:0 0 0 2px rgba(99,102,241,.25);}
.pub-item .row1{display:flex;gap:8px;align-items:center;}
.pub-item .pid{font-size:12px;color:#64748b;}
.pub-item .exp{font-size:13px;font-weight:600;color:#334155;}
.pub-item .time{font-size:11px;color:#64748b;}
.ann-box{background:#fff;border:1px solid #e2e8f0;border-radius:14px;padding:16px 18px 20px;display:flex;flex-direction:column;gap:14px;}
.ann-head{font-size:14px;font-weight:600;color:#334155;}
.ann-list{display:flex;flex-direction:column;gap:12px;max-height:360px;overflow:auto;}
.ann-item{background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:10px 12px;display:flex;flex-direction:column;gap:6px;}
.ann-item.important{background:#fef3c7;border-color:#f59e0b;}
.ann-item.editing{background:#eef2ff;border-color:#6366f1;}
.ann-line{display:flex;justify-content:space-between;gap:12px;align-items:center;}
.ann-title{font-size:13px;font-weight:600;color:#334155;}
.ann-time{font-size:11px;color:#64748b;}
.ann-content{font-size:12px;color:#475569;line-height:1.5;white-space:pre-wrap;}
.empty{font-size:12px;color:#64748b;padding:8px 4px;}
.loading{font-size:12px;color:#64748b;}
.btn{background:#6366f1;color:#fff;border:none;padding:6px 14px;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;}
.btn.primary{background:#4f46e5;}
.btn.ghost{background:#e2e8f0;color:#334155;}
.modal-mask{position:fixed;inset:0;background:rgba(15,23,42,.45);display:flex;align-items:center;justify-content:center;z-index:3000;}
.modal{background:#fff;width:480px;max-width:92%;border-radius:18px;padding:26px 28px 30px;box-shadow:0 10px 40px -4px rgba(0,0,0,.25);display:flex;flex-direction:column;gap:18px;animation:pop .18s ease;}
.modal h3{margin:0;font-size:18px;font-weight:700;color:#1e293b;}
.modal .form{display:flex;flex-direction:column;gap:10px;}
.modal label{font-size:12px;font-weight:600;color:#475569;display:flex;align-items:center;gap:4px;}
.modal .req{color:#dc2626;}
.modal input,.modal textarea{border:1px solid #cbd5e1;border-radius:10px;padding:10px 12px;font-size:14px;resize:vertical;outline:none;font-family:inherit;}
.modal input:focus,.modal textarea:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.2);}
.modal-actions{display:flex;justify-content:flex-end;gap:12px;margin-top:4px;}
.errors{display:flex;flex-direction:column;gap:4px;}
.err{font-size:12px;color:#dc2626;background:#fee2e2;padding:4px 8px;border-radius:6px;}
.inline{display:flex;align-items:center;gap:6px;}
.edit-fields{display:flex;flex-direction:column;gap:8px;}
.edit-input,.edit-textarea{border:1px solid #cbd5e1;border-radius:8px;padding:8px 10px;font-size:13px;outline:none;font-family:inherit;}
.edit-input:focus,.edit-textarea:focus{border-color:#6366f1;box-shadow:0 0 0 2px rgba(99,102,241,.2);}
.edit-actions{display:flex;gap:8px;justify-content:flex-end;}
.btn.mini{padding:4px 10px;font-size:12px;}
.link{background:transparent;border:none;padding:4px 6px;font-size:12px;font-weight:600;color:#6366f1;cursor:pointer;border-radius:6px;}
.link:hover{background:rgba(99,102,241,.12);}
.link.danger{color:#dc2626;}
.link.danger:hover{background:rgba(220,38,38,.12);}
.ann-ops{display:flex;gap:4px;margin-top:4px;}
@keyframes pop{from{transform:scale(.92);opacity:0;}to{transform:scale(1);opacity:1;}}
</style>
