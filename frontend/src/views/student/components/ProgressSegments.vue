<script setup lang="ts">
import { computed } from 'vue'
interface Props { total:number; finished:number }
const props = defineProps<Props>()
const percent = computed(()=> props.total? Math.round(props.finished/props.total*100):0)
</script>
<template>
  <div class="progress-wrapper" :title="finished + '/' + total">
    <div v-if="total>0" class="progress-bar segmented">
      <div v-for="i in total" :key="i" :class="['seg',{done:i<=finished}]" />
    </div>
    <div v-else class="progress-bar empty"><div class="inner" style="width:0"></div></div>
    <div class="progress-text">{{ finished }}/{{ total }} ({{ percent }}%)</div>
  </div>
</template>
<style scoped>
.progress-wrapper{display:flex;flex-direction:column;gap:6px;}
.progress-text{font-size:12px;color:#475569;font-weight:500;}
.progress-bar{height:8px;background:#e2e8f0;border-radius:4px;overflow:hidden;position:relative;}
.progress-bar.segmented{display:flex;gap:4px;background:transparent;height:auto;}
.progress-bar.segmented .seg{flex:1;height:8px;background:#e2e8f0;border-radius:4px;position:relative;overflow:hidden;transition:background .25s,opacity .25s;opacity:.55;}
.progress-bar.segmented .seg.done{background:linear-gradient(90deg,#3b82f6,#2563eb);box-shadow:0 0 0 1px #1d4ed8 inset;opacity:1;}
.progress-bar.segmented .seg.done::after{content:"";position:absolute;inset:0;background:linear-gradient(180deg,rgba(255,255,255,.4),rgba(255,255,255,0));mix-blend-mode:overlay;pointer-events:none;}
.progress-bar.segmented:hover .seg:not(.done){opacity:.7;}
</style>
