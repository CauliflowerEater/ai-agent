/**
 * 段落定时输出 Hook
 * 负责管理定时器、控制输出节奏
 * 实现的是一个管理定时执行任务的模板，定时任务通过回调函数的方式在调用start()方法时传入；
 */

import { useRef, useCallback, useEffect } from 'react'

export interface SegmentOutputControl {
  start: (callback: () => void) => void
  stop: () => void
  isRunningRef: React.RefObject<boolean>
}

interface UseSegmentOutputOptions {
  interval: number
}

export function useSegmentOutput(options: UseSegmentOutputOptions): SegmentOutputControl {
  const { interval } = options
  const timerIdRef = useRef<number | null>(null)
  const isRunningRef = useRef(false)
  const callbackRef = useRef<(() => void) | null>(null)

  const stop = useCallback(() => {
    if (timerIdRef.current !== null) {
      clearTimeout(timerIdRef.current)
      timerIdRef.current = null
    }
    isRunningRef.current = false
    callbackRef.current = null
  }, [])

  const executeCallback = useCallback(() => {
    if (callbackRef.current) {
      callbackRef.current()
    }
  }, [])

  const start = useCallback((callback: () => void) => {
    if (isRunningRef.current) {
      return
    }

    isRunningRef.current = true
    callbackRef.current = callback

    const scheduleNext = () => {
      timerIdRef.current = window.setTimeout(() => {
        executeCallback()
        if (isRunningRef.current) {
          scheduleNext()
        }
      }, interval)
    }

    executeCallback()
    scheduleNext()
  }, [interval, executeCallback])

  // 组件卸载时清理定时器
  useEffect(() => {
    return () => {
      if (timerIdRef.current !== null) {
        clearTimeout(timerIdRef.current)
      }
    }
  }, [])

  return {
    start,
    stop,
    isRunningRef
  }
}
