/**
 * useChat 相关类型定义
 */

import type { Message } from '../../types'

export interface UseChatReturn {
  messages: Message[]
  isLoading: boolean
  handleSendMessage: (message: string) => Promise<void>
  clearMessages: () => void
  sendInitialMessage: () => void
}

export interface StreamHandlers {
  onChunk: (deltaText: string) => void
  onComplete: () => void
  onError: (error: Error) => void
}
