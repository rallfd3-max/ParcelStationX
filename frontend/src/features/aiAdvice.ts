import type { AiExceptionAdvice } from '@/types/api'

export function applyExceptionAdviceDraft(
  advice: AiExceptionAdvice,
  draft: { targetStatus: string; resolution: string },
) {
  draft.resolution = advice.suggestedResolution
  return draft
}
