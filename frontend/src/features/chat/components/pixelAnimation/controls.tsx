/**
 * 动画控制组件
 * 提供播放、暂停、重置等控制功能
 */

import './controls.css'

interface AnimationControlsProps {
  isPlaying: boolean
  onTogglePlay: () => void
  onReset: () => void
}

/**
 * 动画控制按钮组
 */
function AnimationControls({ isPlaying, onTogglePlay, onReset }: AnimationControlsProps) {
  return (
    <div className="pixel-animation-controls">
      <button onClick={onTogglePlay} className="control-btn">
        {isPlaying ? '⏸️ 暂停' : '▶️ 播放'}
      </button>
      <button onClick={onReset} className="control-btn">
        🔄 重置
      </button>
    </div>
  )
}

export default AnimationControls
