package com.yama331.togye.dto;

import com.yama331.togye.entity.BetResult;
import com.yama331.togye.entity.BettingRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 클라이언트로 나가는 한 행.
 * 서버가 이미 계산한 값이므로 @NotNull 같은 입력 검증을 붙이지 않는다.
 */
@Getter
@AllArgsConstructor
public class BettingRecordResponse {

    private Long id;
    private LocalDate betDate;
    private String description;
    private BigDecimal odds;
    private BigDecimal betAmount;
    private Integer folderCount;
    private BigDecimal winAmount;
    private BetResult betResult;
    private BigDecimal profit;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BettingRecordResponse from(BettingRecord entity) {
        return new BettingRecordResponse(
                entity.getId(),
                entity.getBetDate(),
                entity.getDescription(),
                entity.getOdds(),
                entity.getBetAmount(),
                entity.getFolderCount(),
                entity.getWinAmount(),
                entity.getBetResult(),
                entity.getProfit(),
                entity.getBalance(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
