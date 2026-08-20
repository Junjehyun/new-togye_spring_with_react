package com.yama331.togye.entity;  
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 
 * BettingRecord
 * 
 * 순번, 날짜, 내역, 배당, 베팅금액, 폴더수, 적중금액, 적중유무, 수익, 잔고 
 */
@Entity
@Table(name = "betting_record")
@lombok.Getter
@lombok.Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BettingRecord {

    /** 순번 */
    @Id // Primary Key (PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 배팅 날짜 */
    @Column(name = "bet_date", nullable = true)
    private LocalDate betDate;

    /** 내역 (경기 + 배팅 내용) */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /** 배당 */
    @Column(name = "odds", nullable = false, precision = 10, scale = 4)
    private BigDecimal odds;

    /** 배팅 금액 */
    @Column(name = "bet_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal betAmount;

    /** 폴더 수 */
    @Column(name = "folder_count", nullable = false)
    private Integer folderCount;

    /** 적중 금액 (낙첨/적특 시 0 또는 null) */
    @Column(name = "win_amount", precision = 15, scale = 2)
    private BigDecimal winAmount;

    /** 적중 유무 */
    @Enumerated(EnumType.STRING)
    @Column(name = "bet_result", nullable = false, length = 20)
    private BetResult betResult;

    /** 수익 (적중 +, 낙첨 -, 적특 0) */
    @Column(name = "profit", nullable = false, precision = 15, scale = 2)
    private BigDecimal profit;

    /** 배팅 후 잔고 */
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    /** 생성 시각 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist // Insert직전
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate // Update직전
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}