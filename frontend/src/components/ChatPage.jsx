import ChatHeader from './ChatHeader'
import MessageList from './MessageList'
import ChatInput from './ChatInput'
import { useChat } from '../hooks/useChat'
import { useAutoScroll } from '../hooks/useAutoScroll'
import './ChatPage.css'

function ChatPage() {
  const { messages, isLoading, handleSendMessage, clearMessages } = useChat()
  const scrollRef = useAutoScroll([messages])

  return (
    <div className="chat-page">
      <ChatHeader 
        onClearChat={clearMessages} 
        hasMessages={messages.length > 0} 
      />
      <MessageList 
        messages={messages} 
        isLoading={isLoading} 
        scrollRef={scrollRef} 
      />
      <ChatInput 
        onSendMessage={handleSendMessage} 
        isLoading={isLoading} 
      />
    </div>
  )
}

export default ChatPage
