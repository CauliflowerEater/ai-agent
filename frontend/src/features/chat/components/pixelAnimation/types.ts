/**
 * PixelAnimation 相关类型定义
 */

/**
 * PixelAnimation 组件属性
 */
export interface PixelAnimationProps {
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
 * 动画加载参数
 */
export interface AnimationLoadParams {
  spriteSheet?: string
  frames?: string[]
  frameWidth: number
  frameHeight: number
  frameCount: number
}
