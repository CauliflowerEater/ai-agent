import PixelAnimation from './PixelAnimation'
import './PixelAnimationDemo.css'

/**
 * 像素动画演示组件
 * 展示如何使用PixelAnimation组件
 */
const PixelAnimationDemo = () => {
  return (
    <div className="demo-container">
      <h2 className="demo-title">像素动画播放器</h2>
      
      <div className="demo-section">
        <h3>使用示例</h3>
        <p className="demo-desc">将你的精灵图放在 <code>/public</code> 目录下，然后这样使用：</p>
        
        {/* 示例1：基础动画 */}
        <div className="demo-item">
          <h4>基础动画示例</h4>
          <PixelAnimation
            spriteSheet="/path/to/your/spritesheet.png"
            frameWidth={32}
            frameHeight={32}
            frameCount={8}
            fps={12}
            scale={3}
            loop={true}
            autoPlay={true}
          />
          <pre className="code-block">
{`<PixelAnimation
  spriteSheet="/path/to/your/spritesheet.png"
  frameWidth={32}
  frameHeight={32}
  frameCount={8}
  fps={12}
  scale={3}
  loop={true}
  autoPlay={true}
/>`}
          </pre>
        </div>
      </div>

      <div className="demo-section">
        <h3>组件属性说明</h3>
        <table className="props-table">
          <thead>
            <tr>
              <th>属性</th>
              <th>类型</th>
              <th>默认值</th>
              <th>说明</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>spriteSheet</td>
              <td>string</td>
              <td>-</td>
              <td>精灵图路径（必填）</td>
            </tr>
            <tr>
              <td>frameWidth</td>
              <td>number</td>
              <td>32</td>
              <td>每帧宽度（像素）</td>
            </tr>
            <tr>
              <td>frameHeight</td>
              <td>number</td>
              <td>32</td>
              <td>每帧高度（像素）</td>
            </tr>
            <tr>
              <td>frameCount</td>
              <td>number</td>
              <td>1</td>
              <td>总帧数</td>
            </tr>
            <tr>
              <td>fps</td>
              <td>number</td>
              <td>12</td>
              <td>帧率（每秒帧数）</td>
            </tr>
            <tr>
              <td>loop</td>
              <td>boolean</td>
              <td>true</td>
              <td>是否循环播放</td>
            </tr>
            <tr>
              <td>scale</td>
              <td>number</td>
              <td>2</td>
              <td>缩放比例</td>
            </tr>
            <tr>
              <td>autoPlay</td>
              <td>boolean</td>
              <td>true</td>
              <td>是否自动播放</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="demo-section">
        <h3>精灵图格式要求</h3>
        <ul className="requirements-list">
          <li>✅ 精灵图应该是水平排列的帧序列</li>
          <li>✅ 每帧大小必须一致</li>
          <li>✅ 支持PNG格式（带透明通道）</li>
          <li>✅ 建议使用2的幂次方尺寸（32x32, 64x64等）</li>
          <li>✅ 文件应放在 <code>public</code> 目录下</li>
        </ul>
      </div>
    </div>
  )
}

export default PixelAnimationDemo
