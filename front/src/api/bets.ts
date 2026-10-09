/**
 * 베팅(Bet) 관련 API 호출을 한 곳에 모아둔 파일입니다. 
 * BetController.java의 엔드포인트 5개와 이 파일의 함수 5개가 정확히 1:1로 매칭한다.
 * 두 파일을 나란히 펴놓고 비교해면 구조가 완전히 같다는걸 알 수 있다. 
*/

import { api } from './client.ts'
import type { 
    BetCreateRequest,
    BetResponse,
    BetResult,
    BetStatsResponse,
    BetUpdateRequest
 } from '../types.ts'

/**
 * 베팅 목록을 조회할 때 걸 수 있는 필터 조건들의 모양이다. 
 * 셋 다 물음표(?)가 붙어 있어서 전부 선택사항(optional)이다. 
 * 아무것도 안 넘기면 전체 목록을 조회하게 된다.
 */
export interface BetFilter {
    from?: string
    to?: string
    result?: BetResult
}

/**
 * 필터 객체를 실제 URL 뒤에 붙일 "쿼리스트링" 문자열로 바꿔주는 함수다.
 * 예: { from: '2026-08-01', result: 'WIN' } -> "?from=2026-08-01&result=WIN"
 */
function toQuery(filter: BetFilter): string{
    // URLSearchParams : "?key=value&key2=value2" 형태를 안전하게 (특수문자
    // 이스케이프까지 포함해서) 만들어주는, 브라우저에 내장된 도구다.
    const params = new URLSearchParams()
    if (filter.from) params.set('from', filter.from)
    if (filter.to) params.set('to', filter.to)
    if (filter.result) params.set('result', filter.result)
    const query = params.toString()
    // 조건이 하나도 없으면 물음표조차 안 붙이고 빈 문자열을 돌려준다.
    return query ? `?${query}` : ''
}
    /**
     * 이 객체가 실제로 다른 화면 (페이지) 코드에서 불러다 쓰는 진짜 API 함수다.
     * 백엔드 BetController.java와 짝을 맞춰서 읽어보자. 
     */
    export const betsApi = {
        // GET /api/bets?from=...&to=...&result=... -> betController.findAll()
        list: (filter: BetFilter = {}) => api.get<BetResponse[]>(`/bets${toQuery(filter)}`),
        // GET /api/bets/stats -> BetController.stats()
        stats: () => api.get<BetStatsResponse>('/bets/stats'),
        // POST /api/bets -> BetController.create()
        create: (body: BetCreateRequest) => api.post<BetResponse>('/bets', body),
        // PUT /api/bets/{id} -> BetController.update()
        update: (id: number, body: BetUpdateRequest) => api.put<BetResponse>('/bets/${id}', body),
        // DELETE /api/bets/{id} -> BetController.delete()
        remove: (id: number) => api.delete(`/bets/${id}`)
    }
