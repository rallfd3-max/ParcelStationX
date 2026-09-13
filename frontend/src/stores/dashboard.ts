import { defineStore } from 'pinia'
import { apiRequest } from '@/api/client'
import type { DashboardSummary } from '@/types/api'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({ summary: null as DashboardSummary | null, loading: false, error: '' }),
  actions: {
    async refresh() {
      this.loading = true
      this.error = ''
      try { this.summary = await apiRequest<DashboardSummary>('/api/dashboard/summary') }
      catch (error) { this.error = error instanceof Error ? error.message : '驾驶舱加载失败。' }
      finally { this.loading = false }
    },
  },
})
