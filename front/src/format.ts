// 화면에 숫자나 결과값을 "사람이 보기 좋은 글자"로 바꿔주는 도우미
// 함수들을 모은 파일이다. 예를 들어 30640이라는 숫자를 그냥 보여주지 않고,
// "30,640원"처럼 천 단위 쉼표를 붙인다던지 그런 보조적인 역할이다.

/**
 * 원 단위 금액을 "10,000원" 형태의 문자열로 바꿔준다.
 * number | null | undefined : 이 값은 숫자이거나, null이거나, undefined
 * 일 수도 있다는 뜻이다. (값이 아예 없을 수 있는 경우까지 대비한것)
 */
export function formatWon(amount: number | null | undefined): string {
    if(amount === null || amount === undefined) return '-'
    // Intl.NumberForamtr('ko-KR'): 브라우저에 내장된, 한국식 숫자 표기(천 단위 쉼표)
    // 로 바꿔주는 도구다. .format(amount)를 호출하면 "10,000"처럼 나오고 그 뒤에
    // "원"을 문자열로 붙여준다.
    return `${new Intl.NumberFormat('ko-KR').format(amount)}원`
}

/**
 * 0.8889 같은 소수를 "88.9%" 형태의 문자열로 바꿔준다.(환수율, 적중률 등에 사용)
 */
export function formatPercent(value: number | null | undefined):string {
    if(value === null || value === undefined) return '-'
    // value * 100 으로 퍼센트 값으로 바꾸고, .toFixed(1)로 소수점 첫째 자리까지만 남긴다.
    return `${(value * 100).toFixed(1)}%`
}

/**
 * 배당률(예: 4.3245)을 "4.32"처럼 소수점 둘째 자리까지만 보여준다. 
 */
export function formatOdds(value: number | null | undefined): string {
    if (value === null || value === undefined) return '-'
    return value.toFixed(2)
}

/**
 * 베팅 결과값(영어코드)을 화면에 보여줄 한글 단어로 바꿔준다.
 * switch (result) { ... } : 값에 따라 다른 문자열을 돌려준다.
 * 자바의 switch와 거의 문법이 같다. default는 "위의 case들에 하나도 안 걸리면
 * 이걸 써라"라는 뜻인데, 여기서는 result가 'PENDING'일 때
 * 여기로 온다.
 */
export function resultLabel(result: string): string {
    switch (result) {
        case 'WIN':
            return '적중'
        case 'LOSE':
            return '낙첨'
        case 'PUSH':
            return '적특'
        default:
            return '대기중'
    }
}