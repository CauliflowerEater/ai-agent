import { useEffect } from 'react'
import MessageItem from './MessageItem'
import './MessageList.css'

function MessageList({ messages, isLoading, scrollRef, onInitialRequest }) {
  // 当消息为空时，发起初始请求
  useEffect(() => {
    if (messages.length === 0 && !isLoading && onInitialRequest) {
      onInitialRequest()
    }
  }, [messages.length, isLoading, onInitialRequest])

  return (
    <div className="chat-messages">
      {messages.map(message => (
        <MessageItem key={message.id} message={message} />
      ))}
      <div ref={scrollRef} />
    </div>
  )
}

export default MessageList
