/**
 * 全局类型定义统一导出文件
 * 
 * 注意：聊天相关类型已迁移至 features/chat/types
 */

// API类型
export type {
  HttpMethod,
  ApiEndpoints,
  HttpStatus,
  HealthCheckResponse
} from './api'

// 通用类型
export type {
  BaseResponse,
  PaginationParams,
  PaginationResponse
} from './common'
