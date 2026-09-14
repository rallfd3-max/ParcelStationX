import type { ExceptionRecord, ParcelDetails, Shelf } from '@/types/api'

export function groupShelvesByZone(shelves: Shelf[]) {
  const groups = new Map<string, Shelf[]>()
  for (const shelf of [...shelves].sort((a,b)=>a.shelfCode.localeCompare(b.shelfCode,undefined,{numeric:true}))) {
    const zone=shelf.zone||'未分区';groups.set(zone,[...(groups.get(zone)??[]),shelf])
  }
  return [...groups.entries()].sort(([a],[b])=>a.localeCompare(b)).map(([zone,items])=>({zone,shelves:items}))
}

export function parcelFocusLocation(parcelId: number, threeDimensional = true) {
  return { path: threeDimensional ? '/digital-twin' : '/warehouse', query: { parcelId } }
}

export function canAccessSettings(role?: string) { return role === 'ADMIN' }

export function filterExceptions(items: ExceptionRecord[], query: string, status: string, type: string,
  tracking: (id: number) => string) {
  const normalized = query.trim().toLowerCase()
  return items.filter(item => (!normalized || [String(item.parcelId), item.description, tracking(item.parcelId)]
    .some(value => value.toLowerCase().includes(normalized)))
    && (!status || item.status === status) && (!type || item.exceptionType === type))
}

export function viewState(loading: boolean, error: string, size: number) {
  if (loading) return 'loading'; if (error) return 'error'; return size ? 'ready' : 'empty'
}

export function filterParcelDetails(items: ParcelDetails[], query: string) {
  const normalized = query.trim().toLowerCase()
  return items.filter(item => !normalized || [item.parcel.trackingNo, item.pickupCode,
    item.customer?.name, item.customer?.maskedMobile].some(value => value?.toLowerCase().includes(normalized)))
}
