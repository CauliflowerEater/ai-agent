import { MESSAGE_ROLES } from '../constants/messages'
import './MessageItem.css'

function MessageItem({ message }) {
  const isUser = message.role === MESSAGE_ROLES.USER
  
  return (
    <div className={`message ${message.role} ${message.isError ? 'error' : ''}`}>
      <div className="message-avatar">
        {isUser ? '👤' : '🤖'}
      </div>
      <div className="message-content">
        <div className="message-text">{message.content}</div>
        <div className="message-time">{message.timestamp}</div>
      </div>
    </div>
  )
}

export default MessageItem
