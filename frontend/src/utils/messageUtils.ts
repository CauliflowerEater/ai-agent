import { MESSAGE_ROLES } from '../constants/messages'
import type { Message, MessageRole } from '../types'

/**
 * 创建消息对象
 * @param role - 消息角色
 * @param content - 消息内容
 * @param isError - 是否为错误消息
 * @param id - 消息ID（可选）
 * @returns 消息对象
 */
export const createMessage = (
  role: MessageRole,
  content: string,
  isError: boolean = false,
  id?: string
): Message => {
  return {
    id: id || `${Date.now()}_${Math.random()}`,
    role,
    content,
    timestamp: new Date().toLocaleTimeString(),
    isError,
  }
}

/**
 * 创建用户消息
 * @param content - 消息内容
 * @returns 用户消息对象
 */
export const createUserMessage = (content: string): Message => {
  return createMessage(MESSAGE_ROLES.USER, content)
}

/**
 * 创建助手消息
 * @param content - 消息内容
 * @param isError - 是否为错误消息
 * @param id - 消息ID（可选）
 * @returns 助手消息对象
 */
export const createAssistantMessage = (
  content: string,
  isError: boolean = false,
  id?: string
): Message => {
  return createMessage(MESSAGE_ROLES.ASSISTANT, content, isError, id)
}

/**
 * 格式化时间
 * @param date - 日期对象
 * @returns 格式化后的时间
 */
export const formatTime = (date: Date = new Date()): string => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * 验证消息内容是否有效
 * @param content - 消息内容
 * @returns 是否有效
 */
export const validateMessage = (content: string): boolean => {
  return content.trim().length > 0 && content.length <= 2000
}

/**
 * 清理消息内容（去除多余空格）
 * @param content - 消息内容
 * @returns 清理后的内容
 */
export const sanitizeMessage = (content: string): string => {
  return content.trim().replace(/\s+/g, ' ')
}
