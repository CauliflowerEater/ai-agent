/**
 * 聊天状态管理 Hook
 * 使用 Zustand 管理消息列表、加载状态、会话ID等
 */

import { useRef } from 'react'
import { useChatStore } from '../../../../stores'

export function useChatState() {
  const messages = useChatStore((state) => state.messages)
  const setMessages = useChatStore((state) => state.setMessages)
  const isLoading = useChatStore((state) => state.isLoading)
  const setIsLoading = useChatStore((state) => state.setIsLoading)
  const chatId = useChatStore((state) => state.chatId)
  
  const cancelRequestRef = useRef<(() => void) | null>(null)
  const hasInitializedRef = useRef<boolean>(false) // 标记是否已初始化

  return {
    messages,
    setMessages,
    isLoading,
    setIsLoading,
    chatId,
    cancelRequestRef,
    hasInitializedRef
  }
}
