import './TypingIndicator.css'

/**
 * 打字指示器组件
 * 用于显示 AI 正在输入的动画效果
 */
function TypingIndicator() {
  return (
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
  )
}

export default TypingIndicator
