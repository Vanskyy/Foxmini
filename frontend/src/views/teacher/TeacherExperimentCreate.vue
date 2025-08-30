<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'

const router = useRouter()
function goBack(){
  if(window.confirm('你确定要放弃创建这个实验吗？未保存的内容将会丢失。')){
    router.push('/teacher')
  }
}
// 移除全局 title，改为阶段内字段
const sidebarCollapsed = ref(false)
function toggleSidebar(){ sidebarCollapsed.value = !sidebarCollapsed.value }
function handleResizeSidebar(){ if(window.innerWidth < 1100 && sidebarCollapsed.value){ sidebarCollapsed.value = false } }
// 为阶段增加 title 与 answer 字段
interface Stage { title: string; intro: string; content: string; answer: string; resources: string[] }
const stages = ref<Stage[]>([{ title:'', intro:'', content:'', answer:'', resources:[] }])
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
function addStage(){ if(stages.value.length >= 8){ window.alert('最多 8 个阶段'); return } stages.value.push({ title:'', intro:'', content:'', answer:'', resources:[] }); activeIndex.value = stages.value.length - 1; nextTick(()=>{ autoResize(introRef.value); setContentFullHeight() }) }
function addResource(){ const link = window.prompt('请输入资源链接 (例如 https://...)'); if(link && link.trim()){ currentStage.value.resources.push(link.trim()) } }
onMounted(()=>{ window.addEventListener('resize', handleResize); nextTick(()=>{ autoResize(introRef.value); setContentFullHeight() }) })
onBeforeUnmount(()=> window.removeEventListener('resize', handleResize))
</script>

<template>
  <div class="create-layout">
    <aside class="stage-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sb-top">
        <div class="left-group" v-if="!sidebarCollapsed">
          <button class="icon-btn back" @click="goBack">放弃</button>
        </div>
        <div class="right-group">
          <button class="icon-btn add" @click="addStage" :disabled="stages.length>=8" v-if="!sidebarCollapsed">＋</button>
          <button class="collapse-btn" @click="toggleSidebar" :aria-label="sidebarCollapsed? '展开侧边栏' : '收起侧边栏'">
            <svg class="chevron" viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6" /></svg>
          </button>
        </div>
      </div>
      <ul class="stage-single-list" v-if="!sidebarCollapsed">
        <li v-for="(s,i) in stages" :key="i" :class="['only', { active: i===activeIndex }]" @click="switchStage(i)">阶段 {{ i+1 }}</li>
      </ul>
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
          <div class="resource-box">
            <button type="button" class="resource-add-btn" @click="addResource">＋ 添加资源链接</button>
            <ul v-if="currentStage.resources.length" class="resource-list">
              <li v-for="(r,i) in currentStage.resources" :key="i"><a :href="r" target="_blank" rel="noopener">{{ r }}</a></li>
            </ul>
          </div>
        </div>
      </section>
      <section class="pane right">
        <header class="pane-head"><h2>作答区域预览</h2></header>
        <div class="pane-body code-preview-body">
          <textarea class="code-preview" v-model="currentStage.answer" spellcheck="false" placeholder="此处输入答案"></textarea>
        </div>
        <footer class="pane-foot"><div class="status">预览模式</div></footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* 保留原基础布局与侧边栏样式 */
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
.code-preview-body{padding:0;display:flex;flex:1;}
.code-preview{flex:1;border:none;margin:0;padding:18px 20px 22px;font-family:ui-monospace,monospace;line-height:1.5;font-size:13px;resize:none;outline:none;background:#0f172a;color:#e2e8f0;border-top:1px solid #1e293b;}
.code-preview:focus{box-shadow:inset 0 0 0 2px #6366f1;}
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
.resource-list{list-style:none;margin:0;padding:0;display:flex;flex-direction:column;gap:6px;font-size:12px;}
.resource-list li{background:#eef2ff;padding:6px 10px;border-radius:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
.resource-list a{color:#4338ca;text-decoration:none;}
.resource-list a:hover{text-decoration:underline;}
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
</style>
