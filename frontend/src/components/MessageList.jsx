import { useEffect } from 'react'
import MessageItem from './MessageItem'
import './MessageList.css'

function MessageList({ messages, isLoading, scrollContainerRef, scrollBottomRef, onInitialRequest }) {
  // 当消息为空时，发起初始请求
  useEffect(() => {
    if (messages.length === 0 && !isLoading && onInitialRequest) {
      onInitialRequest()
    }
  }, [messages.length, isLoading, onInitialRequest])

  return (
    <div className="chat-messages" ref={scrollContainerRef}>
      {messages.map(message => (
        <MessageItem key={message.id} message={message} />
      ))}
      <div ref={scrollBottomRef} />
    </div>
  )
}

export default MessageList
