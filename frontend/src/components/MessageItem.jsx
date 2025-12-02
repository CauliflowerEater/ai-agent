import { forwardRef } from 'react'
import { MESSAGE_ROLES } from '../constants/messages'
import './MessageItem.css'

const MessageItem = forwardRef(({ message }, ref) => {
  const isUser = message.role === MESSAGE_ROLES.USER
  
  return (
    <div ref={ref} className={`message ${message.role} ${message.isError ? 'error' : ''}`}>
      <div className="message-avatar">
        {isUser ? (
          '👤'
        ) : (
          <img src="/Cuty/Happy.png" alt="对面" className="avatar-image" />
        )}
      </div>
      <div className="message-content">
        <div className="message-text">{message.content}</div>
        <div className="message-time">{message.timestamp}</div>
      </div>
    </div>
  )
})

MessageItem.displayName = 'MessageItem'

export default MessageItem
