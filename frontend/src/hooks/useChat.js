import { useState, useCallback } from 'react'
import { sendMessage } from '../api/chatApi'
import { createUserMessage, createAssistantMessage } from '../utils/messageUtils'
import { DEFAULT_MESSAGES } from '../constants/messages'

/**
 * 聊天功能自定义Hook
 */
export const useChat = () => {
  const [messages, setMessages] = useState([])
  const [isLoading, setIsLoading] = useState(false)

  // 发送消息
  const handleSendMessage = useCallback(async (inputValue) => {
    if (!inputValue.trim() || isLoading) return

    // 添加用户消息
    const userMessage = createUserMessage(inputValue)
    setMessages(prev => [...prev, userMessage])
    setIsLoading(true)

    try {
      // 调用API
      const data = await sendMessage(inputValue)

      // 添加助手回复
      const content = data.message || data.data || DEFAULT_MESSAGES.EMPTY_RESPONSE
      const assistantMessage = createAssistantMessage(content)
      setMessages(prev => [...prev, assistantMessage])
    } catch (error) {
      // 添加错误消息
      const errorMessage = createAssistantMessage(DEFAULT_MESSAGES.ERROR, true)
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setIsLoading(false)
    }
  }, [isLoading])

  // 清空聊天记录
  const clearMessages = useCallback(() => {
    setMessages([])
  }, [])

  return {
    messages,
    isLoading,
    handleSendMessage,
    clearMessages,
  }
}
