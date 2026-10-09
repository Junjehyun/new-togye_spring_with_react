/**
 * 이 프론트엔드 프로그램이 시작되는 "첫 지점"이다. index.html에 있는
 * <div id="root"></div> 라는 빈 상자를 찾아서, 그 안에 <App /> 블록
 * (과 그 밑에 딸린 모든 블록)을 실제로 그려넣기 시작한다. 
 */
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'

/**
 * document.getElementById('root') : index.html 안에서 id="root"인 요소를 찾는다.
 * 뒤에붙은 "!"는 타입스크립트에게 "이 값이 null이 아니라고 내가 보장한다"고
 * 알려주는 표시다 (우리가 index.html에 그 요소를 직접 만들어뒀으니 확실하다.)
 */
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {/** StrickMode : 개발 중에 잠재적인 실수를 더 엄격하게 잡아주는 모드.
     * 실제 화면에는 아무영향 없고, 개발할 때만 도움을 줍니다.
     */}
    <BrowserRouter>
      {/** BrowserRouter : App.tsx안에서 <Routes>/<Route>/<NavLink> 같은
       * "주소 기반 화면 전환" 기능을 쓸 수. 있게 준비해주는 블록이다.
       * 이 블록 없이는 라우팅 기능 자체가 동작하지 않는다.
       */}
      <App />
    </BrowserRouter>
  </StrictMode>,
)
