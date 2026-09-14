import type { AiQueryResponse } from '@/types/api'

export function trustedFocusParcel(response: AiQueryResponse): number | null {
  if (response.action.type !== 'FOCUS_PARCEL' || !Number.isInteger(response.action.parcelId)) return null
  return response.results.some(item => item.parcelId === response.action.parcelId) ? response.action.parcelId : null
}
