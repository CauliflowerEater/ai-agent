import React from 'react'
import { MESSAGE_ROLES } from '../constants/messages'
import { AVATAR_CONFIG } from '../constants/animation'
import type { Message } from '../types/message'
import './MessageItem.css'

/**
 * MessageItem 组件属性
 */
interface MessageItemProps {
  message: Message
}

/**
 * 消息项组件
 * 显示单条消息（用户或 AI）
 * 使用 React.memo 优化，避免不必要的重新渲染
 */
function MessageItem({ message }: MessageItemProps) {
  const isUser = message.role === MESSAGE_ROLES.USER
  
  return (
    <div className={`message ${message.role} ${message.isError ? 'error' : ''}`}>
      <div className="message-avatar">
        {isUser ? (
          AVATAR_CONFIG.USER
        ) : (
          <img src={AVATAR_CONFIG.ASSISTANT} alt="对面" className="avatar-image" />
        )}
      </div>
      <div className="message-content">
        <div className="message-text">{message.content}</div>
        <div className="message-time">{message.timestamp}</div>
      </div>
    </div>
  )
}

// 使用 React.memo 且自定义比较函数，只在关键字段变化时才重新渲染
export default React.memo(MessageItem, (prevProps, nextProps) => {
  // 返回 true 表示 props 相同，不需要重新渲染
  return (
    prevProps.message.id === nextProps.message.id &&
    prevProps.message.content === nextProps.message.content &&
    prevProps.message.isError === nextProps.message.isError
  )
})
