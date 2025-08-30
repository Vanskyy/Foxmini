<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// 路由参数获取实验 ID
const route = useRoute()
const experimentId = computed(() => route.params.id as string)

// 实验阶段（后续对接后端）
interface Stage { id: number; title: string; desc?: string }
const stages = ref<Stage[]>([
  { id: 1, title: '阶段一：阅读实验说明', desc: '了解实验背景与要求。' },
  { id: 2, title: '阶段二：编写代码', desc: '在右侧编写并调试代码。' },
  { id: 3, title: '阶段三：提交与评测', desc: '提交答案并查看评测结果。' },
])
const activeStageId = ref<number>(1)
const activeStage = computed(() => stages.value.find(s => s.id === activeStageId.value))

// 题目/说明区域内容（简化）
const questionContent = computed(() => {
  switch(activeStageId.value){
    case 1: return `实验 ${experimentId.value} - 实验说明：\n1. 按要求阅读背景。\n2. 准备代码环境。`
    case 2: return '请在右侧代码区实现函数 solve()，并通过所给测试。'
    case 3: return '确认所有用例通过后点击下方提交按钮完成实验。'
    default: return ''
  }
})

// 每个阶段独立代码内容（后续可从后端加载历史保存）
const codes = reactive<Record<number,string>>({
  1: `// 阶段1 记录区\nfunction solve(){\n  // 阶段1无需代码，实现前置准备\n}\n`,
  2: `// 阶段2 编写核心算法\nfunction solve(){\n  // TODO: 编写算法逻辑\n  return 0;\n}\n`,
  3: `// 阶段3 最终整理与提交\nfunction solve(){\n  // 确保所有测试通过\n  return 0;\n}\n`
})

function runCode(){
  // 预留：调用后端运行/本地沙箱，传 activeStageId 与 codes[activeStageId]
  alert('运行代码（待实现）\n阶段: '+activeStageId.value)
}
function submit(){
  alert('提交实验（待实现）\n阶段: '+activeStageId.value)
}

const router = useRouter()
const sidebarCollapsed = ref(false)
function goBack(){ router.push('/student') }

onMounted(()=>{
  // TODO: 拉取实验详情、阶段、历史提交、各阶段初始代码
})
</script>

<template>
  <div class="exp-detail-layout">
    <aside :class="['stage-sidebar', { collapsed: sidebarCollapsed }]">
      <div class="sb-top">
        <button class="icon-btn back" @click="goBack" title="返回学生主页">
          <span v-if="!sidebarCollapsed">返回</span>
          <span v-else>‹</span>
        </button>
        <button class="icon-btn toggle" @click="sidebarCollapsed=!sidebarCollapsed" :title="sidebarCollapsed ? '展开阶段列表' : '收起阶段列表'">{{ sidebarCollapsed ? '›' : '‹' }}</button>
      </div>
      <ul class="stage-list">
        <li v-for="s in stages" :key="s.id" :class="['stage-item', { active: s.id===activeStageId }]" @click="activeStageId=s.id">
          <span class="dot"></span>
          <span v-if="!sidebarCollapsed" class="text">{{ s.title }}</span>
        </li>
      </ul>
    </aside>

    <div class="work-area">
      <section class="question-pane">
        <header class="q-header">
          <h2 class="q-title">{{ activeStage?.title }}</h2>
        </header>
        <pre class="q-content">{{ questionContent }}</pre>
      </section>
      <section class="answer-pane">
        <header class="a-header">
          <h2 class="a-title">作答区域（阶段 {{ activeStageId }}）</h2>
        </header>
        <textarea v-model="codes[activeStageId]" class="code-editor" spellcheck="false"></textarea>
        <footer class="a-footer">
          <div class="status">阶段 {{ activeStageId }} 草稿（演示）：未保存</div>
          <div class="a-actions">
            <button class="btn ghost" @click="runCode">运行</button>
            <button class="btn primary" @click="submit">提交</button>
          </div>
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
.exp-detail-layout { display:flex; height:100vh; background:#f1f5f9; }
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
.question-pane, .answer-pane { flex:1; display:flex; flex-direction:column; background:#fff; border:1px solid #e2e8f0; }
/* 分界线 */
.question-pane { border-right:2px solid #e2e8f0; }
.answer-pane { border-left:none; }
.q-header, .a-header { padding:14px 18px; border-bottom:1px solid #e2e8f0; display:flex; align-items:center; justify-content:space-between; gap:14px; background:#f8fafc; }
.q-title, .a-title { margin:0; font-size:15px; font-weight:600; color:#1e293b; }
.q-content { margin:0; padding:18px 20px 24px; font-size:13px; line-height:1.6; white-space:pre-wrap; color:#334155; flex:1; overflow:auto; background:#fff; }
.code-editor { flex:1; width:100%; border:none; border-top:1px solid #e2e8f0; font-family:ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; font-size:13px; line-height:1.5; padding:16px 18px; box-sizing:border-box; outline:none; resize:none; background:#0f172a; color:#e2e8f0; }
.code-editor:focus { box-shadow:inset 0 0 0 2px #2563eb; }
.a-footer { border-top:1px solid #e2e8f0; background:#f8fafc; padding:10px 16px; display:flex; align-items:center; justify-content:space-between; gap:16px; }
.a-footer .status { font-size:12px; color:#64748b; }
.a-actions { display:flex; gap:10px; }
.btn { border:none; cursor:pointer; font-size:13px; font-weight:600; padding:8px 16px; border-radius:8px; letter-spacing:.5px; display:inline-flex; align-items:center; gap:6px; }
.btn.ghost { background:#e2e8f0; color:#1e293b; }
.btn.ghost:hover { background:#cbd5e1; }
.btn.primary { background:#2563eb; color:#fff; }
.btn.primary:hover { background:#1d4ed8; }
@media (max-width:1100px){
  .exp-detail-layout { flex-direction:column; }
  .stage-sidebar { flex-direction:row; width:auto; height:60px; padding:10px 12px; }
  .stage-sidebar.collapsed { width:auto; }
  .sb-top { flex-direction:row; }
  .stage-list { flex-direction:row; }
  .stage-sidebar.collapsed .stage-item { width:48px; }
  .work-area { flex-direction:column; }
  .question-pane { border-right:0; border-bottom:2px solid #e2e8f0; }
  .answer-pane { border-left:1px solid #e2e8f0; }
}
</style>
