/**
 * 대시보드에 나오는 작은 카드 하나("총 베팅 선수: 3건"처럼 라벨+값을 보여주는
 * 상자)를 만드는 "레고 블록"이다. DashboardPage.tsx에서 이 블록을 9번, 3번
 * 이런 식으로 필요한 만큼 여러 번 찍어내서 쓴다.
 * ================================================================
 * 이 컴포넌트(블록)가 밖에서 받을 수 있는 값(=Props, "부모가 넘겨주는 재료")의
 * 모양을 정의한다. 라라벨의 Blade 컴포넌트에 @props로 넘기는 값들과 비슷한 개념
 * 
 */
interface StatCardProps {
    label: string // 카드 위쪽에 작게 나오는 이름표. 예: "환수율"
    value: string // 카드에 크게 나오는 실제 값. 예: "436.2%"
    tone?: 'default' | 'good' | 'bad'
    // 카드 색깔 톤. 물음표(?)가 있으니 안 넘기면 기본값 default를 쓴다.
    // good이면 초록색(좋은 지표), bad면 빨간색(나쁜지표)로 표시된다.
}

/**
 * 실제 카드 블록을 만들어내는 함수(=React 컴포넌트)이다.
 * { lsabel, value, tone = 'default' } 부분은 "구조분해할당"이라고 부르는데,
 * StatCardProps 객체 안에서 label, value, tone값을 바로 꺼내 쓰는 문법이다. 
 * tone = 'default'는 "tone을 안 넘겨줬으면 기본값으로 'default'를 써라"는 뜻이다.
 */
export function StatCard({ label, value, tone = 'default' }:StatCardProps) {
    // return (...) 안의 내용은 JSX라고 부르는, HTML처럼 생긴 문법이다.
    // 실제로는 "이런 화면 구조를 그려라"는 뜻의 자바스크립트 코드로 변환된다.
    return(
        <div className={`stat-card stat-card--${tone}`}>
            {/** 백틱 문자열로 클래스 이름을 조합한다. tone이 'good'이면
             * className은 "stat-card stat-card-good"이 되고, index.css에
             * 정의된 그 스타일(초록색)이 적용된다.*/}
            <span className="stat-card__label">{label}</span>
            {/** {label}처럼 중괄호로 감싼 부분은 "여기에 자바스크립트 값을
             * 그대로 끼워넣어라"라는 뜻이다. */}
            <span className="stat-card__value">{value}</span>
        </div>
    )
}