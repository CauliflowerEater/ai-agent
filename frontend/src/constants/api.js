// API基础配置
export const API_BASE_URL = '/api'

// API端点
export const API_ENDPOINTS = {
  CHAT_STREAM: '/chat/send/stream',  // SSE流式接口
  HEALTH: '/health',
}

// HTTP状态码
export const HTTP_STATUS = {
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500,
}
