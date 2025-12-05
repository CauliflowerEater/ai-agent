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

export default MessageItem
