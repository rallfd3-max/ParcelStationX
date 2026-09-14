import { describe, expect, it } from 'vitest'
import { applyExceptionAdviceDraft } from './aiAdvice'

describe('AI exception advice human-in-the-loop', () => {
  it('only fills the local resolution draft', () => {
    const draft = { targetStatus: 'IN_STOCK', resolution: '' }
    const result = applyExceptionAdviceDraft({
      suggestedType: 'DAMAGED', riskLevel: 'MEDIUM', reason: 'inspect', steps: ['photo'], suggestedResolution: '人工核验后恢复库存',
    }, draft)
    expect(result.resolution).toBe('人工核验后恢复库存')
    expect(result.targetStatus).toBe('IN_STOCK')
  })
})
