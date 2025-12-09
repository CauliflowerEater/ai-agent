/**
 * 段落队列管理 Hook
 * 负责管理待输出的段落队列
 * 维持了一个队列，并提供了管理该队列的方法
 */

import { useRef, useCallback } from 'react'

export interface SegmentQueueControl {
  enqueue: (segment: string) => void
  dequeue: () => string | undefined
  peek: () => string | undefined
  isEmpty: () => boolean
  clear: () => void
  getLengthRef: () => React.RefObject<number>
}

export function useSegmentQueue(): SegmentQueueControl {
  const queueRef = useRef<string[]>([])
  const lengthRef = useRef<number>(0)

  const updateLength = () => {
    lengthRef.current = queueRef.current.length
  }

  const enqueue = useCallback((segment: string) => {
    if (segment.trim()) {
      queueRef.current.push(segment)
      updateLength()
    }
  }, [])

  const dequeue = useCallback((): string | undefined => {
    const item = queueRef.current.shift()
    updateLength()
    return item
  }, [])

  const peek = useCallback((): string | undefined => {
    return queueRef.current[0]
  }, [])

  const isEmpty = useCallback((): boolean => {
    return queueRef.current.length === 0
  }, [])

  const clear = useCallback(() => {
    queueRef.current = []
    updateLength()
  }, [])

  const getLengthRef = useCallback(() => {
    return lengthRef
  }, [])

  return {
    enqueue,
    dequeue,
    peek,
    isEmpty,
    clear,
    getLengthRef
  }
}
