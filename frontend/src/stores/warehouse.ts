import { defineStore } from 'pinia'
import { ApiError, apiRequest } from '@/api/client'
import type { Parcel, RelocateResponse, ShelfSlot, WarehouseSnapshot } from '@/types/api'

export const useWarehouseStore = defineStore('warehouse', {
  state: () => ({
    snapshot: null as WarehouseSnapshot | null,
    selectedParcelId: null as number | null,
    draggingParcelId: null as number | null,
    pendingParcelId: null as number | null,
    query: '', loading: false, error: '', notice: '', lastSyncAt: 0,
  }),
  getters: {
    selectedParcel(state): Parcel | undefined { return state.snapshot?.parcels.find(p => p.id === state.selectedParcelId) },
    waitingParcels(state): Parcel[] { return state.snapshot?.parcels.filter(p => p.status === 'IN_STOCK' && p.slotId === null) ?? [] },
    filteredParcels(state): Parcel[] {
      const query = state.query.trim().toLowerCase()
      return state.snapshot?.parcels.filter(p => !query || p.trackingNo.toLowerCase().includes(query) || p.courierCompany.toLowerCase().includes(query)) ?? []
    },
  },
  actions: {
    async refresh() {
      this.loading = true; this.error = ''
      try { this.snapshot = await apiRequest<WarehouseSnapshot>('/api/warehouse'); this.lastSyncAt = Date.now() }
      catch (error) { this.error = error instanceof Error ? error.message : '仓库数据加载失败。' }
      finally { this.loading = false }
    },
    selectParcel(id: number) { this.selectedParcelId = id },
    isSlotAvailable(slot: ShelfSlot, parcelId?: number) {
      if (!slot.enabled) return false
      return !this.snapshot?.parcels.some(p => p.slotId === slot.id && p.id !== parcelId && p.status !== 'PICKED_UP')
    },
    async relocate(parcelId: number, targetSlotId: number, reason = '2d warehouse move') {
      if (!this.snapshot || this.pendingParcelId !== null) return false
      const parcel = this.snapshot.parcels.find(p => p.id === parcelId)
      const target = this.snapshot.slots.find(s => s.id === targetSlotId)
      if (!parcel || !target || !this.isSlotAvailable(target, parcelId) || parcel.status !== 'IN_STOCK') { this.error = '目标仓位不可用。'; return false }
      const original = { ...parcel }
      parcel.slotId = target.id; parcel.shelfId = target.shelfId
      this.pendingParcelId = parcelId; this.error = ''; this.notice = ''
      try {
        const result = await apiRequest<RelocateResponse>(`/api/parcels/${parcelId}/relocate`, { method: 'POST', body: JSON.stringify({ targetSlotId, expectedVersion: original.version, reason }) })
        Object.assign(parcel, result.parcel); this.notice = `已移动到 ${target.slotCode}`; return true
      } catch (error) {
        Object.assign(parcel, original)
        if (error instanceof ApiError && error.status === 409) { await this.refresh(); this.error = '仓位状态已变化，已刷新服务器状态。' }
        else this.error = error instanceof Error ? error.message : '移动失败，已恢复原位置。'
        return false
      } finally { this.pendingParcelId = null; this.draggingParcelId = null }
    },
  },
})
