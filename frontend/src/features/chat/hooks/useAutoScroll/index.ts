/**
 * 智能自动滚动 Hook
 * 整合所有子模块，提供统一的滚动管理功能
 * 
 * 功能：
 * 1. 流式显示时实时滚动：跟随新内容显示而滚动
 * 2. 智能阅读模式：当新增内容超过一屏时，进入阅读模式不再滚动
 * 3. 手动控制：用户向上滚动后禁用自动滚动，滚回底部后恢复
 */

import { useRef } from 'react'
import { useScrollControl } from './useScrollControl'
import { useScrollState } from './useScrollState'
import { useLoadingTracker } from './useLoadingTracker'
import { useMessageDetection } from './useMessageDetection'
import type { Message } from '../../types'
import type { UseAutoScrollReturn } from './types'

export function useAutoScroll(
  messages: Message[]
): UseAutoScrollReturn {
  // 创建 ref
  const scrollContainerRef = useRef<HTMLDivElement>(null)
  const scrollBottomRef = useRef<HTMLDivElement>(null)
  
  // 1. 滚动控制（提供滚动操作方法）
  const scrollControl = useScrollControl(scrollContainerRef, scrollBottomRef)
  
  // 2. 滚动状态管理（监听用户手动滚动）
  const { autoScroll, setAutoScroll } = useScrollState(
    scrollContainerRef,
    scrollControl.isProgrammaticScrollRef
  )
  
  // 3. 加载状态跟踪（记录加载时的高度等信息）
  const loadingTracker = useLoadingTracker(scrollContainerRef)
  
  // 4. 消息变化检测（检测新消息，触发滚动）
  // 注意: isLoading 已经在 useMessageDetection 内部从 store 获取
  useMessageDetection(
    messages,
    autoScroll,
    setAutoScroll,
    scrollControl,
    loadingTracker,
    scrollContainerRef
  )
  
  return {
    scrollContainerRef,  // 需要绑定到滚动容器上
    scrollBottomRef,     // 需要绑定到底部元素上
    autoScroll,          // 当前是否处于自动滚动模式
  }
}

// 导出类型
export type { UseAutoScrollReturn } from './types'
