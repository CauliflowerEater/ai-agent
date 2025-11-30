import { useState, useCallback, useRef, useEffect } from 'react'
import { sendMessageStream } from '../api/chatApi'
import { createUserMessage, createAssistantMessage } from '../utils/messageUtils'
import { DEFAULT_MESSAGES } from '../constants/messages'

/**
 * 聊天功能自定义Hook（SSE流式版本）
 */
export const useChat = () => {
  const [messages, setMessages] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [chatId] = useState('user_' + Date.now()) // 生成唯一会话ID
  const cancelRequestRef = useRef(null)
  const hasInitializedRef = useRef(false) // 标记是否已初始化

  // 发送消息（SSE流式）
  const handleSendMessage = useCallback(async (inputValue) => {
    if (!inputValue.trim() || isLoading) return

    // 添加用户消息
    const userMessage = createUserMessage(inputValue)
    setMessages(prev => [...prev, userMessage])
    setIsLoading(true)

    // 创建助手消息占位符
    const assistantMessageId = 'assistant_' + Date.now()
    const assistantMessage = createAssistantMessage('', false, assistantMessageId)
    setMessages(prev => [...prev, assistantMessage])

    let accumulatedContent = ''

    // 取消上一次请求（如果存在）
    if (cancelRequestRef.current) {
      cancelRequestRef.current()
    }

    // 发送 SSE 流式请求
    cancelRequestRef.current = sendMessageStream(
      inputValue,
      chatId,
      // onChunk: 收到数据块
      (chunk) => {
        accumulatedContent += chunk
        setMessages(prev => 
          prev.map(msg => 
            msg.id === assistantMessageId 
              ? { ...msg, content: accumulatedContent }
              : msg
          )
        )
      },
      // onComplete: 流结束
      () => {
        setIsLoading(false)
        cancelRequestRef.current = null
        
        // 如果没有收到任何内容，显示默认消息
        if (!accumulatedContent.trim()) {
          setMessages(prev => 
            prev.map(msg => 
              msg.id === assistantMessageId 
                ? { ...msg, content: DEFAULT_MESSAGES.EMPTY_RESPONSE }
                : msg
            )
          )
        }
      },
      // onError: 发生错误
      (error) => {
        console.error('流式请求错误:', error)
        setIsLoading(false)
        cancelRequestRef.current = null
        
        // 显示错误消息
        setMessages(prev => 
          prev.map(msg => 
            msg.id === assistantMessageId 
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
      handleSendMessage('你好')
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
