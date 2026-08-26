package com.yama331.togye.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.yama331.togye.entity.BetResult;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 
 * BetCreateRequest
 * @param betDate
 * @param description
 * @param odds
 * @param stake
 * @param foldCount
 * @param result
 * 
 * "새 베팅을 하나 만들어줘"라고 손님(프론트엔드)이 보내는 주문서다.
 * BetController의 create() 메서드가 이 모양의 JSON을 받는다.
 * 
 * 이건 "record"라는 자바 문법으로 만들어진 클래스다. Bet.java(Entity)처럼
 * 직접 필드/생성자/getter를 다 써주지 않고, 아래처럼 딱 한줄만 써도 자바가 알아서
 * 생성자, betDate()/description()같은 getter(자바 record는 필드 이름 그대로
 * 메서드가 된다.) equals(), toString()을 전부 만들어준다. "그냥 값만 담는 상자"를
 * 짧게 정의할 때 자주 쓴다. 라라벨에서 이거랑 비슷한 건 From Request 클래스다. 
 * "이런 모양의 데이터가 들어오야 하고, 이런 검증규칙을 적용한다"를 한 곳에 정의하는 역할이 같다.
 */
public record BetCreateRequest(
    /**betDate
     * @NotNull: "이 값은 절대 비어(null) 있으면 안 된다."는 검증규칙
     * 라라벨의 required 규칙과 같다. 이 값이 비어서 오면, 실제 create()
     * 메서드 코드는 실행조차 되지 않고, 스프링이 자동으로 400 에러를 응답한다.
     */
    @NotNull LocalDate betDate, // 베팅한 날짜

    /**description
     * @NotBlank: null도 안되고, 빈 문자열("")이나 공백만 있는 것도 안 된다는 뜻.
     */
    @NotBlank String description, // 베팅 내역

    /**odds
     * @DecimalMin(value = "1.0"): 이 숫자는 1.0보다 작으면 안 된다는 뜻.
     * 배당률이 1.0보다 작을 수는 없으니(원금보다 적게 받는 배당은 말이 안 됨) 넣은 규칙.
     */
    @NotNull @DecimalMin(value = "1.0") BigDecimal odds, // 배당률

    /**stake
     * @Min(1): 이 숫자는 1보다 작으면 안된다(즉 최소 1 이상)
     */
    @NotNull @Min(1) Long stake, // 베팅금
    @NotNull @Min(1) Integer foldCount, // 폴더수

    /**result
     * result는 @NotNull이 없다. "안 보내도 되는 값"이라는 뜻.
     * 베팅을 처음 등록했을 때는 아직 결과를 모르니, 이 값을 안 보내면
     * 아래 resultOrDefault()에서 자동으로 PENDING(대기중)으로 채워진다.
     */
    BetResult result
) {
    /**
     * result 값이 안 왔으면(null) PENDING(대기중)을 기본값을 쓰고,
     * 왔으면 그 값을 그대로 쓴다. record 안에 이렇게 우리가 직접 메서드를
     * 추가로 정의할 수도 있다. 자동으로 생기는 것 말고 우리가 필요한 걸
     * 얼마든지 더 얹을 수 있다.
     * @return
     */
    public BetResult resultOrDefault() {
        return result == null ? BetResult.PENDING : result;
    }
}
