export type DragPhase = 'idle' | 'dragging' | 'pending' | 'rolling-back'
export interface DragState { phase: DragPhase; parcelId: number | null; targetSlotId: number | null; valid: boolean }
export const idleDragState = (): DragState => ({ phase: 'idle', parcelId: null, targetSlotId: null, valid: false })
export function startDrag(parcelId: number): DragState { return { phase: 'dragging', parcelId, targetSlotId: null, valid: false } }
export function targetDrag(state: DragState, targetSlotId: number | null, valid: boolean): DragState { return state.phase === 'dragging' ? { ...state, targetSlotId, valid } : state }
export function submitDrag(state: DragState): DragState { return state.phase === 'dragging' && state.targetSlotId && state.valid ? { ...state, phase: 'pending' } : { ...state, phase: 'rolling-back' } }
