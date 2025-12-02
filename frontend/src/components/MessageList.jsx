import { useEffect, useRef, useCallback } from 'react'
import MessageItem from './MessageItem'
import { MESSAGE_ROLES } from '../constants/messages'
import './MessageList.css'

function MessageList({ messages, isLoading, scrollContainerRef, scrollBottomRef, onInitialRequest }) {
  const lastUserMessageRef = useRef(null)

  // 获取最后一条用户消息的ref的方法
  const getLastUserMessageRef = useCallback(() => {
    return lastUserMessageRef.current
  }, [])

  // 将获取ref的方法暴露到window（用于外部访问）
  useEffect(() => {
    window.getLastUserMessageRef = getLastUserMessageRef
    return () => {
      delete window.getLastUserMessageRef
    }
  }, [getLastUserMessageRef])
  // 当消息为空时，发起初始请求
  useEffect(() => {
    if (messages.length === 0 && !isLoading && onInitialRequest) {
      onInitialRequest()
    }
  }, [messages.length, isLoading, onInitialRequest])

  return (
    <div className="chat-messages" ref={scrollContainerRef}>
      {/* 消息列表 */}
      {messages.map((message, index) => {
        // 判断是否为最后一条用户消息
        const isLastUserMessage = message.role === MESSAGE_ROLES.USER && 
          index === messages.map(m => m.role).lastIndexOf(MESSAGE_ROLES.USER)
        
        return (
          <MessageItem 
            key={message.id} 
            message={message} 
            ref={isLastUserMessage ? lastUserMessageRef : null}
          />
        )
      })}
      
      <div ref={scrollBottomRef} />
    </div>
  )
}

export default MessageList
