import type { ApiEndpoints, HttpStatus } from '../types/index'

/**
 * API基础配置
 * 开发环境：通过 Vite 代理转发到后端
 * 生产环境：直接请求后端服务器
 */
export const API_BASE_URL = import.meta.env.MODE === 'development' 
  ? '/api'  // 开发环境使用代理
  : import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'  // 生产环境使用环境变量

/**
 * API端点
 */
export const API_ENDPOINTS: ApiEndpoints = {
  CHAT_STREAM: '/chat/send/stream',  // SSE流式接口
  HEALTH: '/health',
}

/**
 * HTTP状态码
 */
export const HTTP_STATUS: HttpStatus = {
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500,
}
