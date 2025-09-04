<template>
  <div class="group-panel panel fade-in">
    <div class="top-bar">
      <div class="title">分组列表</div>
      <div class="actions-inline">
        <button class="btn" @click="loadGroups">刷新</button>
        <button class="btn primary" @click="openCreate">新建分组</button>
      </div>
    </div>
    <div class="groups" v-if="!loadingGroups">
      <div v-for="g in groups" :key="g.id" :class="['group-item', {active:g.id===selectedGroupId}]" @click="onSelectGroup(g.id)">
        <div class="name">{{ g.name }}</div>
        <div class="meta">人数 {{ g.memberCount }}</div>
        <div class="actions" @click.stop>
          <button class="mini" @click="openEdit(g)">编辑</button>
          <button class="mini danger" @click="onDelete(g)">删除</button>
        </div>
      </div>
      <div v-if="!groups.length" class="empty">暂无分组</div>
    </div>
    <div v-else class="loading">加载中...</div>

    <div v-if="currentGroup" class="members-area">
      <h3 class="sub-title">当前分组: {{ currentGroup.name }}</h3>
      <div class="member-ops">
        <input v-model="singleUserId" placeholder="学生用户ID" />
        <button class="btn" @click="addSingle">添加</button>
        <span class="move-target">
          <span class="move-label">转移到目标分组:</span>
          <select v-model.number="moveTargetGroupId">
            <option v-for="g in groups.filter(x=>x.id!==selectedGroupId)" :key="g.id" :value="g.id">{{ g.name }}</option>
          </select>
        </span>
      </div>

      <table class="member-table" v-if="!loadingMembers">
        <thead>
          <tr><th>用户ID</th><th>用户名</th><th>姓名</th><th>加入时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="m in members" :key="m.userId">
            <td>{{ m.userId }}</td>
            <td>{{ m.username }}</td>
            <td>{{ m.realName || '-' }}</td>
            <td>{{ m.joinedAt?.replace('T',' ') || '' }}</td>
            <td>
              <button class="mini" @click="moveSingle(m.userId)" :title="moveTargetGroupId ? '转移到所选分组' : '请先选择上方目标分组'">转移</button>
              <button class="mini danger" @click="removeSingle(m.userId)">移出</button>
            </td>
          </tr>
          <tr v-if="!members.length"><td colspan="5" class="empty">暂无成员</td></tr>
        </tbody>
      </table>
      <div v-else class="loading">成员加载中...</div>

      <div class="batch-box">
        <div class="batch-section">
          <h4>批量添加</h4>
          <textarea v-model="batchUserIdsText" placeholder="多个用户ID，逗号/空格分隔"></textarea>
          <button class="btn" @click="batchAdd">批量添加</button>
        </div>
        <div class="batch-section">
          <h4>批量移除</h4>
            <textarea v-model="batchRemoveIdsText" placeholder="多个用户ID"></textarea>
          <button class="btn danger" @click="batchRemove">批量移除</button>
        </div>
      </div>
    </div>

    <div v-if="showEdit" class="modal-mask" @click.self="showEdit=false">
      <div class="modal">
        <h3>{{ editMode==='create' ? '新建分组' : '编辑分组' }}</h3>
        <div class="form-row">
          <label>名称</label>
          <input v-model="form.name" maxlength="50" />
        </div>
        <div class="form-row">
          <label>描述</label>
          <textarea v-model="form.description" maxlength="255" />
        </div>
        <div class="modal-actions">
          <button class="btn" @click="submitForm">确定</button>
          <button class="btn ghost" @click="showEdit=false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import {
  apiListGroups, apiCreateGroup, apiUpdateGroup, apiDeleteGroup,
  apiListGroupMembers, apiAddStudentToGroup, apiRemoveStudentFromGroup,
  apiMoveStudent, apiBatchAdd, apiBatchRemove,
  type GroupResponse, type GroupMemberResponse
} from '@/api/group'

const auth = useAuthStore()
const teacherId = computed(()=> auth.user?.id)

// 分组列表
const groups = ref<GroupResponse[]>([])
const loadingGroups = ref(false)
const selectedGroupId = ref<number | null>(null)

// 成员
const members = ref<GroupMemberResponse[]>([])
const loadingMembers = ref(false)

// 创建/编辑
const showEdit = ref(false)
const editMode = ref<'create'|'edit'>('create')
const form = ref({ name: '', description: '' })

// 单个操作输入
const singleUserId = ref<string>('')
const moveTargetGroupId = ref<number | null>(null)

// 批量输入 (以逗号/空白分隔)
const batchUserIdsText = ref('')
const batchRemoveIdsText = ref('')

const currentGroup = computed(()=> groups.value.find(g=>g.id===selectedGroupId.value) || null)

async function loadGroups(){
  if(!teacherId.value) return
  loadingGroups.value = true
  try {
    groups.value = await apiListGroups(teacherId.value)
    if(!selectedGroupId.value && groups.value.length) selectedGroupId.value = groups.value[0].id
  } finally { loadingGroups.value = false }
  if(selectedGroupId.value) loadMembers(selectedGroupId.value)
}

async function loadMembers(groupId: number){
  loadingMembers.value = true
  try { members.value = await apiListGroupMembers(groupId) } finally { loadingMembers.value = false }
}

function openCreate(){
  editMode.value = 'create'
  form.value = { name: '', description: '' }
  showEdit.value = true
}
function openEdit(g: GroupResponse){
  editMode.value = 'edit'
  form.value = { name: g.name, description: g.description || '' }
  selectedGroupId.value = g.id
  showEdit.value = true
}
async function submitForm(){
  if(!teacherId.value) return
  if(!form.value.name.trim()) { alert('名称必填'); return }
  if(editMode.value==='create'){
    const g = await apiCreateGroup(teacherId.value, { ...form.value })
    groups.value.push(g)
    selectedGroupId.value = g.id
  } else if(selectedGroupId.value){
    const g = await apiUpdateGroup(selectedGroupId.value, { ...form.value })
    const idx = groups.value.findIndex(x=>x.id===g.id)
    if(idx>=0) groups.value[idx] = g
  }
  showEdit.value = false
}
async function onDelete(g: GroupResponse){
  if(!confirm(`确认删除分组 ${g.name}?`)) return
  await apiDeleteGroup(g.id)
  groups.value = groups.value.filter(x=>x.id!==g.id)
  if(selectedGroupId.value===g.id) {
    selectedGroupId.value = groups.value[0]?.id || null
    if(selectedGroupId.value) loadMembers(selectedGroupId.value)
    else members.value = []
  }
}

async function onSelectGroup(id: number){
  selectedGroupId.value = id
  await loadMembers(id)
}

// 单个成员增删
async function addSingle(){
  if(!selectedGroupId.value) return
  const id = Number(singleUserId.value)
  if(!id){ alert('请输入有效用户ID'); return }
  try { await apiAddStudentToGroup(selectedGroupId.value, id); await loadMembers(selectedGroupId.value); singleUserId.value='' } catch(e:any){ alert(e.message) }
}
async function removeSingle(userId: number){
  if(!selectedGroupId.value) return
  if(!confirm('移出该学生?')) return
  await apiRemoveStudentFromGroup(selectedGroupId.value, userId); await loadMembers(selectedGroupId.value)
}
async function moveSingle(userId: number){
  if(!selectedGroupId.value || !moveTargetGroupId.value) { alert('请选择目标分组'); return }
  await apiMoveStudent(selectedGroupId.value, moveTargetGroupId.value, userId)
  await loadMembers(selectedGroupId.value)
}

// 批量
function parseIds(text: string): number[] {
  return text.split(/[\s,，;；]+/).map(s=>s.trim()).filter(Boolean).map(Number).filter(n=>!isNaN(n))
}
async function batchAdd(){
  if(!selectedGroupId.value) return
  const ids = parseIds(batchUserIdsText.value)
  if(!ids.length){ alert('请输入用户ID'); return }
  const res = await apiBatchAdd(selectedGroupId.value, ids)
  let msg = `成功: ${res.successIds.length}`
  if(res.alreadyInGroupIds.length) msg += `\n已存在: ${res.alreadyInGroupIds.join(',')}`
  if(res.notFoundIds.length) msg += `\n不存在/非法: ${res.notFoundIds.join(',')}`
  alert(msg)
  await loadMembers(selectedGroupId.value)
  batchUserIdsText.value=''
}
async function batchRemove(){
  if(!selectedGroupId.value) return
  const ids = parseIds(batchRemoveIdsText.value)
  if(!ids.length){ alert('请输入用户ID'); return }
  const res = await apiBatchRemove(selectedGroupId.value, ids)
  let msg = `移除: ${res.removedIds.length}`
  if(res.notInGroupIds.length) msg += `\n不在分组: ${res.notInGroupIds.join(',')}`
  alert(msg)
  await loadMembers(selectedGroupId.value)
  batchRemoveIdsText.value=''
}

loadGroups()
</script>

<style scoped>
.group-panel { position:relative; display:flex; flex-direction:column; gap:18px; }
.top-bar { display:flex; align-items:center; justify-content:space-between; }
.btn { background:#6366f1; color:#fff; border:none; padding:6px 14px; border-radius:6px; cursor:pointer; font-size:13px; }
.btn.primary { background:#4f46e5; }
.btn.danger { background:#dc2626; }
.btn.ghost { background:#e2e8f0; color:#334155; }
.btn:disabled { opacity:.5; cursor:default; }
.groups { display:grid; grid-template-columns:repeat(auto-fill,minmax(180px,1fr)); gap:12px; }
.group-item { background:#fff; border:1px solid #e2e8f0; border-radius:12px; padding:10px 12px 12px; cursor:pointer; position:relative; display:flex; flex-direction:column; gap:4px; }
.group-item.active { border-color:#6366f1; box-shadow:0 0 0 2px rgba(99,102,241,.25); }
.group-item .name { font-weight:600; font-size:14px; color:#334155; }
.group-item .meta { font-size:12px; color:#64748b; }
.group-item .actions { margin-top:4px; display:flex; gap:6px; }
.mini { background:#64748b; color:#fff; border:none; padding:3px 8px; border-radius:5px; font-size:12px; cursor:pointer; }
.mini.danger { background:#dc2626; }
.mini + .mini { margin-left:4px; }
.members-area { background:#fff; border:1px solid #e2e8f0; border-radius:14px; padding:18px 20px 22px; display:flex; flex-direction:column; gap:14px; }
.sub-title { margin:0; font-size:16px; font-weight:600; color:#334155; }
.member-ops { display:flex; gap:10px; flex-wrap:wrap; }
.member-ops input, .member-ops select { padding:6px 8px; border:1px solid #cbd5e1; border-radius:6px; font-size:13px; }
.member-table { width:100%; border-collapse:collapse; font-size:13px; }
.member-table th, .member-table td { border-bottom:1px solid #e2e8f0; padding:6px 8px; text-align:left; }
.member-table th { background:#f1f5f9; font-weight:500; color:#475569; }
.empty { text-align:center; color:#64748b; font-size:12px; padding:10px 0; }
.batch-box { display:flex; gap:30px; flex-wrap:wrap; }
.batch-section { flex:1 1 280px; display:flex; flex-direction:column; gap:8px; }
.batch-section textarea { min-height:70px; resize:vertical; padding:8px; border:1px solid #cbd5e1; border-radius:8px; font-size:12px; font-family:inherit; }
.loading { font-size:13px; color:#64748b; }
.modal-mask { position:fixed; inset:0; background:rgba(15,23,42,.45); display:flex; align-items:center; justify-content:center; z-index:1000; }
.modal { background:#fff; width:400px; max-width:92%; border-radius:14px; padding:20px 22px 24px; display:flex; flex-direction:column; gap:14px; }
.form-row { display:flex; flex-direction:column; gap:6px; }
.form-row label { font-size:12px; color:#475569; }
.form-row input, .form-row textarea { padding:8px 10px; border:1px solid #cbd5e1; border-radius:8px; font-size:14px; font-family:inherit; }
.modal-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:6px; }
.move-label { font-size:13px; color:#475569; display:flex; align-items:center; }
.move-target { margin-left:auto; display:flex; align-items:center; gap:6px; }
.actions-inline { display:flex; align-items:center; gap:10px; }
</style>
