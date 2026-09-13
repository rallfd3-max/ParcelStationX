import { describe,expect,it } from 'vitest'
import { sceneBounds } from './sceneBounds'

describe('scene bounds',()=>{
  it('frames all layouts including rotation',()=>{
    const bounds=sceneBounds([
      {shelfId:1,positionX:-6,positionY:0,positionZ:-3,rotationY:0,width:3.6,height:2.6,depth:.8,columns:6,levels:5},
      {shelfId:2,positionX:6,positionY:0,positionZ:2,rotationY:Math.PI,width:3.6,height:2.6,depth:.8,columns:6,levels:5},
    ])
    expect(bounds.centerX).toBeCloseTo(0)
    expect(bounds.spanX).toBeGreaterThan(15)
    expect(bounds.spanZ).toBeGreaterThan(5)
    expect(bounds.maxHeight).toBe(2.6)
  })
})
