import { defineStore } from 'pinia'
import { apiRequest } from '@/api/client'
import type { DashboardActivity,DashboardDistributions,DashboardSummary,DashboardTrends } from '@/types/api'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({ summary:null as DashboardSummary|null,trends:null as DashboardTrends|null,distributions:null as DashboardDistributions|null,activity:null as DashboardActivity|null,loading:false,error:'' }),
  actions: {
    async refresh() {
      this.loading = true
      this.error = ''
      try { [this.summary,this.trends,this.distributions,this.activity]=await Promise.all([apiRequest<DashboardSummary>('/api/dashboard/summary'),apiRequest<DashboardTrends>('/api/dashboard/trends'),apiRequest<DashboardDistributions>('/api/dashboard/distributions'),apiRequest<DashboardActivity>('/api/dashboard/activity')]) }
      catch (error) { this.error = error instanceof Error ? error.message : '首页数据加载失败。' }
      finally { this.loading = false }
    },
  },
})
