import { ChatPage } from './features/chat'
import ErrorBoundary from './components/ErrorBoundary'
import './App.css'

function App() {
  return (
    <ErrorBoundary>
      <div className="App">
        <ChatPage />
      </div>
    </ErrorBoundary>
  )
}

export default App
