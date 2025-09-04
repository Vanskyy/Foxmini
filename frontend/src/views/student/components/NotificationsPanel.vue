<script setup lang="ts">
interface Notice { id:number; title:string; time:string }
const props = defineProps<{ notices:Notice[]; loading?:boolean; error?:string }>()
const emit = defineEmits<{(e:'read', id:number):void;(e:'read-all'):void}>()
</script>
<template>
  <section class="panel fade-in">
    <div class="notify-head">
      <h3>我的通知</h3>
      <button v-if="notices.length" class="text-btn" @click="emit('read-all')">全部标记已读</button>
    </div>
    <div v-if="loading" class="loading-line">加载中...</div>
    <div v-else-if="error" class="error-line">{{ error }}</div>
    <div v-else-if="!notices.length" class="empty">暂无通知</div>
    <ul v-else class="notice-list">
      <li v-for="n in notices" :key="n.id" class="notice-item">
        <div class="n-main">
          <div class="n-title">{{ n.title }}</div>
          <div class="n-time">{{ n.time }}</div>
        </div>
        <button class="outline small" @click="emit('read', n.id)">已读</button>
      </li>
    </ul>
  </section>
</template>
<style scoped>
.notify-head{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-bottom:4px;}
.notice-list{list-style:none;padding:0;margin:12px 0 0;display:flex;flex-direction:column;gap:12px;}
.notice-item{display:flex;align-items:flex-start;gap:16px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:14px 16px;}
.n-main{flex:1;display:flex;flex-direction:column;gap:6px;}
.n-title{font-size:14px;font-weight:600;color:#1e293b;line-height:1.4;}
.n-time{font-size:12px;color:#64748b;}
</style>
