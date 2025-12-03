/**
 * 加载状态跟踪 Hook
 * 使用 Zustand scrollStore 记录加载开始时的高度、滚动位置、占位符高度等
 */

import { useEffect, RefObject } from 'react'
import { useScrollStore } from '../../../../stores'
import { getPlaceholderHeight } from './scrollUtils'
import type { LoadingTrackerReturn } from './types'

export function useLoadingTracker(
  containerRef: RefObject<HTMLDivElement>,
  isLoading: boolean
): LoadingTrackerReturn {
  const loadingStartHeight = useScrollStore((state) => state.loadingStartHeight)
  const loadingStartScrollTop = useScrollStore((state) => state.loadingStartScrollTop)
  const placeholderHeight = useScrollStore((state) => state.placeholderHeight)
  
  const setLoadingStartHeight = useScrollStore((state) => state.setLoadingStartHeight)
  const setLoadingStartScrollTop = useScrollStore((state) => state.setLoadingStartScrollTop)
  const setPlaceholderHeight = useScrollStore((state) => state.setPlaceholderHeight)
  
  /**
   * 监听 isLoading 变化，记录开始加载时的状态
   */
  useEffect(() => {
    const container = containerRef.current
    if (!container) return
    
    if (isLoading) {
      // 开始加载时，记录当前高度、滚动位置和占位符高度
      setPlaceholderHeight(getPlaceholderHeight(container))
      setLoadingStartHeight(container.scrollHeight)
      setLoadingStartScrollTop(container.scrollTop)
    }
  }, [isLoading, containerRef, setLoadingStartHeight, setLoadingStartScrollTop, setPlaceholderHeight])
  
  // 返回值保持原有的 ref 结构，以便与现有代码兼容
  return {
    loadingStartHeightRef: { current: loadingStartHeight } as React.MutableRefObject<number>,
    loadingStartScrollTopRef: { current: loadingStartScrollTop } as React.MutableRefObject<number>,
    placeholderHeightRef: { current: placeholderHeight } as React.MutableRefObject<number>
  }
}
