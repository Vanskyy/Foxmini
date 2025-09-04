<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ref, onMounted, onBeforeUnmount, nextTick, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import { apiCreateExperiment, apiUpdateExperiment, apiDeleteExperiment, apiGetExperiment, type ExperimentCreateRequest, type ExperimentStageCreateRequest, type ExperimentResourceCreateRequest, type ExperimentResponse } from '@/api/teacher'

const router = useRouter()
const route = useRoute()
const experimentId = ref<number | null>(null)
const baseTitle = ref('')
const baseDescription = ref('')
// 从路由 query 预填（新建后跳转传入）
onMounted(()=>{
  const idQ = route.query.id
  if(idQ){ const n = Number(idQ); if(!Number.isNaN(n)) experimentId.value = n }
  baseTitle.value = (route.query.title as string)||''
  baseDescription.value = (route.query.description as string)||''
  if(route.query.title && route.query.description) isNewlyCreated.value = true
})

const saving = ref(false)
const saveMessage = ref('')
const deleting = ref(false)
const auth = useAuthStore()

function goBack(){
  // 编辑模式：若不是“刚创建”并且有未保存修改则提示；不直接删除
  if(experimentId.value && !isNewlyCreated.value){
    if(isDirty() && !window.confirm('有未保存的更改，确认离开？')) return
    router.push('/teacher')
    return
  }
  // 新建模式（含刚创建未编辑完）
  if(!window.confirm(experimentId.value ? '确认放弃并删除该实验？此操作不可恢复。' : '你确定要放弃创建这个实验吗？未保存的内容将会丢失。')) return
  if(experimentId.value && auth.user?.id){
    deleting.value = true
    apiDeleteExperiment(Number(auth.user.id), experimentId.value)
      .catch(err=> console.warn('删除实验失败(已忽略):', err))
      .finally(()=>{ deleting.value=false; router.push('/teacher') })
  } else { router.push('/teacher') }
}
const sidebarCollapsed = ref(false)
function toggleSidebar(){ sidebarCollapsed.value = !sidebarCollapsed.value }
function handleResizeSidebar(){ if(window.innerWidth < 1100 && sidebarCollapsed.value){ sidebarCollapsed.value = false } }
// 阶段不再包含资源；资源改为实验级
interface Stage { title: string; intro: string; content: string; answer: string; hint?: string; type: 'CHOICE' | 'FILL' | 'CODE' | 'TEXT'; score: number; }
interface ExpResource { name: string; type: 'DOCUMENT' | 'VIDEO' | 'CODE' | 'OTHER'; url: string }
const stages = ref<Stage[]>([{ title:'', intro:'', content:'', answer:'', hint:'', type:'TEXT', score:0 }])
const experimentResources = ref<ExpResource[]>([]) // 实验级资源集合（含名称/类型/链接）
const showResDialog = ref(false)
const resourceForm = ref<ExpResource>({ name:'', type:'DOCUMENT', url:'' })
function openResourceDialog(){
  if(experimentResources.value.length >= 5){ window.alert('最多添加 5 个资源'); return }
  resourceForm.value = { name:'', type:'DOCUMENT', url:'' }; editingResourceIndex.value = -1; showResDialog.value = true }
const editingResourceIndex = ref(-1)
function editResource(i:number){ const r = experimentResources.value[i]; resourceForm.value = { ...r }; editingResourceIndex.value = i; showResDialog.value = true }
function deleteResource(i:number){ if(!window.confirm('确认删除该资源？')) return; experimentResources.value.splice(i,1) }
function confirmResource(){ const { name, type, url } = resourceForm.value; if(!name.trim() || !url.trim()){ window.alert('名称与链接必填'); return } const data = { name: name.trim(), type, url: url.trim() }; if(editingResourceIndex.value>=0){ experimentResources.value.splice(editingResourceIndex.value,1,data) } else { experimentResources.value.push(data) } showResDialog.value = false }
function cancelResource(){ showResDialog.value = false }
function addResource(){ openResourceDialog() }
// 其他现有代码保持不变
const activeIndex = ref(0)
const currentStage = computed(()=> stages.value[activeIndex.value])
const introRef = ref<HTMLTextAreaElement | null>(null)
const contentRef = ref<HTMLTextAreaElement | null>(null)
function autoResize(el: HTMLTextAreaElement | null){ if(!el) return; el.style.height='auto'; el.style.height = el.scrollHeight + 'px' }
function setContentFullHeight(){ const el = contentRef.value; if(!el) return; const available = window.innerHeight - el.getBoundingClientRect().top - 32; if(available>0){ el.style.height = available + 'px' } }
function handleResize(){ handleResizeSidebar(); setContentFullHeight() }
function onIntroInput(){ autoResize(introRef.value) }
function onContentInput(){ }
function switchStage(i:number){ if(i===activeIndex.value) return; activeIndex.value = i; nextTick(()=>{ autoResize(introRef.value); setContentFullHeight() }) }
function addStage(){ if(stages.value.length >= 8){ window.alert('最多 8 个阶段'); return } stages.value.push({ title:'', intro:'', content:'', answer:'', hint:'', type:'TEXT', score:0 }); activeIndex.value = stages.value.length - 1; nextTick(()=>{ autoResize(introRef.value); setContentFullHeight() }) }
function deleteStage(i:number){
  if(stages.value.length===1){ window.alert('至少保留一个阶段'); return }
  if(!window.confirm(`确认删除阶段 ${i+1} 吗？`)) return
  stages.value.splice(i,1)
  if(activeIndex.value >= stages.value.length){ activeIndex.value = stages.value.length - 1 }
  nextTick(()=>{ autoResize(introRef.value); setContentFullHeight() })
}
/* function addResource(){
  const name = window.prompt('资源名称（显示给学生）:')?.trim();
  if(!name) return;
  let type = window.prompt('资源类型: 输入 1=DOCUMENT 2=VIDEO 3=CODE 4=OTHER', '1')?.trim();
  const map: Record<string, ExpResource['type']> = { '1':'DOCUMENT','2':'VIDEO','3':'CODE','4':'OTHER' };
  if(!type || !map[type]) type = '1';
  const url = window.prompt('资源链接 URL: (例如 https://...)')?.trim();
  if(!url){ return; }
  experimentResources.value.push({ name, type: map[type], url });
} */
onMounted(()=>{ window.addEventListener('resize', handleResize); nextTick(()=>{ autoResize(introRef.value); setContentFullHeight() }) })
onBeforeUnmount(()=> window.removeEventListener('resize', handleResize))

// 简介 & 标题编辑弹窗状态
const showDescDialog = ref(false)
const descDraft = ref('')
const titleDraft = ref('')
function openDescDialog(){ titleDraft.value = baseTitle.value; descDraft.value = baseDescription.value; showDescDialog.value = true }
function cancelDescDialog(){ showDescDialog.value = false }
function confirmDescDialog(){ baseTitle.value = titleDraft.value.trim(); baseDescription.value = descDraft.value.trim(); showDescDialog.value = false }

function mapStages(): ExperimentStageCreateRequest[]{
  return stages.value.map((s, idx)=>({
    title: s.title.trim() || `阶段${idx+1}`,
    description: s.intro?.trim() || '',
    type: s.type,
    content: s.content?.trim() || '',
    hint: s.hint?.trim() || undefined,
    maxScore: isFinite(Number(s.score)) ? Number(s.score) : 0,
    sequence: idx+1,
    evaluation: s.answer ? {
      correctOptions: s.type==='CHOICE'? (s.answer||'') : undefined,
      fillAnswers: s.type==='FILL'? (s.answer||'') : undefined,
      testCases: s.type==='CODE'? (s.answer||'') : undefined
    } : undefined
  }))
}
function mapResources(): ExperimentResourceCreateRequest[]{
  return experimentResources.value.map(r=> ({ name: r.name.trim(), type: r.type, url: r.url.trim() }))
}
function validateAll(): string[]{
  const errs: string[] = []
  if(!baseTitle.value.trim()) errs.push('实验标题不能为空')
  stages.value.forEach((s,i)=>{
    if(!s.title.trim()) errs.push(`阶段${i+1} 标题为空`)
    if(!s.content.trim()) errs.push(`阶段${i+1} 内容为空`)
    if(s.score<0) errs.push(`阶段${i+1} 分数不能为负`)
  })
  return errs
}
const loadingDetail = ref(false)
const loadError = ref('')
const hasLoadedDetail = ref(false)
const isNewlyCreated = ref(false) // 区分刚创建进入与真正编辑

async function loadDetail(){
  if(!experimentId.value || !auth.user?.id || hasLoadedDetail.value) return
  loadingDetail.value = true; loadError.value=''
  try {
    const resp: ExperimentResponse = await apiGetExperiment(Number(auth.user.id), experimentId.value)
    baseTitle.value = resp.title
    baseDescription.value = resp.description || ''
    const list = (resp.stages||[]).slice().sort((a,b)=> (a.sequence||0)-(b.sequence||0))
    stages.value = list.length? list.map(s=>({
      title: s.title||'',
      intro: s.description||'',
      content: s.content||'',
      answer: (s.type==='CHOICE'||(s as any).stageType==='CHOICE'? (s.evaluation?.correctOptions || s.correctAnswer) : (s.type==='FILL'||(s as any).stageType==='FILL')? (s.evaluation?.fillAnswers || s.correctAnswer) : (s.type==='CODE'||(s as any).stageType==='CODE')? (s.evaluation?.testCases || '') : '') || '',
      hint: s.hint||'',
      type: (s as any).type || (s as any).stageType, // 兼容后端字段名 stageType
      score: s.maxScore || 0
    })) : [{ title:'', intro:'', content:'', answer:'', hint:'', type:'TEXT', score:0 }]
    experimentResources.value = (resp.resources||[]).map(r=> ({ name:r.name, type:r.type, url:r.url }))
    await nextTick()
    autoResize(introRef.value); setContentFullHeight()
    hasLoadedDetail.value = true
    originalSnapshot.value = serializeAll()
  } catch(e:any){
    loadError.value = e.message||'加载失败'; alert('加载实验详情失败: '+ loadError.value); router.push('/teacher')
  } finally { loadingDetail.value = false }
}

const originalSnapshot = ref('')
function serializeAll(){
  return JSON.stringify({
    title: baseTitle.value.trim(),
    desc: baseDescription.value.trim(),
    stages: stages.value.map(s=>({t:s.title,i:s.intro,c:s.content,a:s.answer,h:s.hint,ty:s.type,sc:s.score})),
    resources: experimentResources.value.map(r=>({n:r.name,ty:r.type,u:r.url}))
  })
}
function isDirty(){ return serializeAll() !== originalSnapshot.value }

onMounted(()=>{ loadDetail() })
// 当路由变化可能切换 id
watch(()=> route.query.id, ()=>{ const idQ = route.query.id; if(idQ){ const n = Number(idQ); if(!Number.isNaN(n)) experimentId.value = n; hasLoadedDetail.value=false; loadDetail() } })

window.addEventListener('beforeunload', (e)=>{
  if(isDirty()) { e.preventDefault(); e.returnValue = '' }
})
onBeforeUnmount(()=>{ window.removeEventListener('beforeunload', ()=>{}) })
// 保存成功后刷新快照
function afterSave(){ originalSnapshot.value = serializeAll(); isNewlyCreated.value=false }

async function onFinish(){
  if(!auth.user?.id){ alert('未登录'); return }
  const errs = validateAll(); if(errs.length){ alert('请先修正:\n'+errs.join('\n')); return }
  const teacherId = Number(auth.user.id)
  const payload: ExperimentCreateRequest = {
    title: baseTitle.value.trim(),
    description: baseDescription.value.trim() || undefined,
    status: 'DRAFT',
    stages: mapStages(),
    resources: mapResources()
  }
  saving.value = true; saveMessage.value=''
  try {
    if(experimentId.value){
      await apiUpdateExperiment(teacherId, experimentId.value, {
        title: payload.title,
        description: payload.description,
        status: payload.status,
        stages: payload.stages,
        resources: payload.resources
      })
      saveMessage.value = '保存成功'
    } else {
      const res = await apiCreateExperiment(teacherId, payload)
      experimentId.value = (res as any).id || res.experimentId
      saveMessage.value = '创建成功'
      router.replace({ query: { ...route.query, id: String(experimentId.value) } })
      isNewlyCreated.value = true
    }
    afterSave()
    alert(saveMessage.value)
  } catch(e:any){ alert('保存失败: '+ (e.message||e)) } finally { saving.value=false }
}
// 确保 answerPlaceholder 已定义在此作用域供模板使用（若上方已定义则忽略）
const answerPlaceholder = computed(()=>
  currentStage.value.type === 'CODE' ? '// 请在此处输入代码并测试\nfunction test(){\n  // 测试代码\n}\ntest();' :
  currentStage.value.type === 'CHOICE' ? '选择题答案示例：A,B' :
  currentStage.value.type === 'FILL' ? '填空题答案示例' :
  ''
)
</script>

<template>
  <div class="create-layout">
    <aside class="stage-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sb-top">
        <div class="left-group" v-if="!sidebarCollapsed">
          <button class="icon-btn back" @click="goBack" :disabled="deleting || saving">{{ deleting? '删除中...' : '放弃' }}</button>
        </div>
        <div class="right-group">
          <button class="icon-btn add" @click="addStage" :disabled="stages.length>=8" v-if="!sidebarCollapsed">＋</button>
          <button class="collapse-btn" @click="toggleSidebar" :aria-label="sidebarCollapsed? '展开侧边栏' : '收起侧边栏'">
            <svg class="chevron" viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6" /></svg>
          </button>
        </div>
      </div>
      <!-- 基本信息展示（只显示标题，简介通过按钮弹窗编辑） -->
      <div v-if="!sidebarCollapsed" class="basic-info">
        <!-- 移除侧边栏标题区域的实验 ID 显示 -->
        <div class="exp-base-row">
          <div class="exp-base-title" :title="baseTitle">{{ baseTitle || '（未命名实验）' }}</div>
          <button type="button" class="edit-desc-icon-btn" @click="openDescDialog" aria-label="修改实验标题与简介" title="修改实验标题与简介">
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z"/></svg>
          </button>
        </div>
      </div>
      <ul class="stage-single-list" v-if="!sidebarCollapsed">
        <li v-for="(s,i) in stages" :key="i" :class="['only', { active: i===activeIndex }]" @click="switchStage(i)">
          <span>阶段 {{ i+1 }}</span>
          <button type="button" class="stage-del-btn" title="删除该阶段" @click.stop="deleteStage(i)">×</button>
        </li>
      </ul>
      <!-- 实验级资源管理区域 -->
      <div v-if="!sidebarCollapsed" class="exp-resource-box">
        <button type="button" class="resource-add-btn" @click="addResource" :disabled="experimentResources.length>=5">＋ 添加资源</button>
        <div v-if="experimentResources.length>=5" class="res-limit-hint">已达到 5 个资源上限</div>
        <ul v-if="experimentResources.length" class="resource-list">
          <li v-for="(r,i) in experimentResources" :key="i" class="resource-item">
            <span class="res-name" :title="r.name" @click.stop="editResource(i)">{{ r.name }}</span>
            <span class="res-type" :title="r.type">{{ r.type ? r.type.charAt(0) : '' }}</span>
            <a class="res-link" :href="r.url" target="_blank" rel="noopener" :title="r.url" aria-label="打开资源 (新窗口)">↗</a>
            <!-- 编辑按钮已移除，点击名称进入编辑 -->
            <button type="button" class="res-op del" @click.stop="deleteResource(i)">×</button>
          </li>
        </ul>
      </div>
      <button v-if="!sidebarCollapsed" type="button" class="finish-btn" @click="onFinish" :disabled="saving || loadingDetail">{{ loadingDetail? '加载中...' : (saving? '保存中...' : (experimentId? '保存实验' : '完成创建')) }}</button>
    </aside>

    <div class="work" :class="{ 'expand': sidebarCollapsed }">
      <section class="pane left">
        <header class="pane-head">
          <input class="title-input" v-model="currentStage.title" placeholder="在此处输入阶段标题" />
        </header>
        <div class="pane-body intro-body">
          <textarea ref="introRef" class="intro-text" v-model="currentStage.intro" @input="onIntroInput" placeholder="此处输入实验阶段简介描述"></textarea>
          <div class="light-divider"></div>
          <textarea ref="contentRef" class="content-text" v-model="currentStage.content" @input="onContentInput" placeholder="此处输入实验内容和实验问题"></textarea>
          <textarea class="hint-text" v-model="currentStage.hint" placeholder="这里输入hint"></textarea>
        </div>
      </section>
      <section class="pane right">
        <header class="pane-head"><h2>作答区域预览</h2></header>
        <div class="pane-body code-preview-body">
          <div class="stage-type-select top">
            <label>
              类型
              <select v-model="currentStage.type">
                <option value="CHOICE">CHOICE</option>
                <option value="FILL">FILL</option>
                <option value="CODE">CODE</option>
                <option value="TEXT">TEXT</option>
              </select>
            </label>
            <label class="score-label">分数
              <input class="score-input" type="number" min="0" v-model.number="currentStage.score" />
            </label>
            <label v-if="currentStage.type==='CHOICE'" class="choice-multiple" style="display:none">
              <input type="checkbox" disabled /> 多选
            </label>
          </div>
          <div class="answer-area">
            <textarea class="code-preview" v-model="currentStage.answer" spellcheck="false" :placeholder="answerPlaceholder"></textarea>
          </div>
        </div>
        <footer class="pane-foot">
          <div class="status">{{ experimentId? (isDirty()? '有未保存更改' : '已保存') : '预览模式' }}</div>
        </footer>
      </section>
    </div>
  </div>
  <!-- 资源添加弹窗 -->
  <div v-if="showResDialog" class="modal-mask">
    <div class="modal-panel">
      <h3>{{ editingResourceIndex>=0? '编辑资源' : '添加资源' }}</h3>
      <div class="form-row">
        <label>名称 <input v-model="resourceForm.name" placeholder="资源名称" /></label>
      </div>
      <div class="form-row">
        <label>类型
          <select v-model="resourceForm.type">
            <option value="DOCUMENT">DOCUMENT</option>
            <option value="VIDEO">VIDEO</option>
            <option value="CODE">CODE</option>
            <option value="OTHER">OTHER</option>
          </select>
        </label>
      </div>
      <div class="form-row">
        <label>链接 <input v-model="resourceForm.url" placeholder="https://..." /></label>
      </div>
      <div class="actions">
        <button type="button" class="btn cancel" @click="cancelResource">取消</button>
        <button type="button" class="btn ok" @click="confirmResource">{{ editingResourceIndex>=0? '保存' : '添加' }}</button>
      </div>
    </div>
  </div>
  <!-- 标题+简介编辑弹窗 -->
  <div v-if="showDescDialog" class="modal-mask">
    <div class="modal-panel wide">
      <h3>编辑实验标题与简介</h3>
      <div class="form-row">
        <label>标题
          <input v-model="titleDraft" placeholder="输入实验总标题" />
        </label>
      </div>
      <div class="form-row">
        <label>简介
          <textarea class="desc-textarea" v-model="descDraft" placeholder="输入简短简介（可留空）"></textarea>
        </label>
      </div>
      <div class="actions">
        <button type="button" class="btn cancel" @click="cancelDescDialog">取消</button>
        <button type="button" class="btn ok" @click="confirmDescDialog">保存</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stage-sidebar{width:210px;background:#4338ca;color:#fff;display:flex;flex-direction:column;padding:14px 12px;box-sizing:border-box;gap:14px;transition:width .22s ease, padding .22s ease;position:relative;}
.stage-sidebar.collapsed{width:50px;padding:14px 8px;}
.stage-sidebar.collapsed .sb-top{flex-direction:column;align-items:stretch;}
.sb-top{display:flex;align-items:center;justify-content:space-between;gap:8px;}
.left-group{display:flex;align-items:center;gap:8px;}
.right-group{margin-left:auto;display:flex;align-items:center;gap:8px;}
.icon-btn{background:rgba(255,255,255,.12);border:none;color:#fff;height:34px;border-radius:10px;cursor:pointer;display:flex;align-items:center;justify-content:center;font-size:14px;padding:0 14px;font-weight:600;letter-spacing:.5px;}
.icon-btn.add{width:40px;padding:0;font-size:18px;font-weight:400;}
.icon-btn:hover{background:rgba(255,255,255,.22);} 
.icon-btn.back{background:#dc2626;color:#fff;}
.icon-btn.back:hover{background:#b91c1c;}
.collapse-btn{background:rgba(255,255,255,.12);border:none;color:#fff;height:32px;width:32px;padding:0;border-radius:10px;cursor:pointer;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600;letter-spacing:.5px;}
.collapse-btn .chevron{transition:transform .25s ease;}
.stage-sidebar:not(.collapsed) .collapse-btn .chevron{transform:rotate(180deg);} /* 展开时指向左，提示可收起 */
.collapse-btn:hover{background:rgba(255,255,255,.22);} 
.stage-single-list{list-style:none;margin:0;padding:0;display:flex;flex-direction:column;gap:6px;}
.stage-single-list .only{background:rgba(255,255,255,.15);padding:10px 12px;border-radius:8px;font-size:13px;font-weight:600;}
.stage-single-list .only{cursor:pointer;transition:background .18s ease, color .18s ease;}
.stage-single-list .only.active{background:#fff;color:#4338ca;box-shadow:0 0 0 2px rgba(255,255,255,.4);}
.create-layout{display:flex;height:100vh;background:#f1f5f9;font-size:14px;color:#1e293b;}
.work{flex:1;display:flex;height:100vh;} /* 保证左右同高 */
.work.expand{transition:margin-left .22s ease;}
.pane{flex:1;display:flex;flex-direction:column;background:#fff;border:1px solid #e2e8f0;}
.pane.left{border-right:2px solid #e2e8f0;overflow:hidden;} /* 隐藏包裹层溢出 */
.pane-head{padding:14px 18px;border-bottom:1px solid #e2e8f0;background:#f8fafc;display:flex;align-items:center;justify-content:space-between;}
.pane-head h2{margin:0;font-size:15px;font-weight:600;color:#1e293b;}
.pane-body{flex:1;overflow:auto;padding:18px 20px 24px;}
.center-text{display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:600;color:#64748b;}
.pane-foot{border-top:1px solid #e2e8f0;background:#f8fafc;padding:8px 14px;display:flex;align-items:center;justify-content:space-between;font-size:12px;color:#64748b;}
.title-input{width:90%;border:1px solid #cbd5e1;border-radius:8px;padding:8px 12px;font-size:15px;font-weight:600;outline:none;background:#fff;}
.title-input:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.18);}
.code-preview-body{padding:0;display:flex;flex:1;flex-direction:column;} /* 调整为列布局，使类型选择在上 */
.pane.right .code-preview_body{background:#0f172a;}
/* 暗色答案编辑区样式 */
.code-preview{flex:1;width:100%;background:#0f172a;color:#e2e8f0;border:none;outline:none;padding:14px 18px;font-size:13px;line-height:1.55;font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,"Liberation Mono",monospace;resize:none;box-sizing:border-box;}
.code-preview::placeholder{color:#64748b;}
.code-preview:focus{box-shadow:0 0 0 2px rgba(99,102,241,.35) inset;}
/* 代码区域滚动条 */
.code-preview::-webkit-scrollbar{width:10px;}
.code-preview::-webkit-scrollbar-track{background:transparent;}
.code-preview::-webkit-scrollbar-thumb{background:#1e293b;border-radius:6px;border:2px solid #0f172a;}
.code-preview::-webkit-scrollbar-thumb:hover{background:#334155;}
/* Firefox */
.code-preview{scrollbar-width:thin;scrollbar-color:#334155 #0f172a;}
.stage-type-select.top{background:#0f172a;color:#94a3b8;border-bottom:1px solid #1e293b;display:flex;align-items:center;padding:8px 16px;gap:16px;}
.stage-type-select.top label{color:#e2e8f0;}
/* 深色下拉样式 */
.stage-type-select select{background:#1e293b;color:#fff;/* 取消原1px边框改为更细视觉（使用阴影模拟） */border:0;padding:6px 10px;border-radius:6px;font-size:12px;outline:none;cursor:pointer;appearance:none;-webkit-appearance:none;box-shadow:0 0 0 1px #334155;} 
.stage-type-select select:focus{box-shadow:0 0 0 1px #6366f1,0 0 0 3px rgba(99,102,241,.35);}
.answer-area{flex:1;display:flex;padding:0;} /* 去除外层留白，使答案框紧贴上方分界线与容器边界 */
.intro-body{padding:16px 18px;display:flex;flex-direction:column;align-items:flex-start;overflow:auto;}
.intro-body{flex:1;overflow-y:auto;overflow-x:hidden;} /* 左侧内容滚动 */
/* 自定义滚动条（WebKit） */
.intro-body::-webkit-scrollbar{width:8px;}
.intro-body::-webkit-scrollbar-track{background:transparent;}
.intro-body::-webkit-scrollbar-thumb{background:#cbd5e1;border-radius:4px;}
.intro-body::-webkit-scrollbar-thumb:hover{background:#94a3b8;}
/* Firefox */
.intro-body{scrollbar-width:thin;scrollbar-color:#cbd5e1 transparent;}
.intro-text{width:90%;max-width:none;min-height:160px;border:1px solid #cbd5e1;border-radius:10px;padding:12px 14px;font-size:13px;line-height:1.55;outline:none;background:#fff;overflow:hidden;resize:none;}
.intro-text:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.18);} 
.light-divider{margin:18px 0 14px;height:1px;background:linear-gradient(90deg,#e2e8f0,rgba(226,232,240,0));border:none;}
.content-text{width:90%;max-width:none;min-height:260px;border:1px dashed #cbd5e1;border-radius:10px;padding:14px 16px;font-size:13px;line-height:1.6;outline:none;background:#f8fafc;overflow:auto;resize:none;} /* 始终最大高度，内部滚动 */
.content-text:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.18);} 
.resource-box{margin-top:22px;width:90%;max-width:none;display:flex;flex-direction:column;gap:10px;}
.resource-add-btn{background:#6366f1;color:#fff;border:none;cursor:pointer;padding:10px 16px;font-size:13px;font-weight:600;border-radius:8px;display:inline-flex;align-items:center;gap:4px;box-shadow:0 2px 4px rgba(0,0,0,.08);}
.resource-add-btn:hover{background:#4f46e5;}
.resource-add-btn:disabled{background:#94a3b8;cursor:not-allowed;}
.res-limit-hint{font-size:11px;color:#fcd34d;font-weight:600;}
.resource-list{list-style:none;margin:0;padding:0;display:flex;flex-direction:column;gap:6px;font-size:12px;}
.resource-list li.resource-item{background:#eef2ff;padding:6px 8px;border-radius:6px;display:flex;align-items:center;gap:6px;}
.resource-item{position:relative;}
.res-op{background:rgba(67,56,202,.9);color:#fff;border:none;border-radius:6px;padding:4px 8px;font-size:11px;cursor:pointer;}
.res-op.edit{margin-left:6px;background:#6366f1;}
.res-op.del{margin-left:4px;background:#dc2626;}
.res-op.del:hover{background:#b91c1c;}
.res-op.edit:hover{background:#4f46e5;}
.resource-item .res-name{cursor:pointer;}
.resource-item .res-name:hover{text-decoration:underline;}
/* 增强可见性：修改资源名称颜色与悬停效果 */
.resource-item .res-name{color:#4338ca !important;}
.resource-item .res-name:hover{color:#d97706 !important;}
.resource-item .res-type{background:#4338ca;color:#fff;font-size:10px;padding:2px 6px;border-radius:12px;line-height:1;letter-spacing:.5px;}
.resource-item .res-link{margin-left:auto;color:#4338ca;text-decoration:none;font-weight:600;}
.resource-item .res-link:hover{text-decoration:underline;}
.finish-btn{margin-top:auto;background:#16a34a;color:#fff;border:none;height:40px;border-radius:10px;font-size:14px;font-weight:600;cursor:pointer;box-shadow:0 2px 4px rgba(0,0,0,.18);}
.finish-btn:hover{background:#15803d;}
.finish-btn:active{transform:translateY(1px);} 
@media (max-width:1100px){
  .create-layout{flex-direction:column;}
  .stage-sidebar{width:100%;flex-direction:row;align-items:center;gap:12px;padding:10px 16px;justify-content:flex-start;}
  .stage-sidebar.collapsed{width:100%;padding:10px 16px;} /* 强制全宽 */
  .collapse-btn{display:none !important;} /* 隐藏折叠按钮 */
  .sb-top{flex-direction:row;margin-left:0;}
  .right-group{margin-left:0;}
  .stage-single-list{flex-direction:row;gap:8px;margin:0 0 0 12px;}
  .stage-single-list .only{padding:8px 12px;}
  .work{flex-direction:column;}
  .pane.left{border-right:none;border-bottom:2px solid #e2e8f0;}
}
.exp-resource-box{display:flex;flex-direction:column;gap:10px;}
.hint-text{width:90%;max-width:none;min-height:70px;border:1px dashed #cbd5e1;border-radius:10px;margin-top:14px;padding:12px 14px;font-size:13px;line-height:1.55;outline:none;background:#f8fafc;overflow:hidden;resize:none;}
.hint-text:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.18);}
.score-label{display:flex;align-items:center;gap:4px;font-size:14px;color:#e2e8f0;font-weight:400;margin-left:auto;} /* 与“类型”标签统一 */
.score-input{width:70px;background:#1e293b;color:#e2e8f0;border:1px solid #334155;border-radius:6px;padding:6px 8px;font-size:12px;outline:none;}
.score-input:focus{border-color:#6366f1;box-shadow:0 0 0 2px rgba(99,102,241,.35);} 
/* 窄屏时分数输入排版 */
@media (max-width:1100px){
  .score-input{width:60px;}
}
/* 细化类型下拉框边界，消除左上“双色/双线”视觉；使用更浅色并移除外层加粗效果 */
.stage-type-select select{
  appearance:none; /* 统一外观，避免系统内置双层边框 */
  -webkit-appearance:none;
  -moz-appearance:none;
  border:1px solid rgba(255,255,255,.22); /* 更细更柔和 */
  box-shadow:none; /* 去掉可能的默认内阴影 */
}
.stage-type-select select:focus{
  border-color:#6366f1;
  box-shadow:0 0 0 1px rgba(99,102,241,.55); /* 改为 1px 细发光而非 2px */
}
/* 选项面板(部分浏览器)背景保持深色避免视觉“白边” */
.stage-type-select select option{background:#1e293b;color:#fff;}
.modal-mask{position:fixed;inset:0;background:rgba(15,23,42,.55);display:flex;align-items:center;justify-content:center;z-index:2000;}
.modal-panel{background:#fff;width:360px;max-width:92%;border-radius:14px;padding:20px 22px 22px;box-shadow:0 10px 32px -4px rgba(0,0,0,.3);display:flex;flex-direction:column;gap:14px;font-size:13px;}
.modal-panel h3{margin:0;font-size:16px;font-weight:600;color:#1e293b;}
.modal-panel .form-row{display:flex;flex-direction:column;gap:6px;}
.modal-panel label{font-weight:600;color:#334155;display:flex;flex-direction:column;gap:4px;font-size:12px;}
.modal-panel input, .modal-panel select{border:1px solid #cbd5e1;border-radius:8px;padding:8px 10px;font-size:13px;background:#fff;outline:none;}
.modal-panel input:focus, .modal-panel select:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.18);} 
.modal-panel select{cursor:pointer;}
.modal-panel .actions{display:flex;justify-content:flex-end;gap:10px;margin-top:4px;}
.modal-panel .btn{border:none;cursor:pointer;height:36px;padding:0 18px;font-size:13px;font-weight:600;border-radius:8px;display:inline-flex;align-items:center;}
.modal-panel .btn.cancel{background:#e2e8f0;color:#334155;}
.modal-panel .btn.cancel:hover{background:#cbd5e1;}
.modal-panel .btn.ok{background:#6366f1;color:#fff;}
.modal-panel .btn.ok:hover{background:#4f46e5;}
.basic-info{background:rgba(255,255,255,.12);padding:10px 12px;border-radius:10px;display:flex;flex-direction:column;gap:6px;font-size:12px;}
.basic-info .exp-id{font-weight:600;letter-spacing:.5px;}
.basic-info .exp-base-title{font-weight:700;font-size:13px;line-height:1.3;max-height:2.6em;overflow:hidden;}
.basic-info .exp-base-desc{font-size:11px;line-height:1.3;opacity:.85;max-height:3.9em;overflow:hidden;}
.basic-info .exp-base-row{display:flex;align-items:center;gap:8px;}
.basic-info .title-row{display:flex;align-items:center;gap:8px;}
.basic-info .title-row .exp-base-title{flex:1;}
.edit-desc-icon-btn{background:rgba(255,255,255,.18);border:none;color:#fff;width:30px;height:30px;border-radius:8px;display:inline-flex;align-items:center;justify-content:center;cursor:pointer;padding:0;}
.edit-desc-icon-btn:hover{background:rgba(255,255,255,.3);} 
.modal-panel.wide{width:480px;}
.modal-panel.wide input{border:1px solid #cbd5e1;border-radius:8px;padding:8px 10px;font-size:13px;background:#fff;outline:none;}
.modal-panel.wide input:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.18);} 
/* 统一标题与简介输入框样式 */
.modal-panel.wide input, .modal-panel.wide textarea{
  border:1px solid #cbd5e1;
  border-radius:8px;
  padding:8px 10px;
  font-size:13px;
  line-height:1.5;
  background:#fff;
  outline:none;
  width:100%;
  box-sizing:border-box;
  font-family:inherit;
}
.modal-panel.wide input:focus, .modal-panel.wide textarea:focus{
  border-color:#6366f1;
  box-shadow:0 0 0 3px rgba(99,102,241,.18);
}
/* 若需要限制高度，可单独为 textarea 设置最小高度 */
.modal-panel.wide textarea{min-height:120px;resize:vertical;}
/* 保留原 desc-textarea 样式可简化为引用统一规则，无需额外定义 */
.stage-single-list .only{position:relative;display:flex;align-items:center;justify-content:space-between;}
.stage-del-btn{background:rgba(255,255,255,.25);border:none;color:#fff;width:22px;height:22px;line-height:22px;padding:0;border-radius:6px;cursor:pointer;font-weight:700;font-size:14px;display:flex;align-items:center;justify-content:center;}
.stage-del-btn:hover{background:#dc2626;}
.stage-single-list .only.active .stage-del-btn{color:#4338ca;background:rgba(0,0,0,.08);}
.stage-single-list .only.active .stage-del-btn:hover{background:#dc2626;color:#fff;}
</style>
