import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiRequest, setTokenProvider } from './client'

describe('api client', () => {
  afterEach(() => vi.unstubAllGlobals())
  it('returns successful envelope data and sends bearer token', async () => {
    setTokenProvider(() => 'token-1')
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, data: { value: 7 }, message: null, code: null }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    vi.stubGlobal('fetch', fetchMock)
    await expect(apiRequest<{ value: number }>('/api/value')).resolves.toEqual({ value: 7 })
    expect((fetchMock.mock.calls[0]?.[1] as RequestInit).headers).toSatisfy((headers: Headers) => headers.get('Authorization') === 'Bearer token-1')
  })
  it('maps server errors', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: false, data: null, message: '冲突', code: 'CONFLICT' }), { status: 409 })))
    await expect(apiRequest('/api/value')).rejects.toEqual(new ApiError('冲突', 409, 'CONFLICT'))
  })
})
