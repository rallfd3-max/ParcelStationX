<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useWarehouseStore } from '@/stores/warehouse'
import type { ShelfSlot } from '@/types/api'
const store = useWarehouseStore()
const route = useRoute()
const moveTarget = ref<number | null>(null)
onMounted(async () => { await store.refresh(); const requested=Number(route.query.parcelId); if(requested) store.selectParcel(requested) })
const shelves = computed(() => store.snapshot?.shelves ?? [])
const visibleWaiting = computed(() => store.waitingParcels.filter(p => !store.query || p.trackingNo.toLowerCase().includes(store.query.toLowerCase())))
function parcelAt(slotId: number) { return store.filteredParcels.find(p => p.slotId === slotId && p.status !== 'PICKED_UP') }
function slotsFor(shelfId: number) { return store.snapshot?.slots.filter(s => s.shelfId === shelfId).sort((a,b) => a.levelIndex-b.levelIndex || a.columnIndex-b.columnIndex) ?? [] }
function dragStart(id: number, event: DragEvent) { store.draggingParcelId = id; store.selectParcel(id); event.dataTransfer?.setData('text/plain', String(id)); if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move' }
async function drop(slot: ShelfSlot, event: DragEvent) { const id = Number(event.dataTransfer?.getData('text/plain') || store.draggingParcelId); if (id && store.isSlotAvailable(slot, id)) await store.relocate(id, slot.id) }
async function moveSelected() { if (store.selectedParcelId && moveTarget.value) await store.relocate(store.selectedParcelId, moveTarget.value, 'accessible move dialog') }
</script>
<template>
  <section class="page warehouse-page">
    <header class="page-heading"><div><p class="eyebrow">WAREHOUSE CONTROL</p><h1>二维仓位作业中心</h1><p>拖拽仅为预览，服务器事务成功后才确认位置</p></div><button class="ghost" @click="store.refresh">刷新现场</button></header>
    <div class="warehouse-toolbar"><input v-model="store.query" placeholder="搜索运单号或快递公司" aria-label="搜索快件" /><span v-if="store.notice" class="success" aria-live="polite">{{ store.notice }}</span><span v-if="store.error" class="error" role="alert">{{ store.error }}</span></div>
    <div v-if="store.loading && !store.snapshot" class="state-panel">正在加载仓库快照…</div>
    <div v-else-if="store.snapshot" class="warehouse-layout">
      <aside class="waiting-panel"><header><strong>待上架</strong><span>{{ visibleWaiting.length }}</span></header><div v-if="!visibleWaiting.length" class="empty">暂无待上架快件</div><button v-for="parcel in visibleWaiting" :key="parcel.id" class="parcel-card" draggable="true" @dragstart="dragStart(parcel.id, $event)" @click="store.selectParcel(parcel.id)"><b>{{ parcel.trackingNo }}</b><small>{{ parcel.courierCompany }} · v{{ parcel.version }}</small></button></aside>
      <div class="shelf-canvas"><article v-for="shelf in shelves" :key="shelf.id" class="shelf-block"><header><div><b>{{ shelf.shelfCode }}</b><small>{{ shelf.zone }}</small></div><span>{{ shelf.occupied }}/{{ shelf.capacity }}</span></header><div class="slot-grid"><button v-for="slot in slotsFor(shelf.id)" :key="slot.id" class="slot" :class="{ occupied: parcelAt(slot.id), disabled: !slot.enabled, available: store.draggingParcelId && store.isSlotAvailable(slot, store.draggingParcelId), invalid: store.draggingParcelId && !store.isSlotAvailable(slot, store.draggingParcelId) }" :disabled="!slot.enabled" @dragover.prevent @drop.prevent="drop(slot, $event)" @click="parcelAt(slot.id) && store.selectParcel(parcelAt(slot.id)!.id)"><small>{{ slot.slotCode.split('-').slice(-2).join('-') }}</small><b v-if="parcelAt(slot.id)">{{ parcelAt(slot.id)?.trackingNo }}</b><span v-else>{{ store.draggingParcelId ? (store.isSlotAvailable(slot, store.draggingParcelId) ? '可放置' : '不可放') : '空闲' }}</span></button></div></article></div>
      <aside class="detail-panel"><template v-if="store.selectedParcel"><p class="eyebrow">SELECTED PARCEL</p><h2>{{ store.selectedParcel.trackingNo }}</h2><dl><dt>快递公司</dt><dd>{{ store.selectedParcel.courierCompany }}</dd><dt>状态</dt><dd>{{ store.selectedParcel.status }}</dd><dt>仓位</dt><dd>{{ store.snapshot.slots.find(s => s.id === store.selectedParcel?.slotId)?.slotCode ?? '待上架' }}</dd><dt>到站时间</dt><dd>{{ store.selectedParcel.arrivedAt.replace('T', ' ') }}</dd><dt>版本</dt><dd>{{ store.selectedParcel.version }}</dd><dt>备注</dt><dd>{{ store.selectedParcel.remark || '—' }}</dd></dl><label>移动到…<select v-model="moveTarget"><option :value="null">选择空闲仓位</option><option v-for="slot in store.snapshot.slots.filter(s => store.isSlotAvailable(s, store.selectedParcel?.id))" :key="slot.id" :value="slot.id">{{ slot.slotCode }}</option></select></label><button class="primary" :disabled="!moveTarget || store.pendingParcelId !== null" @click="moveSelected">确认移动</button></template><div v-else class="empty">选择一个快件查看详情</div></aside>
    </div>
  </section>
</template>
