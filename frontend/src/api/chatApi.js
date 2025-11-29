import { API_BASE_URL, API_ENDPOINTS } from '../constants/api'

/**
 * 发送聊天消息（SSE 流式）
 * @param {string} message - 用户消息
 * @param {string} chatId - 会话ID
 * @param {Function} onChunk - 收到数据块的回调函数
 * @param {Function} onComplete - 完成时的回调函数
 * @param {Function} onError - 错误时的回调函数
 * @returns {Function} 取消请求的函数
 */
export const sendMessageStream = (message, chatId, onChunk, onComplete, onError) => {
  const controller = new AbortController()
  const signal = controller.signal

  const requestBody = {
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
    .then(async response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          onComplete?.()
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
              onChunk?.(data)
            }
          }
        }
      }
    })
    .catch(error => {
      if (error.name === 'AbortError') {
        console.log('请求已取消')
      } else {
        console.error('发送消息失败:', error)
        onError?.(error)
      }
    })

  // 返回取消函数
  return () => controller.abort()
}

/**
 * 检查健康状态
 * @returns {Promise<Object>} 健康状态
 */
export const checkHealth = async () => {
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
