/**
 * SSE 流式响应处理器 Hook
 * 组合子 Hook 实现完整的流式处理功能
 */

import { useRef, useCallback } from 'react'
import { CHUNK_DELIMITER } from './constants'
import { generateSegmentMessageId, updateMessageContent, addMessage } from './messageUtils'
import { createAssistantMessage } from '../../utils/messageUtils'
import { DEFAULT_MESSAGES } from '../../constants/messages'
import type { Message } from '../../types'
import type { StreamHandlers } from './types'
import { useSegmentQueue } from './useSegmentQueue'
import { useStreamBuffer } from './useStreamBuffer'
import { useSegmentOutput } from './useSegmentOutput'
import { shouldCreateNewBubble } from './streamUtils'

// 输出配置
const OUTPUT_CONFIG = {
  SEGMENT_INTERVAL: 500  // 段落输出间隔（ms）
}

interface StreamState {
  currentMessageId: string
  currentSegmentContent: string
  streamEnded: boolean
}

export function useStreamProcessor(
  setMessages: (messages: Message[] | ((prev: Message[]) => Message[])) => void,
  setIsLoading: (loading: boolean) => void,
  cancelRequestRef: React.MutableRefObject<(() => void) | null>
) {
  const stateRef = useRef<StreamState>({
    currentMessageId: '',
    currentSegmentContent: '',
    streamEnded: false
  })

  // 使用子 Hook
  const queue = useSegmentQueue()
  const buffer = useStreamBuffer({ delimiter: CHUNK_DELIMITER })
  const output = useSegmentOutput({ interval: OUTPUT_CONFIG.SEGMENT_INTERVAL })

  // 辅助函数：更新当前消息
  const updateCurrentMessage = useCallback((content: string, isError = false) => {
    setMessages(prev => 
      updateMessageContent(prev, stateRef.current.currentMessageId, content, isError)
    )
  }, [setMessages])

  // 辅助函数：创建新消息气泡
  const createNewMessageBubble = useCallback((content: string) => {
    stateRef.current.currentMessageId = generateSegmentMessageId()
    const newMessage = createAssistantMessage(content, false, stateRef.current.currentMessageId)
    setMessages(prev => addMessage(prev, newMessage))
    stateRef.current.currentSegmentContent = content
  }, [setMessages])

  // 辅助函数：结束加载状态
  const finishLoading = useCallback(() => {
    setIsLoading(false)
    cancelRequestRef.current = null
  }, [setIsLoading, cancelRequestRef])

  // 辅助函数：处理空响应
  const handleEmptyResponse = useCallback(() => {
    if (!stateRef.current.currentSegmentContent.trim()) {
      updateCurrentMessage(DEFAULT_MESSAGES.EMPTY_RESPONSE)
    }
  }, [updateCurrentMessage])

  // 辅助函数：处理剩余 buffer 内容
  const processRemainingBuffer = useCallback(() => {
    const remaining = buffer.flush()
    const cleaned = remaining.trim()
    
    if (cleaned) {
      if (!stateRef.current.currentSegmentContent) {
        stateRef.current.currentSegmentContent = cleaned
        updateCurrentMessage(cleaned)
      } else {
        stateRef.current.currentSegmentContent += cleaned
        updateCurrentMessage(stateRef.current.currentSegmentContent)
      }
    }
  }, [buffer, updateCurrentMessage])

  // 辅助函数：处理输出完成
  const handleOutputComplete = useCallback(() => {
    processRemainingBuffer()
    finishLoading()
    handleEmptyResponse()
  }, [processRemainingBuffer, finishLoading, handleEmptyResponse])

  // 处理下一个段落
  const processNextSegment = useCallback((): boolean => {
    if (queue.isEmpty()) {
      if (stateRef.current.streamEnded) {
        output.stop()
        handleOutputComplete()
      } else {
        output.stop()
      }
      return false
    }

    const segment = queue.dequeue()!
    
    if (shouldCreateNewBubble(stateRef.current.currentSegmentContent)) {
      createNewMessageBubble(segment)
    } else {
      stateRef.current.currentSegmentContent = segment
      updateCurrentMessage(segment)
    }

    return true
  }, [queue, output, handleOutputComplete, createNewMessageBubble, updateCurrentMessage])

  // 启动输出
  const startOutput = useCallback(() => {
    if (output.isRunningRef.current) {
      return
    }

    output.start(processNextSegment)
  }, [output, processNextSegment])

  /**
   * 创建流式处理回调函数
   */
  const createStreamHandlers = useCallback((initialMessageId: string): StreamHandlers => {
    // 清理之前的状态
    output.stop()
    queue.clear()
    buffer.clear()

    // 重置状态
    stateRef.current = {
      currentMessageId: initialMessageId,
      currentSegmentContent: '',
      streamEnded: false
    }

    return {
      // 处理数据块
      onChunk: (deltaText: string) => {
        const segments = buffer.appendChunk(deltaText)
        
        segments.forEach(segment => {
          queue.enqueue(segment)
        })

        if (segments.length > 0 && !output.isRunningRef.current) {
          startOutput()
        }
      },

      // 流结束
      onComplete: () => {
        stateRef.current.streamEnded = true

        if (!output.isRunningRef.current) {
          if (!queue.isEmpty()) {
            startOutput()
          } else {
            // 处理剩余 buffer 内容
            const remaining = buffer.flush()
            const cleaned = remaining.trim()
            
            if (cleaned) {
              if (!stateRef.current.currentSegmentContent) {
                stateRef.current.currentSegmentContent = cleaned
                updateCurrentMessage(cleaned)
              } else {
                stateRef.current.currentSegmentContent += cleaned
                updateCurrentMessage(stateRef.current.currentSegmentContent)
              }
            }

            finishLoading()
            handleEmptyResponse()
          }
        }
      },

      // 发生错误
      onError: (error: Error) => {
        console.error('流式请求错误:', error)
        
        output.stop()
        queue.clear()
        buffer.clear()
        
        stateRef.current.streamEnded = true
        
        finishLoading()
        updateCurrentMessage(DEFAULT_MESSAGES.ERROR, true)
      }
    }
  }, [output, queue, buffer, startOutput, updateCurrentMessage, finishLoading, handleEmptyResponse])

  return {
    createStreamHandlers
  }
}
