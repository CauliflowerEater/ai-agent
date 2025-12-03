import { API_BASE_URL, API_ENDPOINTS } from '../constants/api'
import type {
  ChatRequest,
  OnChunkCallback,
  OnCompleteCallback,
  OnErrorCallback,
  CancelRequestFn,
  HealthCheckResponse
} from '../types'

/**
 * 发送聊天消息（SSE 流式）
 * @param message - 用户消息
 * @param chatId - 会话ID
 * @param onChunk - 收到数据块的回调函数
 * @param onComplete - 完成时的回调函数
 * @param onError - 错误时的回调函数
 * @returns 取消请求的函数
 */
export const sendMessageStream = (
  message: string,
  chatId: string,
  onChunk: OnChunkCallback,
  onComplete: OnCompleteCallback,
  onError: OnErrorCallback
): CancelRequestFn => {
  const controller = new AbortController()
  const { signal } = controller

  const requestBody: ChatRequest = {
    message,
    chatId
  }

  fetch(`${API_BASE_URL}${API_ENDPOINTS.CHAT_STREAM}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(requestBody),
    signal
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('Response body is null')
      }

      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          onComplete()
          break
        }

        // 解码数据块
        buffer += decoder.decode(value, { stream: true })
        
        // 处理 SSE 数据格式：data: xxx\n\n
        const lines = buffer.split('\n')
        buffer = lines.pop() || '' // 保留未完成的行

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data) {
              onChunk(data)
            }
          }
        }
      }
    })
    .catch((error: unknown) => {
      if (error instanceof Error && error.name === 'AbortError') {
        console.log('请求已取消')
      } else {
        console.error('发送消息失败:', error)
        onError(error instanceof Error ? error : new Error(String(error)))
      }
    })

  // 返回取消函数
  return () => controller.abort()
}

/**
 * 检查健康状态
 * @returns 健康状态响应
 */
export const checkHealth = async (): Promise<HealthCheckResponse> => {
  try {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.HEALTH}`)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('健康检查失败:', error)
    throw error
  }
}
