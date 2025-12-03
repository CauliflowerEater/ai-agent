# PixelAnimation 模块

像素动画播放组件，支持精灵图和独立图片两种模式。

## 📁 目录结构

```
pixelAnimation/
├── PixelAnimation.tsx      # 主组件 (61 行)
├── PixelAnimation.css      # 主组件样式
├── types.ts               # 类型定义 (42 行)
├── loader.ts              # 资源加载模块 (77 行)
├── renderer.ts            # 渲染模块 (83 行)
├── usePixiAnimation.ts    # 动画管理 Hook (153 行)
├── controls.tsx           # 控制组件 (30 行)
├── controls.css           # 控制组件样式
└── index.ts               # 统一导出
```

## 🎯 模块职责

### 1. **types.ts** - 类型定义
- `PixelAnimationProps`: 组件属性接口
- `AnimationLoadParams`: 加载参数接口

### 2. **loader.ts** - 资源加载
- `loadFromSpriteSheet()`: 从精灵图加载
- `loadFromFrames()`: 从独立图片加载
- `loadAnimationTextures()`: 自动选择加载方式

### 3. **renderer.ts** - 渲染管理
- `initPixiApp()`: 初始化 PIXI 应用
- `createAnimatedSprite()`: 创建动画精灵
- `destroyPixiApp()`: 销毁应用
- `destroyAnimatedSprite()`: 销毁精灵

### 4. **usePixiAnimation.ts** - Hook
封装动画的完整生命周期：
- 初始化 PIXI 应用
- 加载动画资源
- 创建动画精灵
- 控制播放状态
- 清理资源

### 5. **controls.tsx** - 控制组件
播放控制按钮组：
- 播放/暂停切换
- 重置动画

### 6. **PixelAnimation.tsx** - 主组件
组合所有模块，提供统一的使用接口。

## 📊 重构效果

| 指标 | 重构前 | 重构后 |
|------|--------|--------|
| 单文件行数 | 207 行 | 最大 153 行 |
| 模块数量 | 1 个 | 9 个文件 |
| 职责分离 | ❌ | ✅ |
| 可测试性 | 低 | 高 |
| 可维护性 | 低 | 高 |

## 🚀 使用方式

```tsx
import PixelAnimation from './pixelAnimation'

// 独立图片模式
<PixelAnimation 
  frames={['/frame1.png', '/frame2.png']}
  width={96}
  height={96}
  fps={12}
  loop={true}
  scale={1}
  autoPlay={true}
/>

// 精灵图模式
<PixelAnimation 
  spriteSheet="/spritesheet.png"
  frameWidth={96}
  frameHeight={96}
  frameCount={8}
  fps={12}
  loop={true}
  scale={1}
  autoPlay={true}
/>
```
