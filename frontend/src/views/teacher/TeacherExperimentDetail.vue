<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'
import { apiGetTeacherExperiment, type ExperimentResponse, type ExperimentStage } from '@/api/teacher'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const experimentId = computed(()=> Number(route.params.id))

// 移除自定义 StageView，直接使用 ExperimentStage 并扩展本地只读视图
interface StageView extends ExperimentStage {}
const stages = ref<StageView[]>([])
const loading = ref(false)
const error = ref('')
const activeStageId = ref<number | null>(null)
const activeStage = computed(()=> stages.value.find(s=>s.id===activeStageId.value))
const showDesc = computed(()=> {
  if(!activeStage.value) return false
  const d = (activeStage.value.description||'').trim()
  const c = (activeStage.value.content||'').trim()
  return !!d && d !== c
})

async function load(){
  if(!auth.user?.id) return
  loading.value = true
  error.value = ''
  try {
    const detail: ExperimentResponse = await apiGetTeacherExperiment(Number(auth.user.id), experimentId.value)
    const rawStages: any[] = Array.isArray(detail.stages) ? detail.stages : []
    let sorted = rawStages
      .map(s=>({
        // 兼容两种命名：后台 ExperimentStageResponse vs 完整 ExperimentStage
        id: s.id ?? s.stageId ?? -Math.random(),
        title: s.title ?? s.name ?? `阶段${s.orderNo || s.sequence || ''}`,
        description: s.description,
        content: s.content ?? s.description ?? '',
        type: s.type ?? s.stageType ?? 'TEXT',
        maxScore: s.maxScore,
        hint: s.hint,
        sequence: s.sequence ?? s.orderNo,
        evaluation: s.evaluation
      }))
      .sort((a,b)=> (a.sequence||0) - (b.sequence||0) || (a.id - b.id)) as StageView[]
    if(!sorted.length && detail.description){
      sorted = [{ id:-1, title:'实验说明', description: detail.objective, content: detail.description, type: 'TEXT' as any }] as StageView[]
    }
    stages.value = sorted
    activeStageId.value = stages.value[0]?.id || null
  } catch(e:any){
    error.value = e.message || '加载失败'
  } finally { loading.value = false }
}

function goBack(){ router.push({ name:'teacher-home' }) }
function goEdit(){
  const seq = activeStage.value?.sequence || (stages.value.findIndex(s=>s.id===activeStageId.value) + 1) || undefined
  router.push({ name:'teacher-home', query:{ editId: experimentId.value, editStage: seq } })
}
const collapsed = ref(false)

onMounted(load)
</script>

<template>
  <div class="exp-preview-layout">
    <aside :class="['stage-sidebar', { collapsed: collapsed }]">
      <div class="sb-top">
        <button class="icon-btn back" @click="goBack" title="返回教师主页">
          <span v-if="!collapsed">返回</span>
          <span v-else>‹</span>
        </button>
        <button class="icon-btn toggle" @click="collapsed=!collapsed" :title="collapsed ? '展开阶段列表' : '收起阶段列表'">{{ collapsed ? '›' : '‹' }}</button>
      </div>
      <ul class="stage-list">
        <li v-for="s in stages" :key="s.id" :class="['stage-item', { active: s.id===activeStageId }]" @click="activeStageId=s.id">
          <span class="dot"></span>
          <span v-if="!collapsed" class="text">{{ s.title }}</span>
        </li>
      </ul>
    </aside>
    <div class="work-area">
      <section class="question-pane">
        <header class="q-header">
          <h2 class="q-title">{{ activeStage?.title || '加载中...' }}</h2>
          <button class="outline-btn" @click="goEdit" v-if="!loading && !error">编辑该实验</button>
        </header>
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else-if="activeStage" class="q-content">
          <div class="meta-line">
            <span class="tag type">类型: {{ activeStage.type }}</span>
            <span class="tag score" v-if="activeStage.maxScore!=null">满分: {{ activeStage.maxScore }}</span>
            <span class="tag seq" v-if="activeStage.sequence">序号: {{ activeStage.sequence }}</span>
          </div>
          <p class="desc" v-if="showDesc">{{ activeStage.description }}</p>
          <pre class="pre">{{ activeStage.content }}</pre>
          <p class="hint" v-if="activeStage.hint">提示: {{ activeStage.hint }}</p>
        </div>
        <div v-else class="empty">暂无阶段数据</div>
      </section>
      <section class="readonly-tip-pane">
        <div class="tip-box">
          预览模式：仅查看与切换阶段。点击右上“编辑该实验”可进入编辑模式。
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.exp-preview-layout { display:flex; height:100vh; background:#f1f5f9; }
.stage-sidebar { width:210px; background:#1e3a8a; color:#fff; display:flex; flex-direction:column; padding:14px 12px; box-sizing:border-box; gap:14px; transition:width .25s; }
.stage-sidebar.collapsed { width:64px; }
.sb-top { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.icon-btn { background:rgba(255,255,255,.12); border:none; color:#fff; width:34px; height:34px; border-radius:10px; cursor:pointer; display:flex; align-items:center; justify-content:center; font-size:16px; transition:.25s; }
.icon-btn.back { width:auto; padding:0 14px; font-size:14px; font-weight:600; letter-spacing:.5px; }
.stage-sidebar.collapsed .icon-btn.back { padding:0; width:34px; font-size:18px; }
.icon-btn:hover { background:rgba(255,255,255,.22); }
.stage-list { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:6px; }
.stage-item { display:flex; align-items:center; gap:10px; padding:10px 10px; cursor:pointer; border-radius:8px; font-size:13px; font-weight:500; color:#dbeafe; transition:.2s; }
.stage-item .dot { width:8px; height:8px; border-radius:50%; background:#60a5fa; box-shadow:0 0 0 4px rgba(255,255,255,.08); }
.stage-item:hover { background:rgba(255,255,255,.08); }
.stage-item.active { background:#2563eb; color:#fff; }
.stage-item.active .dot { background:#fff; box-shadow:0 0 0 4px rgba(255,255,255,.25); }
.work-area { flex:1; display:flex; }
.question-pane, .readonly-tip-pane { flex:1; display:flex; flex-direction:column; background:#fff; border:1px solid #e2e8f0; }
.question-pane { border-right:2px solid #e2e8f0; }
.q-header { padding:14px 18px; border-bottom:1px solid #e2e8f0; background:#f8fafc; display:flex; align-items:center; justify-content:space-between; gap:12px; }
.q-title { margin:0; font-size:15px; font-weight:600; color:#1e293b; }
.q-content { margin:0; padding:18px 20px 24px; font-size:13px; line-height:1.6; color:#334155; flex:1; overflow:auto; }
.q-content .pre { white-space:pre-wrap; font-family:inherit; background:#fff; margin:0; }
.readonly-tip-pane { align-items:center; justify-content:center; }
.tip-box { max-width:360px; background:#f1f5f9; border:1px solid #e2e8f0; padding:18px 20px; border-radius:12px; font-size:13px; line-height:1.6; color:#334155; }
.loading { padding:24px; font-size:14px; color:#475569; }
.error { padding:24px; font-size:14px; color:#dc2626; }
.empty { padding:24px; font-size:14px; color:#64748b; }
.outline-btn { background:#fff; border:1px solid #cbd5e1; color:#1e293b; padding:8px 14px; border-radius:8px; font-size:12px; font-weight:600; cursor:pointer; }
.outline-btn:hover { background:#f1f5f9; }
.meta-line { display:flex; flex-wrap:wrap; gap:8px; margin-bottom:10px; }
.tag { background:#eef2ff; color:#4338ca; font-size:11px; font-weight:600; padding:4px 8px; border-radius:6px; letter-spacing:.5px; }
.hint { margin-top:14px; font-size:12px; color:#475569; background:#f1f5f9; padding:10px 12px; border-radius:8px; }
@media (max-width:1100px){
  .exp-preview-layout { flex-direction:column; }
  .stage-sidebar { flex-direction:row; width:auto; height:60px; padding:10px 12px; }
  .stage-sidebar.collapsed { width:auto; }
  .stage-list { flex-direction:row; }
  .work-area { flex-direction:column; }
  .question-pane { border-right:0; border-bottom:2px solid #e2e8f0; }
}
</style>
