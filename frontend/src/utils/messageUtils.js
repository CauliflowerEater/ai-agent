import { MESSAGE_ROLES } from '../constants/messages'

/**
 * 创建消息对象
 * @param {string} role - 消息角色
 * @param {string} content - 消息内容
 * @param {boolean} isError - 是否为错误消息
 * @returns {Object} 消息对象
 */
export const createMessage = (role, content, isError = false) => {
  return {
    id: Date.now() + Math.random(),
    role,
    content,
    timestamp: new Date().toLocaleTimeString(),
    isError,
  }
}

/**
 * 创建用户消息
 * @param {string} content - 消息内容
 * @returns {Object} 用户消息对象
 */
export const createUserMessage = (content) => {
  return createMessage(MESSAGE_ROLES.USER, content)
}

/**
 * 创建助手消息
 * @param {string} content - 消息内容
 * @param {boolean} isError - 是否为错误消息
 * @returns {Object} 助手消息对象
 */
export const createAssistantMessage = (content, isError = false) => {
  return createMessage(MESSAGE_ROLES.ASSISTANT, content, isError)
}

/**
 * 格式化时间
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的时间
 */
export const formatTime = (date = new Date()) => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}
