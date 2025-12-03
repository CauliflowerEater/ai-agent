/**
 * PIXI 动画自定义 Hook
 * 封装动画的加载、渲染和控制逻辑
 */

import { useEffect, useRef, useState } from 'react'
import * as PIXI from 'pixi.js'
import { loadAnimationTextures } from './loader'
import { 
  initPixiApp, 
  createAnimatedSprite, 
  destroyPixiApp, 
  destroyAnimatedSprite 
} from './renderer'
import type { AnimationLoadParams } from './types'

interface UsePixiAnimationParams {
  containerRef: React.RefObject<HTMLDivElement>
  loadParams: AnimationLoadParams
  canvasWidth: number
  canvasHeight: number
  fps: number
  loop: boolean
  scale: number
  autoPlay: boolean
}

interface UsePixiAnimationReturn {
  isPlaying: boolean
  togglePlay: () => void
  reset: () => void
}

/**
 * PIXI 动画管理 Hook
 */
export function usePixiAnimation(params: UsePixiAnimationParams): UsePixiAnimationReturn {
  const {
    containerRef,
    loadParams,
    canvasWidth,
    canvasHeight,
    fps,
    loop,
    scale,
    autoPlay
  } = params

  const appRef = useRef<PIXI.Application | null>(null)
  const animatedSpriteRef = useRef<PIXI.AnimatedSprite | null>(null)
  const [isPlaying, setIsPlaying] = useState<boolean>(autoPlay)

  // 初始化动画
  useEffect(() => {
    if (!containerRef.current) return

    let app: PIXI.Application | null = null
    let mounted = true

    const initAnimation = async () => {
      try {
        // 1. 初始化 PIXI 应用
        app = await initPixiApp({
          width: canvasWidth,
          height: canvasHeight
        })

        // 检查组件是否仍然挂载
        if (!mounted || !containerRef.current) {
          destroyPixiApp(app)
          return
        }

        // 2. 将画布添加到容器
        containerRef.current.appendChild(app.canvas)
        appRef.current = app

        // 3. 加载动画纹理
        const textures = await loadAnimationTextures(loadParams)
        
        if (textures.length === 0 || !mounted) {
          return
        }

        // 4. 创建动画精灵
        const sprite = createAnimatedSprite(textures, {
          fps,
          loop,
          scale,
          autoPlay: isPlaying
        })

        // 5. 添加到舞台
        app.stage.addChild(sprite)
        animatedSpriteRef.current = sprite

      } catch (error) {
        console.error('初始化 PIXI 动画失败:', error)
      }
    }

    initAnimation()

    return () => {
      mounted = false
      // 清理资源
      destroyAnimatedSprite(animatedSpriteRef.current)
      animatedSpriteRef.current = null
      destroyPixiApp(appRef.current)
      appRef.current = null
    }
  }, [
    containerRef,
    loadParams.spriteSheet,
    loadParams.frames,
    loadParams.frameWidth,
    loadParams.frameHeight,
    loadParams.frameCount,
    canvasWidth,
    canvasHeight,
    fps,
    loop,
    scale
  ])

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

  return {
    isPlaying,
    togglePlay,
    reset
  }
}
