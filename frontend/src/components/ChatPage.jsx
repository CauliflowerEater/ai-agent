import { useState, useRef, useEffect } from 'react'
import './ChatPage.css'

function ChatPage() {
  const [messages, setMessages] = useState([])
  const [inputValue, setInputValue] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const messagesEndRef = useRef(null)

  // 自动滚动到最新消息
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  // 发送消息
  const handleSendMessage = async () => {
    if (!inputValue.trim() || isLoading) return

    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: inputValue,
      timestamp: new Date().toLocaleTimeString()
    }

    setMessages(prev => [...prev, userMessage])
    setInputValue('')
    setIsLoading(true)

    try {
      // 调用后端API
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: inputValue
        })
      })

      if (!response.ok) {
        throw new Error('网络请求失败')
      }

      const data = await response.json()

      const assistantMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: data.message || data.data || '抱歉，我无法回答这个问题。',
        timestamp: new Date().toLocaleTimeString()
      }

      setMessages(prev => [...prev, assistantMessage])
    } catch (error) {
      console.error('发送消息失败:', error)
      const errorMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: '抱歉，发送消息失败，请稍后重试。',
        timestamp: new Date().toLocaleTimeString(),
        isError: true
      }
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setIsLoading(false)
    }
  }

  // 处理回车发送
  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  // 清空聊天记录
  const handleClearChat = () => {
    setMessages([])
  }

  return (
    <div className="chat-page">
      <div className="chat-header">
        <h2>AI 智能对话</h2>
        <button 
          className="clear-button" 
          onClick={handleClearChat}
          disabled={messages.length === 0}
        >
          清空对话
        </button>
      </div>

      <div className="chat-messages">
        {messages.length === 0 ? (
          <div className="empty-state">
            <p>👋 你好！我是AI助手，有什么可以帮助你的吗？</p>
          </div>
        ) : (
          messages.map(message => (
            <div 
              key={message.id} 
              className={`message ${message.role} ${message.isError ? 'error' : ''}`}
            >
              <div className="message-avatar">
                {message.role === 'user' ? '👤' : '🤖'}
              </div>
              <div className="message-content">
                <div className="message-text">{message.content}</div>
                <div className="message-time">{message.timestamp}</div>
              </div>
            </div>
          ))
        )}
        {isLoading && (
          <div className="message assistant loading">
            <div className="message-avatar">🤖</div>
            <div className="message-content">
              <div className="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="chat-input-area">
        <textarea
          className="chat-input"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="输入消息... (回车发送，Shift+回车换行)"
          disabled={isLoading}
          rows={1}
        />
        <button 
          className="send-button" 
          onClick={handleSendMessage}
          disabled={!inputValue.trim() || isLoading}
        >
          {isLoading ? '发送中...' : '发送'}
        </button>
      </div>
    </div>
  )
}

export default ChatPage
