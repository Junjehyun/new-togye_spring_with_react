package com.yama331.togye.dto;

import java.math.BigDecimal;

/**
 * BetStatsResponse
 * 
 * 대시보드 화면 맨 위에 나오는 "배팅 통계 카드들"에 쓰이는 영수증
 * BetController의 GET /api/bets/stats가 이 모양의 JSON을 응답으로 돌려준다.
 * 이 값들도 전부 DB컬럼이 아니라, BetCalculationService.stats() 메서드가 
 * 전체 베팅 기록을 훑어서 그 자리에서 계산해내는 값들이다. 
 */
public record BetStatsResponse(
    long betCount, // 전체 베팅 건수
    long winCount, // 적중(WIN) 건수
    long loseCount, // 미적중(LOSE) 건수
    long pushCount, // 적특(PUSH) 건수
    long pendingCount, // 아직 결과가 안 나온(PENDING) 건수
    long totalStake, // 지금까지 걸었던 베팅금액을 전부 더한 총액
    long cumulativeProfit, // 확정된(PENDING 제외) 수익을 전부 더한 값. 음수면 손실 
    BigDecimal returnRate, // 환수율: (총배팅액 + 누적수익) ÷ 총배팅액. 예: 0.8889 = 88.89%
    BigDecimal hitRate, // 적중률: 적중 건수 ÷ (적중 건수 + 낙첨 건수)
    BigDecimal avgOdds, // 평균 배당률
    int maxWinStreak, // 연승기록 (양수)
    int maxLoseStreak // 연패기록 (음수)
) {
    
}
