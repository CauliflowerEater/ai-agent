/**
 * 滚动状态管理 Hook
 * 使用 Zustand scrollStore 管理自动滚动状态，监听用户手动滚动
 * 使用节流优化滚动事件处理
 */

import { useEffect, useCallback, useMemo, RefObject, MutableRefObject } from 'react'
import { useScrollStore } from '../../../../stores'
import { isNearBottom } from './scrollUtils'
import { throttle } from '../../utils/throttle'

export function useScrollState(
  containerRef: RefObject<HTMLDivElement>,
  isProgrammaticScrollRef: MutableRefObject<boolean>
) {
  const autoScroll = useScrollStore((state) => state.autoScroll)
  const setAutoScroll = useScrollStore((state) => state.setAutoScroll)
  
  /**
   * 处理用户手动滚动事件
   * 使用 useMemo 创建节流函数，避免每次渲染都重新创建
   */
  const handleScroll = useMemo(
    () => throttle(() => {
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
    }, 100), // 100ms 节流
    [containerRef, isProgrammaticScrollRef, setAutoScroll]
  )
  
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
