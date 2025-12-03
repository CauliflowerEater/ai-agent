/**
 * 加载状态跟踪 Hook
 * 记录加载开始时的高度、滚动位置、占位符高度等
 */

import { useRef, useEffect, RefObject } from 'react'
import { getPlaceholderHeight } from './scrollUtils'
import type { LoadingTrackerReturn } from './types'

export function useLoadingTracker(
  containerRef: RefObject<HTMLDivElement>,
  isLoading: boolean
): LoadingTrackerReturn {
  const loadingStartHeightRef = useRef(0)
  const loadingStartScrollTopRef = useRef(0)
  const placeholderHeightRef = useRef(0)
  
  /**
   * 监听 isLoading 变化，记录开始加载时的状态
   */
  useEffect(() => {
    const container = containerRef.current
    if (!container) return
    
    if (isLoading) {
      // 开始加载时，记录当前高度、滚动位置和占位符高度
      placeholderHeightRef.current = getPlaceholderHeight(container)
      loadingStartHeightRef.current = container.scrollHeight
      loadingStartScrollTopRef.current = container.scrollTop
    }
  }, [isLoading, containerRef])
  
  return {
    loadingStartHeightRef,
    loadingStartScrollTopRef,
    placeholderHeightRef
  }
}
