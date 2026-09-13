// @vitest-environment jsdom
import {mount} from '@vue/test-utils'
import {afterEach,beforeEach,describe,expect,it,vi} from 'vitest'
import {nextTick} from 'vue'
import DataChart from './DataChart.vue'

const mocks=vi.hoisted(()=>{
  const charts=new Map<Element,{getDom:ReturnType<typeof vi.fn>;setOption:ReturnType<typeof vi.fn>;resize:ReturnType<typeof vi.fn>;dispose:ReturnType<typeof vi.fn>}>()
  const makeChart=(dom:Element)=>({getDom:vi.fn(()=>dom),setOption:vi.fn(),resize:vi.fn(),dispose:vi.fn(()=>charts.delete(dom))})
  return {charts,init:vi.fn((dom:Element)=>{const chart=makeChart(dom);charts.set(dom,chart);return chart}),getInstanceByDom:vi.fn((dom:Element)=>charts.get(dom)),use:vi.fn()}
})
vi.mock('echarts/core',()=>({init:mocks.init,getInstanceByDom:mocks.getInstanceByDom,use:mocks.use}))
vi.mock('echarts/charts',()=>({BarChart:{},LineChart:{},PieChart:{}}))
vi.mock('echarts/components',()=>({GridComponent:{},LegendComponent:{},TooltipComponent:{}}))
vi.mock('echarts/renderers',()=>({CanvasRenderer:{}}))

const observers:ResizeObserverMock[]=[]
class ResizeObserverMock {
  observe=vi.fn()
  disconnect=vi.fn()
  constructor(public callback:ResizeObserverCallback){observers.push(this)}
}

beforeEach(()=>{
  mocks.charts.clear();mocks.init.mockClear();mocks.getInstanceByDom.mockClear();observers.length=0
  vi.stubGlobal('ResizeObserver',ResizeObserverMock)
  vi.stubGlobal('requestAnimationFrame',(callback:FrameRequestCallback)=>{callback(0);return 1})
  vi.stubGlobal('cancelAnimationFrame',vi.fn())
})
afterEach(()=>vi.unstubAllGlobals())

async function settle(){await nextTick();await nextTick()}

describe('DataChart lifecycle',()=>{
  it('keeps the chart DOM alive through loading and empty transitions',async()=>{
    const wrapper=mount(DataChart,{props:{title:'趋势',type:'line',labels:['09-13'],series:[{name:'入库',values:[2]}]}})
    await settle();const dom=wrapper.get('.chart').element
    await wrapper.setProps({loading:true});expect(wrapper.get('.chart').element).toBe(dom)
    await wrapper.setProps({loading:false,series:[]});expect(wrapper.get('.chart').element).toBe(dom);expect(wrapper.text()).toContain('暂无可用数据')
    await wrapper.setProps({series:[{name:'入库',values:[3]}]});await settle()
    expect(wrapper.get('.chart').element).toBe(dom);expect(mocks.charts.get(dom)?.setOption).toHaveBeenCalled()
    wrapper.unmount()
  })

  it('renders line again after data to loading to data',async()=>{
    const wrapper=mount(DataChart,{props:{title:'趋势',type:'line',labels:['1'],series:[{name:'库存',values:[1]}]}})
    await settle();const chart=mocks.charts.get(wrapper.get('.chart').element)!
    expect(chart.setOption).toHaveBeenCalledTimes(1)
    await wrapper.setProps({loading:true});await wrapper.setProps({loading:false,series:[{name:'库存',values:[2]}]});await settle()
    expect(chart.setOption).toHaveBeenCalledTimes(2)
    expect(chart.setOption.mock.calls[chart.setOption.mock.calls.length-1]![0].series[0].type).toBe('line')
    wrapper.unmount()
  })

  it.each(['pie','donut'] as const)('keeps %s rendering intact',async(type)=>{
    const wrapper=mount(DataChart,{props:{title:'分布',type,labels:['顺丰'],values:[2]}})
    await settle();const chart=mocks.charts.get(wrapper.get('.chart').element)!
    expect(chart.setOption.mock.calls[0]![0].series[0].type).toBe('pie')
    wrapper.unmount()
  })

  it('creates a fresh instance after a route-style remount',async()=>{
    const props={title:'趋势',type:'line' as const,labels:['1'],series:[{name:'入库',values:[1]}]}
    const first=mount(DataChart,{props});await settle();const firstChart=mocks.charts.get(first.get('.chart').element)!
    first.unmount();expect(firstChart.dispose).toHaveBeenCalledOnce()
    const second=mount(DataChart,{props});await settle()
    expect(mocks.init).toHaveBeenCalledTimes(2);expect(mocks.charts.get(second.get('.chart').element)?.setOption).toHaveBeenCalledOnce()
    second.unmount()
  })

  it('observes the container and cleans observer and chart on unmount',async()=>{
    const wrapper=mount(DataChart,{props:{title:'柱图',labels:['A'],values:[1]}});await settle()
    const chart=mocks.charts.get(wrapper.get('.chart').element)!
    expect(observers[0]!.observe).toHaveBeenCalledWith(wrapper.get('.chart-container').element)
    observers[0]!.callback([],observers[0] as unknown as ResizeObserver);expect(chart.resize).toHaveBeenCalled()
    wrapper.unmount();expect(observers[0]!.disconnect).toHaveBeenCalledOnce();expect(chart.dispose).toHaveBeenCalledOnce()
  })
})
