/**
 * 消息处理工具函数
 */

import type { Message } from '../../types'

/**
 * 生成助手消息 ID
 */
export function generateAssistantMessageId(): string {
  return 'assistant_' + Date.now()
}

/**
 * 生成新段落的消息 ID
 */
export function generateSegmentMessageId(): string {
  return 'assistant_' + Date.now() + '_' + Math.random()
}

/**
 * 更新消息内容
 */
export function updateMessageContent(
  messages: Message[],
  messageId: string,
  content: string,
  isError: boolean = false
): Message[] {
  return messages.map(msg =>
    msg.id === messageId
      ? { ...msg, content, isError }
      : msg
  )
}

/**
 * 添加新消息到列表
 */
export function addMessage(
  messages: Message[],
  newMessage: Message
): Message[] {
  return [...messages, newMessage]
}
