package com.yama331.togye.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.yama331.togye.entity.BetResult;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * BetUpdateRequest
 * 
 * "이미 있는 베팅 기록을 이 내용으로 통째로 바꿔줘! 한마디로 수정!"하라고 보내는 주문서다.
 * BetController의 update() 메서드, 즉 PUT 요청이 이 모양의 JSON을 받는다. 
 * 
 * BetCreateRequest와 거의 똑같이 생겼는데 딱 하나 다른점이 있음.
 * 여기서는 result에도 @NotNull이 붙어 잇다. PUT은 "전체 교체" 방식이라서,
 * 결과가 뭐라도 반드시 같이 보내야 한다. (안 보내면 "결과를 지워라"라는 뜻이 되어버려 위험)
 * 그래서 프론트엔드의 
 */
public record BetUpdateRequest(
    @NotNull LocalDate betDate,
    @NotBlank String description,
    @NotNull @DecimalMin(value = "1.0") BigDecimal odds,
    @NotNull @Min(1) Long stake,
    @NotNull @Min(1) Integer foldCount,
    @NotNull BetResult result // 수정할 때는 결과값이 반드시 필수. (수정할 결과가 있어야하기 때문)

) {
}
