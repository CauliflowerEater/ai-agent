import { useRef, useEffect } from 'react'

/**
 * 自动滚动Hook
 * @param {Array} dependencies - 依赖数组，当依赖变化时触发滚动
 */
export const useAutoScroll = (dependencies = []) => {
  const scrollRef = useRef(null)

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, dependencies)

  return scrollRef
}
