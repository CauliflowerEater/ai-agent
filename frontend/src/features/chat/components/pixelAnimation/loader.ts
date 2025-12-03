/**
 * 动画资源加载模块
 * 负责加载精灵图或独立图片资源
 */

import * as PIXI from 'pixi.js'
import type { AnimationLoadParams } from './types'

/**
 * 从精灵图加载动画纹理
 */
export async function loadFromSpriteSheet(
  params: AnimationLoadParams
): Promise<PIXI.Texture[]> {
  const { spriteSheet, frameWidth, frameHeight, frameCount } = params
  
  if (!spriteSheet) {
    throw new Error('spriteSheet 路径未提供')
  }

  try {
    // 加载纹理
    const texture = await PIXI.Assets.load(spriteSheet)
    
    // 创建帧数组
    const frameTextures: PIXI.Texture[] = []
    for (let i = 0; i < frameCount; i++) {
      const frame = new PIXI.Texture({
        source: texture.source,
        frame: new PIXI.Rectangle(i * frameWidth, 0, frameWidth, frameHeight)
      })
      frameTextures.push(frame)
    }

    return frameTextures
  } catch (error) {
    console.error('加载精灵图动画失败:', error)
    throw error
  }
}

/**
 * 从独立图片加载动画纹理
 */
export async function loadFromFrames(frames: string[]): Promise<PIXI.Texture[]> {
  if (!frames || frames.length === 0) {
    throw new Error('frames 数组为空')
  }

  try {
    // 加载所有图片
    const textures = await Promise.all(
      frames.map(framePath => PIXI.Assets.load(framePath))
    )
    return textures
  } catch (error) {
    console.error('加载独立图片动画失败:', error)
    throw error
  }
}

/**
 * 根据配置自动选择加载方式
 */
export async function loadAnimationTextures(
  params: AnimationLoadParams
): Promise<PIXI.Texture[]> {
  // 优先使用独立图片模式
  if (params.frames && params.frames.length > 0) {
    return loadFromFrames(params.frames)
  } else if (params.spriteSheet) {
    return loadFromSpriteSheet(params)
  } else {
    console.warn('PixelAnimation: 未提供图片资源（frames 或 spriteSheet）')
    return []
  }
}
