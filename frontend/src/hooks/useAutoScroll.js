import { useRef, useEffect, useState, useCallback } from 'react'

/**
 * 智能自动滚动Hook
 * 功能：
 * 1. 跟随模式：用户在底部时，新消息自动滚到底
 * 2. 阅读模式：用户向上滚动后，不再自动滚动
 * @param {Array} dependencies - 依赖数组，当依赖变化时触发滚动判断
 */
export const useAutoScroll = (dependencies = []) => {
  const scrollContainerRef = useRef(null) // 滚动容器引用
  const scrollBottomRef = useRef(null)    // 底部元素引用
  const [autoScroll, setAutoScroll] = useState(true) // 是否自动滚动
  const THRESHOLD = 30 // 判断是否在底部的阈值（像素）

  /**
   * 判断是否在底部附近
   */
  const isNearBottom = useCallback(() => {
    const container = scrollContainerRef.current
    if (!container) return true

    const { scrollTop, scrollHeight, clientHeight } = container
    const distanceToBottom = scrollHeight - scrollTop - clientHeight
    
    return distanceToBottom < THRESHOLD
  }, [])

  /**
   * 滚动到底部
   */
  const scrollToBottom = useCallback(() => {
    scrollBottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [])

  /**
   * 处理滚动事件
   */
  const handleScroll = useCallback(() => {
    if (isNearBottom()) {
      // 用户滚回底部，启用自动滚动
      setAutoScroll(true)
    } else {
      // 用户向上滚动，禁用自动滚动
      setAutoScroll(false)
    }
  }, [isNearBottom])

  /**
   * 监听滚动事件
   */
  useEffect(() => {
    const container = scrollContainerRef.current
    if (!container) return

    container.addEventListener('scroll', handleScroll)
    return () => {
      container.removeEventListener('scroll', handleScroll)
    }
  }, [handleScroll])

  /**
   * 当依赖变化时，根据 autoScroll 状态决定是否滚动
   */
  useEffect(() => {
    if (autoScroll) {
      scrollToBottom()
    }
  }, [...dependencies, autoScroll])

  return {
    scrollContainerRef,  // 需要绑定到滚动容器上
    scrollBottomRef,     // 需要绑定到底部元素上
    autoScroll,          // 当前是否处于自动滚动模式
  }
}
