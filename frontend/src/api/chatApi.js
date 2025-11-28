import { API_BASE_URL, API_ENDPOINTS } from '../constants/api'

/**
 * 发送聊天消息
 * @param {string} message - 用户消息
 * @returns {Promise<Object>} 响应数据
 */
export const sendMessage = async (message) => {
  try {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.CHAT}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ message })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
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
