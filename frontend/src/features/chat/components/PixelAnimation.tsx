import { useEffect, useRef, useState } from 'react'
import * as PIXI from 'pixi.js'
import './PixelAnimation.css'

/**
 * PixelAnimation 组件属性
 */
interface PixelAnimationProps {
  /** 精灵图路径（与frames二选一） */
  spriteSheet?: string
  /** 独立图片路径数组（与spriteSheet二选一） */
  frames?: string[]
  /** 每帧宽度（spriteSheet模式必填） */
  frameWidth?: number
  /** 每帧高度（spriteSheet模式必填） */
  frameHeight?: number
  /** 总帧数（spriteSheet模式必填） */
  frameCount?: number
  /** 画布宽度（frames模式使用） */
  width?: number
  /** 画布高度（frames模式使用） */
  height?: number
  /** 帧率 (默认12) */
  fps?: number
  /** 是否循环播放 (默认true) */
  loop?: boolean
  /** 缩放比例 (默认1) */
  scale?: number
  /** 是否自动播放 */
  autoPlay?: boolean
}

/**
 * 像素动画播放组件
 * 支持精灵图和独立图片两种模式
 */
function PixelAnimation({
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
}: PixelAnimationProps) {
  const canvasRef = useRef<HTMLDivElement>(null)
  const appRef = useRef<PIXI.Application | null>(null)
  const animatedSpriteRef = useRef<PIXI.AnimatedSprite | null>(null)
  const [isPlaying, setIsPlaying] = useState<boolean>(autoPlay || false)

  useEffect(() => {
    if (!canvasRef.current) return

    // 计算画布尺寸
    const canvasWidth = width ? width * scale : frameWidth * scale
    const canvasHeight = height ? height * scale : frameHeight * scale

    let app: PIXI.Application | null = null
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
  const loadAnimationFromSpriteSheet = async (app: PIXI.Application): Promise<void> => {
    try {
      // 加载纹理
      if (!spriteSheet) return
      const texture = await PIXI.Assets.load(spriteSheet)
      
      // 创建帧数组
      const frameTextures = []
      for (let i = 0; i < frameCount; i++) {
        const frame = new PIXI.Texture({
          source: texture.source,
          frame: new PIXI.Rectangle(i * frameWidth, 0, frameWidth, frameHeight)
        })
        frameTextures.push(frame)
      }

      createAnimatedSprite(app, frameTextures)
    } catch (error) {
      console.error('加载精灵图动画失败:', error)
    }
  }

  // 从独立图片加载动画
  const loadAnimationFromFrames = async (app: PIXI.Application): Promise<void> => {
    try {
      // 加载所有图片
      if (!frames) return
      const textures = await Promise.all(
        frames.map(framePath => PIXI.Assets.load(framePath))
      )

      createAnimatedSprite(app, textures)
    } catch (error) {
      console.error('加载独立图片动画失败:', error)
    }
  }

  // 创建动画精灵
  const createAnimatedSprite = (app: PIXI.Application, textures: PIXI.Texture[]): void => {
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
