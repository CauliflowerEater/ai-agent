/**
 * 动画渲染模块
 * 负责创建和管理 PIXI 应用及动画精灵
 */

import * as PIXI from 'pixi.js'

/**
 * 初始化 PIXI 应用配置
 */
export interface AppInitConfig {
  width: number
  height: number
  backgroundColor?: number
  antialias?: boolean
  resolution?: number
}

/**
 * 动画精灵配置
 */
export interface SpriteConfig {
  fps: number
  loop: boolean
  scale: number
  autoPlay: boolean
}

/**
 * 初始化 PIXI 应用
 */
export async function initPixiApp(config: AppInitConfig): Promise<PIXI.Application> {
  const app = new PIXI.Application()
  
  await app.init({
    width: config.width,
    height: config.height,
    backgroundColor: config.backgroundColor ?? 0x000000,
    antialias: config.antialias ?? false, // 像素风格不需要抗锯齿
    resolution: config.resolution ?? 1
  })

  return app
}

/**
 * 创建动画精灵
 */
export function createAnimatedSprite(
  textures: PIXI.Texture[],
  config: SpriteConfig
): PIXI.AnimatedSprite {
  const animatedSprite = new PIXI.AnimatedSprite(textures)
  
  animatedSprite.anchor.set(0)
  animatedSprite.scale.set(config.scale)
  animatedSprite.animationSpeed = config.fps / 60 // PIXI 使用 60fps 作为基准
  animatedSprite.loop = config.loop

  if (config.autoPlay) {
    animatedSprite.play()
  }

  return animatedSprite
}

/**
 * 销毁 PIXI 应用及相关资源
 */
export function destroyPixiApp(app: PIXI.Application | null): void {
  if (app) {
    app.destroy(true, { children: true, texture: true })
  }
}

/**
 * 销毁动画精灵
 */
export function destroyAnimatedSprite(sprite: PIXI.AnimatedSprite | null): void {
  if (sprite) {
    sprite.destroy()
  }
}
