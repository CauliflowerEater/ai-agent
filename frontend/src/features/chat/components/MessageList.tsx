import { useEffect, useMemo, RefObject } from 'react'
import { useScrollStore } from '../../../stores'
import MessageItem from './MessageItem'
import { MESSAGE_ROLES } from '../constants/messages'
import type { Message } from '../types/message'
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
  const setLastUserMessageId = useScrollStore((state) => state.setLastUserMessageId)

  // 更新最后一条用户消息的 ID
  useEffect(() => {
    // 查找最后一条用户消息
    const lastUserMessageIndex = messages.map(m => m.role).lastIndexOf(MESSAGE_ROLES.USER)
    if (lastUserMessageIndex !== -1) {
      const lastUserMessage = messages[lastUserMessageIndex]
      setLastUserMessageId(`message-${lastUserMessage.id}`)
    } else {
      setLastUserMessageId(null)
    }
  }, [messages, setLastUserMessageId])
  
  // 当消息为空时，发起初始请求
  useEffect(() => {
    if (messages.length === 0 && !isLoading && onInitialRequest) {
      onInitialRequest()
    }
  }, [messages.length, isLoading, onInitialRequest])

  // 优化：将重复计算提取到组件外，使用 useMemo 缓存
  const lastUserMessageIndex = useMemo(
    () => messages.map(m => m.role).lastIndexOf(MESSAGE_ROLES.USER),
    [messages]
  )

  return (
    <div className="chat-messages" ref={scrollContainerRef}>
      {/* 消息列表 */}
      {messages.map((message, index) => {
        // 判断是否为最后一条用户消息
        const isLastUserMessage = message.role === MESSAGE_ROLES.USER && 
          index === lastUserMessageIndex
        
        return (
          <div 
            key={message.id}
            id={isLastUserMessage ? `message-${message.id}` : undefined}
          >
            <MessageItem message={message} />
          </div>
        )
      })}
      
      <div ref={scrollBottomRef} />
    </div>
  )
}

export default MessageList
