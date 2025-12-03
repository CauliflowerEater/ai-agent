import type { MessageRole } from '../types'

/**
 * 消息角色常量
 */
export const MESSAGE_ROLES: Record<'USER' | 'ASSISTANT', MessageRole> = {
  USER: 'user',
  ASSISTANT: 'assistant',
} as const

/**
 * 默认消息
 */
export const DEFAULT_MESSAGES = {
  ERROR: '抱歉，发送消息失败，请稍后重试。',
  EMPTY_RESPONSE: '抱歉，我无法回答这个问题。',
  WELCOME: '👋 你好！我是AI助手，有什么可以帮助你的吗？',
  INITIAL: '你好', 
} as const

/**
 * 消息提示文本
 */
export const MESSAGE_PLACEHOLDERS = {
  INPUT: '输入消息... (回车发送，Shift+回车换行)',
} as const
