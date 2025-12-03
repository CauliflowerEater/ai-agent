/**
 * 消息变化检测 Hook
 * 检测新消息、流式内容变化，触发相应的滚动行为
 */

import { useRef, useEffect, RefObject, Dispatch, SetStateAction } from 'react'
import { hasUserMessageInNewMessages, scrollToLastUserMessage } from './scrollUtils'
import type { Message } from '../../types'
import type { ScrollControlReturn, LoadingTrackerReturn } from './types'

export function useMessageDetection(
  messages: Message[],
  isLoading: boolean,
  autoScroll: boolean,
  setAutoScroll: Dispatch<SetStateAction<boolean>>,
  scrollControl: ScrollControlReturn,
  loadingTracker: LoadingTrackerReturn,
  containerRef: RefObject<HTMLDivElement>
) {
  const prevMessageCountRef = useRef(0)
  const prevScrollHeightRef = useRef(0)
  
  useEffect(() => {
    const container = containerRef.current
    if (!container) return
    
    const { scrollHeight, clientHeight } = container
    const currentMessageCount = messages.length
    const prevMessageCount = prevMessageCountRef.current
    const deltaHeight = scrollHeight - prevScrollHeightRef.current
    
    // ========== 1. 检测新消息 ==========
    const hasNewMessage = currentMessageCount > prevMessageCount
    
    if (hasNewMessage) {
      const hasUserMessage = hasUserMessageInNewMessages(
        messages,
        currentMessageCount,
        prevMessageCount
      )
      
      // 用户发送了新消息，强制滚动到底部并启用自动滚动
      if (hasUserMessage) {
        setAutoScroll(true)
        scrollControl.scrollToBottom()
        prevMessageCountRef.current = currentMessageCount
        prevScrollHeightRef.current = scrollHeight
        return
      }
      
      // 有新消息但不包含用户消息且不在加载中
      if (!isLoading) {
        setAutoScroll(true)
        scrollControl.scrollToBottom()
        prevMessageCountRef.current = currentMessageCount
        prevScrollHeightRef.current = scrollHeight
        return
      }
      
      // 有新消息加入且正在加载（助手开始回复）
      // 只更新计数，让后续的智能滚动逻辑处理
      if (isLoading) {
        prevMessageCountRef.current = currentMessageCount
      }
    }
    
    // ========== 2. 流式内容滚动逻辑 ==========
    if (deltaHeight > 0 && autoScroll && isLoading) {
      // 检查从开始加载到现在的总增量（需要减去占位符高度）
      const actualStartHeight = 
        loadingTracker.loadingStartHeightRef.current - 
        loadingTracker.placeholderHeightRef.current
      const totalDelta = scrollHeight - actualStartHeight
      
      if (totalDelta <= clientHeight) {
        // 总增量不超过一屏，继续实时滚动
        scrollControl.scrollToBottom()
      } else {
        // 总增量已超过一屏，滚动到用户最后一条消息的底部
        scrollToLastUserMessage(
          container,
          scrollControl.isProgrammaticScrollRef,
          loadingTracker.loadingStartScrollTopRef
        )
        // 停止自动滚动，进入阅读模式
        setAutoScroll(false)
      }
    }
    
    // ========== 3. 更新记录 ==========
    if (deltaHeight > 0) {
      prevScrollHeightRef.current = scrollHeight
    }
    
  }, [
    messages,
    isLoading,
    autoScroll,
    setAutoScroll,
    scrollControl,
    loadingTracker,
    containerRef
  ])
  
  /**
   * 监听 isLoading 结束，检查是否超过一屏
   */
  useEffect(() => {
    const container = containerRef.current
    if (!container || isLoading) return
    
    // 加载结束时，检查总增量是否超过一屏
    const { scrollHeight, clientHeight } = container
    const totalDelta = 
      scrollHeight - loadingTracker.loadingStartHeightRef.current
    
    if (totalDelta > clientHeight) {
      // 总增量超过一屏，进入阅读模式
      setAutoScroll(false)
    }
    
    // 更新记录
    prevScrollHeightRef.current = scrollHeight
  }, [isLoading, containerRef, loadingTracker, setAutoScroll])
}
