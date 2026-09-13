<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/session'

const username = ref('admin')
const password = ref('')
const session = useSessionStore()
const route = useRoute()
const router = useRouter()
async function submit() {
  if (!username.value.trim() || !password.value) { session.error = '请输入用户名和密码。'; return }
  try { await session.login(username.value, password.value); await router.push(String(route.query.redirect ?? '/dashboard')) } catch { /* store displays error */ }
}
</script>

<template>
  <section class="login-page">
    <div class="login-visual"><p class="eyebrow">SMART PARCEL STATION · 02</p><h1>让每一个快件<br><em>在空间中可见。</em></h1><p>业务数据、二维仓位与三维现场共享同一个事实源。</p><div class="scan-line"></div></div>
    <form class="login-card" @submit.prevent="submit">
      <p class="eyebrow">SECURE ACCESS</p><h2>登录控制台</h2><p class="muted">使用数据库中的员工账号继续</p>
      <label>用户名<input v-model="username" autocomplete="username" autofocus /></label>
      <label>密码<input v-model="password" type="password" autocomplete="current-password" /></label>
      <p v-if="session.error" class="error" role="alert">{{ session.error }}</p>
      <button class="primary" :disabled="session.loading">{{ session.loading ? '正在验证…' : '进入数字孪生' }}</button>
      <small>连接状态由 Java API 实时验证</small>
    </form>
  </section>
</template>
