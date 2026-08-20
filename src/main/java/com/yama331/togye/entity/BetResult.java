package com.yama331.togye.entity;

/**
 * 배팅 결과 상태
 * - HIT     : 적중
 * - MISS    : 미적중
 * - PUSH    : 적중특례 (본전)
 * - PENDING : 결과 대기
 */
public enum BetResult {
    HIT,     
    MISS,    
    PUSH,    
    PENDING  
}
