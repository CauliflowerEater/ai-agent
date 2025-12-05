import ChatHeader from './ChatHeader'
import MessageList from './MessageList'
import ChatInput from './ChatInput'
import PixelAnimation from './pixelAnimation'
import { useChat } from '../hooks/useChat'
import { useAutoScroll } from '../hooks/useAutoScroll'
import { PIXEL_ANIMATION_CONFIG } from '../constants/animation'
import './ChatPage.css'

/**
 * 聊天页面主组件
 * 集成聊天功能的所有子组件
 */
function ChatPage() {
  const { messages, isLoading, handleSendMessage, clearMessages, sendInitialMessage } = useChat()
  const { scrollContainerRef, scrollBottomRef } = useAutoScroll(messages)

  return (
    <div className="chat-page">
      <div className="animation-section">
        <PixelAnimation 
          frames={PIXEL_ANIMATION_CONFIG.FRAMES} 
          width={PIXEL_ANIMATION_CONFIG.WIDTH}
          height={PIXEL_ANIMATION_CONFIG.HEIGHT}
          fps={PIXEL_ANIMATION_CONFIG.FPS}
          loop={PIXEL_ANIMATION_CONFIG.LOOP}
          scale={PIXEL_ANIMATION_CONFIG.SCALE}
          autoPlay={PIXEL_ANIMATION_CONFIG.AUTO_PLAY}
        />
      </div>
      <div className="chat-section">
        <ChatHeader 
          onClearChat={clearMessages} 
          hasMessages={messages.length > 0} 
        />
        <MessageList 
          messages={messages} 
          isLoading={isLoading} 
          scrollContainerRef={scrollContainerRef}
          scrollBottomRef={scrollBottomRef}
          onInitialRequest={sendInitialMessage}
        />
        <ChatInput 
          onSendMessage={handleSendMessage} 
          isLoading={isLoading} 
        />
      </div>
    </div>
  )
}

export default ChatPage
