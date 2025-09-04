<script setup lang="ts">
import ProgressSegments from './ProgressSegments.vue'

export interface ExperimentItem { id:number; title:string; deadline:string; totalStages:number; finishedStages:number; progress:number }

const props = defineProps<{ list:ExperimentItem[]; loading:boolean; error:string }>()
const emit = defineEmits<{ (e:'refresh'):void; (e:'open', id:number):void }>()

function formatDeadline(raw:string){
  if(!raw || raw==='-') return '-'
  const d = new Date(raw)
  if(isNaN(d.getTime())) return raw
  const pad = (n:number)=> n.toString().padStart(2,'0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>
<template>
  <section class="panel fade-in">
    <div class="exp-head">
      <h3>实验列表</h3>
      <button class="text-btn" @click="emit('refresh')" :disabled="loading">刷新</button>
    </div>
    <div v-if="loading" class="loading-line">加载中...</div>
    <div v-else-if="error" class="error-line">{{ error }} <button class="text-btn" @click="emit('refresh')">重试</button></div>
    <div v-else-if="!list.length" class="empty">暂无未完成实验</div>
    <ul v-else class="exp-list">
      <li v-for="exp in list" :key="exp.id" class="exp-item">
        <div class="main">
          <div class="title">{{ exp.title }}</div>
          <div class="meta">截止：{{ formatDeadline(exp.deadline) }}</div>
          <ProgressSegments :total="exp.totalStages" :finished="exp.finishedStages" />
          <!-- 修改：仅展示已完成数量 / 总数量 -->
          <div class="progress-line" v-if="exp.totalStages>0">
            <span v-if="exp.finishedStages < exp.totalStages" class="stage-info">
              已完成 <strong>{{ exp.finishedStages }}</strong> / {{ exp.totalStages }} 阶段
            </span>
            <span v-else class="stage-info done">已完成全部 {{ exp.totalStages }} 阶段</span>
            <span class="percent" :aria-label="'进度 ' + exp.progress + '%'">{{ exp.progress }}%</span>
          </div>
        </div>
        <div class="actions">
          <button class="primary small" @click="emit('open', exp.id)">进入实验</button>
        </div>
      </li>
    </ul>
  </section>
</template>
<style scoped>
.exp-head{display:flex;align-items:center;justify-content:space-between;gap:16px;}
.exp-head h3{margin:0;font-size:18px;font-weight:600;}
.exp-list{list-style:none;padding:0;margin:16px 0 0;display:flex;flex-direction:column;gap:16px;}
.exp-item{display:flex;gap:24px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:14px;padding:16px 18px;align-items:center;}
.exp-item .main{flex:1;display:flex;flex-direction:column;gap:8px;}
.exp-item .title{font-size:15px;font-weight:600;color:#1e293b;}
.exp-item .meta{font-size:12px;color:#64748b;}
/* 新增文字进度样式 */
.progress-line{display:flex;align-items:center;gap:14px;font-size:12px;color:#475569;flex-wrap:wrap;margin-top:2px;}
.progress-line .stage-info strong{color:#2563eb;}
.progress-line .stage-info.done{color:#10b981;font-weight:600;}
.progress-line .percent{margin-left:auto;font-weight:600;color:#334155;}
.actions{display:flex;align-items:center;}
button.primary.small{background:#2563eb;color:#fff;border:none;border-radius:8px;padding:8px 14px;font-size:13px;cursor:pointer;font-weight:600;letter-spacing:.5px;transition:.25s;}
button.primary.small:hover{background:#1d4ed8;box-shadow:0 4px 12px -4px rgba(37,99,235,.45);}
</style>
