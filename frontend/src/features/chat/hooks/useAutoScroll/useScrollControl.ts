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
   * 跳转到底部（瞬间，无动画）
   */
  const jumpToBottom = useCallback(() => {
    if (!scrollContainerRef.current) return
    
    isProgrammaticScrollRef.current = true
    // 直接设置 scrollTop 到最大值，实现瞬间跳转
    scrollContainerRef.current.scrollTop = scrollContainerRef.current.scrollHeight
    
    // 使用 requestAnimationFrame 确保 scroll 事件处理完成后再重置标志位
    requestAnimationFrame(() => {
      isProgrammaticScrollRef.current = false
    })
  }, [scrollContainerRef])
  
  /**
   * 滚动到底部（平滑滚动）
   */
  const scrollToBottom = useCallback(() => {
    if (!scrollContainerRef.current) return
    
    isProgrammaticScrollRef.current = true
    // 使用 scrollTo 实现平滑滚动
    scrollContainerRef.current.scrollTo({
      top: scrollContainerRef.current.scrollHeight,
      behavior: 'smooth'
    })
    
    // 使用 setTimeout 等待滚动动画完成后再重置标志位
    setTimeout(() => {
      isProgrammaticScrollRef.current = false
    }, 500)
  }, [scrollContainerRef])
  
  /**
   * 滚动到指定位置
   */
  const scrollToPosition = useCallback((position: number) => {
    if (!scrollContainerRef.current) return
    
    isProgrammaticScrollRef.current = true
    scrollContainerRef.current.scrollTop = position
    
    // 使用 requestAnimationFrame 确保 scroll 事件处理完成后再重置标志位
    requestAnimationFrame(() => {
      isProgrammaticScrollRef.current = false
    })
  }, [scrollContainerRef])
  
  return {
    jumpToBottom,
    scrollToBottom,
    scrollToPosition,
    isProgrammaticScrollRef
  }
}
