/**
 * 첫 화면(대시보드)을 그리는 컴포넌트다. 베팅 통계와 입출금 통계를
 * 백엔드에서 가져와서, StatCard 블록들과 표로 보여준다. 
 */

import { useEffect, useState } from 'react'
import { betsApi } from '../api/bets'
// import { transactionsApi } from '../api/transactions'
import { StatCard } from '../components/StatCard'
import { formatOdds, formatPercent, formatWon } from '../format'
import type { BetStatsResponse, 
    //TransactionsSummaryResponse 
} from '../types'

export function DashboardPage() {
    // useState: 이 화면이 "기억해야 하는 값"을 담는 메모지이다. 메모지의 내용을 바꿀때는
    // 반드시 setStats, setSummary 같은 짝지어진 함수를 통해서만 바꿔야하고, 
    // 그렇게 바꾸면 리액트가 자동으로 화면을 다시 그린다. 

    // stats : 베팅 통계(BetStatsResponse). 아직 안 가져왔으면 null
    const [stats, setStats] = useState<BetStatsResponse | null>(null)

    // summary : 입출금 통계(TransactionSummaryResponse). 아직 안 가져왔으면 null
    // const [summary, setSummary] = useState<TransactionSummaryResponse | null>(null)

    // error: API 호출이 실패했을 때 보여줄 에러 메시지. 실패한 적 없으면 null
    const [error, setError] = useState<string | null>(null)

    // loading : 지금 데이터를 불러오는 중인지 여부. 처음엔 당연히 불러오는 중이므로 true로 시작
    const [loading, setLoading] = useState(true)

    // useEffect(() => { ... },[]) : "이 화면이 처음 나타나는 순간, 딱 한 번만"
    // 괄호 안의 코드를 자동으로 실행해달라는 뜻이다. 맨 뒤의 빈 배열 []이 
    // "딱 한 번만"을 의미한다.
    useEffect(() => {
        // Promise.all([...]) : 여러 개의 "나중에 결과가 나올 작업"(API 호출)을 
        // 동시에 시작해두고, 전부 다 끝날 때 까지 기다린다. 베팅 통계와 입출금 
        // 통계를 하나씩 순서대로 기다리는 것보다 더 빠르게 둘 다 가져올 수 있다.
        Promise.all([betsApi.stats()]) //,transactionsApi.summary()])
            .then(([betStats]) =>{
                // 성공하면 , 결과가 순서대로 배열에 담겨서 들어온다. 
                // [betStats, txSummary] 처럼 바로 이름을 붙여서 꺼내 쓰는 걸
                // "배열 구조분해할당" 이라고 부른다. 
                setStats(betStats)
                //setSummary(txSummary)
            })
            .catch((err : Error) => setError(err.message))
            // 둘 중 하나라도 실패하면(예: 백엔드 서버가 꺼져있으면) 여기로 온다.
            .finally(() => setLoading(false))
            // 성공/실패 상관없이 마지막에 항상 실행된다. "이제 불러오는 중이 아니다"
            // 라고 표시해준다.
    },[])

    /**
     * 아래 3줄은 "이런 상황이면, 화면 전체를 다른 걸로 대신 그려라"는 뜻이다.
     * 데이터가 아직 없는데 무리해서 그리려고 하면 에러가 나기 때문에, 로딩/에러/데이터없음
     * 상황을 먼저 걸러 내는 것이다.
     */
    if (loading) return <p className='muted'>불러오는 중</p>
    if (error) return <p className='error'>{error}</p>
    if (!stats) return null // 나중에 !summary추가

    // 여기부터는 stats와 summary에 실제 데이터가 들어있다는 게 보장된 상태이다.
    return (
        <div className='page-stack'>
            <section>
                <h2>베팅 통계</h2>
                <div className='stat-grid'>
                    {/**
                     * {`${stats.betCount}건`} 처럼 중괄호+백틱 조합은 "자바스크립트 값을
                     * 문자열에 끼워서 화면에 보여줘라"는 뜻이다.
                     */}
                     <StatCard label="총 베팅 건수" value={`${stats.betCount}건`}/>
                     <StatCard label="베팅총액" value={formatWon(stats.totalStake)}/>
                     <StatCard
                        label="누적수익"
                        value={formatWon(stats.cumulativeProfit)}
                        tone={stats.cumulativeProfit >= 0 ? 'good' : 'bad'}
                        // "조건 ? A : B"는 삼항연산자. 누적수익이 0 이상이면 초록색 
                        // 음수면 빨간색 톤을 쓴다.
                     />
                     <StatCard label="환수율" value={formatPercent(stats.returnRate)} />
                     <StatCard label="적중률" value={formatPercent(stats.hitRate)} />
                     <StatCard label="평균배당" value={formatOdds(stats.avgOdds)} />
                     <StatCard label="최다연승" value={`${stats.maxWinStreak}승`} tone="good" />
                     <StatCard label="최다연패" value={`${Math.abs(stats.maxLossStreak)}패`} tone="bad" />
                     {/**
                      * maxLoseStreak는 백엔드에서 음수가 오므로, Math.abs로 절댓값(3)을 취해서
                      * "3패"처럼 자연스럽게 보이게 한다.
                      */}
                     <StatCard label="대기중 베팅" value={`${stats.pendingCount}건`} />
                </div>
            </section>
            <section>
                <h2>입출금 현황(나중에)</h2>
                <div className='stat-grid'>
                    {/**
                     * 나중에 해야함. 일단은 베팅관련만 먼저 끝내고 
                     */}
                </div>
            </section>
        </div>
    ) 
}   