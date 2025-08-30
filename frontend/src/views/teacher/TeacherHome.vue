<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import { apiGetTeacherProfile, apiUpdateTeacherProfile, apiCreateTeacherExperiment, apiGetTeacherExperiment, apiUpdateTeacherExperiment, apiListTeacherExperiments, apiDeleteTeacherExperiment, apiChangeTeacherExperimentStatus, type TeacherProfileResponse, type UpdateTeacherProfileRequest, type ExperimentCreateRequest, type ExperimentStageCreateRequest, type ExperimentResourceCreateRequest, type StageType, type ResourceType, type ExperimentResponse, type ExperimentUpdateRequest, type ExperimentListItem, type ExperimentListQuery, type PageResult } from '@/api/teacher'
import { useRouter, useRoute } from 'vue-router'
import ExperimentStagesForm from './components/ExperimentStagesForm.vue'
import ExperimentResourcesForm from './components/ExperimentResourcesForm.vue'

// 栏目定义：欢迎 / 个人信息 / 创建实验 / 修改实验 / 实验列表
 type SectionKey = 'welcome' | 'profile' | 'create' | 'edit' | 'list'
// 默认进入实验列表并自动加载
const active = ref<SectionKey>('list')
const collapsed = ref(false)
const menus = [
  { key: 'profile', label: '个人信息', icon: '👤' },
  { key: 'create', label: '创建实验', icon: '🧪' },
  { key: 'edit', label: '修改实验', icon: '✏️' },
  { key: 'list', label: '实验列表', icon: '📂' },
]

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

// 显示名基于 auth.user
const displayName = computed(() => auth.user?.realName || auth.user?.username || '教师')

// ===== 教师档案 =====
const loading = ref(false)
const error = ref('')
const teacherProfile = ref<TeacherProfileResponse | null>(null)

async function loadProfile(){
  if(!auth.user?.id) return
  loading.value = true
  error.value = ''
  try {
    teacherProfile.value = await apiGetTeacherProfile(Number(auth.user.id))
  } catch(e:any){
    error.value = e.message || '加载失败'
  } finally { loading.value = false }
}
onMounted(()=>{
  loadProfile()
  // 若当前默认是实验列表则自动加载一次（watch 不会对初始值触发）
  if(active.value==='list') {
    loadList()
  }
})

// 个人信息编辑
interface LocalEditForm extends UpdateTeacherProfileRequest {}
const editing = ref(false)
const saving = ref(false)
const form = ref<LocalEditForm>({})
function startEdit(){
  form.value = {
    title: teacherProfile.value?.title,
    department: teacherProfile.value?.department,
  }
  editing.value = true
}
function cancel(){ editing.value = false }
async function save(){
  if(!auth.user?.id) return
  saving.value = true
  try {
    const updated = await apiUpdateTeacherProfile(Number(auth.user.id), { title: form.value.title, department: form.value.department })
    teacherProfile.value = { ...(teacherProfile.value||{}), ...updated }
    editing.value = false
    alert('已保存')
  } catch(e:any){
    alert(e.message || '保存失败')
  } finally { saving.value = false }
}

// ===== 创建实验表单 =====
const stageTypes: StageType[] = ['CHOICE','FILL','CODE','TEXT']
const resourceTypes: ResourceType[] = ['FILE','LINK','VIDEO','IMAGE','DOC']
interface StageDraft extends Omit<ExperimentStageCreateRequest,'type'|'content'> { type: StageType; content: string }
interface ResourceDraft extends ExperimentResourceCreateRequest {}
interface ExperimentCreateForm extends Omit<ExperimentCreateRequest,'stages'|'resources'> { stages: StageDraft[]; resources: ResourceDraft[] }
const creating = ref(false)
const createSaving = ref(false)
const createError = ref('')
const expForm = ref<ExperimentCreateForm>({ title: '', description: '', stages: [{ title: '阶段1', type: 'CHOICE', content: '', description: '' }], resources: [] })
function addStage(){ expForm.value.stages.push({ title: '新阶段', type:'CHOICE', content:'', description:'' }) }
function removeStage(i:number){ expForm.value.stages.splice(i,1) }
function addResource(){ expForm.value.resources.push({ name: '新资源', type: 'FILE' }) }
function removeResource(i:number){ expForm.value.resources.splice(i,1) }
async function submitCreate(){
  if(!auth.user?.id) return
  if(!expForm.value.title.trim()) return alert('实验标题不能为空')
  if(!expForm.value.stages.length) return alert('至少需要一个阶段')
  for(const s of expForm.value.stages){
    if(!s.title?.trim()) return alert('阶段标题不能为空')
    if(!s.type) return alert('阶段类型不能为空')
    if(!s.content?.trim()) return alert('阶段内容不能为空')
  }
  for(const r of expForm.value.resources){
    if(!r.name?.trim()) return alert('资源名称不能为空')
    if(!r.type) return alert('资源类型不能为空')
    if(r.size!=null && r.size < 0) return alert('资源大小不能为负')
  }
  createSaving.value = true
  createError.value = ''
  try {
    const payload: ExperimentCreateRequest = {
      title: expForm.value.title,
      description: expForm.value.description,
      // objective 创建请求后端暂不支持
      stages: expForm.value.stages.map((s,i)=>({
        title: s.title,
        description: s.description,
        type: s.type,
        content: s.content,
        hint: s.hint,
        maxScore: s.maxScore,
        sequence: i+1,
        evaluation: s.evaluation,
      })),
      resources: expForm.value.resources.map(r=>({ name: r.name, url: r.url, type: r.type, size: r.size }))
    }
    const resp = await apiCreateTeacherExperiment(Number(auth.user.id), payload)
    alert('创建成功，实验ID: '+resp.experimentId)
    // 创建后跳转到教师预览界面
    if(resp.experimentId){
      router.push({ name: 'teacher-experiment-detail', params: { id: resp.experimentId } }).catch(()=>{})
    }
    expForm.value = { title: '', description: '', stages: [{ title: '阶段1', type:'CHOICE', content:'', description:'' }], resources: [] }
  } catch(e:any){
    createError.value = e.message || '创建失败'
  } finally { createSaving.value = false }
}

// ===== 修改实验（通过输入实验ID加载 & 编辑） =====
const editExpIdInput = ref<string>('')
const editLoading = ref(false)
const editError = ref('')
const editLoaded = ref(false)
const editOriginal = ref<ExperimentResponse | null>(null)
interface StageEditDraft extends StageDraft { id?: number }
interface ResourceEditDraft extends ResourceDraft { id?: number }
interface ExperimentEditForm { id?: number; title: string; description?: string; stages: StageEditDraft[]; resources: ResourceEditDraft[] }
const editForm = ref<ExperimentEditForm>({ title: '', description: '', stages: [], resources: [] })

function editAddStage(){ editForm.value.stages.push({ title: '新阶段', type: 'CHOICE', content:'', description:'' }) }
function editRemoveStage(i:number){ editForm.value.stages.splice(i,1) }
function editAddResource(){ editForm.value.resources.push({ name: '新资源', type: 'FILE' }) }
function editRemoveResource(i:number){ editForm.value.resources.splice(i,1) }

async function loadEditExperiment(idNumber?: number){
  const id = idNumber ?? Number(editExpIdInput.value)
  if(!id) { alert('请输入有效实验ID'); return }
  if(!auth.user?.id) return
  editLoading.value = true
  editError.value = ''
  try {
    const detail = await apiGetTeacherExperiment(Number(auth.user.id), id)
    editOriginal.value = detail
    const rawStages:any[] = Array.isArray((detail as any).stages) ? (detail as any).stages : []
    const stageDrafts: StageEditDraft[] = rawStages.map(s=>({
      id: s.id ?? s.stageId,
      title: s.title ?? s.name ?? `阶段${s.orderNo || s.sequence || ''}`,
      description: s.description,
      type: s.type ?? s.stageType ?? 'TEXT',
      content: s.content ?? '',
      hint: s.hint,
      maxScore: s.maxScore,
      // sequence 在保存时重新计算，不强制存入
    }))
    const resourceDrafts: ResourceEditDraft[] = ((detail as any).resources||[]).map((r:any)=>({
      id: r.id,
      name: r.name,
      url: r.url,
      type: r.type,
      size: r.size,
    }))
    editForm.value = {
      id: detail.id,
      title: detail.title,
      description: detail.description,
      stages: stageDrafts,
      resources: resourceDrafts
    }
    editLoaded.value = true
  } catch(e:any){
    editError.value = e.message || '加载失败'
    editLoaded.value = false
  } finally { editLoading.value = false }
}

async function submitUpdate(){
  if(!auth.user?.id) return
  if(!editForm.value.id) { alert('未加载实验'); return }
  if(!editForm.value.title.trim()) return alert('标题不能为空')
  if(!editForm.value.stages.length) return alert('至少一个阶段')
  // 校验阶段必填
  for(const s of editForm.value.stages){
    if(!s.title?.trim()) return alert('阶段标题不能为空')
    if(!s.type) return alert('阶段类型不能为空')
    if(!s.content?.trim()) return alert('阶段内容不能为空')
  }
  // 资源校验（若存在）
  for(const r of editForm.value.resources){
    if(!r.name?.trim()) return alert('资源名称不能为空')
    if(!r.type) return alert('资源类型不能为空')
    if(r.size!=null && r.size < 0) return alert('资源大小不能为负')
  }
  editLoading.value = true
  editError.value = ''
  try {
    const payload: ExperimentUpdateRequest = {
      title: editForm.value.title,
      description: editForm.value.description,
      objective: (editForm.value as any).objective,
      stages: editForm.value.stages.map((s,i)=>({ id: s.id, title: s.title, description: s.description, type: s.type, content: s.content, hint: s.hint, maxScore: s.maxScore, sequence: i+1 })),
      resources: editForm.value.resources.map(r=>({ id: r.id, name: r.name, url: r.url, type: r.type, size: r.size }))
    }
    await apiUpdateTeacherExperiment(Number(auth.user.id), editForm.value.id, payload)
    alert('更新成功')
    // 可选择刷新原始数据
    await loadEditExperiment(editForm.value.id)
  } catch(e:any){
    editError.value = e.message || '更新失败'
  } finally { editLoading.value = false }
}

// 若通过 ?editId= 直接进入编辑
watch(()=>route.query.editId, (v)=>{
  if(v){
    active.value = 'edit'
    editExpIdInput.value = String(v)
    loadEditExperiment(Number(v)).then(()=>{
      const es = route.query.editStage ? Number(route.query.editStage) : undefined
      if(es && es>0){
        // 定位到阶段（简单滚动）
        requestAnimationFrame(()=>{
          const container = document.querySelector('.stages-draft')
          if(container){
            const item = (container as HTMLElement).children[es-1] as HTMLElement
            if(item && item.scrollIntoView){ item.scrollIntoView({ behavior:'smooth', block:'center' }) }
          }
        })
      }
    })
  }
})

// ===== 实验列表（分页显示与操作） =====
const listQuery = ref<ExperimentListQuery>({ page:1, size:10, status: undefined, keyword: '' })
const listLoading = ref(false)
const listError = ref('')
const pageData = ref<PageResult<ExperimentListItem> | null>(null)

async function loadList(){
  if(!auth.user?.id) return
  listLoading.value = true
  listError.value = ''
  try {
    pageData.value = await apiListTeacherExperiments(Number(auth.user.id), listQuery.value)
  } catch(e:any){
    listError.value = e.message || '加载失败'
  } finally { listLoading.value = false }
}
function searchList(){ listQuery.value.page = 1; loadList() }
function resetList(){ listQuery.value = { page:1, size:10 }; loadList() }
function pageChange(p:number){ if(p<1) return; listQuery.value.page = p; loadList() }
async function deleteExperiment(id:number){
  if(!auth.user?.id) return
  if(!confirm('确认删除实验 #'+id+' ?')) return
  try { await apiDeleteTeacherExperiment(Number(auth.user.id), id); alert('已删除'); loadList() } catch(e:any){ alert(e.message || '删除失败') }
}
async function toggleStatus(exp: ExperimentListItem){
  if(!auth.user?.id) return
  const target = exp.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
  try { await apiChangeTeacherExperimentStatus(Number(auth.user.id), exp.id, target); exp.status = target } catch(e:any){ alert(e.message || '状态更新失败') }
}
function goPreview(id:number){
  // 直接路由跳转全屏预览
  router.push({ name:'teacher-experiment-detail', params:{ id } }).catch(()=>{})
}

// ====== 监视器：控制不同状态下的加载 ======
watch(()=>active.value, v=>{ if(v==='list') loadList() })
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
      <h1 class="page-title">
        {{ active==='welcome' ? '欢迎' : active==='profile' ? '个人信息' : active==='create' ? '创建实验' : active==='edit' ? '修改实验' : '实验列表' }}
      </h1>

      <!-- 欢迎页 -->
      <section v-if="active==='welcome'" class="panel fade-in welcome-panel">
        <h2 class="welcome-title">欢迎，{{ displayName }} 老师</h2>
        <p class="welcome-tip">此处为教师端首页。后续将加入：实验创建、发布、批改、公告管理等功能。</p>
        <p class="welcome-sub">可从侧边栏进入创建实验或个人信息。</p>
        <div class="welcome-actions">
          <button class="primary" @click="active='create'">创建实验</button>
          <button class="outline small" @click="active='profile'">个人信息</button>
          <button class="outline small" @click="active='edit'">修改实验</button>
          <button class="outline small" @click="active='list'">实验列表</button>
        </div>
      </section>

      <!-- 个人信息 -->
      <section v-else-if="active==='profile'" class="panel fade-in">
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
            <li><span class="pl-label">用户名</span><span class="pl-value">{{ teacherProfile?.username || auth.user?.username }}</span></li>
            <li><span class="pl-label">职称</span><span class="pl-value">{{ teacherProfile?.title || '-' }}</span></li>
            <li><span class="pl-label">部门</span><span class="pl-value">{{ teacherProfile?.department || '-' }}</span></li>
          </ul>
        </div>
        <div v-else class="profile-box">
          <form class="profile-form" @submit.prevent="save">
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
        <div class="tips">后续可加入：研究方向、联系方式等。</div>
      </section>

      <!-- 创建实验 -->
      <section v-else-if="active==='create'" class="panel fade-in">
        <div class="panel-head">
          <h3>创建实验</h3>
          <div class="actions">
            <button class="primary small" :disabled="createSaving" @click="submitCreate">{{ createSaving ? '提交中...' : '提交创建' }}</button>
          </div>
        </div>
        <div v-if="createError" class="error-line">{{ createError }}</div>
        <form class="create-form" @submit.prevent="submitCreate">
          <div class="form-row">
            <label>标题 <span class="req">*</span></label>
            <input v-model="expForm.title" placeholder="请输入实验标题" />
          </div>
          <div class="form-row">
            <label>简介</label>
            <textarea v-model="expForm.description" rows="3" placeholder="实验简介 / 说明"></textarea>
          </div>
          <!-- objective 创建时不再显示，因为后端创建接口无该字段 -->
          <div class="block-head">
            <h4>阶段</h4>
            <button type="button" class="text-btn" @click="addStage">新增阶段</button>
          </div>
          <div v-if="!expForm.stages.length" class="empty">暂无阶段</div>
          <!-- 阶段 -->
          <ExperimentStagesForm
            v-model:stages="expForm.stages"
            :editable="true"
            @add="addStage"
            @remove="removeStage"
          />
          <!-- 资源 -->
          <ExperimentResourcesForm
            v-model:resources="expForm.resources"
            :editable="true"
            @add="addResource"
            @remove="removeResource"
          />
        </form>
        <div class="tips">提交后自动跳转到实验详情（已实现）。</div>
      </section>

      <!-- 修改实验 -->
      <section v-else-if="active==='edit'" class="panel fade-in">
        <div class="panel-head">
          <h3>修改实验</h3>
          <div class="actions" v-if="editLoaded">
            <button class="primary small" :disabled="editLoading" @click="submitUpdate">{{ editLoading ? '保存中...' : '保存修改' }}</button>
          </div>
        </div>
        <div class="edit-search-row">
          <input class="id-input" v-model="editExpIdInput" placeholder="输入实验ID" />
          <button class="outline small" :disabled="editLoading" @click="loadEditExperiment()">{{ editLoading ? '加载中...' : '加载实验' }}</button>
          <span class="error-line" v-if="editError">{{ editError }}</span>
        </div>
        <div v-if="editLoaded" class="tips" style="margin-top:4px;">已加载实验 ID: {{ editForm.id }}（修改后点击上方保存修改）</div>
        <div v-if="editLoaded" class="divider"></div>
        <div v-if="editLoaded">
          <form class="create-form" @submit.prevent="submitUpdate">
            <div class="form-row">
              <label>标题 <span class="req">*</span></label>
              <input v-model="editForm.title" />
            </div>
            <div class="form-row">
              <label>简介</label>
              <textarea v-model="editForm.description" rows="3"></textarea>
            </div>
            <div class="form-row">
              <label>目标 (objective)</label>
              <input v-model="(editForm as any).objective" placeholder="实验目标 / 学习成果" />
            </div>
            <div class="block-head">
              <h4>阶段</h4>
              <button type="button" class="text-btn" @click="editAddStage">新增阶段</button>
            </div>
            <div v-if="!editForm.stages.length" class="empty">暂无阶段</div>
            <!-- 编辑阶段/资源表单替换 -->
            <ExperimentStagesForm
              v-if="editLoaded"
              v-model:stages="editForm.stages"
              :editable="true"
              title="阶段（编辑）"
              @add="editAddStage"
              @remove="editRemoveStage"
            />
            <ExperimentResourcesForm
              v-if="editLoaded"
              v-model:resources="editForm.resources"
              :editable="true"
              title="资源（编辑）"
              @add="editAddResource"
              @remove="editRemoveResource"
            />
          </form>
          <div class="tips">删除的阶段/资源将不会出现在提交数组中（需确认后端是否按缺失即删除处理）。</div>
        </div>
      </section>

      <!-- 实验列表 -->
      <section v-else class="panel fade-in">
        <div class="panel-head">
          <h3>实验列表</h3>
          <div class="actions">
            <button class="outline small" @click="loadList" :disabled="listLoading">刷新</button>
          </div>
        </div>
        <div class="list-filters">
          <input v-model="listQuery.keyword" placeholder="搜索标题关键字" @keyup.enter="searchList" />
          <select v-model="listQuery.status" @change="searchList">
            <option value="">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="PUBLISHED">已发布</option>
          </select>
          <button class="primary small" @click="searchList" :disabled="listLoading">搜索</button>
          <button class="outline small" @click="resetList" :disabled="listLoading">重置</button>
        </div>
        <div v-if="listLoading" class="loading-line">加载中...</div>
        <div v-else-if="listError" class="error-line">{{ listError }}</div>
        <div v-else>
          <table v-if="pageData?.content?.length" class="exp-table">
            <thead>
              <tr><th>ID</th><th>标题</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="exp in pageData!.content" :key="exp.id">
                <td>{{ exp.id }}</td>
                <td class="title">{{ exp.title }}</td>
                <td>
                  <span class="status" :class="exp.status && exp.status.toLowerCase()">{{ exp.status || '-' }}</span>
                </td>
                <td class="ops">
                  <button class="link-btn" @click="goPreview(exp.id)">预览</button>
                  <button class="link-btn" @click="goEdit(exp.id)">编辑</button>
                  <button class="link-btn" @click="toggleStatus(exp)">{{ exp.status==='PUBLISHED' ? '撤回' : '发布' }}</button>
                  <button class="link-btn danger" @click="deleteExperiment(exp.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty">暂无实验</div>
          <div class="pagination" v-if="pageData && pageData.total > pageData.size">
            <button :disabled="listLoading || listQuery.page===1" @click="pageChange((listQuery.page||1)-1)">上一页</button>
            <span class="page-info">第 {{ listQuery.page }} / {{ Math.ceil(pageData.total / (pageData.size||10)) }} 页，共 {{ pageData.total }} 条</span>
            <button :disabled="listLoading || (listQuery.page||1) >= Math.ceil(pageData.total / (pageData.size||10))" @click="pageChange((listQuery.page||1)+1)">下一页</button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
/* 继承 + 扩展样式 */
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
.panel-head { display:flex; align-items:center; justify-content:space-between; gap:16px; margin-bottom:10px; }
.profile-box { background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; padding:14px 18px 6px; }
.profile-list { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:10px; }
.profile-list li { display:flex; flex-wrap:wrap; font-size:14px; }
.pl-label { width:80px; color:#64748b; font-size:12px; letter-spacing:.5px; font-weight:600; }
.pl-value { flex:1; color:#1e293b; }
.profile-form { display:flex; flex-direction:column; gap:14px; }
.form-row { display:flex; flex-direction:column; gap:6px; }
.form-row label { font-size:12px; font-weight:600; color:#64748b; letter-spacing:.5px; }
.form-row input, .create-form input, .create-form textarea, .stage-draft-item select, .res-draft-item select { height:38px; border:1px solid #cbd5e1; border-radius:8px; padding:0 12px; font-size:14px; outline:none; background:#fff; transition:.2s; }
.create-form textarea { height:auto; padding:10px 12px; resize:vertical; }
.form-row input:focus, .create-form input:focus, .create-form textarea:focus, .stage-draft-item select:focus, .res-draft-item select:focus { border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,.2); }
.text-btn { background:transparent; border:none; color:#6366f1; cursor:pointer; font-size:14px; font-weight:600; padding:6px 10px; border-radius:6px; }
.text-btn:hover { background:rgba(99,102,241,.1); }
button.primary, button.primary.small { background:#4f46e5; color:#fff; border:none; border-radius:10px; padding:10px 18px; font-size:14px; cursor:pointer; font-weight:600; letter-spacing:.5px; transition:.25s; }
button.primary.small { padding:8px 14px; font-size:13px; }
button.primary:hover { background:#4338ca; box-shadow:0 4px 12px -4px rgba(79,70,229,.5); }
button.outline.small { background:#fff; color:#475569; border:1px solid #cbd5e1; border-radius:10px; padding:8px 14px; font-size:13px; cursor:pointer; font-weight:600; letter-spacing:.5px; transition:.25s; }
button.outline.small:hover { background:#f1f5f9; }
.welcome-panel { text-align:center; padding:60px 40px; display:flex; flex-direction:column; gap:18px; }
.welcome-title { margin:0; font-size:28px; font-weight:600; background:linear-gradient(90deg,#6366f1,#4f46e5); background-clip:text; -webkit-background-clip:text; color:transparent; }
.welcome-tip { margin:0; font-size:15px; color:#475569; }
.welcome-sub { margin:0; font-size:13px; color:#64748b; }
.welcome-actions { display:flex; gap:14px; justify-content:center; margin-top:10px; flex-wrap:wrap; }
.tips { margin-top:18px; font-size:12px; color:#64748b; }
.loading-line { padding:6px 4px; font-size:13px; color:#64748b; }
.error-line { padding:6px 4px; font-size:13px; color:#dc2626; }
/* 创建 / 编辑 */
.create-form { display:flex; flex-direction:column; gap:18px; }
.block-head { display:flex; align-items:center; justify-content:space-between; margin-top:6px; }
.block-head h4 { margin:0; font-size:14px; font-weight:600; color:#1e293b; }
.stages-draft, .resources-draft { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:16px; }
.stage-draft-item, .res-draft-item { background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:14px 16px; display:flex; flex-direction:column; gap:10px; }
.stage-draft-item input, .res-draft-item input { height:36px; }
.stage-draft-item textarea { border:1px solid #cbd5e1; border-radius:8px; padding:8px 10px; resize:vertical; font-size:13px; line-height:1.5; outline:none; background:#fff; }
.stage-draft-item textarea:focus { border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.sd-top { display:flex; align-items:center; justify-content:space-between; }
.mini-btn { background:#e2e8f0; border:none; color:#475569; width:26px; height:26px; border-radius:8px; cursor:pointer; font-size:14px; font-weight:600; display:flex; align-items:center; justify-content:center; }
.mini-btn:hover { background:#cbd5e1; }
.empty { padding:10px 2px; font-size:13px; color:#64748b; }
.two-cols { display:flex; gap:12px; }
.two-cols .col { flex:1; display:flex; flex-direction:column; gap:4px; }
.mini-label { font-size:11px; font-weight:600; color:#475569; letter-spacing:.5px; }
.stage-draft-item select, .res-draft-item select { height:36px; border:1px solid #cbd5e1; border-radius:8px; padding:0 10px; background:#fff; }
.stage-draft-item select:focus, .res-draft-item select:focus { border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,.15); outline:none; }
.codearea { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace; }
.id-tag { font-size:11px; color:#6366f1; font-weight:500; margin-left:4px; }
.edit-search-row { display:flex; align-items:center; gap:10px; }
.edit-search-row .id-input { flex:0 0 160px; height:38px; border:1px solid #cbd5e1; border-radius:8px; padding:0 12px; }
.divider { height:1px; background:#e2e8f0; margin:20px 0 10px; }
/* 列表 */
.list-filters { display:flex; flex-wrap:wrap; gap:10px; margin-bottom:12px; }
.list-filters input { height:38px; border:1px solid #cbd5e1; border-radius:8px; padding:0 12px; }
.list-filters select { height:38px; border:1px solid #cbd5e1; border-radius:8px; padding:0 10px; }
.exp-table { width:100%; border-collapse:collapse; font-size:13px; }
.exp-table th, .exp-table td { padding:10px 10px; border-bottom:1px solid #e2e8f0; text-align:left; }
.exp-table th { background:#f8fafc; font-size:12px; font-weight:600; letter-spacing:.5px; color:#475569; }
.exp-table td.title { font-weight:600; color:#1e293b; }
.status { padding:2px 8px; border-radius:12px; font-size:12px; font-weight:600; background:#e2e8f0; color:#475569; }
.status.published { background:#4ade80; color:#065f46; }
.status.draft { background:#fcd34d; color:#92400e; }
.ops { display:flex; gap:6px; }
.link-btn { background:transparent; border:none; color:#6366f1; cursor:pointer; font-size:12px; font-weight:600; padding:4px 6px; border-radius:6px; }
.link-btn:hover { background:rgba(99,102,241,.12); }
.link-btn.danger { color:#dc2626; }
.link-btn.danger:hover { background:rgba(220,38,38,.12); }
.pagination { margin-top:14px; display:flex; align-items:center; gap:16px; font-size:13px; }
.pagination button { background:#fff; border:1px solid #cbd5e1; border-radius:8px; padding:6px 12px; cursor:pointer; font-size:12px; font-weight:600; }
.pagination button:disabled { opacity:.5; cursor:not-allowed; }
.page-info { color:#475569; }
@media (max-width: 900px){
  .sidebar { position:fixed; left:0; top:0; bottom:0; z-index:40; box-shadow:4px 0 14px -6px rgba(0,0,0,.35); }
  .content-area { padding:24px 20px 34px; }
}
@media (max-width: 640px){
  .sidebar.collapsed { width:54px; }
  .page-title { font-size:20px; margin-bottom:18px; }
  .welcome-panel { padding:42px 24px; }
  .welcome-title { font-size:22px; }
  .welcome-tip { font-size:14px; }
  .ops { flex-wrap:wrap; }
}
</style>
