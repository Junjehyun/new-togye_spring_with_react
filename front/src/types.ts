/**
 * 이 파일은 "백엔드가 주고받는 데이터의 모양"을 타입스크립트 언어로
 * 옮겨 적어둔 파일이다. 백엔드의 record(BetResponse.java 등)와 정확히
 * 짝이 맞아야 한다. 백엔드가 이 모양으로 JSON을 보내주기로 약속 했으니,
 * 프론트엔드도 "우리가 받을 데이터는 이런 모양이다"라고 미리 알고 있어야 
 * 실수 없이 안전하게 쓸 수 있다.
 * =====================================================================
 * 베팅 결과가 될 수 있는 4가지 값. 백엔드 BetResult.java(enum)를
 * 타입스크립트로 흉내낸 것이라고 볼 수 있다. 이렇게 문자열 몇 개만 허용되는 타입을
 * "리터럴 유니언 타입"이라고 부른다. "이 값은 이 4개 문자열 중 하나여야 한다"는 뜻이다.
 * 철자가 백엔드의 enum과 정확히 똑같아야함. (대소문자까지도)
 */
export type BetResult = 'PENDING' | 'WIN' | 'LOSE' | 'PUSH'

/**
 * 베팅 기록 하나를 화면에 표시할 때 쓰는 모양이다. 백엔드의 
 * BetResponse.java(record)와 필드 이름,순서 의미가 그대로 대응된다.
 * `interface`는 "이런 모양의 객체다"를 정의하는 타입스크립트 문법이다.
 */
export interface BetResponse {
    id: number
    betDate: string
    description: string
    odds: number
    stake: number
    foldCount: number
    result: BetResult
    payoutAmount: number
    profit: number | null
    /**
     * ↑ "숫자이거나 null일 수 있다"는 뜻. PENDING(대기중)일 때는
     * 아직 수익을 모르니 백엔드가 null을 보내온다. 이렇게 타입을 정확히 맞춰두면
     * bet.profit을 null 체크 없이 함부로 쓰면 타입스크립트가 바로 경고를 해준다.
     */
    runningBalance: number
    cumulativeTotal: number
    streak: number
    outcomeLable: string
}

/**
 * 새 베팅을 등록할 때 서버로 보내는 데이터의 모양. BetCreateRequest.java와 대응한다.
 */
export interface BetCreateRequest {
    betDate: string
    description: string
    odds: number
    stake: number
    foldCount: number
    result?: BetResult
    // 이름 뒤의 물음표(?)는 "이 필드는 없어도 된다(optional)"는 뜻.
    // 등록할 때는 결과를 아직 모르니 안 보내도 되게 해둠.
}

/**
 * 기존 베팅을 수정할 때 서버로 보내는 데이터의 모양. BetUpdateRequest.java와 대응 
 */
export interface BetUpdateRequest {
    betDate: string
    description: string
    odds: number
    stake: number
    foldCount: number
    result: BetResult // 물음표가 없으므로 이건 필수 값이다. 수정 전 결과가 나왔기 때문
}

/**
 * 대시보드 통계 카드에 쓰이는 데이터 모양. BetStatsResponse.java와 대응
 */
export interface BetStatsResponse {
    betCount: number
    winCount: number
    loseCount: number
    pushCount: number
    pendingCount: number
    totalStake: number
    cumulativeProfit: number
    returnRate: number | null
    hitRate: number | null
    avgOdds: number | null
    maxWinStreak: number
    maxLossStreak: number
}

/**
 * 입출금 구분(입금/출금). 백엔드의 TransactionType.java(enum)와 대응.
 * 아직 트렌젝션쪽 작업 안했으니 나중에 
 */

/** 
 * 입출금 기록 하나를 화면에 표시할 때 쓰는 모양. TransactionResponse.java와 대응. 
 * 아직 트렌젝션쪽 작업 안했으니 나중에 
 */

/** 
 * 입출금 기록을 등록/수정할 때 서버로 보내는 데이터 모양. TransactionRequest.java와 대응.
 * 아직 트렌젝션쪽 작업 안했으니 나중에 
 */

/** 
 * 사이트 한 곳에 대한 입출금 요약. SiteSummary.java와 대응. 
 * 아직 트렌젝션쪽 작업 안했으니 나중에 
 */

/** 
 * 대시보드에 보여줄 입출금 전체 통계. TransactionSummaryResponse.java와 대응. 
 * 아직 트렌젝션쪽 작업 안했으니 나중에
 */