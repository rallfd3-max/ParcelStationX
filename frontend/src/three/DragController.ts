import { Color, Mesh, Raycaster, Vector2, Vector3 } from 'three'
import type { Camera, Object3D } from 'three'
import type { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import type { SceneIndex } from './SceneIndex'
import { idleDragState, startDrag, submitDrag, targetDrag, type DragState } from './dragState'

export class DragController {
  state: DragState = idleDragState()
  private raycaster = new Raycaster()
  private pointer = new Vector2()
  private parcel?: Object3D
  private origin = new Vector3()
  private highlighted?: Mesh
  private originalColor?: Color

  constructor(private element: HTMLElement, private camera: Camera, private controls: OrbitControls, private index: SceneIndex, private available: (slotId:number, parcelId:number)=>boolean, private commit:(parcelId:number,slotId:number)=>Promise<boolean>, private select:(parcelId:number)=>void) {
    element.addEventListener('pointerdown', this.down)
    element.addEventListener('pointermove', this.move)
    element.addEventListener('pointerup', this.up)
    element.addEventListener('pointercancel', this.cancel)
  }
  isDragging(){return this.state.phase!=='idle'}
  private cast(event:PointerEvent, objects:Object3D[]) { const rect=this.element.getBoundingClientRect(); this.pointer.set((event.clientX-rect.left)/rect.width*2-1,-(event.clientY-rect.top)/rect.height*2+1); this.raycaster.setFromCamera(this.pointer,this.camera); return this.raycaster.intersectObjects(objects,true)[0]?.object }
  private down=(event:PointerEvent)=>{ const hit=this.cast(event,[...this.index.parcels.values()]); const id=this.index.parcelId(hit??null); if(!id)return; this.parcel=this.index.parcels.get(id); if(!this.parcel)return; this.origin.copy(this.parcel.position); this.state=startDrag(id); this.controls.enabled=false; this.select(id); this.element.setPointerCapture?.(event.pointerId) }
  private move=(event:PointerEvent)=>{ if(this.state.phase!=='dragging'||!this.parcel||!this.state.parcelId)return; const hit=this.cast(event,[...this.index.slots.values()]); const slotId=hit?.userData.slotId as number|undefined; const valid=Boolean(slotId&&this.available(slotId,this.state.parcelId)); this.state=targetDrag(this.state,slotId??null,valid); this.clearHighlight(); if(hit instanceof Mesh){this.highlighted=hit;const material=Array.isArray(hit.material)?hit.material[0]:hit.material;if(material&&'color'in material){this.originalColor=(material.color as Color).clone();(material.color as Color).set(valid?0x35d98b:0xff5269)}} if(valid&&hit)this.parcel.position.copy(hit.position) }
  private up=async()=>{ if(this.state.phase!=='dragging')return; this.state=submitDrag(this.state); const {parcelId,targetSlotId}=this.state; if(this.state.phase==='pending'&&parcelId&&targetSlotId){const ok=await this.commit(parcelId,targetSlotId);if(!ok)await this.rollback();else this.finish()}else await this.rollback() }
  private cancel=()=>{void this.rollback()}
  private async rollback(){if(!this.parcel){this.finish();return}this.state={...this.state,phase:'rolling-back'};const from=this.parcel.position.clone(),start=performance.now();await new Promise<void>(resolve=>{const tick=(now:number)=>{const t=Math.min(1,(now-start)/300);this.parcel?.position.lerpVectors(from,this.origin,t);if(t<1)requestAnimationFrame(tick);else resolve()};requestAnimationFrame(tick)});this.finish()}
  private clearHighlight(){if(this.highlighted&&this.originalColor){const material=Array.isArray(this.highlighted.material)?this.highlighted.material[0]:this.highlighted.material;if(material&&'color'in material)(material.color as Color).copy(this.originalColor)}this.highlighted=undefined;this.originalColor=undefined}
  private finish(){this.clearHighlight();this.controls.enabled=true;this.parcel=undefined;this.state=idleDragState()}
  dispose(){this.element.removeEventListener('pointerdown',this.down);this.element.removeEventListener('pointermove',this.move);this.element.removeEventListener('pointerup',this.up);this.element.removeEventListener('pointercancel',this.cancel);this.finish()}
}
