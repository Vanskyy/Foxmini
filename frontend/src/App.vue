<script setup lang="ts">
// 全局初始化逻辑可在此编写（如：权限、用户信息加载等）
</script>

<template>
  <div class="app-shell">
    <!-- 顶部导航 / 侧边栏 等可后续扩展 -->
    <router-view />
  </div>
</template>

<style>
html, body, #app { margin: 0; padding: 0; height: 100%; font-family: system-ui, Arial, sans-serif; }
.app-shell { min-height: 100%; display: flex; flex-direction: column; }
</style>
