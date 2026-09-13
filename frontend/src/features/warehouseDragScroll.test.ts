import { describe, expect, it, vi } from 'vitest'
import { calculateEdgeScroll, WarehouseDragScroller } from './warehouseDragScroll'

describe('warehouse drag edge scroll', () => {
  it('calculates proportional, capped direction and speed', () => {
    expect(calculateEdgeScroll(101, 100, 500)).toEqual({ direction: -1, speed: 22 * 59 / 60 })
    expect(calculateEdgeScroll(499, 100, 500)).toEqual({ direction: 1, speed: 22 * 59 / 60 })
    expect(calculateEdgeScroll(300, 100, 500)).toEqual({ direction: 0, speed: 0 })
    expect(calculateEdgeScroll(100, 100, 500).speed).toBe(22)
    expect(calculateEdgeScroll(500, 100, 500).speed).toBe(22)
    expect(calculateEdgeScroll(50, 100, 500)).toEqual({ direction: 0, speed: 0 })
  })

  it('scrolls on frames and cancels cleanly', () => {
    let callback: FrameRequestCallback | undefined
    const request = vi.fn((next: FrameRequestCallback) => { callback = next; return 7 })
    const cancel = vi.fn()
    const element = { scrollTop: 100, getBoundingClientRect: () => ({ top: 100, bottom: 500 }) as DOMRect }
    const scroller = new WarehouseDragScroller(element, request, cancel)
    scroller.update(499)
    expect(request).toHaveBeenCalledOnce()
    callback?.(0)
    expect(element.scrollTop).toBeGreaterThan(100)
    scroller.stop()
    expect(cancel).toHaveBeenCalledWith(7)
    const requestsAfterStop = request.mock.calls.length
    callback?.(16)
    expect(request).toHaveBeenCalledTimes(requestsAfterStop)
  })
})
