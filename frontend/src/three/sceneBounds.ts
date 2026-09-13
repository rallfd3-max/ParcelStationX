import type { ShelfLayout } from '@/types/api'

export interface SceneBounds { centerX:number; centerZ:number; spanX:number; spanZ:number; maxHeight:number }

export function sceneBounds(layouts:ShelfLayout[]):SceneBounds {
  if (!layouts.length) return { centerX:0,centerZ:0,spanX:10,spanZ:8,maxHeight:3 }
  const extents=layouts.map(layout=>{
    const c=Math.abs(Math.cos(layout.rotationY)),s=Math.abs(Math.sin(layout.rotationY))
    return { minX:layout.positionX-(layout.width*c+layout.depth*s)/2,maxX:layout.positionX+(layout.width*c+layout.depth*s)/2,minZ:layout.positionZ-(layout.width*s+layout.depth*c)/2,maxZ:layout.positionZ+(layout.width*s+layout.depth*c)/2,height:layout.positionY+layout.height }
  })
  const minX=Math.min(...extents.map(x=>x.minX)),maxX=Math.max(...extents.map(x=>x.maxX)),minZ=Math.min(...extents.map(x=>x.minZ)),maxZ=Math.max(...extents.map(x=>x.maxZ))
  return { centerX:(minX+maxX)/2,centerZ:(minZ+maxZ)/2,spanX:maxX-minX,spanZ:maxZ-minZ,maxHeight:Math.max(...extents.map(x=>x.height)) }
}
