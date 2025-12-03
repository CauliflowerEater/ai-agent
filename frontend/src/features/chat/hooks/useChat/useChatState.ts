/**
 * 聊天状态管理 Hook
 * 管理消息列表、加载状态、会话ID等
 */

import { useState, useRef } from 'react'
import type { Message } from '../../types'

export function useChatState() {
  const [messages, setMessages] = useState<Message[]>([])
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const [chatId] = useState<string>(`user_${Date.now()}`) // 生成唯一会话ID
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
