<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
const props = defineProps<{ title: string; labels: string[]; values: number[]; color?: string }>()
const element = ref<HTMLDivElement>()
let chart: echarts.ECharts | undefined
function render() {
  if (!element.value) return
  chart ??= echarts.init(element.value)
  chart.setOption({ backgroundColor: 'transparent', grid: { left: 42, right: 18, top: 38, bottom: 30 }, tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: props.labels, axisLabel: { color: '#8195a9' }, axisLine: { lineStyle: { color: '#26394b' } } }, yAxis: { type: 'value', axisLabel: { color: '#8195a9' }, splitLine: { lineStyle: { color: '#162838' } } }, series: [{ name: props.title, type: 'bar', data: props.values, itemStyle: { color: props.color ?? '#29c7e8', borderRadius: [4, 4, 0, 0] } }] })
}
const resize = () => chart?.resize()
onMounted(() => { render(); window.addEventListener('resize', resize) })
watch(() => [props.labels, props.values], render, { deep: true })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })
</script>
<template><article class="chart-card"><header><span>{{ title }}</span><small>LIVE DATA</small></header><div ref="element" class="chart"></div></article></template>
