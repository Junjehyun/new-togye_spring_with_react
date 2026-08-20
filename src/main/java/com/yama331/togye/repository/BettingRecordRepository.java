package com.yama331.togye.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yama331.togye.entity.BetResult;
import com.yama331.togye.entity.BettingRecord;
import java.time.LocalDate;

/**
 * DB 접근 창구.
 *
 * JpaRepository를 상속하면 아래 메서드는 안 적어도 생긴다.
 *   findAll()          전체
 *   findById(id)       단건. 없으면 빈 Optional
 *   save(entity)       id가 없으면 INSERT, 있으면 UPDATE
 *   deleteById(id)     삭제
 *   count()
 *   existsById(id)
 *
 * 우리가 추가로 적는 메서드는 "이름 = SQL" 이다. 중괄호 본문이 없다.
 */
public interface BettingRecordRepository extends JpaRepository<BettingRecord, Long>{

    /**
     * 1차가 실제로 부르는 메서드.
     * 목록과 잔고 재계산 둘 다 "엑셀처럼 위에서 아래로" = id 오름차순.
     * 날짜순이 아니다. 날짜가 null인 행이 섞여 있다.
     */
    List<BettingRecord> findAllByOrderByIdAsc();

    // 아래는 이름 연습용. Service에서 안 불러도 컴파일된다. 헷갈리면 지워도 된다.
    List<BettingRecord> findAllByOrderByIdDesc();

    // Java 필드명 betDate → WHERE bet_date = ?
    List<BettingRecord> findByBetDate(LocalDate betDate);

    // Java 필드명 betResult → WHERE bet_result = ?
    // findByResult 가 아니다. 필드가 result가 아니기 때문이다.
    List<BettingRecord> findByBetResult(BetResult betResult);
}
