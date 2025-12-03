import './ChatHeader.css'

/**
 * ChatHeader 组件属性
 */
interface ChatHeaderProps {
  onClearChat: () => void
  hasMessages: boolean
}

/**
 * 聊天头部组件
 * 显示标题和清空按钮
 */
function ChatHeader({ onClearChat, hasMessages }: ChatHeaderProps) {
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
