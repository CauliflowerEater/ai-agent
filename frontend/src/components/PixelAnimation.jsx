import { useEffect, useRef, useState } from 'react'
import * as PIXI from 'pixi.js'
import './PixelAnimation.css'

/**
 * 像素动画播放组件
 * @param {Object} props
 * @param {string} props.spriteSheet - 精灵图路径
 * @param {number} props.frameWidth - 每帧宽度
 * @param {number} props.frameHeight - 每帧高度
 * @param {number} props.frameCount - 总帧数
 * @param {number} props.fps - 帧率 (默认12)
 * @param {boolean} props.loop - 是否循环播放 (默认true)
 * @param {number} props.scale - 缩放比例 (默认2)
 */
const PixelAnimation = ({
  spriteSheet,
  frameWidth = 32,
  frameHeight = 32,
  frameCount = 1,
  fps = 12,
  loop = true,
  scale = 2,
  autoPlay = true
}) => {
  const canvasRef = useRef(null)
  const appRef = useRef(null)
  const animatedSpriteRef = useRef(null)
  const [isPlaying, setIsPlaying] = useState(autoPlay)

  useEffect(() => {
    if (!canvasRef.current) return

    // 创建PIXI应用
    const app = new PIXI.Application({
      width: frameWidth * scale,
      height: frameHeight * scale,
      backgroundColor: 0x000000,
      antialias: false, // 像素风格不需要抗锯齿
      resolution: 1
    })

    canvasRef.current.appendChild(app.view)
    appRef.current = app

    // 加载精灵图
    if (spriteSheet) {
      loadAnimation(app)
    }

    return () => {
      // 清理资源
      if (animatedSpriteRef.current) {
        animatedSpriteRef.current.destroy()
      }
      app.destroy(true, { children: true, texture: true })
    }
  }, [spriteSheet, frameWidth, frameHeight, frameCount, scale])

  // 加载动画
  const loadAnimation = async (app) => {
    try {
      // 加载纹理
      const texture = await PIXI.Assets.load(spriteSheet)
      
      // 创建帧数组
      const frames = []
      for (let i = 0; i < frameCount; i++) {
        const frame = new PIXI.Texture(
          texture,
          new PIXI.Rectangle(i * frameWidth, 0, frameWidth, frameHeight)
        )
        frames.push(frame)
      }

      // 创建动画精灵
      const animatedSprite = new PIXI.AnimatedSprite(frames)
      animatedSprite.anchor.set(0)
      animatedSprite.scale.set(scale)
      animatedSprite.animationSpeed = fps / 60 // PIXI使用60fps作为基准
      animatedSprite.loop = loop

      if (isPlaying) {
        animatedSprite.play()
      }

      app.stage.addChild(animatedSprite)
      animatedSpriteRef.current = animatedSprite

    } catch (error) {
      console.error('加载动画失败:', error)
    }
  }

  // 控制播放/暂停
  useEffect(() => {
    if (animatedSpriteRef.current) {
      if (isPlaying) {
        animatedSpriteRef.current.play()
      } else {
        animatedSpriteRef.current.stop()
      }
    }
  }, [isPlaying])

  const togglePlay = () => {
    setIsPlaying(prev => !prev)
  }

  const reset = () => {
    if (animatedSpriteRef.current) {
      animatedSpriteRef.current.gotoAndStop(0)
      setIsPlaying(false)
    }
  }

  return (
    <div className="pixel-animation-container">
      <div ref={canvasRef} className="pixel-animation-canvas" />
      <div className="pixel-animation-controls">
        <button onClick={togglePlay} className="control-btn">
          {isPlaying ? '⏸️ 暂停' : '▶️ 播放'}
        </button>
        <button onClick={reset} className="control-btn">
          🔄 重置
        </button>
      </div>
    </div>
  )
}

export default PixelAnimation
