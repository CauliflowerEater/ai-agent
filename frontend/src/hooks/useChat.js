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
  const [chatId] = useState('user_' + Date.now()) // 生成唯一会话ID

  // 发送消息
  const handleSendMessage = useCallback(async (inputValue) => {
    if (!inputValue.trim() || isLoading) return

    // 添加用户消息
    const userMessage = createUserMessage(inputValue)
    setMessages(prev => [...prev, userMessage])
    setIsLoading(true)

    try {
      // 调用API，传入chatId保持会话上下文
      const data = await sendMessage(inputValue, chatId)

      // 添加助手回复 - 后端返回 { reply, chatId }
      const content = data.reply || DEFAULT_MESSAGES.EMPTY_RESPONSE
      const assistantMessage = createAssistantMessage(content)
      setMessages(prev => [...prev, assistantMessage])
    } catch (error) {
      // 添加错误消息
      const errorMessage = createAssistantMessage(DEFAULT_MESSAGES.ERROR, true)
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setIsLoading(false)
    }
  }, [isLoading, chatId])

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
