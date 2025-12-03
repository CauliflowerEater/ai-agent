/**
 * Zustand Store 使用示例
 * 
 * 这个文件展示了如何在组件中使用 useChatStore 和 useScrollStore
 */

import { useChatStore, useScrollStore } from '../stores'

// ============================================
// 示例 1: 基本使用 - 获取状态和 actions
// ============================================
export function ExampleComponent1() {
  // 方式1: 获取整个状态(不推荐,会导致不必要的重渲染)
  // const { messages, isLoading, addMessage } = useChatStore()
  
  // 方式2: 选择性获取需要的状态(推荐)
  const messages = useChatStore((state) => state.messages)
  const isLoading = useChatStore((state) => state.isLoading)
  const addMessage = useChatStore((state) => state.addMessage)
  
  // 使用示例
  const handleClick = () => {
    addMessage({
      id: `msg_${Date.now()}`,
      role: 'user',
      content: 'Hello',
      timestamp: new Date().toISOString()
    })
  }
  
  return (
    <div>
      <div>消息数量: {messages.length}</div>
      <div>加载中: {isLoading ? '是' : '否'}</div>
      <button onClick={handleClick}>添加消息</button>
    </div>
  )
}

// ============================================
// 示例 2: 在组件外部访问 store
// ============================================
export function useOutsideStore() {
  // 在组件外部或工具函数中访问 store
  const currentMessages = useChatStore.getState().messages
  
  // 调用 actions
  useChatStore.getState().clearMessages()
  
  // 订阅状态变化
  const unsubscribe = useChatStore.subscribe(
    (state) => {
      console.log('消息更新:', state.messages)
    }
  )
  
  // 记得在适当的时候取消订阅
  // unsubscribe()
  
  return currentMessages
}

// ============================================
// 示例 3: 获取多个状态
// ============================================
export function ExampleComponent3() {
  // 方式1: 分别获取(推荐)
  const messages = useChatStore((state) => state.messages)
  const isLoading = useChatStore((state) => state.isLoading)
  
  // 方式2: 使用单个选择器
  const messagesCount = useChatStore((state) => state.messages.length)
  
  return (
    <div>
      <div>消息: {messages.length}</div>
      <div>加载: {isLoading}</div>
      <div>消息数: {messagesCount}</div>
    </div>
  )
}

// ============================================
// 示例 4: 使用 actions
// ============================================
export function ExampleComponent4() {
  const clearMessages = useChatStore((state) => state.clearMessages)
  const resetChat = useChatStore((state) => state.resetChat)
  const updateMessage = useChatStore((state) => state.updateMessage)
  
  const handleUpdate = () => {
    // 更新特定消息
    updateMessage('msg_123', { content: '更新后的内容' })
  }
  
  return (
    <div>
      <button onClick={clearMessages}>清空消息</button>
      <button onClick={resetChat}>重置聊天</button>
      <button onClick={handleUpdate}>更新消息</button>
    </div>
  )
}

// ============================================
// 示例 5: 派生状态(计算属性)
// ============================================
export function ExampleComponent5() {
  // 使用选择器创建派生状态
  const userMessageCount = useChatStore((state) => 
    state.messages.filter(msg => msg.role === 'user').length
  )
  
  const lastMessage = useChatStore((state) => 
    state.messages[state.messages.length - 1]
  )
  
  return (
    <div>
      <div>用户消息数: {userMessageCount}</div>
      <div>最后一条消息: {lastMessage?.content}</div>
    </div>
  )
}

// ============================================
// 示例 6: 使用 scrollStore
// ============================================
export function ExampleComponent6() {
  // 获取滚动状态
  const autoScroll = useScrollStore((state) => state.autoScroll)
  const setAutoScroll = useScrollStore((state) => state.setAutoScroll)
  const resetScrollState = useScrollStore((state) => state.resetScrollState)
  
  // 获取加载跟踪状态
  const loadingStartHeight = useScrollStore((state) => state.loadingStartHeight)
  const loadingStartScrollTop = useScrollStore((state) => state.loadingStartScrollTop)
  
  return (
    <div>
      <div>自动滚动: {autoScroll ? '开启' : '关闭'}</div>
      <div>加载开始高度: {loadingStartHeight}px</div>
      <div>加载开始滚动位置: {loadingStartScrollTop}px</div>
      <button onClick={() => setAutoScroll(!autoScroll)}>切换自动滚动</button>
      <button onClick={resetScrollState}>重置滚动状态</button>
    </div>
  )
}

// ============================================
// 示例 7: 组合使用多个 store
// ============================================
export function ExampleComponent7() {
  // 同时使用 chatStore 和 scrollStore
  const messages = useChatStore((state) => state.messages)
  const isLoading = useChatStore((state) => state.isLoading)
  const autoScroll = useScrollStore((state) => state.autoScroll)
  
  const clearAll = () => {
    useChatStore.getState().resetChat()
    useScrollStore.getState().resetScrollState()
  }
  
  return (
    <div>
      <div>消息数: {messages.length}</div>
      <div>加载中: {isLoading ? '是' : '否'}</div>
      <div>自动滚动: {autoScroll ? '开启' : '关闭'}</div>
      <button onClick={clearAll}>清空所有</button>
    </div>
  )
}
