export interface ApiEnvelope<T> {
  success: boolean
  data: T
  message: string | null
  code: string | null
}

export interface User {
  id: number
  username: string
  displayName: string
  role: 'ADMIN' | 'STAFF'
}

export interface LoginResponse { token: string; user: User }

export interface DashboardSummary {
  todayInbound: number
  todayOutbound: number
  inventory: number
  exceptions: number
  overdue: number
  slotUtilization: number
  courierVolumes: Record<string, number>
  shelfOccupancy: Record<string, number>
}

export interface Shelf { id: number; shelfCode: string; zone: string; capacity: number; occupied: number; status: 'ACTIVE' | 'DISABLED' }
export interface ShelfLayout { shelfId: number; positionX: number; positionY: number; positionZ: number; rotationY: number; width: number; height: number; depth: number; columns: number; levels: number }
export interface ShelfSlot { id: number; shelfId: number; slotCode: string; levelIndex: number; columnIndex: number; enabled: boolean }
export interface Parcel { id: number; trackingNo: string; courierCompany: string; customerId: number; shelfId: number | null; slotId: number | null; status: string; arrivedAt: string; pickedUpAt: string | null; operatorId: number; remark: string; version: number }
export interface Relocation { id: number; parcelId: number; fromSlotId: number | null; newSlotId: number; operatorId: number; reason: string; createdAt: string }
export interface WarehouseSnapshot { shelves: Shelf[]; layouts: ShelfLayout[]; slots: ShelfSlot[]; parcels: Parcel[] }
export interface RelocateResponse { parcel: Parcel; relocation: Relocation }
