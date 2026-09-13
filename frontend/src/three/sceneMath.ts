import type { ShelfLayout, ShelfSlot } from '@/types/api'
export interface Vec3 { x:number; y:number; z:number }
export function slotWorldPosition(layout:ShelfLayout, slot:ShelfSlot):Vec3 { const cw=layout.width/layout.columns, ch=layout.height/layout.levels; const x=(slot.columnIndex+.5)*cw-layout.width/2, y=(slot.levelIndex+.5)*ch; const c=Math.cos(layout.rotationY),s=Math.sin(layout.rotationY); return {x:layout.positionX+x*c, y:layout.positionY+y, z:layout.positionZ-x*s} }
export function shelfFront(layout:ShelfLayout):Vec3 { return {x:Math.sin(layout.rotationY),y:0,z:Math.cos(layout.rotationY)} }
export function cameraFocus(layout:ShelfLayout,target:Vec3){const front=shelfFront(layout),distance=Math.max(layout.width,layout.height)*1.8;return {position:{x:target.x+front.x*distance,y:target.y+layout.height*.25,z:target.z+front.z*distance},target}}
