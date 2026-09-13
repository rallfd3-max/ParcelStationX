import { AmbientLight, Color, DirectionalLight, Scene } from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import type { WarehouseSnapshot } from '@/types/api'
import { CameraController } from './CameraController'
import { DragController } from './DragController'
import { GlbEnvironmentLoader } from './EnvironmentLoader'
import { buildParcel } from './ParcelRenderer'
import { buildShelf } from './ShelfBuilder'
import { SceneIndex } from './SceneIndex'
import { buildSlot } from './SlotBuilder'
import { WarehouseRenderer } from './WarehouseRenderer'
import { buildOperationalAreas } from './OperationalAreas'
import { buildStagingParcel } from './StagingParcelRenderer'
import { HoverController,type ParcelHover } from './HoverController'

export class WarehouseScene {
  readonly index = new SceneIndex()
  readonly camera: CameraController
  private scene = new Scene()
  private renderer: WarehouseRenderer
  private controls: OrbitControls
  private drag: DragController
  private hover: HoverController
  private frame = 0
  private resize: ResizeObserver

  constructor(container: HTMLElement, private data: WarehouseSnapshot, select:(id:number)=>void, relocate:(parcelId:number,slotId:number)=>Promise<boolean>,hover:(value:ParcelHover|null)=>void=()=>{}) {
    this.scene.background = new Color(0x07111d)
    this.renderer = new WarehouseRenderer(container)
    this.controls = new OrbitControls(this.renderer.camera, this.renderer.renderer.domElement)
    this.controls.enableDamping = true
    this.camera = new CameraController(this.renderer.camera, this.controls, () => data.layouts, () => data.slots)
    this.drag = new DragController(this.renderer.renderer.domElement, this.renderer.camera, this.controls, this.index, (slotId,parcelId) => this.isAvailable(slotId,parcelId), relocate, select)
    this.hover = new HoverController(this.renderer.renderer.domElement,this.renderer.camera,this.index,()=>this.drag.isDragging(),hover)
    this.scene.add(new AmbientLight(0x9bc7dc, 1.5))
    const light = new DirectionalLight(0xffffff, 2); light.position.set(5,10,7); this.scene.add(light)
    this.scene.add(buildOperationalAreas())
    void this.build()
    this.resize = new ResizeObserver(() => this.renderer.resize(container.clientWidth, container.clientHeight))
    this.resize.observe(container); this.loop()
  }
  private isAvailable(slotId:number,parcelId:number) { const slot=this.data.slots.find(s=>s.id===slotId); return Boolean(slot?.enabled&&!this.data.parcels.some(p=>p.slotId===slotId&&p.id!==parcelId&&p.status!=='PICKED_UP')) }
  private async build() {
    this.scene.add(await new GlbEnvironmentLoader().load())
    for (const shelf of this.data.shelves) { const layout=this.data.layouts.find(x=>x.shelfId===shelf.id); if(layout)this.scene.add(buildShelf(shelf,layout,this.index)) }
    for (const slot of this.data.slots) { const layout=this.data.layouts.find(x=>x.shelfId===slot.shelfId); if(layout)this.scene.add(buildSlot(slot,layout,this.index)) }
    for (const parcel of this.data.parcels) { const slot=this.data.slots.find(x=>x.id===parcel.slotId),layout=slot&&this.data.layouts.find(x=>x.shelfId===slot.shelfId); if(slot&&layout&&parcel.status!=='PICKED_UP')this.scene.add(buildParcel(parcel,slot,layout,this.index)) }
    this.data.parcels.filter(parcel=>parcel.status==='IN_STOCK'&&parcel.slotId===null).forEach((parcel,order)=>this.scene.add(buildStagingParcel(parcel,order,this.index)))
    this.camera.resetCamera()
  }
  private loop=()=>{this.controls.update();this.renderer.renderer.render(this.scene,this.renderer.camera);this.frame=requestAnimationFrame(this.loop)}
  dispose(){cancelAnimationFrame(this.frame);this.camera.cancelAnimation();this.hover.dispose();this.drag.dispose();this.resize.disconnect();this.controls.dispose();this.scene.traverse(object=>{const mesh=object as any;mesh.geometry?.dispose?.();if(Array.isArray(mesh.material))mesh.material.forEach((material:any)=>material.dispose());else mesh.material?.dispose?.()});this.index.clear();this.renderer.dispose()}
}
