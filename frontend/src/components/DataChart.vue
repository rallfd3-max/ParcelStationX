<script setup lang="ts">
import * as echarts from 'echarts/core'
import {BarChart,LineChart,PieChart} from 'echarts/charts'
import {GridComponent,LegendComponent,TooltipComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import {computed,nextTick,onBeforeUnmount,onMounted,ref,watch} from 'vue'
import {buildChartOption,type ChartKind,type ChartSeriesInput} from '@/features/chartOptions'
const props=withDefaults(defineProps<{title:string;labels:string[];values?:number[];color?:string;type?:ChartKind;series?:ChartSeriesInput[];loading?:boolean}>(),{type:'bar',values:()=>[],series:()=>[],loading:false})
echarts.use([BarChart,LineChart,PieChart,GridComponent,LegendComponent,TooltipComponent,CanvasRenderer])
const container=ref<HTMLDivElement>(),element=ref<HTMLDivElement>(),empty=computed(()=>props.series.length?props.series.every(x=>x.values.length===0):props.values.length===0)
let chart:echarts.ECharts|undefined,resizeObserver:ResizeObserver|undefined,resizeFrame:number|undefined
function ensureChart(){const dom=element.value;if(!dom)return;if(chart&&chart.getDom()!==dom){chart.dispose();chart=undefined}chart=echarts.getInstanceByDom(dom)??chart??echarts.init(dom)}
function render(){if(!element.value||props.loading||empty.value)return;ensureChart();chart?.setOption(buildChartOption({type:props.type,title:props.title,labels:props.labels,values:props.values,color:props.color,series:props.series}),true)}
function scheduleResize(){if(resizeFrame!==undefined)cancelAnimationFrame(resizeFrame);resizeFrame=requestAnimationFrame(()=>{resizeFrame=undefined;chart?.resize()})}
const resize=()=>scheduleResize()
async function refreshChart(){await nextTick();render();scheduleResize()}
onMounted(()=>{refreshChart();window.addEventListener('resize',resize);if(container.value&&typeof ResizeObserver!=='undefined'){resizeObserver=new ResizeObserver(scheduleResize);resizeObserver.observe(container.value)}})
watch(()=>[props.type,props.labels,props.values,props.series,props.loading],refreshChart,{deep:true,flush:'post'})
onBeforeUnmount(()=>{resizeObserver?.disconnect();resizeObserver=undefined;window.removeEventListener('resize',resize);if(resizeFrame!==undefined)cancelAnimationFrame(resizeFrame);chart?.dispose();chart=undefined})
</script>
<template><article class="chart-card" :data-chart-type="type"><header><span>{{title}}</span><small>LIVE DATA · {{type.toUpperCase()}}</small></header><div ref="container" class="chart-container"><div ref="element" class="chart"></div><div v-if="loading" class="chart-overlay">图表加载中…</div><div v-else-if="empty" class="chart-overlay">暂无可用数据</div></div></article></template>
