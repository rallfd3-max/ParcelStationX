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
