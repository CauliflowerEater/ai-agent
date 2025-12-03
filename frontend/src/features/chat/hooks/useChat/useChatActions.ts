/**
 * 聊天操作 Hook
 * 提供发送消息、清空消息、初始化消息等操作
 */

import { useCallback, Dispatch, SetStateAction, MutableRefObject } from 'react'
import { sendMessageStream } from '../../services/chatApi'
import { createUserMessage, createAssistantMessage } from '../../utils/messageUtils'
import { DEFAULT_MESSAGES } from '../../constants/messages'
import { generateAssistantMessageId, addMessage } from './messageUtils'
import { useStreamProcessor } from './useStreamProcessor'
import type { Message } from '../../types'

interface UseChatActionsParams {
  messages: Message[]
  setMessages: Dispatch<SetStateAction<Message[]>>
  isLoading: boolean
  setIsLoading: Dispatch<SetStateAction<boolean>>
  chatId: string
  cancelRequestRef: MutableRefObject<(() => void) | null>
  hasInitializedRef: MutableRefObject<boolean>
}

export function useChatActions({
  messages,
  setMessages,
  isLoading,
  setIsLoading,
  chatId,
  cancelRequestRef,
  hasInitializedRef
}: UseChatActionsParams) {
  
  // 获取流处理器
  const { createStreamHandlers } = useStreamProcessor(
    setMessages,
    setIsLoading,
    cancelRequestRef
  )

  /**
   * 发送消息（SSE流式）
   */
  const handleSendMessage = useCallback(async (inputValue: string): Promise<void> => {
    if (!inputValue.trim() || isLoading) return

    // 1. 添加用户消息
    const userMessage = createUserMessage(inputValue)
    setMessages(prev => addMessage(prev, userMessage))
    setIsLoading(true)

    // 2. 创建助手消息占位符
    const currentMessageId = generateAssistantMessageId()
    const assistantMessage = createAssistantMessage('', false, currentMessageId)
    setMessages(prev => addMessage(prev, assistantMessage))

    // 3. 取消上一次请求（如果存在）
    if (cancelRequestRef.current) {
      cancelRequestRef.current()
    }

    // 4. 创建流处理回调
    const handlers = createStreamHandlers(currentMessageId)

    // 5. 发送 SSE 流式请求
    cancelRequestRef.current = sendMessageStream(
      inputValue,
      chatId,
      handlers.onChunk,
      handlers.onComplete,
      handlers.onError
    )
  }, [isLoading, chatId, setMessages, setIsLoading, cancelRequestRef, createStreamHandlers])

  /**
   * 清空聊天记录
   */
  const clearMessages = useCallback(() => {
    // 取消正在进行的请求
    if (cancelRequestRef.current) {
      cancelRequestRef.current()
      cancelRequestRef.current = null
    }
    setMessages([])
    setIsLoading(false)
    hasInitializedRef.current = false // 重置初始化标记
  }, [setMessages, setIsLoading, cancelRequestRef, hasInitializedRef])

  /**
   * 发送初始消息
   */
  const sendInitialMessage = useCallback(() => {
    if (!hasInitializedRef.current && messages.length === 0 && !isLoading) {
      hasInitializedRef.current = true
      handleSendMessage(DEFAULT_MESSAGES.INITIAL)
    }
  }, [messages.length, isLoading, handleSendMessage, hasInitializedRef])

  return {
    handleSendMessage,
    clearMessages,
    sendInitialMessage
  }
}
