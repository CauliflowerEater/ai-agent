/**
 * 消息角色类型
 */
export type MessageRole = 'user' | 'assistant'

/**
 * 消息对象接口
 */
export interface Message {
  /** 消息唯一标识 */
  id: string
  /** 消息角色 */
  role: MessageRole
  /** 消息内容 */
  content: string
  /** 时间戳 */
  timestamp: string
  /** 是否为错误消息 */
  isError?: boolean
}

/**
 * 消息创建参数
 */
export interface CreateMessageParams {
  role: MessageRole
  content: string
  isError?: boolean
  id?: string
}
