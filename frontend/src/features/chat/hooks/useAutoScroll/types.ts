/**
 * useAutoScroll 相关类型定义
 */

import type { RefObject, MutableRefObject } from 'react'

export interface UseAutoScrollReturn {
  scrollContainerRef: RefObject<HTMLDivElement>
  scrollBottomRef: RefObject<HTMLDivElement>
  autoScroll: boolean
}

export interface ScrollControlReturn {
  jumpToBottom: () => void
  scrollToBottom: () => void
  scrollToPosition: (position: number) => void
  isProgrammaticScrollRef: MutableRefObject<boolean>
}

export interface LoadingTrackerReturn {
  loadingStartHeight: number
  loadingStartScrollTop: number
  placeholderHeight: number
}
