import './ChatHeader.css'

function ChatHeader({ onClearChat, hasMessages }) {
  return (
    <div className="chat-header">
      <h2>社区聊天室</h2>
      <button 
        className="clear-button" 
        onClick={onClearChat}
        disabled={!hasMessages}
      >
        清空对话
      </button>
    </div>
  )
}

export default ChatHeader
