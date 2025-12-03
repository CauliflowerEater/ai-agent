/**
 * 像素动画播放组件（重构版）
 * 支持精灵图和独立图片两种模式
 */

import { useRef } from 'react'
import { usePixiAnimation } from './usePixiAnimation'
import AnimationControls from './controls'
import type { PixelAnimationProps } from './types'
import './PixelAnimation.css'

function PixelAnimation({
  spriteSheet,
  frames,
  frameWidth = 96,
  frameHeight = 96,
  frameCount = 1,
  width = 96,
  height = 96,
  fps = 12,
  loop = true,
  scale = 1,
  autoPlay = true
}: PixelAnimationProps) {
  const canvasRef = useRef<HTMLDivElement>(null)

  // 计算画布尺寸
  const canvasWidth = width ? width * scale : frameWidth * scale
  const canvasHeight = height ? height * scale : frameHeight * scale

  // 使用动画管理 Hook
  const { isPlaying, togglePlay, reset } = usePixiAnimation({
    containerRef: canvasRef,
    loadParams: {
      spriteSheet,
      frames,
      frameWidth,
      frameHeight,
      frameCount
    },
    canvasWidth,
    canvasHeight,
    fps,
    loop,
    scale,
    autoPlay
  })

  return (
    <div className="pixel-animation-container">
      <div ref={canvasRef} className="pixel-animation-canvas" />
      <AnimationControls 
        isPlaying={isPlaying}
        onTogglePlay={togglePlay}
        onReset={reset}
      />
    </div>
  )
}

export default PixelAnimation
