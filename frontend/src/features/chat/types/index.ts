/**
 * Chat feature 类型定义统一导出
 */

export type { MessageRole, Message, CreateMessageParams } from './message'
export type {
  ChatRequest,
  ChatResponse,
  OnChunkCallback,
  OnCompleteCallback,
  OnErrorCallback,
  CancelRequestFn,
  UseChatReturn
} from './chat'

// 导入并重新导出 api.ts 和 common.ts 中的类型（如果需要）
import type { ApiEndpoints, HttpStatus, HealthCheckResponse } from '../../../types/api'
import type { BaseResponse, PaginationParams, PaginationResponse } from '../../../types/common'

export type {
  ApiEndpoints,
  HttpStatus,
  HealthCheckResponse,
  BaseResponse,
  PaginationParams,
  PaginationResponse
}
