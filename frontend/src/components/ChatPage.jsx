import ChatHeader from './ChatHeader'
import MessageList from './MessageList'
import ChatInput from './ChatInput'
import PixelAnimation from './PixelAnimation'
import { useChat } from '../hooks/useChat'
import { useAutoScroll } from '../hooks/useAutoScroll'
import './ChatPage.css'

function ChatPage() {
  const { messages, isLoading, handleSendMessage, clearMessages, sendInitialMessage } = useChat()
  const scrollRef = useAutoScroll([messages])

  return (
    <div className="chat-page">
      <div className="animation-section">
        <PixelAnimation 
          frames={['/Cuty/Speaking_0.png', '/Cuty/Speaking_1.png']} 
          width={2048}
          height={2048}
          fps={2}
          loop={true}
          scale={0.15}
          autoPlay={true}
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
          scrollRef={scrollRef}
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
