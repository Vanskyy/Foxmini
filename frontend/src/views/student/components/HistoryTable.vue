<script setup lang="ts">
import type { StudentExperimentHistoryItem } from '@/api/student'
import ProgressSegments from './ProgressSegments.vue'
const props = defineProps<{ list:StudentExperimentHistoryItem[]; loading:boolean; error:string }>()
</script>
<template>
  <section class="panel fade-in">
    <h3>历史实验记录</h3>
    <div v-if="loading" class="loading-line">加载中...</div>
    <div v-else-if="error" class="error-line">{{ error }}</div>
    <div v-else-if="!list.length" class="empty">暂无历史记录</div>
    <table v-else class="history-table">
      <thead><tr><th>ID</th><th>实验名称</th><th>进度</th><th>成绩</th><th>提交时间</th></tr></thead>
      <tbody>
        <tr v-for="r in list" :key="r.experimentId">
          <td>{{ r.experimentId }}</td>
            <td>{{ r.experimentTitle }}</td>
            <td>
              <ProgressSegments v-if="r.totalStages" :total="r.totalStages || 0" :finished="r.finishedStages || 0" />
              <span v-else class="no-stages">-</span>
            </td>
            <td><span class="score" :class="{ high:(r.latestScore||0)>=90, mid:(r.latestScore||0)>=75 && (r.latestScore||0)<90 }">{{ r.latestScore ?? '-' }}</span></td>
            <td>{{ r.submittedAt }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
<style scoped>
.history-table{width:100%;border-collapse:collapse;margin-top:14px;font-size:14px;}
.history-table th,.history-table td{padding:10px 12px;text-align:left;border-bottom:1px solid #e2e8f0;vertical-align:top;}
.history-table th{background:#f1f5f9;font-weight:600;font-size:12px;letter-spacing:.5px;color:#475569;text-transform:uppercase;}
.history-table tbody tr:hover{background:#f8fafc;}
.score{font-weight:600;}
.score.high{color:#16a34a;}
.score.mid{color:#2563eb;}
.no-stages{color:#94a3b8;font-size:12px;}
.history-table td:nth-child(3){min-width:160px;}
</style>
