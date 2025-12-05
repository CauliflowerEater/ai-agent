/**
 * 动画配置常量
 */

/**
 * 像素动画配置
 */
export const PIXEL_ANIMATION_CONFIG = {
  // 动画帧图片路径
  FRAMES: ['/Cuty/Speaking_0.png', '/Cuty/Speaking_1.png'] as string[],
  
  // 画布尺寸
  WIDTH: 2048,
  HEIGHT: 2048,
  
  // 动画参数
  FPS: 2,           // 帧率
  LOOP: true,       // 是否循环
  SCALE: 0.15,      // 缩放比例
  AUTO_PLAY: true,  // 自动播放
}

/**
 * 头像配置
 */
export const AVATAR_CONFIG = {
  // AI 助手头像路径
  ASSISTANT: '/Cuty/Happy.png',
  
  // 用户头像表情符号
  USER: '👤',
} as const
