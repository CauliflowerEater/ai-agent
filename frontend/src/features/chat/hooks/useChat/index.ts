/**
 * 聊天功能 Hook
 * 整合状态管理和操作逻辑，提供完整的聊天功能
 * 
 * 功能：
 * 1. SSE 流式消息接收
 * 2. 分段文本处理（支持多气泡）
 * 3. 消息列表管理
 * 4. 请求取消控制
 */

import { useChatState } from './useChatState'
import { useChatActions } from './useChatActions'
import type { UseChatReturn } from './types'

export function useChat(): UseChatReturn {
  // 1. 状态管理
  const {
    messages,
    setMessages,
    isLoading,
    setIsLoading,
    chatId,
    hasInitialized,
    setHasInitialized,
    cancelRequestRef
  } = useChatState()

  // 2. 操作方法
  const {
    handleSendMessage,
    clearMessages,
    sendInitialMessage
  } = useChatActions({
    messages,
    setMessages,
    isLoading,
    setIsLoading,
    chatId,
    hasInitialized,
    setHasInitialized,
    cancelRequestRef
  })

  return {
    messages,
    isLoading,
    handleSendMessage,
    clearMessages,
    sendInitialMessage
  }
}

// 导出类型
export type { UseChatReturn } from './types'
