import { createRouter, createWebHistory } from 'vue-router'
import { useSessionStore } from '@/stores/session'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', name: 'dashboard', component: DashboardView },
    { path: '/warehouse', name: 'warehouse', component: PlaceholderView, props: { title: '仓库作业中心', phase: 'Phase 4' } },
    { path: '/digital-twin', name: 'digital-twin', component: PlaceholderView, props: { title: '数字孪生', phase: 'Phase 5' } },
    { path: '/parcels', name: 'parcels', component: PlaceholderView, props: { title: '快件查询', phase: 'Phase 4' } },
    { path: '/exceptions', name: 'exceptions', component: PlaceholderView, props: { title: '异常件', phase: 'Phase 7' } },
    { path: '/analytics', name: 'analytics', component: PlaceholderView, props: { title: '数据分析', phase: 'Phase 7' } },
    { path: '/settings', name: 'settings', component: PlaceholderView, props: { title: '系统设置', phase: 'Phase 8' }, meta: { admin: true } },
  ],
})

router.beforeEach(async (to) => {
  const session = useSessionStore()
  session.initializeClient()
  if (to.meta.public) return session.authenticated ? '/dashboard' : true
  if (!session.currentUser && session.token) await session.loadMe()
  if (!session.authenticated) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.meta.admin && !session.isAdmin) return '/dashboard'
  return true
})
