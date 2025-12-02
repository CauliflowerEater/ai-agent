/**
 * 类型定义统一导出文件
 */

// 消息类型
export type { MessageRole, Message, CreateMessageParams } from './message'

// 聊天类型
export type {
  ChatRequest,
  ChatResponse,
  OnChunkCallback,
  OnCompleteCallback,
  OnErrorCallback,
  CancelRequestFn,
  UseChatReturn
} from './chat'

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
