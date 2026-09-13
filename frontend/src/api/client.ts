import type { ApiEnvelope } from '@/types/api'

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code: string,
  ) {
    super(message)
  }
}

let tokenProvider = () => ''
export const setTokenProvider = (provider: () => string) => { tokenProvider = provider }

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = tokenProvider()
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(path, { ...options, headers })
  let envelope: ApiEnvelope<T>
  try {
    envelope = await response.json() as ApiEnvelope<T>
  } catch {
    throw new ApiError('服务器返回了无法解析的响应。', response.status, 'INVALID_RESPONSE')
  }
  if (!response.ok || !envelope.success) {
    throw new ApiError(envelope.message ?? '请求失败。', response.status, envelope.code ?? 'API_ERROR')
  }
  return envelope.data
}
