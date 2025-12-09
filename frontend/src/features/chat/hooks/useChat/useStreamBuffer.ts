/**
 * 流式数据缓冲处理 Hook
 * 负责处理 SSE 增量数据、分隔符识别、段落提取
 * 管理一个string buffer，处理新接受的chunk的拼接以及分隔符识别、段落提取
 */

import { useRef, useCallback } from 'react'

export interface StreamBufferControl {
  appendChunk: (deltaText: string) => string[]
  flush: () => string
  clear: () => void
  getBufferRef: () => React.RefObject<string>
}

interface UseStreamBufferOptions {
  delimiter: string
}

export function useStreamBuffer(options: UseStreamBufferOptions): StreamBufferControl {
  const { delimiter } = options
  const bufferRef = useRef<string>('')

  const appendChunk = useCallback((deltaText: string): string[] => {
    const segments: string[] = []
    
    bufferRef.current += deltaText

    while (true) {
      const idx = bufferRef.current.indexOf(delimiter)
      if (idx === -1) {
        break
      }

      const segment = bufferRef.current.slice(0, idx)
      bufferRef.current = bufferRef.current.slice(idx + delimiter.length)

      if (segment.trim()) {
        segments.push(segment)
      }
    }

    return segments
  }, [delimiter])

  const flush = useCallback((): string => {
    const remaining = bufferRef.current
    bufferRef.current = ''
    return remaining
  }, [])

  const clear = useCallback(() => {
    bufferRef.current = ''
  }, [])

  const getBufferRef = useCallback(() => {
    return bufferRef
  }, [])

  return {
    appendChunk,
    flush,
    clear,
    getBufferRef
  }
}
