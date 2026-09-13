export interface EdgeScrollResult { direction: -1 | 0 | 1; speed: number }

export function calculateEdgeScroll(pointerY: number, top: number, bottom: number, edge = 60, maxSpeed = 22): EdgeScrollResult {
  if (edge <= 0 || bottom <= top) return { direction: 0, speed: 0 }
  if (pointerY < top || pointerY > bottom) return { direction: 0, speed: 0 }
  if (pointerY < top + edge) {
    const ratio = Math.min(1, (top + edge - pointerY) / edge)
    return { direction: -1, speed: maxSpeed * ratio }
  }
  if (pointerY > bottom - edge) {
    const ratio = Math.min(1, (pointerY - (bottom - edge)) / edge)
    return { direction: 1, speed: maxSpeed * ratio }
  }
  return { direction: 0, speed: 0 }
}

type FrameRequest = (callback: FrameRequestCallback) => number
type FrameCancel = (id: number) => void

export class WarehouseDragScroller {
  private frame: number | null = null
  private speed = 0
  private direction: -1 | 0 | 1 = 0

  constructor(
    private readonly element: Pick<HTMLElement, 'scrollTop' | 'getBoundingClientRect'>,
    private readonly requestFrame: FrameRequest = requestAnimationFrame,
    private readonly cancelFrame: FrameCancel = cancelAnimationFrame,
  ) {}

  update(pointerY: number) {
    const { top, bottom } = this.element.getBoundingClientRect()
    const next = calculateEdgeScroll(pointerY, top, bottom)
    this.direction = next.direction
    this.speed = next.speed
    if (this.direction === 0) this.stopFrame()
    else if (this.frame === null) this.frame = this.requestFrame(this.tick)
  }

  stop() {
    this.direction = 0
    this.speed = 0
    this.stopFrame()
  }

  private stopFrame() {
    if (this.frame !== null) this.cancelFrame(this.frame)
    this.frame = null
  }

  private tick = () => {
    this.frame = null
    if (this.direction === 0) return
    this.element.scrollTop += this.direction * this.speed
    this.frame = this.requestFrame(this.tick)
  }
}
