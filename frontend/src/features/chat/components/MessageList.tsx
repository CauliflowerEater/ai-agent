import { useEffect, useRef, useCallback, RefObject } from 'react'
import MessageItem from './MessageItem'
import { MESSAGE_ROLES } from '../constants/messages'
import type { Message } from '../types'
import './MessageList.css'

/**
 * MessageList 组件属性
 */
interface MessageListProps {
  messages: Message[]
  isLoading: boolean
  scrollContainerRef: RefObject<HTMLDivElement>
  scrollBottomRef: RefObject<HTMLDivElement>
  onInitialRequest?: () => void
}

/**
 * 消息列表组件
 * 显示所有聊天消息
 */
function MessageList({ 
  messages, 
  isLoading, 
  scrollContainerRef, 
  scrollBottomRef, 
  onInitialRequest 
}: MessageListProps) {
  const lastUserMessageRef = useRef<HTMLDivElement>(null)

  // 获取最后一条用户消息的ref的方法
  const getLastUserMessageRef = useCallback((): HTMLDivElement | null => {
    return lastUserMessageRef.current
  }, [])

  // 将获取ref的方法暴露到window（用于外部访问）
  useEffect(() => {
    (window as any).getLastUserMessageRef = getLastUserMessageRef
    return () => {
      delete (window as any).getLastUserMessageRef
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
