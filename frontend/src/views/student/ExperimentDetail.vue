<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiInitExperiment, apiGetStageAnswer, /* apiSaveStageAnswer, */ apiFinalSubmitStageAnswer, type ExperimentExecutionInitResponse, type ExperimentStageResponse, type StudentAnswerResponse } from '@/api/student'

// 路由参数（发布实验ID）
const route = useRoute()
const publishedExperimentId = computed(() => Number(route.params.id))

// 加载状态
const loading = ref(false)
const loadError = ref('')
const detail = ref<ExperimentExecutionInitResponse | null>(null)

// 阶段
const stages = ref<ExperimentStageResponse[]>([])
const activeStageId = ref<number | null>(null)
const activeStage = computed(()=> stages.value.find(s=>s.stageId===activeStageId.value))
// 新增：阶段标题（随当前阶段变化）
const stageTitle = computed(()=> activeStage.value?.title || activeStage.value?.name || '阶段')
// 新增：阶段简介（取多个可能字段）
const stageIntro = computed(()=> activeStage.value?.description || (activeStage.value as any)?.desc || (activeStage.value as any)?.intro || '')

// 答案缓存
const stageAnswers = reactive<Record<number, StudentAnswerResponse | null>>({})
const stageCodes = reactive<Record<number,string>>({})

const loadingStageAnswer = ref(false)
const stageAnswerError = ref('')

async function loadInit(){
  if(!publishedExperimentId.value) return
  loading.value = true
  loadError.value = ''
  try {
    const data = await apiInitExperiment(publishedExperimentId.value)
    detail.value = data
    stages.value = data.experiment?.stages || []
    // 默认选第一阶段
    if(stages.value.length){ activeStageId.value = stages.value[0].stageId }
    // 加载已保存的草稿答案
    if(data.allStageDrafts){
      for(const ans of data.allStageDrafts){
        if(ans.stageId!=null){
          stageAnswers[ans.stageId] = ans
          stageCodes[ans.stageId] = ans.codeContent || ans.answerContent || ''
        }
      }
    }
  } catch(e:any){ loadError.value = e.message || '加载失败' } finally { loading.value=false }
}

const isFullScore = computed(()=>{
  const st = activeStage.value
  if(!st || activeStageId.value==null) return false
  const ans = stageAnswers[activeStageId.value]
  if(!ans) return false
  // 使用 passed 或 分数与 maxScore 相等 任一条件判断为满分
  if(ans.passed===true) return true
  if(typeof st.maxScore==='number' && ans.score!=null && ans.score===st.maxScore) return true
  return false
})

function applyBinaryScore(ans: StudentAnswerResponse | null, st: ExperimentStageResponse | undefined){
  if(!ans || !st) return
  if(typeof st.maxScore === 'number'){
    if(ans.passed === true){ ans.score = st.maxScore }
    else if(ans.passed === false){ ans.score = 0 }
  }
}
// 调整加载阶段答案后也应用二档显示
async function loadStageAnswer(id:number){
  if(!publishedExperimentId.value) return
  loadingStageAnswer.value = true
  stageAnswerError.value = ''
  try {
    const ans = await apiGetStageAnswer(publishedExperimentId.value, id)
    const st = stages.value.find(s=>s.stageId===id)
    applyBinaryScore(ans, st)
    stageAnswers[id] = ans
    if(ans.codeContent || ans.answerContent){
      stageCodes[id] = ans.codeContent || ans.answerContent || ''
    } else if(stageCodes[id] == null){
      stageCodes[id] = ''
    }
  } catch(e:any){ stageAnswerError.value = e.message || '加载阶段答案失败' } finally { loadingStageAnswer.value=false }
}

watch(activeStageId, (nid) => { if(nid!=null && !stageAnswers[nid]) loadStageAnswer(nid) })

// 提交
const submitting = ref(false)
const submitMsg = ref('')
async function finalSubmit(){
  if(activeStageId.value==null || !publishedExperimentId.value) return
  if(isFullScore.value){ toast('该阶段已满分，不能再次提交','info'); return }
  if(!confirm('确认提交并评分?')) return
  submitting.value = true
  submitMsg.value = ''
  try {
    const ans = await apiFinalSubmitStageAnswer({
      publishedExperimentId: publishedExperimentId.value,
      stageId: activeStageId.value,
      answerContent: stageCodes[activeStageId.value],
      confirm: true
    })
    const st = activeStage.value
    applyBinaryScore(ans, st)
    stageAnswers[activeStageId.value] = ans
    submitMsg.value = ans.passed ? `通过，得分 ${ans.score??''}` : `未通过，得分 ${ans.score??''}`
  } catch(e:any){ submitMsg.value = e.message||'提交失败'; toast(submitMsg.value,'error') } finally { submitting.value=false }
}

// 运行代码占位
function runCode(){
  if(activeStageId.value==null) return
  toast('运行代码功能待实现','info')
}

// 侧边栏折叠
const sidebarCollapsed = ref(false)
function toggleSidebar(){ sidebarCollapsed.value = !sidebarCollapsed.value }

// 阶段状态计算
function stageStatus(s: ExperimentStageResponse){
  const ans = stageAnswers[s.stageId]
  if(!ans) return 'unanswered'
  if(ans.score != null){
    if(ans.passed) return 'passed'
    return 'graded'
  }
  return 'unanswered'
}

const router = useRouter()
function goBack(){ router.push('/student') }

// 简易 toast（使用本地队列）
interface ToastItem { id:number; msg:string; type:'info'|'error'|'success'; }
const toasts = ref<ToastItem[]>([])
let toastId = 0
function toast(msg:string, type:ToastItem['type']='info'){
  const id = ++toastId
  toasts.value.push({ id, msg, type })
  setTimeout(()=>{ toasts.value = toasts.value.filter(t=>t.id!==id) }, 3000)
}

onMounted(()=>{ loadInit() })

const showIntro = ref(false)
function openIntro(){ showIntro.value = true }

const progressFinishedCount = computed(()=> detail.value?.progress?.finishedStages ?? 0)
const progressTotal = computed(()=> detail.value?.progress?.totalStages ?? stages.value.length)
</script>

<template>
  <div class="student-exp-page">
    <div class="body-layout">
      <aside :class="['stage-sidebar', { collapsed: sidebarCollapsed }]">
        <div class="exp-header">
          <div class="top-row">
            <button class="icon-btn back" v-show="!sidebarCollapsed" @click="goBack" title="返回">返回</button>
            <!-- 折叠按钮：改为与教师端一致的 SVG chevron -->
            <button class="collapse-btn" @click="toggleSidebar" :aria-label="sidebarCollapsed? '展开侧边栏' : '收起侧边栏'">
              <svg class="chevron" viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6" /></svg>
            </button>
          </div>
          <div class="title-wrap" v-show="!sidebarCollapsed">
            <button class="exp-title-btn" :title="detail?.experiment?.title" @click="openIntro">
              {{ detail?.experiment?.title || '实验详情' }}
            </button>
          </div>
        </div>
        <div class="sb-section">
          <ul class="stage-list">
            <li v-for="(s, idx) in stages" :key="s.stageId"
                :class="['stage-item', stageStatus(s), { active: s.stageId===activeStageId, 'first-stage': idx===0 }]"
                @click="activeStageId = s.stageId">
              <div class="status-dot"></div>
              <div class="flex-col" v-show="!sidebarCollapsed">
                <span class="name">{{ s.title || s.name }}</span>
                <span class="badge" :class="stageStatus(s)">
                  {{ stageStatus(s)==='unanswered'? '未作答' : stageStatus(s)==='draft'? '草稿' : stageStatus(s)==='passed'? '已通过' : '已评分' }}
                </span>
              </div>
            </li>
          </ul>
        </div>
        <!-- 新增：底部进度统计 -->
        <div class="progress-footer" v-show="!sidebarCollapsed">
          <div class="pf-line">
            <span class="pf-label">阶段完成</span>
            <span class="pf-count">{{ progressFinishedCount }} / {{ progressTotal }}</span>
          </div>
          <div class="pf-bar-wrap" v-if="progressTotal>0">
            <div class="pf-bar-bg">
              <div class="pf-bar-fg" :style="{ width: (progressFinishedCount/progressTotal*100).toFixed(1)+'%' }"></div>
            </div>
          </div>
        </div>
      </aside>

      <div class="qa-wrapper" v-if="!loading && !loadError">
        <div class="panel qa-panel" v-if="activeStage">
          <div class="qa-sections">
            <section class="q-section">
              <div class="section-head">
                <h2>{{ stageTitle }}</h2>
              </div>
              <div class="section-body q-body">
                <!-- 移除整体实验说明，仅显示当前阶段内容。如需恢复可将以下注释解开 -->
                <!-- <div class="exp-desc" v-if="detail?.experiment?.description">
                  <h4>实验说明</h4>
                  <p class="desc-text">{{ detail?.experiment?.description }}</p>
                </div> -->
                <div class="stage-block">
                  <!-- 新增阶段简介展示 -->
                  <pre v-if="stageIntro" class="stage-content intro">{{ stageIntro }}</pre>
                  <pre class="stage-content">{{ activeStage.content }}</pre>
                  <div v-if="activeStage.hint" class="hint-line">提示：{{ activeStage.hint }}</div>
                </div>
              </div>
            </section>
            <section class="a-section">
              <div class="section-head answer-head">
                <h2>作答区</h2>
                <div class="btn-group">
                  <button class="btn ghost" @click="runCode" :disabled="!activeStageId">运行</button>
                  <button class="btn primary" @click="finalSubmit" :disabled="submitting || !activeStageId || isFullScore">{{ isFullScore? '已满分' : (submitting? '提交中...' : '提交评分') }}</button>
                </div>
              </div>
              <div class="section-body answer-body">
                <textarea v-model="stageCodes[activeStageId!]" class="code-editor" spellcheck="false"></textarea>
                <div class="footer-line">
                  <div class="left-status">
                    <span v-if="loadingStageAnswer">加载阶段答案...</span>
                    <span v-else-if="stageAnswerError" class="error">{{ stageAnswerError }}</span>
                    <span v-else>
                      <template v-if="stageAnswers[activeStageId!]?.submittedAt">提交：{{ stageAnswers[activeStageId!]?.submittedAt }}</template>
                      <template v-else>未提交</template>
                    </span>
                  </div>
                  <div class="right-status" v-if="submitMsg">{{ submitMsg }}</div>
                </div>
                <div class="evaluation-box" v-if="stageAnswers[activeStageId!]?.score!=null">
                  <div class="result-row">
                    <span class="label">评分结果</span>
                    <span class="value">
                      <span :class="['eval-badge', stageAnswers[activeStageId!]?.passed? 'passed':'failed']">
                        {{ stageAnswers[activeStageId!]?.passed? '通过' : '未通过' }}
                      </span>
                      <span class="score">得分：{{ stageAnswers[activeStageId!]?.score }}</span>
                    </span>
                  </div>
                  <div class="result-row" v-if="stageAnswers[activeStageId!]?.feedback">
                    <span class="label">反馈</span>
                    <span class="feedback-text">{{ stageAnswers[activeStageId!]?.feedback }}</span>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </div>
        <div class="panel qa-panel empty" v-else>请选择一个阶段</div>
      </div>
      <div v-else class="loading-holder">
        <div v-if="loading">加载实验中...</div>
        <div v-else class="error-line">{{ loadError }}</div>
      </div>
    </div>

    <!-- 简介弹窗 -->
    <div v-if="showIntro" class="intro-modal-overlay" @click.self="showIntro=false">
      <div class="intro-modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ detail?.experiment?.title || '实验详情' }}</h3>
          <button class="close-btn" @click="showIntro=false" aria-label="关闭">×</button>
        </div>
        <div class="modal-body">
          <div v-if="detail?.experiment?.description && detail.experiment.description.trim()" class="intro-text">{{ detail.experiment.description }}</div>
          <div v-else class="intro-empty">暂无简介</div>
        </div>
      </div>
    </div>

    <div class="toast-stack">
      <div v-for="t in toasts" :key="t.id" :class="['toast', t.type]">{{ t.msg }}</div>
    </div>
  </div>
</template>

<style scoped>
.student-exp-page{display:flex;flex-direction:column;height:100vh;background:#f1f5f9;color:#1e293b;font-size:14px;}
.body-layout{flex:1;display:flex;min-height:0;}
/* ===== 侧边栏（迁移教师端风格，改为浅蓝主题） ===== */
.stage-sidebar{width:220px;background:#3b82f6;color:#fff;display:flex;flex-direction:column;padding:14px 12px 18px;box-sizing:border-box;gap:14px;transition:width .22s ease,padding .22s ease,background .3s ease;position:relative;font-size:13px;}
.stage-sidebar.collapsed{width:50px;padding:14px 8px;} /* 原60px -> 50px */
/* 让折叠态图标更居中 */
.stage-sidebar.collapsed .top-row{justify-content:center;}
.stage-sidebar.collapsed .collapse-btn{margin-left:0;}
.exp-header{display:flex;flex-direction:column;gap:10px;}
.exp-header .top-row{display:flex;align-items:center;gap:8px;width:100%;}
.top-row .collapse-btn{margin-left:auto;} /* 使折叠按钮贴右 */
.stage-sidebar.collapsed .exp-header{gap:8px;}
/* 按钮风格对齐教师端 */
.icon-btn.back{background:#dc2626;color:#fff;border:none;height:34px;border-radius:10px;cursor:pointer;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:600;padding:0 16px;letter-spacing:.5px;box-shadow:0 2px 4px rgba(0,0,0,.18);}
.icon-btn.back:hover{background:#b91c1c;}
.collapse-btn{background:rgba(255,255,255,.12);border:none;color:#fff;height:32px;width:32px;padding:0;border-radius:10px;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:background .18s ease;}
.collapse-btn:hover{background:rgba(255,255,255,.22);} 
.collapse-btn .chevron{transition:transform .25s ease;}
.stage-sidebar:not(.collapsed) .collapse-btn .chevron{transform:rotate(180deg);} /* 展开时指向左 */
/* 实验标题按钮与教师风格一致（浅色蒙层+悬停增强） */
.exp-title-btn{width:100%;text-align:center;background:rgba(255,255,255,.12);color:#fff;border:none;padding:10px 12px;border-radius:10px;font-size:14px;font-weight:700;line-height:1.3;cursor:pointer;white-space:normal;word-break:break-word;transition:background .18s ease,box-shadow .18s ease;}
.exp-title-btn:hover{background:rgba(255,255,255,.22);} 
.exp-title-btn:focus{outline:0;box-shadow:0 0 0 3px rgba(255,255,255,.4);} 

/* 阶段列表（复刻教师单列卡片风格 + 状态徽章） */
.stage-list{list-style:none;margin:0;padding:0;display:flex;flex-direction:column;gap:6px;overflow:auto;}
.stage-item{background:rgba(255,255,255,.18);padding:8px 10px;border-radius:10px;display:flex;align-items:center;gap:8px;cursor:pointer;transition:background .18s ease,color .18s ease,box-shadow .18s ease;font-weight:600;position:relative;min-height:38px;box-sizing:border-box;}
.stage-item:hover:not(.active){background:rgba(255,255,255,.28);} 
/* 激活高亮重制：移除挤压视觉的 box-shadow，使用伪元素描边不影响尺寸 */
.stage-item.active{background:#fff;color:#1d4ed8;}
.stage-item.active:after{content:"";position:absolute;inset:0;border-radius:inherit;border:2px solid rgba(255,255,255,.9);box-shadow:0 0 0 3px rgba(59,130,246,.35),0 2px 4px -1px rgba(0,0,0,.18);pointer-events:none;}
.stage-item .status-dot{width:8px;height:8px;border-radius:50%;background:#bfdbfe;box-shadow:0 0 0 4px rgba(255,255,255,.15);flex-shrink:0;transition:background .25s ease,box-shadow .25s ease;}
.stage-item.active .status-dot{box-shadow:0 0 0 4px rgba(59,130,246,.25);} 
.stage-item.passed .status-dot{background:#10b981;}
.stage-item.graded .status-dot{background:#f59e0b;}
.stage-item.draft .status-dot{background:#64748b;}
.stage-item.unanswered .status-dot{background:#94a3b8;}
/* 去冗余：移除重复的 .stage-item.active .status-dot 第二次定义 */
.stage-item .flex-col{display:flex;flex-direction:row;align-items:center;gap:6px;flex:1;min-width:0;}
.stage-item .name{flex:1;min-width:0;font-size:12px;line-height:1.15;letter-spacing:.3px;}
.stage-item.first-stage .name{font-size:14px;}
/* 状态徽章贴右，与名称同列 */
.stage-item .badge{margin-left:auto;display:inline-block;padding:4px 10px;font-size:11px;line-height:1;letter-spacing:.5px;border-radius:999px;font-weight:600;white-space:nowrap;background:rgba(255,255,255,.22);color:#fff;backdrop-filter:blur(2px);}
.stage-item.active .badge{background:#eff6ff;color:#1d4ed8;}
/* 根据状态微调颜色 */
.stage-item .badge.unanswered{background:rgba(255,255,255,.22);color:#fff;}
.stage-item .badge.draft{display:none;}
.stage-item .badge.graded{background:#fcd34d;color:#92400e;}
.stage-item .badge.passed{background:#bbf7d0;color:#047857;}
.stage-item.active .badge.passed{background:#bbf7d0;color:#047857;}
.stage-item.active .badge.graded{background:#fde68a;color:#92400e;}
.stage-item.active .badge.draft{background:#e2e8f0;color:#475569;}

/* 折叠时隐藏文本列，仅保留状态点（保持紧凑） */
.stage-sidebar.collapsed .stage-item{justify-content:center;padding:10px 0;}
.stage-sidebar.collapsed .stage-item .flex-col{display:none;}
.stage-sidebar.collapsed .stage-item .status-dot{margin:0;}
/* 新增：折叠时整体隐藏阶段列表 */
.sb-section{flex:1;min-height:0;transition:opacity .18s ease, max-height .22s ease;}
.stage-sidebar.collapsed .sb-section{opacity:0;max-height:0;overflow:hidden;pointer-events:none;} 

/* 滚动条柔和 */
.stage-list::-webkit-scrollbar{width:8px;}
.stage-list::-webkit-scrollbar-track{background:transparent;}
.stage-list::-webkit-scrollbar-thumb{background:rgba(255,255,255,.28);border-radius:4px;}
.stage-list::-webkit-scrollbar-thumb:hover{background:rgba(255,255,255,.45);} 

/* ===== 主体面板（沿用原样式） ===== */
.qa-wrapper{flex:1;display:flex;flex-direction:column;padding:0;box-sizing:border-box;overflow:auto;}
.qa-panel{flex:1;display:flex;flex-direction:column;background:#fff;border:1px solid #e2e8f0;border-radius:0;}
.qa-panel.empty{align-items:center;justify-content:center;font-size:14px;color:#64748b;}
.qa-sections{flex:1;display:flex;min-height:0;}
.q-section,.a-section{flex:1;display:flex;flex-direction:column;min-width:0;}
.q-section{border-right:1px solid #e2e8f0;}
.section-head{padding:16px 20px;border-bottom:1px solid #e2e8f0;display:flex;align-items:center;justify-content:space-between;gap:16px;background:#f8fafc;}
.section-head h2{margin:0;font-size:15px;font-weight:700;color:#1e293b;}
.section-body{flex:1;overflow:auto;display:flex;flex-direction:column;gap:18px;padding:18px 20px 20px;}
.q-body h4{margin:0 0 6px;font-size:13px;font-weight:700;color:#475569;}
.exp-desc .desc-text{margin:4px 0 0;white-space:pre-wrap;font-size:13px;line-height:1.5;}
.stage-block .stage-content{margin:8px 0 0;white-space:pre-wrap;font-size:13px;line-height:1.5;background:#f1f5f9;padding:12px 14px;border-radius:10px;border:1px solid #e2e8f0;}
/* 简介与内容分隔：给第一个简介块更小的上边距 */
.stage-block .stage-content.intro{margin-top:4px;}
.hint-line{font-size:12px;color:#64748b;margin-top:6px;}
.answer-head .btn-group{display:flex;gap:10px;flex-wrap:wrap;}
.code-editor{flex:1;width:100%;border:1px solid #33415533;background:#0f172a;color:#e2e8f0;font-family:ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Courier New", monospace;font-size:13px;line-height:1.5;padding:14px 16px;border-radius:12px;resize:none;outline:none;}
.code-editor:focus{border-color:#6366f1;box-shadow:0 0 0 3px rgba(99,102,241,.25);}
.footer-line{display:flex;align-items:center;justify-content:space-between;gap:16px;font-size:12px;color:#64748b;flex-wrap:wrap;}
.footer-line .error{color:#dc2626;}
.evaluation-box{background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:14px 16px;display:flex;flex-direction:column;gap:12px;}
.result-row{display:flex;align-items:flex-start;gap:10px;font-size:13px;flex-wrap:wrap;}
.result-row .label{font-weight:600;color:#475569;min-width:70px;}
.eval-badge{display:inline-block;padding:4px 10px;border-radius:999px;font-size:12px;font-weight:600;letter-spacing:.5px;margin-right:8px;}
.eval-badge.passed{background:#d1fae5;color:#047857;}
.eval-badge.failed{background:#fee2e2;color:#dc2626;}
.score{font-weight:600;color:#334155;margin-left:4px;}
.feedback-text{white-space:pre-wrap;line-height:1.5;color:#334155;}
.btn{border:none;cursor:pointer;font-size:13px;font-weight:600;padding:8px 16px;border-radius:8px;letter-spacing:.5px;display:inline-flex;align-items:center;gap:6px;}
.btn.primary{background:#6366f1;color:#fff;}
.btn.primary:hover{background:#4f46e5;}
.btn.ghost{background:#e2e8f0;color:#1e293b;}
.btn.ghost:hover{background:#cbd5e1;}
.loading-holder{flex:1;display:flex;align-items:center;justify-content:center;font-size:14px;}
.error-line{color:#dc2626;}
.toast-stack{position:fixed;right:18px;bottom:18px;display:flex;flex-direction:column;gap:10px;z-index:4000;}
.toast{background:#334155;color:#f1f5f9;padding:10px 14px;border-radius:10px;font-size:13px;box-shadow:0 4px 16px -2px rgba(0,0,0,.25);animation:fadeIn .25s ease;}
.toast.info{background:#334155;}
.toast.error{background:#dc2626;}
.toast.success{background:#10b981;}
@keyframes fadeIn{from{opacity:0;transform:translateY(6px);}to{opacity:1;transform:translateY(0);}}
/* overrides: 作答区去空隙 & 直角代码框 */
.answer-body{padding:0 !important;gap:0 !important;}
.answer-body .code-editor{border-radius:0 !important;border-left:none;border-right:none;}
.answer-body .footer-line{padding:10px 20px;}
.answer-body .evaluation-box{margin:0;border-left:none;border-right:none;border-radius:0;padding:14px 20px;}

/***** 追加底部统计样式 *****/
.progress-footer{margin-top:4px;padding:10px 10px 12px;background:rgba(255,255,255,.14);border-radius:12px;display:flex;flex-direction:column;gap:6px;font-size:12px;box-shadow:0 2px 4px rgba(0,0,0,.15);} 
.pf-line{display:flex;align-items:center;justify-content:space-between;font-weight:600;letter-spacing:.3px;color:#f8fafc;}
.pf-count{font-variant-numeric:tabular-nums;}
.pf-bar-wrap{height:6px;border-radius:4px;overflow:hidden;background:rgba(255,255,255,.25);}
.pf-bar-bg{position:relative;width:100%;height:100%;}
.pf-bar-fg{position:absolute;inset:0;background:linear-gradient(90deg,#34d399,#10b981);transition:width .35s ease;}
.stage-sidebar.collapsed .progress-footer{display:none;}
/* 响应式：顶部横排时放到右侧或隐藏，根据需要暂隐藏 */
@media (max-width:1100px){
  .progress-footer{order:5;width:200px;flex-shrink:0;}
}
@media (max-width:800px){
  .stage-item{min-width:110px;padding:6px 8px;}
  .section-head{padding:14px 16px;}
  .section-body{padding:16px 16px 18px;}
  .exp-title-btn{padding:8px 10px;font-size:13px;}
}
@media (max-width:640px){
  .answer-head .btn-group{width:100%;justify-content:space-between;}
  .btn{padding:8px 12px;}
  .modal-header{padding:16px 18px;}
  .close-btn{width:36px;height:36px;border-radius:12px;}
  .modal-body{padding:18px 18px 22px;}
}
@media (max-width:480px){
  .answer-head .btn-group{flex-direction:column;gap:8px;}
  .answer-head .btn-group .btn{flex:1;width:100%;justify-content:center;}
  .footer-line{flex-direction:column;align-items:flex-start;}
  .stage-item{min-width:100px;}
  .exp-title-btn{font-size:12px;}
}
</style>
