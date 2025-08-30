<!-- 已移除：分组管理独立页面内容。该功能已整合进 TeacherHome.vue 的 'groups' 栏目，不再单独使用。
如需恢复旧实现，请从版本控制历史检出。 -->
<script setup lang="ts">
// 占位：避免误访问时出现空白错误，可选择自动跳转教师主页。
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'
const router = useRouter()
onMounted(()=>{ router.replace({ name: 'teacher-home' }) })
</script>
<template>
  <div class="deprecated-group-manage" />
</template>
<style scoped>
.deprecated-group-manage { display:none; }
</style>
