import type { Object3D } from 'three'
export class SceneIndex { parcels=new Map<number,Object3D>(); slots=new Map<number,Object3D>(); shelves=new Map<number,Object3D>(); clear(){this.parcels.clear();this.slots.clear();this.shelves.clear()} parcelId(object:Object3D|null){while(object){const id=object.userData.parcelId as number|undefined;if(id)return id;object=object.parent}return undefined} }
