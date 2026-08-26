package com.yama331.togye.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.yama331.togye.entity.BetResult;

/**
 * 베팅 기록 하나를 화면에 보여줄 때 쓰는 "영수증"이다. != Request는 주문서. 
 * BetController가 손님(프론트엔드)에게 돌려주는 응답이 바로 이 모양. 
 * 
 * 이 record의 필드는 크게 두 그룹으로 나뉜다.
 * 
 * ① id~result (앞쪽7개) - Bet.java(Entity, "냉장고 속 재료")에 실제로 
 * 저장되어 있는 값을 그대로 옮겨온 것이다. 
 * 
 * ② payoutAmount ~ outcomeLabel (뒤쪽 6개) - Bet.java에는 이런 필드가
 * 존재하지 않는다. 이 값들은 BetCalculationService가 조회하는 "그 순간"에 새로 계산
 * 해서 여기에 끼워 넣는 값으로 한다. 
 * 
 * 바로 이 ①과 ②가 나뉘어 있다는 사실이, 지난번에 이야기했던 "Entity랑 DTO가 왜 따로 있어야
 * 하는가"에 대한 실제 답이다. 냉장고 속 재료(Entity)와 손님에게 나가는 영수증(DTO)의 모양이
 * 원래부터 다르기 때문에, 둘을 하나로 합칠 수가 없다. 
 */
public record BetResponse(
    // ①
    Long id,
    LocalDate betDate,
    String description,
    BigDecimal odds,
    Long stake,
    Integer foldCount,
    BetResult result,
    // ② (여기부터는 DB에 없는, 계산해서 만들어낸 값들이다.)
    /**
     * Long payoutAmount(적중금액): 이 베팅이 "만약 적중했다면" 받는 금액 (배당 * 베팅금액)
     * Long profit(수익): WIN이면 (payoutAmount - stake), LOSE면 -stake, 
     * PUSH면 0, 아직 결과를 모르는 PENDING이면 null(=모른다)
     * Long runningBalance(잔고): 이 베팅까지 포함해서, 지금까지 누적된 자금 상태
     * Long cumulativeTotal(연승연패 스트릭): 양수면 그 숫자만큼 연승 중, 음수면 그 숫자만큼 연패 중
     * String outcomeLabel: 결과를 사람이 읽기 좋은 한글 단어로: "이익"/"손해"/"본전"/"대기중"
     */
    Long payoutAmount,                
    Long profit,
    Long runningBalance,
    Long cumulativeTotal,
    int streak,
    String outcomeLabel
) {

}
