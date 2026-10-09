/**
 * 이 파일은 화면 전체를 감싸는 "가장 큰 레고 블록"이다. 위쪽에 항상 
 * 보이는 제목/메뉴(헤더)를 그리고, 그 아래에 지금 주소(URL)에 맞는 
 * 화면(대시보드/베팅기록/입출금 중 하나)을 끼워 넣는다.
 */

import { NavLink, Route, Routes } from 'react-router-dom'
import { DashboardPage } from './pages/DashBoardPage.tsx'
import { BetsPage } from './pages/BetsPage.tsx'
//import { BetsPage } from './pages/BetsPage'
//import { TransactionsPage } from './pages/TransactionsPage'

function App() {
  // return (...) 안의 내용은 JSX이다. 자바스크립트 함수 안에서 
  // HTML처럼 화면 구조를 쓰는 문법이다.
  return (
    <div className="app-shell">
      <header className="app-header">
        <span className="app-title">Togye</span>
        <nav className="app-nav">
          {/**
           * NavLink : 클릭하면 페이지 전체가 새로고침 되지 않고, 주소만
           * 바뀌면서 아래 <Routes>가 알아서 화면을 바꿔 끼워준다.
           * 지금 보고 있는 화면의 링크에는 자동으로 "active"라는 
           * CSS클래스가 붙어서(index.css에서 색깔로 표시), 사용자가 지금 
           * 어느 화면에 있는지 알 수 있게 해준다. 
           */}
          <NavLink to="/" end> 
            {/** "/"로 시작하는 다른 주소(예: "/bets")에서는 이 링크가 
             * active로 표시되지 않게, 정확히 "/"일 때만 표시하라는 옵션
             */}
             대시보드
          </NavLink>
          <NavLink to="/bets">베팅 기록</NavLink>
          <NavLink to="/transactions">입출금내역</NavLink>
        </nav>
      </header>
      <main className='app-main'>
        {/**<Routes>와 <Route path="..." element={...} /> : "지금
         * 브라우저 주소가 이거면, 이 컴포넌트를 보여줘라"는 규칙들의 모음이다. 
         * 라라벨의 routes/web.php에 Route::get('/bets', ...) 를 적는 
         * 것과 하는 역할이 같은데, 여기서는 서버가 아니라 브라우저 안에서 주소가 바뀌는걸
         * 감지해서 화면만 바꿔 키워줍니다. (페이지 전체가 새로고침되지 않는 이런 방식을 
         * SPA, Single Page Aplication 이라고 부른다.)*/}
         <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/bets" element={<BetsPage />} />
         </Routes>
      </main>
    </div>
  )
}

export default App