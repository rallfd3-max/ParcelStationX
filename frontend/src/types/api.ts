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
  enabled?: boolean
  createdAt?: string
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
export interface DashboardTrends { points:Array<{date:string;inbound:number;outbound:number;inventory:number}> }
export interface DashboardDistributions { courier:Record<string,number>;status:Record<string,number>;exceptionType:Record<string,number>;shelfUtilization:Record<string,number>;zoneUtilization:Record<string,number>;dwell:Record<string,number> }
export interface DashboardActivity { recentExceptions:ExceptionRecord[];recentOperations:Array<{id:number;userId:number;operationType:string;targetType:string;targetId:number;description:string;createdAt:string}> }
export interface AiOperationsInsight { summary:string;risks:string[];recommendations:string[] }
export interface AiExceptionAdvice { suggestedType:string;riskLevel:'LOW'|'MEDIUM'|'HIGH';reason:string;steps:string[];suggestedResolution:string }
export interface AiQueryResult { parcelId:number|null;trackingNo:string;slotCode:string|null;shelfCode:string|null;status:string }
export interface AiQueryResponse { answer:string;results:AiQueryResult[];action:{type:'FOCUS_PARCEL'|'OPEN_PARCEL_DETAIL'|'NONE';parcelId:number|null} }

export interface Shelf { id: number; shelfCode: string; zone: string; capacity: number; occupied: number; status: 'ACTIVE' | 'DISABLED' }
export interface ShelfLayout { shelfId: number; positionX: number; positionY: number; positionZ: number; rotationY: number; width: number; height: number; depth: number; columns: number; levels: number }
export interface ShelfSlot { id: number; shelfId: number; slotCode: string; levelIndex: number; columnIndex: number; enabled: boolean }
export interface Parcel { id: number; trackingNo: string; courierCompany: string; customerId: number; shelfId: number | null; slotId: number | null; status: string; arrivedAt: string; pickedUpAt: string | null; operatorId: number; remark: string; version: number }
export interface Relocation { id: number; parcelId: number; fromSlotId: number | null; newSlotId: number; operatorId: number; reason: string; createdAt: string }
export interface ParcelEvent { id: number; parcelId: number; eventType: string; fromStatus: string | null; toStatus: string | null; operatorId: number; description: string; createdAt: string }
export interface ParcelDetails { parcel: Parcel; customer: {id:number;name:string;maskedMobile:string}|null; operator:string|null; shelfCode:string|null; slotCode:string|null; events:ParcelEvent[]; relocations:Relocation[]; pickupCode:string }
export interface ExceptionRecord { id:number; parcelId:number; exceptionType:string; description:string; status:string; createdBy:number; handledBy:number|null; createdAt:string; handledAt:string|null; resolution:string|null }
export interface WarehouseSnapshot { shelves: Shelf[]; layouts: ShelfLayout[]; slots: ShelfSlot[]; parcels: Parcel[] }
export interface ShelfCreationItem { shelf:Shelf;layout:ShelfLayout;slotCount:number }
export interface ShelfCreationResult { shelves:ShelfCreationItem[];shelfCount:number;slotCount:number;preview:boolean }
export interface WarehouseAgentPlan { id:string;createdAt:string;expiresAt:string;status:'PENDING'|'EXECUTING'|'CONSUMED'|'CANCELLED'|'EXPIRED';action:{intent:string;summary:string};preview:ShelfCreationResult }
export interface RelocateResponse { parcel: Parcel; relocation: Relocation }
