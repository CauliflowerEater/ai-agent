/**
 * 滚动状态管理 Hook
 * 管理自动滚动状态，监听用户手动滚动
 */

import { useState, useEffect, useCallback, RefObject, MutableRefObject } from 'react'
import { isNearBottom } from './scrollUtils'

export function useScrollState(
  containerRef: RefObject<HTMLDivElement>,
  isProgrammaticScrollRef: MutableRefObject<boolean>
) {
  const [autoScroll, setAutoScroll] = useState(true)
  
  /**
   * 处理用户手动滚动事件
   */
  const handleScroll = useCallback(() => {
    // 如果是程序化滚动，忽略此次事件
    if (isProgrammaticScrollRef.current) {
      return
    }
    
    const container = containerRef.current
    if (!container) return
    
    if (isNearBottom(container)) {
      // 用户滚回底部，启用自动滚动
      setAutoScroll(true)
    } else {
      // 用户向上滚动，禁用自动滚动
      setAutoScroll(false)
    }
  }, [containerRef, isProgrammaticScrollRef])
  
  /**
   * 监听滚动事件
   */
  useEffect(() => {
    const container = containerRef.current
    if (!container) return
    
    container.addEventListener('scroll', handleScroll)
    return () => {
      container.removeEventListener('scroll', handleScroll)
    }
  }, [containerRef, handleScroll])
  
  return {
    autoScroll,
    setAutoScroll
  }
}
