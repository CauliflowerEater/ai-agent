import MessageItem from './MessageItem'
import TypingIndicator from './TypingIndicator'
import { DEFAULT_MESSAGES } from '../constants/messages'
import './MessageList.css'

function MessageList({ messages, isLoading, scrollRef }) {
  return (
    <div className="chat-messages">
      {messages.length === 0 ? (
        <div className="empty-state">
          <p>{DEFAULT_MESSAGES.WELCOME}</p>
        </div>
      ) : (
        messages.map(message => (
          <MessageItem key={message.id} message={message} />
        ))
      )}
      {isLoading && <TypingIndicator />}
      <div ref={scrollRef} />
    </div>
  )
}

export default MessageList
