import type { Message } from './message'

/**
 * 聊天请求参数（对应后端 ChatRequest.java）
 */
export interface ChatRequest {
  /** 用户消息 */
  message: string
  /** 会话ID */
  chatId: string
}

/**
 * 聊天响应（对应后端 ChatResponse.java）
 */
export interface ChatResponse {
  /** 状态码 */
  code: number
  /** 提示信息 */
  message: string
  /** AI回复内容 */
  data: string
}

/**
 * SSE流式回调函数类型
 */
export type OnChunkCallback = (deltaText: string) => void
export type OnCompleteCallback = () => void
export type OnErrorCallback = (error: Error) => void
export type CancelRequestFn = () => void

/**
 * useChat Hook 返回值类型
 */
export interface UseChatReturn {
  messages: Message[]
  isLoading: boolean
  handleSendMessage: (message: string) => Promise<void>
  clearMessages: () => void
  sendInitialMessage: () => void
}
