/**
 * SSE 流式响应处理器 Hook
 * 处理分段文本、缓冲区管理
 */

import { useRef } from 'react'
import { CHUNK_DELIMITER } from './constants'
import { generateSegmentMessageId, updateMessageContent, addMessage } from './messageUtils'
import { createAssistantMessage } from '../../utils/messageUtils'
import { DEFAULT_MESSAGES } from '../../constants/messages'
import type { Message } from '../../types'
import type { StreamHandlers, MessageState } from './types'

export function useStreamProcessor(
  setMessages: (messages: Message[] | ((prev: Message[]) => Message[])) => void,
  setIsLoading: (loading: boolean) => void,
  cancelRequestRef: React.MutableRefObject<(() => void) | null>
) {
  const messageStateRef = useRef<MessageState>({
    currentMessageId: '',
    textBuffer: '',
    currentSegmentContent: ''
  })

  /**
   * 创建流式处理回调函数
   */
  const createStreamHandlers = (initialMessageId: string): StreamHandlers => {
    // 重置状态
    messageStateRef.current = {
      currentMessageId: initialMessageId,
      textBuffer: '',
      currentSegmentContent: ''
    }

    return {
      // 处理数据块
      onChunk: (deltaText: string) => {
        const state = messageStateRef.current
        
        // 1. 把本次增量追加到 buffer
        state.textBuffer += deltaText

        // 2. 反复查找是否出现了完整的分段标记
        while (true) {
          const idx = state.textBuffer.indexOf(CHUNK_DELIMITER)
          if (idx === -1) {
            // 没有完整标记，等待下一次 delta
            break
          }

          // 3. 取出标记前面的内容，作为一个完整段落
          const segment = state.textBuffer.slice(0, idx)
          // 4. 把 buffer 剩余部分（标记之后的内容）保留下来
          state.textBuffer = state.textBuffer.slice(idx + CHUNK_DELIMITER.length)

          // 5. 显示完整段落
          if (segment.trim()) {
            if (!state.currentSegmentContent) {
              // 第一个段落，更新初始气泡
              state.currentSegmentContent = segment
              setMessages(prev => 
                updateMessageContent(prev, state.currentMessageId, segment)
              )
            } else {
              // 创建新气泡显示新段落
              state.currentMessageId = generateSegmentMessageId()
              const newMessage = createAssistantMessage(segment, false, state.currentMessageId)
              setMessages(prev => addMessage(prev, newMessage))
              state.currentSegmentContent = segment
            }
          }
        }
      },

      // 流结束
      onComplete: () => {
        const state = messageStateRef.current
        
        // 处理 buffer 中剩余的内容
        const cleaned = state.textBuffer.trim()
        if (cleaned) {
          state.currentSegmentContent += state.textBuffer
          setMessages(prev => 
            updateMessageContent(prev, state.currentMessageId, state.currentSegmentContent)
          )
        }
        state.textBuffer = ''
        
        setIsLoading(false)
        cancelRequestRef.current = null
        
        // 如果没有收到任何内容，显示默认消息
        if (!state.currentSegmentContent.trim()) {
          setMessages(prev => 
            updateMessageContent(prev, state.currentMessageId, DEFAULT_MESSAGES.EMPTY_RESPONSE)
          )
        }
      },

      // 发生错误
      onError: (error: Error) => {
        const state = messageStateRef.current
        
        console.error('流式请求错误:', error)
        setIsLoading(false)
        cancelRequestRef.current = null
        
        // 显示错误消息
        setMessages(prev => 
          updateMessageContent(prev, state.currentMessageId, DEFAULT_MESSAGES.ERROR, true)
        )
      }
    }
  }

  return {
    createStreamHandlers
  }
}
