import { describe, expect, it } from 'vitest'
import { trustedFocusParcel } from './aiActions'
import type { AiQueryResponse } from '@/types/api'

const response=(ids:number[],action:number|null):AiQueryResponse=>({answer:'',results:ids.map(parcelId=>({parcelId,trackingNo:`P${parcelId}`,slotCode:null,shelfCode:null,status:'IN_STOCK'})),action:{type:action?'FOCUS_PARCEL':'NONE',parcelId:action}})
describe('AI action trust boundary',()=>{
  it('accepts only a parcel id present in returned results',()=>{expect(trustedFocusParcel(response([1],1))).toBe(1);expect(trustedFocusParcel(response([1],99))).toBeNull()})
  it('does not auto-focus multiple results without a backend action',()=>expect(trustedFocusParcel(response([1,2],null))).toBeNull())
})
