/**
 * 基础响应接口（对应后端 BaseResponse.java）
 */
export interface BaseResponse<T = any> {
  /** 状态码 */
  code: number
  /** 提示信息 */
  message: string
  /** 响应数据 */
  data: T
}

/**
 * 分页参数
 */
export interface PaginationParams {
  /** 页码 */
  page: number
  /** 每页数量 */
  pageSize: number
}

/**
 * 分页响应
 */
export interface PaginationResponse<T> {
  /** 数据列表 */
  list: T[]
  /** 总数 */
  total: number
  /** 当前页 */
  page: number
  /** 每页数量 */
  pageSize: number
}
