/**
 * 滚动相关工具函数
 */

import type { Message } from '../../types'

/**
 * 判断是否在底部附近
 */
export function isNearBottom(
  container: HTMLDivElement,
  threshold: number = 50
): boolean {
  const { scrollTop, scrollHeight, clientHeight } = container
  const distanceToBottom = scrollHeight - scrollTop - clientHeight
  return distanceToBottom < threshold
}

/**
 * 获取占位符高度（最后一条助手消息）
 */
export function getPlaceholderHeight(container: HTMLDivElement): number {
  const messageElements = container.querySelectorAll('.message.assistant')
  const lastAssistantMessage = messageElements[messageElements.length - 1]
  
  if (lastAssistantMessage instanceof HTMLElement) {
    const styles = window.getComputedStyle(lastAssistantMessage)
    const marginBottom = parseFloat(styles.marginBottom) || 0
    return lastAssistantMessage.offsetHeight + marginBottom
  }
  
  return 0
}

/**
 * 检测新消息中是否包含用户消息
 */
export function hasUserMessageInNewMessages(
  messages: Message[],
  currentCount: number,
  prevCount: number
): boolean {
  if (currentCount <= prevCount) return false
  
  const newMessageCount = currentCount - prevCount
  for (let i = currentCount - newMessageCount; i < currentCount; i++) {
    if (messages[i]?.role === 'user') {
      return true
    }
  }
  
  return false
}

/**
 * 滚动到用户最后一条消息的底部
 */
export function scrollToLastUserMessage(
  container: HTMLDivElement,
  isProgrammaticScrollRef: { current: boolean },
  lastUserMessageId: string | null
): void {
  if (!lastUserMessageId) return
  
  const lastUserMessageElement = document.getElementById(lastUserMessageId)
  if (!lastUserMessageElement) return
  
  isProgrammaticScrollRef.current = true
  
  // 计算用户消息底部相对于容器的位置
  const messageBottom = lastUserMessageElement.offsetTop + lastUserMessageElement.offsetHeight
  
  // 滚动到用户消息底部位置
  container.scrollTop = messageBottom
  
  // 延迟重置标志位
  setTimeout(() => {
    isProgrammaticScrollRef.current = false
  }, 100)
}
