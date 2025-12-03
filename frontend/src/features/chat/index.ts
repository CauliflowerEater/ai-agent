/**
 * Chat Feature 统一导出
 */

// 组件
export { default as ChatPage } from './components/ChatPage'
export { default as ChatHeader } from './components/ChatHeader'
export { default as ChatInput } from './components/ChatInput'
export { default as MessageList } from './components/MessageList'
export { default as MessageItem } from './components/MessageItem'
export { default as TypingIndicator } from './components/TypingIndicator'
export { default as PixelAnimation } from './components/pixelAnimation'

// Hooks
export { useChat } from './hooks/useChat'
export { useAutoScroll } from './hooks/useAutoScroll'

// Services
export { sendMessageStream, checkHealth } from './services/chatApi'

// Constants
export { API_BASE_URL, API_ENDPOINTS, HTTP_STATUS } from './constants/api'
export { MESSAGE_ROLES, DEFAULT_MESSAGES, MESSAGE_PLACEHOLDERS } from './constants/messages'

// Utils
export {
  createMessage,
  createUserMessage,
  createAssistantMessage,
  formatTime,
  validateMessage,
  sanitizeMessage
} from './utils/messageUtils'

// Types
export type {
  Message,
  MessageRole,
  ChatRequest,
  ChatResponse,
  UseChatReturn
} from './types'
