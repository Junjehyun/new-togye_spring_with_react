package com.yama331.togye.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.yama331.togye.entity.BetResult;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 클라이언트가 POST/PUT 바디로 보내는 입력.
 *
 * Jackson이 JSON 키와 필드 이름을 맞춰 값을 넣는다.
 *   {"description":"...", "odds":1.9, "betResult":"HIT"}
 *
 * Lombok @Setter 가 있어야 JSON → 객체 변환이 된다 (빈 생성자 + setter).
 * @Getter 는 Service가 request.getOdds() 로 읽게 한다.
 *
 * class 필드이므로 한 줄은 세미콜론으로 끝난다.
 */
@Getter
@Setter
@NoArgsConstructor // Jackson이 new BettingRecordRequest() 후 setter 호출
public class BettingRecordRequest {

    // 어노테이션 없음 = null 허용. 엑셀의 빈 날짜
    private LocalDate betDate; 

    // 빈 문자열 "" 도 거절. @NotNull만 있으면 "" 은 통과한다
    @NotBlank
    private String description; 

    // 배당 1.0 미만(0.5 등) 거절
    @NotNull
    @DecimalMin("1.0")
    private BigDecimal odds; // 배당

    // 0원 배팅 거절. 폼 초기값을 0으로 두면 첫 추가가 400이다
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal betAmount; // 베팅금

    @NotNull
    @Min(1)
    private Integer folderCount; // 폴더수 

    // JSON 키는 "betResult". "result" 로 보내면 이 필드가 null → @NotNull 400
    // "WIN" 처럼 enum에 없는 값이면 @Valid 전에 JSON 파싱이 400 (errors 배열 없음)
    @NotNull
    private BetResult betResult; // 결과
}
