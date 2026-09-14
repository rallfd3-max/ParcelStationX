import type { ShelfLayout, ShelfSlot } from '@/types/api'
export interface Vec3 { x:number; y:number; z:number }
export function slotWorldPosition(layout:ShelfLayout, slot:ShelfSlot):Vec3 { const cw=layout.width/layout.columns, ch=layout.height/layout.levels; const x=(slot.columnIndex+.5)*cw-layout.width/2, y=(slot.levelIndex+.5)*ch; const c=Math.cos(layout.rotationY),s=Math.sin(layout.rotationY); return {x:layout.positionX+x*c, y:layout.positionY+y, z:layout.positionZ-x*s} }
export function shelfFront(layout:ShelfLayout):Vec3 { return {x:Math.sin(layout.rotationY),y:0,z:Math.cos(layout.rotationY)} }
export function cameraFocus(layout:ShelfLayout,target:Vec3){const front=shelfFront(layout),distance=Math.max(layout.width,layout.height)*1.8;return {position:{x:target.x+front.x*distance,y:target.y+layout.height*.25,z:target.z+front.z*distance},target}}
function layoutBounds(layout:ShelfLayout){const c=Math.abs(Math.cos(layout.rotationY)),s=Math.abs(Math.sin(layout.rotationY)),halfX=(layout.width*c+layout.depth*s)/2,halfZ=(layout.width*s+layout.depth*c)/2;return{minX:layout.positionX-halfX,maxX:layout.positionX+halfX,minY:layout.positionY,maxY:layout.positionY+layout.height,minZ:layout.positionZ-halfZ,maxZ:layout.positionZ+halfZ}}
export function segmentIntersectsLayout(from:Vec3,to:Vec3,layout:ShelfLayout){const b=layoutBounds(layout),delta={x:to.x-from.x,y:to.y-from.y,z:to.z-from.z};let near=0,far=1;for(const axis of ['x','y','z'] as const){if(Math.abs(delta[axis])<1e-8){if(from[axis]<b[`min${axis.toUpperCase()}` as 'minX']||from[axis]>b[`max${axis.toUpperCase()}` as 'maxX'])return false;continue}const min=b[`min${axis.toUpperCase()}` as 'minX'],max=b[`max${axis.toUpperCase()}` as 'maxX'],a=(min-from[axis])/delta[axis],z=(max-from[axis])/delta[axis];near=Math.max(near,Math.min(a,z));far=Math.min(far,Math.max(a,z));if(near>far)return false}return far>.05&&near<.95}
export const MIN_PARCEL_CAMERA_DISTANCE=1.05
export function parcelCameraView(layout:ShelfLayout,target:Vec3,layouts:ShelfLayout[]){
  const front=shelfFront(layout),side={x:Math.cos(layout.rotationY),z:-Math.sin(layout.rotationY)}
  const preferred=Math.min(3,Math.max(1.8,layout.width*.55+layout.height*.35))
  const lift=Math.min(.9,Math.max(.45,layout.height*.24))
  const others=layouts.filter(x=>x.shelfId!==layout.shelfId)
  const clear=(position:Vec3)=>!others.some(other=>segmentIntersectsLayout(position,target,other))
  const distances:number[]=[]
  for(let distance=preferred;distance>=MIN_PARCEL_CAMERA_DISTANCE;distance-=.2)distances.push(distance)
  if(distances[distances.length-1]!==MIN_PARCEL_CAMERA_DISTANCE)distances.push(MIN_PARCEL_CAMERA_DISTANCE)
  for(const distance of distances){
    const forward=Math.max(MIN_PARCEL_CAMERA_DISTANCE,distance)
    const lateral=Math.min(.65,layout.width*.16)
    const candidates=[0,lateral,-lateral].map(offset=>({x:target.x+front.x*forward+side.x*offset,y:target.y+lift,z:target.z+front.z*forward+side.z*offset}))
    const position=candidates.find(clear)
    if(position)return{position,target:{...target}}
  }
  return{position:{x:target.x+front.x*MIN_PARCEL_CAMERA_DISTANCE,y:target.y+lift,z:target.z+front.z*MIN_PARCEL_CAMERA_DISTANCE},target:{...target}}
}
