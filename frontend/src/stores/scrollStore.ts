/**
 * Zustand 滚动状态管理
 * 管理自动滚动、加载跟踪等滚动相关状态
 */

import { create } from 'zustand'
import { devtools } from 'zustand/middleware'

interface ScrollState {
  // 自动滚动状态
  autoScroll: boolean
  
  // 加载跟踪状态
  loadingStartHeight: number
  loadingStartScrollTop: number
  placeholderHeight: number
  
  // 最后一条用户消息的 DOM ID
  lastUserMessageId: string | null
  
  // Actions
  setAutoScroll: (autoScroll: boolean) => void
  setLoadingStartHeight: (height: number) => void
  setLoadingStartScrollTop: (scrollTop: number) => void
  setPlaceholderHeight: (height: number) => void
  setLastUserMessageId: (id: string | null) => void
  resetScrollState: () => void
}

export const useScrollStore = create<ScrollState>()(
  devtools(
    (set) => ({
      // 初始状态
      autoScroll: true,
      loadingStartHeight: 0,
      loadingStartScrollTop: 0,
      placeholderHeight: 0,
      lastUserMessageId: null,
      
      // Actions
      setAutoScroll: (autoScroll) =>
        set({ autoScroll }),
      
      setLoadingStartHeight: (height) =>
        set({ loadingStartHeight: height }),
      
      setLoadingStartScrollTop: (scrollTop) =>
        set({ loadingStartScrollTop: scrollTop }),
      
      setPlaceholderHeight: (height) =>
        set({ placeholderHeight: height }),
      
      setLastUserMessageId: (id) =>
        set({ lastUserMessageId: id }),
      
      resetScrollState: () =>
        set({
          autoScroll: true,
          loadingStartHeight: 0,
          loadingStartScrollTop: 0,
          placeholderHeight: 0,
          lastUserMessageId: null
        })
    }),
    {
      name: 'ScrollStore' // DevTools 中的名称
    }
  )
)
