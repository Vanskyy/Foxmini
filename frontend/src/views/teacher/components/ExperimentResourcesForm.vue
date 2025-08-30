<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import type { ExperimentResourceCreateRequest, ResourceType } from '@/api/teacher'

export interface ResourceDraft extends ExperimentResourceCreateRequest { id?:number }

const resourceTypes: ResourceType[] = ['FILE','LINK','VIDEO','IMAGE','DOC']

const props = defineProps<{
  resources: ResourceDraft[]
  editable?: boolean
  title?: string
}>()

const emit = defineEmits<{
  (e:'add'):void
  (e:'remove', index:number):void
  (e:'update:resources', value:ResourceDraft[]):void
}>()

function add(){ emit('add') }
function remove(i:number){ emit('remove', i) }
function update(){ emit('update:resources', props.resources) }
</script>

<template>
  <div class="resources-wrapper">
    <div class="block-head">
      <h4>{{ title || '资源' }}</h4>
      <button v-if="editable" type="button" class="text-btn" @click="add">新增资源</button>
    </div>
    <div v-if="!resources.length" class="empty">暂无资源</div>
    <ul class="resources-draft">
      <li v-for="(r,i) in resources" :key="r.id || i" class="res-draft-item">
        <div class="sd-top">
          <strong>资源 {{ i+1 }}<span v-if="r.id" class="id-tag">#{{ r.id }}</span></strong>
          <button v-if="editable" type="button" class="mini-btn" @click="remove(i)">×</button>
        </div>
        <div class="two-cols">
          <div class="col">
            <label class="mini-label">名称</label>
            <input v-model="r.name" @input="update" :disabled="!editable" placeholder="资源名称" />
          </div>
          <div class="col">
            <label class="mini-label">类型</label>
            <select v-model="r.type" @change="update" :disabled="!editable">
              <option v-for="t in resourceTypes" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
        </div>
        <div class="two-cols">
          <div class="col">
            <label class="mini-label">URL</label>
            <input v-model="r.url" @input="update" :disabled="!editable" placeholder="可选" />
          </div>
          <div class="col">
            <label class="mini-label">大小(字节)</label>
            <input v-model.number="r.size" @input="update" :disabled="!editable" type="number" min="0" placeholder="可选" />
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.block-head { display:flex; align-items:center; justify-content:space-between; margin-top:6px; }
.block-head h4 { margin:0; font-size:14px; font-weight:600; color:#1e293b; }
.resources-draft { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:16px; }
.res-draft-item { background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:14px 16px; display:flex; flex-direction:column; gap:10px; }
.res-draft-item input { height:36px; }
.sd-top { display:flex; align-items:center; justify-content:space-between; }
.mini-btn { background:#e2e8f0; border:none; color:#475569; width:26px; height:26px; border-radius:8px; cursor:pointer; font-size:14px; font-weight:600; display:flex; align-items:center; justify-content:center; }
.mini-btn:hover { background:#cbd5e1; }
.empty { padding:10px 2px; font-size:13px; color:#64748b; }
.two-cols { display:flex; gap:12px; }
.two-cols .col { flex:1; display:flex; flex-direction:column; gap:4px; }
.mini-label { font-size:11px; font-weight:600; color:#475569; letter-spacing:.5px; }
select { height:36px; border:1px solid #cbd5e1; border-radius:8px; padding:0 10px; background:#fff; }
select:focus { border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,.15); outline:none; }
.id-tag { font-size:11px; color:#6366f1; font-weight:500; margin-left:4px; }
</style>
