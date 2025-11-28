import { useState } from 'react'
import { MESSAGE_PLACEHOLDERS } from '../constants/messages'
import './ChatInput.css'

function ChatInput({ onSendMessage, isLoading }) {
  const [inputValue, setInputValue] = useState('')

  const handleSend = () => {
    if (inputValue.trim() && !isLoading) {
      onSendMessage(inputValue)
      setInputValue('')
    }
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="chat-input-area">
      <textarea
        className="chat-input"
        value={inputValue}
        onChange={(e) => setInputValue(e.target.value)}
        onKeyPress={handleKeyPress}
        placeholder={MESSAGE_PLACEHOLDERS.INPUT}
        disabled={isLoading}
        rows={1}
      />
      <button 
        className="send-button" 
        onClick={handleSend}
        disabled={!inputValue.trim() || isLoading}
      >
        {isLoading ? '发送中...' : '发送'}
      </button>
    </div>
  )
}

export default ChatInput
