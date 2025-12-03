import { useState, KeyboardEvent, ChangeEvent } from 'react'
import { MESSAGE_PLACEHOLDERS } from '../constants/messages'
import './ChatInput.css'

/**
 * ChatInput 组件属性
 */
interface ChatInputProps {
  onSendMessage: (message: string) => void
  isLoading: boolean
}

/**
 * 聊天输入组件
 * 用于输入和发送消息
 */
function ChatInput({ onSendMessage, isLoading }: ChatInputProps) {
  const [inputValue, setInputValue] = useState<string>('')

  const handleSend = () => {
    if (inputValue.trim() && !isLoading) {
      onSendMessage(inputValue)
      setInputValue('')
    }
  }

  const handleKeyPress = (e: KeyboardEvent<HTMLTextAreaElement>) => {
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
        onChange={(e: ChangeEvent<HTMLTextAreaElement>) => setInputValue(e.target.value)}
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
