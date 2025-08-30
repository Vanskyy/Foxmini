<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import type { StageType, ExperimentStageCreateRequest } from '@/api/teacher'

// 创建与编辑共用阶段草稿类型（包含本地扩展字段）
export interface StageDraft extends Omit<ExperimentStageCreateRequest,'content'|'type'> { title:string; type:StageType; content:string; hint?:string; maxScore?:number; evaluation?:any }

const stageTypes: StageType[] = ['CHOICE','FILL','CODE','TEXT']

const props = defineProps<{
  stages: StageDraft[]
  editable?: boolean
  title?: string
}>()

const emit = defineEmits<{
  (e:'add'):void
  (e:'remove', index:number):void
  (e:'update:stages', value: StageDraft[]):void
}>()

function add(){ emit('add') }
function remove(i:number){ emit('remove', i) }
function update(){ emit('update:stages', props.stages) }
</script>

<template>
  <div class="stages-wrapper">
    <div class="block-head">
      <h4>{{ title || '阶段' }}</h4>
      <button v-if="editable" type="button" class="text-btn" @click="add">新增阶段</button>
    </div>
    <div v-if="!stages.length" class="empty">暂无阶段</div>
    <ul class="stages-draft">
      <li v-for="(s,i) in stages" :key="s.id || i" class="stage-draft-item">
        <div class="sd-top">
          <strong>阶段 {{ i+1 }}<span v-if="(s as any).id" class="id-tag">#{{ (s as any).id }}</span></strong>
          <button v-if="editable && stages.length>1" type="button" class="mini-btn" @click="remove(i)">×</button>
        </div>
        <input v-model="s.title" @input="update" :disabled="!editable" placeholder="阶段标题" />
        <textarea v-model="s.description" @input="update" :disabled="!editable" rows="2" placeholder="阶段描述 (可选)"></textarea>
        <div class="two-cols">
          <div class="col">
            <label class="mini-label">类型</label>
            <select v-model="s.type" @change="update" :disabled="!editable">
              <option v-for="t in stageTypes" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div class="col">
            <label class="mini-label">满分</label>
            <input v-model.number="s.maxScore" @input="update" :disabled="!editable" type="number" min="0" placeholder="可选" />
          </div>
        </div>
        <textarea v-model="s.content" @input="update" :disabled="!editable" rows="3" class="codearea" placeholder="阶段核心内容 / 题干 / 说明"></textarea>
        <textarea v-model="s.hint" @input="update" :disabled="!editable" rows="2" placeholder="提示 (可选)"></textarea>
      </li>
    </ul>
  </div>
</template>

<style scoped>
/* 复用父级样式命名，尽量保持一致 */
.block-head { display:flex; align-items:center; justify-content:space-between; margin-top:6px; }
.block-head h4 { margin:0; font-size:14px; font-weight:600; color:#1e293b; }
.stages-draft { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:16px; }
.stage-draft-item { background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:14px 16px; display:flex; flex-direction:column; gap:10px; }
.stage-draft-item input { height:36px; }
.stage-draft-item textarea { border:1px solid #cbd5e1; border-radius:8px; padding:8px 10px; resize:vertical; font-size:13px; line-height:1.5; outline:none; background:#fff; }
.stage-draft-item textarea:focus { border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.sd-top { display:flex; align-items:center; justify-content:space-between; }
.mini-btn { background:#e2e8f0; border:none; color:#475569; width:26px; height:26px; border-radius:8px; cursor:pointer; font-size:14px; font-weight:600; display:flex; align-items:center; justify-content:center; }
.mini-btn:hover { background:#cbd5e1; }
.empty { padding:10px 2px; font-size:13px; color:#64748b; }
.two-cols { display:flex; gap:12px; }
.two-cols .col { flex:1; display:flex; flex-direction:column; gap:4px; }
.mini-label { font-size:11px; font-weight:600; color:#475569; letter-spacing:.5px; }
select { height:36px; border:1px solid #cbd5e1; border-radius:8px; padding:0 10px; background:#fff; }
select:focus { border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,.15); outline:none; }
.codearea { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace; }
.id-tag { font-size:11px; color:#6366f1; font-weight:500; margin-left:4px; }
</style>
