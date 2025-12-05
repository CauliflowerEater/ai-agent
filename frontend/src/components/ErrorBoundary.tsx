import { Component, ErrorInfo, ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
  error?: Error
}

/**
 * Error Boundary 组件
 * 捕获子组件树中的 JavaScript 错误，显示后备 UI
 */
class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error: Error): State {
    // 更新 state 使下一次渲染能够显示后备 UI
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // 可以将错误日志上报给服务器
    console.error('Error Boundary 捕获到错误:', error, errorInfo)
  }

  handleRefresh = () => {
    window.location.reload()
  }

  render() {
    if (this.state.hasError) {
      // 自定义后备 UI
      return (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100vh',
          padding: '20px',
          textAlign: 'center',
          backgroundColor: '#f5f5f5'
        }}>
          <h1 style={{ fontSize: '4rem', margin: '0 0 20px 0' }}>😵</h1>
          <h2 style={{ color: '#333', marginBottom: '10px' }}>哎呀，出错了！</h2>
          <p style={{ color: '#666', marginBottom: '30px', maxWidth: '500px' }}>
            应用遇到了一些问题。请尝试刷新页面，如果问题持续存在，请联系技术支持。
          </p>
          {this.state.error && (
            <details style={{ 
              marginBottom: '20px', 
              padding: '15px',
              backgroundColor: '#fff',
              borderRadius: '8px',
              maxWidth: '600px',
              textAlign: 'left'
            }}>
              <summary style={{ cursor: 'pointer', fontWeight: 'bold', color: '#666' }}>
                错误详情
              </summary>
              <pre style={{ 
                marginTop: '10px',
                padding: '10px',
                backgroundColor: '#f9f9f9',
                borderRadius: '4px',
                overflow: 'auto',
                fontSize: '0.85rem',
                color: '#c33'
              }}>
                {this.state.error.toString()}
              </pre>
            </details>
          )}
          <button
            onClick={this.handleRefresh}
            style={{
              padding: '12px 32px',
              fontSize: '1rem',
              color: 'white',
              backgroundColor: '#667eea',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              transition: 'background-color 0.3s'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#5568d3'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#667eea'}
          >
            刷新页面
          </button>
        </div>
      )
    }

    return this.props.children
  }
}

export default ErrorBoundary
