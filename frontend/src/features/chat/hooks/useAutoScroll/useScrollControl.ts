/**
 * 滚动控制 Hook
 * 提供滚动到底部、滚动到指定位置等功能
 */

import { useRef, useCallback, RefObject } from 'react'
import type { ScrollControlReturn } from './types'

export function useScrollControl(
  scrollContainerRef: RefObject<HTMLDivElement>,
  scrollBottomRef: RefObject<HTMLDivElement>
): ScrollControlReturn {
  const isProgrammaticScrollRef = useRef(false)
  
  /**
   * 滚动到底部
   */
  const scrollToBottom = useCallback(() => {
    isProgrammaticScrollRef.current = true
    scrollBottomRef.current?.scrollIntoView({ behavior: 'smooth' })
    
    // 延迟重置标志位，等待滚动完成
    setTimeout(() => {
      isProgrammaticScrollRef.current = false
    }, 100)
  }, [scrollBottomRef])
  
  /**
   * 滚动到指定位置
   */
  const scrollToPosition = useCallback((position: number) => {
    if (!scrollContainerRef.current) return
    
    isProgrammaticScrollRef.current = true
    scrollContainerRef.current.scrollTop = position
    
    setTimeout(() => {
      isProgrammaticScrollRef.current = false
    }, 100)
  }, [scrollContainerRef])
  
  return {
    scrollToBottom,
    scrollToPosition,
    isProgrammaticScrollRef
  }
}
