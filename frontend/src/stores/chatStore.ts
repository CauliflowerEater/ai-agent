/**
 * Zustand 聊天状态管理
 */

import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import type { Message } from '../features/chat/types'

interface ChatState {
  // 状态
  messages: Message[]
  isLoading: boolean
  chatId: string
  hasInitialized: boolean  // 是否已发送初始消息
  
  // Actions
  setMessages: (messages: Message[] | ((prev: Message[]) => Message[])) => void
  addMessage: (message: Message) => void
  updateMessage: (id: string, updates: Partial<Message>) => void
  clearMessages: () => void
  setIsLoading: (loading: boolean) => void
  setHasInitialized: (initialized: boolean) => void
  resetChat: () => void
}

const generateChatId = () => `user_${Date.now()}`

export const useChatStore = create<ChatState>()(
  devtools(
    persist(
      (set) => ({
        // 初始状态
        messages: [],
        isLoading: false,
        chatId: generateChatId(),
        hasInitialized: false,
        
        // Actions
        setMessages: (messages) =>
          set((state) => ({
            messages: typeof messages === 'function' ? messages(state.messages) : messages
          })),
        
        addMessage: (message) =>
          set((state) => ({
            messages: [...state.messages, message]
          })),
        
        updateMessage: (id, updates) =>
          set((state) => ({
            messages: state.messages.map((msg) =>
              msg.id === id ? { ...msg, ...updates } : msg
            )
          })),
        
        clearMessages: () =>
          set({ messages: [] }),
        
        setIsLoading: (loading) =>
          set({ isLoading: loading }),
        
        setHasInitialized: (initialized) =>
          set({ hasInitialized: initialized }),
        
        resetChat: () =>
          set({
            messages: [],
            isLoading: false,
            chatId: generateChatId(),
            hasInitialized: false
          })
      }),
      {
        name: 'chat-storage', // localStorage key
        partialize: (state) => ({ messages: state.messages, chatId: state.chatId }) // 只持久化这些字段
      }
    ),
    {
      name: 'ChatStore' // DevTools 中的名称
    }
  )
)
