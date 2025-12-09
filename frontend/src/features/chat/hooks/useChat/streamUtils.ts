/**
 * 流式处理工具函数
 * 纯函数，不依赖 React
 */

export interface MessageUpdateContext {
  currentMessageId: string
  currentSegmentContent: string
}

/**
 * 判断是否应该创建新的消息气泡
 * @param currentContent 当前段落内容
 * @returns 是否应该创建新气泡
 */
export function shouldCreateNewBubble(currentContent: string): boolean {
  return currentContent.trim().length > 0
}

/**
 * 验证段落是否有效
 * @param segment 待验证的段落
 * @returns 是否为有效段落
 */
export function isValidSegment(segment: unknown): segment is string {
  return typeof segment === 'string' && segment.trim().length > 0
}

/**
 * 检查缓冲区是否有剩余内容
 * @param buffer 缓冲区字符串
 * @returns 是否有剩余内容
 */
export function hasRemainingContent(buffer: string): boolean {
  return buffer.trim().length > 0
}
