<script setup lang="ts">
import * as echarts from 'echarts/core'
import {BarChart,LineChart,PieChart} from 'echarts/charts'
import {GridComponent,LegendComponent,TooltipComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import {computed,onBeforeUnmount,onMounted,ref,watch} from 'vue'
import {buildChartOption,type ChartKind,type ChartSeriesInput} from '@/features/chartOptions'
const props=withDefaults(defineProps<{title:string;labels:string[];values?:number[];color?:string;type?:ChartKind;series?:ChartSeriesInput[];loading?:boolean}>(),{type:'bar',values:()=>[],series:()=>[],loading:false})
echarts.use([BarChart,LineChart,PieChart,GridComponent,LegendComponent,TooltipComponent,CanvasRenderer])
const element=ref<HTMLDivElement>(),empty=computed(()=>props.series.length?props.series.every(x=>x.values.length===0):props.values.length===0);let chart:echarts.ECharts|undefined
function render(){if(!element.value||props.loading||empty.value)return;chart??=echarts.init(element.value);chart.setOption(buildChartOption({type:props.type,title:props.title,labels:props.labels,values:props.values,color:props.color,series:props.series}),true)}
const resize=()=>chart?.resize()
onMounted(()=>{render();window.addEventListener('resize',resize)});watch(()=>[props.type,props.labels,props.values,props.series,props.loading],render,{deep:true});onBeforeUnmount(()=>{window.removeEventListener('resize',resize);chart?.dispose();chart=undefined})
</script>
<template><article class="chart-card" :data-chart-type="type"><header><span>{{title}}</span><small>LIVE DATA · {{type.toUpperCase()}}</small></header><div v-if="loading" class="chart-state">图表加载中…</div><div v-else-if="empty" class="chart-state">暂无可用数据</div><div v-else ref="element" class="chart"></div></article></template>
