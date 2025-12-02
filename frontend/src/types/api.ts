/**
 * HTTP请求方法
 */
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

/**
 * API端点配置
 */
export interface ApiEndpoints {
  /** SSE流式聊天接口 */
  CHAT_STREAM: string
  /** 健康检查接口 */
  HEALTH: string
}

/**
 * HTTP状态码
 */
export interface HttpStatus {
  OK: number
  BAD_REQUEST: number
  UNAUTHORIZED: number
  FORBIDDEN: number
  NOT_FOUND: number
  SERVER_ERROR: number
}

/**
 * 健康检查响应
 */
export interface HealthCheckResponse {
  status: string
  timestamp: number
}
