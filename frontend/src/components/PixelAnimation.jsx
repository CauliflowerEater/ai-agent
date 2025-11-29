import { useEffect, useRef, useState } from 'react'
import * as PIXI from 'pixi.js'
import './PixelAnimation.css'

/**
 * 像素动画播放组件
 * @param {Object} props
 * @param {string} props.spriteSheet - 精灵图路径（与frames二选一）
 * @param {Array<string>} props.frames - 独立图片路径数组（与spriteSheet二选一）
 * @param {number} props.frameWidth - 每帧宽度（spriteSheet模式必填）
 * @param {number} props.frameHeight - 每帧高度（spriteSheet模式必填）
 * @param {number} props.frameCount - 总帧数（spriteSheet模式必填）
 * @param {number} props.width - 画布宽度（frames模式使用）
 * @param {number} props.height - 画布高度（frames模式使用）
 * @param {number} props.fps - 帧率 (默认12)
 * @param {boolean} props.loop - 是否循环播放 (默认true)
 * @param {number} props.scale - 缩放比例 (默认2)
 */
const PixelAnimation = ({
  spriteSheet,
  frames,
  frameWidth = 96,
  frameHeight = 96,
  frameCount = 1,
  width=96,
  height=96,
  fps = 12,
  loop = true,
  scale = 1,
  autoPlay = true
}) => {
  const canvasRef = useRef(null)
  const appRef = useRef(null)
  const animatedSpriteRef = useRef(null)
  const [isPlaying, setIsPlaying] = useState(autoPlay)

  useEffect(() => {
    if (!canvasRef.current) return

    // 计算画布尺寸
    const canvasWidth = width ? width * scale : frameWidth * scale
    const canvasHeight = height ? height * scale : frameHeight * scale

    let app = null
    let mounted = true

    // 创建PIXI应用（使用v8新API）
    const initApp = async () => {
      try {
        app = new PIXI.Application()
        await app.init({
          width: canvasWidth,
          height: canvasHeight,
          backgroundColor: 0x000000,
          antialias: false, // 像素风格不需要抗锯齿
          resolution: 1
        })

        // 检查组件是否仍然挂载
        if (!mounted || !canvasRef.current) {
          app.destroy(true)
          return
        }

        canvasRef.current.appendChild(app.canvas)
        appRef.current = app

        // 加载动画：优先使用独立图片模式
        if (frames && frames.length > 0) {
          await loadAnimationFromFrames(app)
        } else if (spriteSheet) {
          await loadAnimationFromSpriteSheet(app)
        } else {
          // 如果没有提供任何资源，显示占位符
          console.warn('PixelAnimation: 未提供图片资源（frames 或 spriteSheet）')
        }
      } catch (error) {
        console.error('初始化PIXI应用失败:', error)
      }
    }

    initApp()

    return () => {
      mounted = false
      // 清理资源
      if (animatedSpriteRef.current) {
        animatedSpriteRef.current.destroy()
        animatedSpriteRef.current = null
      }
      if (appRef.current) {
        appRef.current.destroy(true, { children: true, texture: true })
        appRef.current = null
      }
    }
  }, [spriteSheet, frames, frameWidth, frameHeight, frameCount, width, height, scale])

  // 从精灵图加载动画
  const loadAnimationFromSpriteSheet = async (app) => {
    try {
      // 加载纹理
      const texture = await PIXI.Assets.load(spriteSheet)
      
      // 创建帧数组
      const frameTextures = []
      for (let i = 0; i < frameCount; i++) {
        const frame = new PIXI.Texture(
          texture,
          new PIXI.Rectangle(i * frameWidth, 0, frameWidth, frameHeight)
        )
        frameTextures.push(frame)
      }

      createAnimatedSprite(app, frameTextures)
    } catch (error) {
      console.error('加载精灵图动画失败:', error)
    }
  }

  // 从独立图片加载动画
  const loadAnimationFromFrames = async (app) => {
    try {
      // 加载所有图片
      const textures = await Promise.all(
        frames.map(framePath => PIXI.Assets.load(framePath))
      )

      createAnimatedSprite(app, textures)
    } catch (error) {
      console.error('加载独立图片动画失败:', error)
    }
  }

  // 创建动画精灵
  const createAnimatedSprite = (app, textures) => {
    const animatedSprite = new PIXI.AnimatedSprite(textures)
    animatedSprite.anchor.set(0)
    animatedSprite.scale.set(scale)
    animatedSprite.animationSpeed = fps / 60 // PIXI使用60fps作为基准
    animatedSprite.loop = loop

    if (isPlaying) {
      animatedSprite.play()
    }

    app.stage.addChild(animatedSprite)
    animatedSpriteRef.current = animatedSprite
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
