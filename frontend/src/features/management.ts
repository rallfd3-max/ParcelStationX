import type { ExceptionRecord, ParcelDetails } from '@/types/api'

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
