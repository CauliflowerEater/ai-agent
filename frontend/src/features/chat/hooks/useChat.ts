import { useState, useCallback, useRef } from 'react'
import { sendMessageStream } from '../services/chatApi'
import { createUserMessage, createAssistantMessage } from '../utils/messageUtils'
import { DEFAULT_MESSAGES } from '../constants/messages'
import type { Message, UseChatReturn } from '../types'

/**
 * 聊天功能自定义Hook（SSE流式版本）
 */
export const useChat = (): UseChatReturn => {
  const [messages, setMessages] = useState<Message[]>([])
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const [chatId] = useState<string>(`user_${Date.now()}`) // 生成唯一会话ID
  const cancelRequestRef = useRef<(() => void) | null>(null)
  const hasInitializedRef = useRef<boolean>(false) // 标记是否已初始化

  // 发送消息（SSE流式）
  const handleSendMessage = useCallback(async (inputValue: string): Promise<void> => {
    if (!inputValue.trim() || isLoading) return

    // 添加用户消息
    const userMessage = createUserMessage(inputValue)
    setMessages(prev => [...prev, userMessage])
    setIsLoading(true)

    // 创建助手消息占位符
    let currentMessageId = 'assistant_' + Date.now()
    const assistantMessage = createAssistantMessage('', false, currentMessageId)
    setMessages(prev => [...prev, assistantMessage])

    // 分段相关变量
    const DELIM = '<chunk>'
    let textBuffer = '' // 用于缓存未完成的片段
    let currentSegmentContent = '' // 当前段落的内容

    // 取消上一次请求（如果存在）
    if (cancelRequestRef.current) {
      cancelRequestRef.current()
    }

    // 发送 SSE 流式请求
    cancelRequestRef.current = sendMessageStream(
      inputValue,
      chatId,
      // onChunk: 收到数据块
      (deltaText: string) => {
        // 1. 把本次增量追加到 buffer
        textBuffer += deltaText

        // 2. 反复查找是否出现了完整的分段标记
        while (true) {
          const idx = textBuffer.indexOf(DELIM)
          if (idx === -1) {
            // 没有完整标记,不显示,等待下一次 delta
            break
          }

          // 3. 取出标记前面的内容,作为一个完整段落
          const segment = textBuffer.slice(0, idx)
          // 4. 把 buffer 剩余部分（标记之后的内容）保留下来
          textBuffer = textBuffer.slice(idx + DELIM.length)

          // 5. 显示完整段落
          if (segment.trim()) {
            // 如果是第一个段落,更新初始气泡
            if (!currentSegmentContent) {
              currentSegmentContent = segment
              setMessages(prev => 
                prev.map(msg => 
                  msg.id === currentMessageId 
                    ? { ...msg, content: segment }
                    : msg
                )
              )
            } else {
              // 否则创建新气泡显示新段落
              currentMessageId = 'assistant_' + Date.now() + '_' + Math.random()
              const newMessage = createAssistantMessage(segment, false, currentMessageId)
              setMessages(prev => [...prev, newMessage])
              currentSegmentContent = segment
            }
          }
        }
      },
      // onComplete: 流结束
      () => {
        // 处理 buffer 中剩余的内容
        const cleaned = textBuffer.trim()
        if (cleaned) {
          currentSegmentContent += textBuffer
          setMessages(prev => 
            prev.map(msg => 
              msg.id === currentMessageId 
                ? { ...msg, content: currentSegmentContent }
                : msg
            )
          )
        }
        textBuffer = ''
        
        setIsLoading(false)
        cancelRequestRef.current = null
        
        // 如果没有收到任何内容，显示默认消息
        if (!currentSegmentContent.trim()) {
          setMessages(prev => 
            prev.map(msg => 
              msg.id === currentMessageId 
                ? { ...msg, content: DEFAULT_MESSAGES.EMPTY_RESPONSE }
                : msg
            )
          )
        }
      },
      // onError: 发生错误
      (error: Error) => {
        console.error('流式请求错误:', error)
        setIsLoading(false)
        cancelRequestRef.current = null
        
        // 显示错误消息
        setMessages(prev => 
          prev.map(msg => 
            msg.id === currentMessageId 
              ? { ...msg, content: DEFAULT_MESSAGES.ERROR, isError: true }
              : msg
          )
        )
      }
    )
  }, [isLoading, chatId])

  // 清空聊天记录
  const clearMessages = useCallback(() => {
    // 取消正在进行的请求
    if (cancelRequestRef.current) {
      cancelRequestRef.current()
      cancelRequestRef.current = null
    }
    setMessages([])
    setIsLoading(false)
    hasInitializedRef.current = false // 重置初始化标记
  }, [])

  // 发送初始消息
  const sendInitialMessage = useCallback(() => {
    if (!hasInitializedRef.current && messages.length === 0 && !isLoading) {
      hasInitializedRef.current = true
      handleSendMessage(DEFAULT_MESSAGES.INITIAL)
    }
  }, [messages.length, isLoading, handleSendMessage])

  return {
    messages,
    isLoading,
    handleSendMessage,
    clearMessages,
    sendInitialMessage,
  }
}
