/**
 * 加载状态跟踪 Hook
 * 使用 Zustand scrollStore 记录加载开始时的高度、滚动位置、占位符高度等
 */

import { useEffect, RefObject } from 'react'
import { useChatStore, useScrollStore } from '../../../../stores'
import { getPlaceholderHeight } from './scrollUtils'
import type { LoadingTrackerReturn } from './types'

export function useLoadingTracker(
  containerRef: RefObject<HTMLDivElement>
): LoadingTrackerReturn {
  // 从 chatStore 获取 isLoading
  const isLoading = useChatStore((state) => state.isLoading)
  
  // 从 scrollStore 获取状态
  const loadingStartHeight = useScrollStore((state) => state.loadingStartHeight)
  const loadingStartScrollTop = useScrollStore((state) => state.loadingStartScrollTop)
  const placeholderHeight = useScrollStore((state) => state.placeholderHeight)
  
  // 从 scrollStore 获取 actions
  const setLoadingStartHeight = useScrollStore((state) => state.setLoadingStartHeight)
  const setLoadingStartScrollTop = useScrollStore((state) => state.setLoadingStartScrollTop)
  const setPlaceholderHeight = useScrollStore((state) => state.setPlaceholderHeight)
  
  /**
   * 监听 isLoading 变化,记录开始加载时的状态
   */
  useEffect(() => {
    const container = containerRef.current
    if (!container) return
    
    if (isLoading) {
      // 开始加载时,记录当前高度、滚动位置和占位符高度
      setPlaceholderHeight(getPlaceholderHeight(container))
      setLoadingStartHeight(container.scrollHeight)
      setLoadingStartScrollTop(container.scrollTop)
    }
  }, [isLoading, containerRef, setLoadingStartHeight, setLoadingStartScrollTop, setPlaceholderHeight])
  
  // 返回数值而不是 ref
  return {
    loadingStartHeight,
    loadingStartScrollTop,
    placeholderHeight
  }
}
