<script setup lang="ts">
import { computed, onMounted } from 'vue'
import DataChart from '@/components/DataChart.vue'
import MetricCard from '@/components/MetricCard.vue'
import { useDashboardStore } from '@/stores/dashboard'

const dashboard = useDashboardStore()
onMounted(() => dashboard.refresh())
const courierLabels = computed(() => Object.keys(dashboard.summary?.courierVolumes ?? {}))
const courierValues = computed(() => Object.values(dashboard.summary?.courierVolumes ?? {}))
const shelfLabels = computed(() => Object.keys(dashboard.summary?.shelfOccupancy ?? {}))
const shelfValues = computed(() => Object.values(dashboard.summary?.shelfOccupancy ?? {}))
</script>

<template>
  <section class="dashboard page">
    <header class="page-heading"><div><p class="eyebrow">OPERATIONS OVERVIEW</p><h1>驿站运行驾驶舱</h1><p>从 Java API 实时读取库存与仓位状态</p></div><button class="ghost" :disabled="dashboard.loading" @click="dashboard.refresh">刷新数据</button></header>
    <div v-if="dashboard.loading && !dashboard.summary" class="state-panel">正在同步现场数据…</div>
    <div v-else-if="dashboard.error && !dashboard.summary" class="state-panel error" role="alert">{{ dashboard.error }} <button @click="dashboard.refresh">重试</button></div>
    <template v-else-if="dashboard.summary">
      <div class="metrics">
        <MetricCard label="今日入库" :value="dashboard.summary.todayInbound" note="件 / TODAY" />
        <MetricCard label="今日出库" :value="dashboard.summary.todayOutbound" note="件 / TODAY" accent="#77e6a1" />
        <MetricCard label="当前库存" :value="dashboard.summary.inventory" note="件 / ON SITE" accent="#7aa7ff" />
        <MetricCard label="异常件" :value="dashboard.summary.exceptions" note="需要关注" accent="#ff6577" />
        <MetricCard label="滞留件" :value="dashboard.summary.overdue" note="超过 7 天" accent="#ffbe55" />
        <MetricCard label="仓位利用率" :value="`${dashboard.summary.slotUtilization}%`" note="ENABLED SLOTS" accent="#b58aff" />
      </div>
      <div class="dashboard-grid"><DataChart title="快递公司件量" :labels="courierLabels" :values="courierValues" /><DataChart title="货架占用率" :labels="shelfLabels" :values="shelfValues" color="#ff9b5e" /></div>
      <div v-if="courierLabels.length === 0" class="state-panel">暂无快件数据。完成入库后，这里会自动显示分布。</div>
    </template>
  </section>
</template>
