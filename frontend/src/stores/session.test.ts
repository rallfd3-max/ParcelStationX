import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useSessionStore } from './session'

describe('session store', () => {
  beforeEach(() => { setActivePinia(createPinia()); vi.unstubAllGlobals() })
  it('stores authenticated user from real API envelope', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: true, data: { token: 'abc', user: { id: 1, username: 'admin', displayName: 'Admin', role: 'ADMIN' } }, message: null, code: null }), { status: 200 })))
    const store = useSessionStore(); store.initializeClient(); await store.login('admin', 'secret')
    expect(store.authenticated).toBe(true); expect(store.isAdmin).toBe(true)
  })
  it('clears invalid restored sessions', async () => {
    const store = useSessionStore(); store.token = 'expired'; store.initializeClient()
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ success: false, data: null, message: '请登录', code: 'UNAUTHORIZED' }), { status: 401 })))
    await expect(store.loadMe()).resolves.toBe(false); expect(store.token).toBe('')
  })
})
