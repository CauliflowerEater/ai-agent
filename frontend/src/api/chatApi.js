import { API_BASE_URL, API_ENDPOINTS } from '../constants/api'

/**
 * 发送聊天消息
 * @param {string} message - 用户消息
 * @param {string} chatId - 会话ID（可选）
 * @returns {Promise<Object>} 响应数据
 */
export const sendMessage = async (message, chatId = null) => {
  try {
    const requestBody = { message }
    if (chatId) {
      requestBody.chatId = chatId
    }

    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.CHAT}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()
    
    // 后端返回格式: { code: 0, data: { reply: "...", chatId: "..." }, message: "ok" }
    if (result.code === 0 && result.data) {
      return result.data // 返回 { reply, chatId }
    } else {
      throw new Error(result.message || '请求失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    throw error
  }
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
