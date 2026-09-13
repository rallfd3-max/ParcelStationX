<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()
session.initializeClient()
const navigation = computed(() => [
  { to: '/dashboard', label: '首页' },
  { to: '/warehouse', label: '仓库作业' },
  { to: '/digital-twin', label: '数字孪生' },
  { to: '/parcels', label: '快件' },
  { to: '/exceptions', label: '异常件' },
  ...(session.isAdmin ? [{ to: '/settings', label: '系统设置' }] : []),
])
async function logout() { await session.logout(); await router.push('/login') }
</script>

<template>
  <div class="app-shell">
    <header v-if="session.authenticated" class="topbar">
      <RouterLink class="brand" to="/dashboard"><span class="brand-mark">PX</span><span>ParcelStationX<small> DIGITAL TWIN</small></span></RouterLink>
      <nav aria-label="主导航"><RouterLink v-for="item in navigation" :key="item.to" :to="item.to">{{ item.label }}</RouterLink></nav>
      <div class="user-chip"><span class="status-dot"></span>{{ session.currentUser?.displayName }} · {{ session.currentUser?.role }}<button class="ghost" @click="logout">退出</button></div>
    </header>
    <main :class="{ 'auth-main': !session.authenticated }"><RouterView /></main>
  </div>
</template>
