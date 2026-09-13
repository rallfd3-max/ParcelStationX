# V2.1.1 Phase H1 — Real Line/Pie Charts

## 完成内容

- 将 `DataChart.vue` 改为显式类型化图表组件，支持 `bar`、`line`、`pie`、`donut`。
- 注册真实 ECharts `LineChart`、`PieChart`、`BarChart` 与 Legend/Tooltip。
- 首页将 14 天真实 API 趋势合并为入库、出库、库存三序列折线图，库存使用第二 Y 轴。
- 快递公司使用环图，快件状态和异常类型使用饼图；货架、分区和停留时长继续使用柱图。
- 补齐图例、百分比 tooltip/label、loading、empty、resize 和 dispose。

## 测试

- 新增 `chartOptions.test.ts`，直接断言趋势 series 为 `line`，环图 series 为 `pie` 且 radius 为环形。
- `npm run type-check`：通过。
- `npm test -- --run`：11 files、27 tests 全部通过。
- `npm run build`：通过，仅保留 chunk 大小提示。

## 风险与下一阶段

- 图表业务数据仍全部来自现有 dashboard trends/distributions API，无 mock。
- H2 仅处理 Parcel 镜头语义和遮挡安全视角，不改图表实现。
