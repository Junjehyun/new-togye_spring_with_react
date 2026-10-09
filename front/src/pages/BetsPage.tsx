/**
 * 베팅 기록 화면이다. 이 프로젝트의 프론트엔드 중 가장 복잡하고 중요한 파일이다.
 * 새 베팅 등록 폼, 필터, 목록 표, 결과 변경, 삭제까지 전부 이 파일 하나에 들어있다. 
 * GUIDE.md 7.6장에서 이 파일을 아주 자세히 설명하니, 막히면 그 쪽을 보자.
 */

import { useEffect, useState, type FormEvent } from 'react'
import { betsApi, type BetFilter } from '../api/bets'
import { formatOdds, formatWon, resultLabel } from '../format'
import type { BetResponse, BetResult } from '../types'

// 결과값으로 고를 수 있는 4개 후보. 아래 <select> 드롭다운의 옵션 목록을 
// 만들 때 이 배열을 그대로 돌면서 쓴다. (하드코딩을 한 곳에만 몰아두면,
// 나중에 후보가 바뀌어도 이 한줄만 고치면 된다.)
const RESULT_OPTIONS: BetResult[] = ['PENDING', 'WIN', 'LOSE', 'PUSH']

/**
 * "등록" 폼이 처음 열렸을 때, 또는 등록에 성공한 뒤 다시 비워질 때 쓰는 
 * 기본값 모음이다. 이렇게 한 군데 모아두면 emptyForm이라는 이름 하나로
 * "폼을 초기 상태로 되돌린다"는 뜻을 표현할 수 있다. 
 * (참고: 폼의 input 값들은 모두 문자열(string)로 관리한다. HTML의
 * <input>은 사용자가 뭘 입력하든 항상 문자열을 돌려주기 때문이다.
 * 실제로 서버에 보낼 때는 Number(...)로 진짜 숫자로 바꿔준다.) 
 */
const emptyForm = {
    /**
     * ↑ 오늘 날짜를 "2026-08-24" 형태의 문자열로 만든다.
     * new Date()로 지금 이 순간의 날짜/시간을 만들고, .toISOString()으로
     * "2026-08-24T05:00:00.000Z" 같은 표준 형식 문자열로 바꾼 뒤, 
     * .slice(0, 10)으로 앞의 10글자("2026-08-24")만 잘라낸다.
     */
    betDate: new Date().toISOString().slice(0, 10),
    description: '',
    odds: '',
    stake: '',
    foldCount: '1',
}

export function BetsPage() {
    /**
     * 이 화면이 기억해야 하는 값들 (메모지)
     */
    const [bets, setBets] = useState<BetResponse[]>([])
    // ↑ 지금 화면에 보여줄 베팅 목록. 처음엔 빈 배열로 시작.
    const [loading, setLoading] = useState(true)
    // ↑ 목록을 불러오는 중인지 여부.
    const [error, setError] = useState<string | null>(null)
    // ↑ 에러가 났을 때 보여줄 메세지

    const [form, setForm] = useState(emptyForm)
    // ↑ "베팅 등록" 폼에 지금 입력 중인 값들.
    const [submitting, setSubmitting] = useState(false)
    // ↑ 등록 버튼을 눌러서 서버에 요청을 보내고 있는 중인지 여부, (이 값이 true일 때는 버튼을 비활성화 해서 중복 클릭을 막음)
    
    const [filter, setFilter] = useState<BetFilter>({})
    // ↑ 목록 조회에 걸어둔 필터(기간/결과). 처음엔 필터 없음 ({})

    /**
     * 서버에서 베팅 목록을 다시 불러오는 함수다. 폼 제출, 필터 변경,
     * 결과 변경, 삭제 등 "화면을 최신 상태로 다시 그려야 하는" 모든 순간에 이 함수를 호출한다. 
     * 
     * activeFilter: BetFilter = filter 부분은 "매개변수를 안 넘겨주면 지금 저장된 filter 메모지 값을 
     * 기본 값으로 써라"라는 뜻
     */
    const load = (activeFilter: BetFilter = filter) => {
        setLoading(true)
        betsApi
            .list(activeFilter)
            .then(setBets)
            // ↑ .then(setBets)는 .then((data) => setBets(data))를 짧게 쓴 것이다.
            .catch((err: Error) => setError(error.message))
            .finally(() => setLoading(false))
    }

    // 이 화면이 처음 나타날 때, 필터 없이 전체 목록을 한번에 불러옴 
    useEffect(() => {
        load({})
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    /**
     * 필터 폼("조회" 버튼)을 제출했을 때 실행된다. 
     */
    const handleFilterSubmit = (e: FormEvent) => {
        e.preventDefault()
        /**
         * ↑ HTML의 <form>은 원래 제출하면 페이지 전체가 새로고침되는데, 리액트 SPA에서는 
         * 그러면 안되므로(화면이 통째로 다시 로드되면 안됨), 이 기본 동작을 막는다.
         * 폼을 다루는 함수에서는 거의 항상 이 줄이 맨 앞에 온다.  
         */
        load(filter)
    }

    /**
     * "베팅 등록" 폼을 제출했을 때 실행된다.
     */
    const handleCreateSubmit = async (e: FormEvent) => {
        e.preventDefault()
        setSubmitting(true)
        setError(null)
        try {
            /**
             * await: betsApi.create(...)가 서버에 요청을 보내고 응답이 올 때까지 기다린다.
             * try / catch / finally는 PHP랑 똑같다. 
             * "시도해보고(try), 실패하면 잡고(catch), 끝나면 무조건 실행(finally)"
             */
            await betsApi.create({
                betDate: form.betDate,
                description: form.description,
                odds: Number(form.odds),
                // ↑ form.odds는 <input>에서 온 문자열 ("2.5")이다. 서버는 숫자를 기대하므로, 
                // Number(...)로 진짜 숫자로 바꿔준다. 
                stake: Number(form.stake),
                foldCount: Number(form.foldCount)
            })
            setForm(emptyForm) // 등록에 성공했으니 폼을 다시 비운다.
            load() // 방금 등록한 게 목록에 바로 나타나도록 목록을 다시 불러온다.
        } catch(err) {
            setError((err as Error).message)
            /**
             * ↑ "as Error"는 "이 err 값은 사실 Error 타입이다."라고 타입스크립트에게 알려주는 타입 단언이다.
             * catch로 잡히는 값의 타입은 원래 확실하지 않기 때문에 이렇게 명시한다.
             */
        } finally {
            setSubmitting(false)
        }
    }

    /**
     * 베팅 결과를 바꿀 때 (예: "대기중" -> "적중") 실행된다. 표의 <select>드롭 다운에서 값을 고르는 순간
     * 이 함수가 호출 된다.
     * 
     * bet 매개변수로 "지금 이 행의 베팅 데이터 전체"를 받는 이유 : PUT 요청은 "전체 교체" 방식이라서,
     * 날짜/내역/배당/베팅금액/폴더수도 지금 값 그대로 다시 채워서 같이 보내야 한다. result만 슬쩍 안 보내면
     * 서버가 "그 필드는 지워라"로 오해할 수 있기 때문이다.  
     */
    const handleResultChange = async (bet: BetResponse, result: BetResult) => {
        try {
            await betsApi.update(bet.id, {
                betDate: bet.betDate,
                description: bet.description,
                odds: bet.odds,
                stake: bet.stake,
                foldCount: bet.foldCount,
                result, // ↑ { result }를 줄여쓴 것 { result : result }와 같은 뜻이다. 
            })
            load()
        } catch(err) {
            setError((err as Error).message)
        }
    }

    /**삭제 버튼을 눌렀을 때 실행된다. */
    const handleDelete = async(id: number) => {
        /**
         * window.confirm(...) : 브라우저가 기본으로 제공하는 "확인/취소" 팝업창
         * 사용자가 '취소'를 누르면 confirm이 false를 돌려주고, !false는 true라서 
         * 그 즉시 return으로 함수를 끝내버려서 삭제가 진행되지 않는다.
         */
        if (!window.confirm('이 베팅 기록을 삭제할까요?')) return
        try {
            await betsApi.remove(id)
            load()
        } catch(err) {
            setError((err as Error).message)
        }
    }

    /**
     * 여기부터는 실제로 화면에 그려지는 부분 (JSX)
     */
    return (
        <div className='page-stack'>
            <section>
                <h2>베팅 등록</h2>
                <form className="inline-form" onSubmit={handleCreateSubmit}>
                    <label>
                        날짜
                        <input 
                            type="date" 
                            required
                            value={form.betDate}
                            /**
                             * onChange = 사용자가 이 입력칸의 값을 바꿀 때마다 실행된다.
                             * { ...form, betDate: e.target.value }는 "지금 form
                             * 객체를 통째로 복사하되, betDate값만 새로 입력한 값으로 바꿔서
                             * 새 객체를 만든다"는 뜻이다. (스프레드 연산자 활용)
                             * 리액트에서는 기존 객체를 직접 수정하지 않고, 항상 이렇게
                             * "새 객체를 만들어서" setForm에 넘겨주는 방식을 쓴다. 
                             * 
                             */
                            onChange={(e) => setForm({ ... form, betDate: e.target.value })}
                        />
                    </label>
                    <label className='grow'>
                        내역
                        <input 
                            type="text" 
                            required
                            placeholder="예) 한신 vs 요코하마 (-1.5 한신)"
                            value={form.description}
                            onChange={(e) => setForm({ ...form, description: e.target.value })}
                        />
                    </label>
                    <label>
                        배당
                        <input 
                            type="number"
                            step="0.0001"
                            min="1"
                            required
                            value={form.odds}
                            onChange={(e) => setForm({ ...form, odds: e.target.value })}
                        />
                    </label>
                    <label>
                        베팅금액
                        <input 
                            type="number"
                            min="1"
                            required
                            value={form.stake}
                            onChange={(e) => setForm({ ...form, stake: e.target.value })} 
                        />
                    </label>
                    <label>
                        폴더수
                        <input 
                            type="number"
                            min="1"
                            required
                            value={form.foldCount}
                            onChange={(e) => setForm({ ...form, foldCount: e.target.value })} 
                        />
                    </label>
                    <button type="submit" disabled={submitting}>
                        {/**
                         * {submitting ? '등록 중..' : '등록'} : 등록 요청이 진행 중이면 
                         * 버튼 글자를 "등록 중..."으로 바꿔서 사용자에게 알려줌. 
                         */}
                        {submitting ? '등록 중..' : '등록' }
                    </button>
                </form>
            </section>

            <section>
                <div className='section-head'>
                    <h2>베팅 기록</h2>
                    <form className='filter-form' onSubmit={handleFilterSubmit}>
                        <label>
                            시작일
                            <input 
                                type="date"
                                value={filter.from ?? ''} 
                                // ↑ filter.from이 없으면(undefined) 빈 문자열을 보여준다.
                                onChange={(e) => setFilter({ ...filter, from: e.target.value || undefined })}
                                // ↑ e.target.value가 빈 문자열이면 undefined로, 아니면 그
                                // 값 그대로 저장한다 ("필터를 껐다"는 걸 undefined로 표현)
                            />
                        </label>
                        <label>
                            종료일
                            <input 
                                type="date"
                                value={filter.to ?? ''}
                                onChange={(e) => setFilter({ ...filter, to: e.target.value || undefined })} 
                            />
                        </label>
                        <label>
                            결과
                            <select
                                value={filter.result ?? ''}
                                onChange={(e) => 
                                    setFilter({ ...filter, result: (e.target.value || undefined ) as BetResult | undefined })
                                }
                            >
                                <option value="">전체</option>
                                {/** RESULT_OPTIONS.map(...) : 배열의 각 값마다 <option> 하나씩을
                                 * 반복해서 만들어낸다.
                                 */}
                                 {RESULT_OPTIONS.map((r) => (
                                    <option key={r} value={r}>
                                        {resultLabel(r)}
                                    </option>
                                 ))}
                            </select>
                        </label>
                        <button type="submit">조회</button>
                    </form>
                </div>

                {/** {error && <p>...</p>} : error 메모지에 값이 있을 때만 이 줄을 보여준다. */}
                {error && <p className='error'>{error}</p>}
                {loading ? (
                    <p className="mute">불러오는 중...</p>
                ) : (
                    <div className="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>날짜</th>
                                    <th>내역</th>
                                    <th className="num">배당</th>
                                    <th className="num">배팅금액</th>
                                    <th className="num">폴더수</th>
                                    <th>결과</th>
                                    <th className="num">적중금액</th>
                                    <th className="num">수익</th>
                                    <th className="num">잔고</th>
                                    <th className="num">합계</th>
                                    <th className="num">스트릭</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {bets.length == 0 && (
                                    <tr>
                                        <td colSpan={11} className="muted">
                                            등록된 베팅 기록이 없습니다.
                                        </td>
                                    </tr>
                                )}
                                {/**
                                 * bets.map((bet) => (...)) : 베팅 하나마다 <tr> 한 줄씩을
                                 * 반복해서 만듭니다. key={bet.id}는 리액트가 각 줄을 
                                 * 구별하기 위해 반드시 필요한 고유값이다.
                                 */}
                                 {bets.map((bet) => (
                                    <tr key={bet.id}>
                                        <td>{bet.betDate}</td>
                                        <td className="description-cell">{bet.description}</td>
                                        <td className="num">{formatOdds(bet.odds)}</td>
                                        <td className="num">{formatWon(bet.stake)}</td>
                                        <td className="num">{bet.foldCount}</td>
                                        <td>
                                            <select
                                                value={bet.result}
                                                onChange={(e) => handleResultChange(bet, e.target.value as BetResult)}
                                                // ↑ e.target.value는 항상 문자열이라서, "as BetResult"로
                                                // "이건 사실 BetResult 타입의 문자열이야"라고 알려준다.
                                                className={`result-select result-select--${bet.result.toLowerCase()}`}
                                                // ↑ 결과값에 따라 다른 CSS 클래스가 붙어서, WIN이면 초록,
                                                // LOSE면 빨간 테두리가 되도록 index.css에서 색을 입힌다.
                                            >
                                                {RESULT_OPTIONS.map((r) => (
                                                    <option key={r} value={r}>
                                                        {resultLabel(r)}
                                                    </option>
                                                ))}
                                            </select>
                                        </td>
                                        <td className="num>">{formatWon(bet.payoutAmount)}</td>
                                    </tr>
                                 ))}
                            </tbody>
                        </table>
                    </div>
                )}



            </section>
        </div>
    )

}