import './TypingIndicator.css'

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
