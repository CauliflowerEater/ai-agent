import { useRef, useEffect, useState, useCallback } from 'react'

/**
 * 智能自动滚动Hook
 * 功能：
 * 1. 流式显示时实时滚动：跟随新内容显示而滚动
 * 2. 智能阅读模式：当新增内容超过一屏时，进入阅读模式不再滚动
 * 3. 手动控制：用户向上滚动后禁用自动滚动，滚回底部后恢复
 * @param {Array} messages - 消息数组，用于监听内容变化
 * @param {boolean} isLoading - 是否正在加载，用于判断回复是否完成
 */
export const useAutoScroll = (messages, isLoading) => {
  const scrollContainerRef = useRef(null) // 滚动容器引用
  const scrollBottomRef = useRef(null)    // 底部元素引用
  const [autoScroll, setAutoScroll] = useState(true) // 是否自动滚动
  const prevScrollHeightRef = useRef(0)   // 记录上次的滚动高度
  const loadingStartHeightRef = useRef(0) // 记录开始加载时的高度
  const prevMessageCountRef = useRef(0)   // 记录上次的消息数量
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
    const container = scrollContainerRef.current
    if (!container) return

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
   * 监听 isLoading 变化，记录开始/结束加载时的高度
   */
  useEffect(() => {
    const container = scrollContainerRef.current
    if (!container) return

    if (isLoading) {
      // 开始加载时，记录当前高度
      loadingStartHeightRef.current = container.scrollHeight
      prevScrollHeightRef.current = container.scrollHeight
    } else {
      // 加载结束时，检查是否超过一屏
      const { scrollHeight, clientHeight } = container
      const totalDelta = scrollHeight - loadingStartHeightRef.current
      
      if (totalDelta > clientHeight) {
        // 总增量超过一屏，进入阅读模式
        setAutoScroll(false)
      }
      
      // 更新记录
      prevScrollHeightRef.current = scrollHeight
    }
  }, [isLoading])

  /**
   * 实时滚动：监听 messages 变化，流式显示时实时滚动
   */
  useEffect(() => {
    const container = scrollContainerRef.current
    if (!container) return

    const { scrollHeight, clientHeight, scrollTop } = container
    const prevScrollHeight = prevScrollHeightRef.current
    const currentMessageCount = messages.length
    const prevMessageCount = prevMessageCountRef.current

    // 计算本次新增的高度
    const deltaHeight = scrollHeight - prevScrollHeight

    // 检测是否有新消息加入（用户发送消息或助手回复）
    const hasNewMessage = currentMessageCount > prevMessageCount
    
    // 检测新增的消息中是否包含用户消息
    // 因为用户发送消息时会同时添加用户消息和助手占位符（两条消息）
    // 所以需要检查新增的消息而不是最后一条
    let hasUserMessageInNew = false
    if (hasNewMessage) {
      const newMessageCount = currentMessageCount - prevMessageCount
      // 检查新增的消息
      for (let i = currentMessageCount - newMessageCount; i < currentMessageCount; i++) {
        if (messages[i] && messages[i].role === 'user') {
          hasUserMessageInNew = true
          break
        }
      }
    }
    
    if (hasNewMessage && hasUserMessageInNew) {
      // 用户发送了新消息，强制滚动到底部并启用自动滚动
      setAutoScroll(true)
      scrollToBottom()
      prevMessageCountRef.current = currentMessageCount
      prevScrollHeightRef.current = scrollHeight
      return
    } else if (hasNewMessage && !isLoading) {
      // 有新消息但不包含用户消息且不在加载中（理论上不会进入此分支）
      // 强制滚动到底部并启用自动滚动
      setAutoScroll(true)
      scrollToBottom()
      prevMessageCountRef.current = currentMessageCount
      prevScrollHeightRef.current = scrollHeight
      return
    } else if (hasNewMessage && isLoading) {
      // 有新消息加入且正在加载（助手开始回复）
      // 只更新计数，让后续的智能滚动逻辑处理
      prevMessageCountRef.current = currentMessageCount
    }

    if (deltaHeight > 0 && autoScroll) {
      // 有新内容且处于自动滚动模式
      if (isLoading) {
        // 检查从开始加载到现在的总增量
        const totalDelta = scrollHeight - loadingStartHeightRef.current
        
        if (totalDelta <= clientHeight) {
          // 总增量不超过一屏，继续实时滚动
          scrollToBottom()
        } else {
          // 总增量已超过一屏，停止自动滚动
          setAutoScroll(false)
        }
      }
      // 更新记录
      prevScrollHeightRef.current = scrollHeight
    } else if (deltaHeight > 0 && !autoScroll) {
      // 有新内容但不自动滚动，只更新记录
      prevScrollHeightRef.current = scrollHeight
    }
  }, [messages, autoScroll, isLoading, scrollToBottom])

  return {
    scrollContainerRef,  // 需要绑定到滚动容器上
    scrollBottomRef,     // 需要绑定到底部元素上
    autoScroll,          // 当前是否处于自动滚动模式
  }
}
