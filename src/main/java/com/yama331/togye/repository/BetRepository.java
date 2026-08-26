package com.yama331.togye.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yama331.togye.entity.Bet;

/**
 * "베팅 기록 창고 관리인"이다. DB에서 베팅 기록을 가져오거나, 저장하는 일만 담당하고,
 * 계산이나 판단 같은건 하지 않는다.(그건 Service의 영역)
 * 
 * 이 파일이 특이한 점: {@code inteface}(인터페이스)이고, 몸통(실제 동작하는 코드)이
 * 비어있다. 그런데도 동작을 한다. 이유는 두가지다. 
 * 
 * 1) {@code extends JPARepository<Bet, Long>}를 쓰는 순간, 스프링이 자동으로
 * save(저장), findById(id로 하나 찾기), findAll(전체 찾기), deleteById(id로 삭제),
 * existsById(존재 여부 확인) 같은 기본 기능들을 알아서 만들어서 끼워 넣어준다. 
 * 코드 한 줄 안써도 BetCalculationService에서 betRepository.save(bet) 처럼
 * 바로 쓸 수 있는 이유가 이것이다. {@code <Bet, Long>}은 "이 창고가 다루는 재료 종류는
 * Bet이고, 그 재료의 번호표(기본키) 타입은 Long이다"라는 뜻이다.
 * 
 * 2) 아래의 findAllByOrderByBetDateAsIdAsc()처럼, 스프링 데이터가 정해둔
 * 규칙대로 메서드 이름을 지으면, 그 이름을 스프링이 해석해서 SQL을 자동으로 만들어준다.
 * 라라벨의 Eloquent가 {@code Bet::OrderBy('bet_date')->get()}
 * 처럼 조건을 조합해서 SQL을 만들어주는 것과 결과가 같다. 스프링은 그걸 
 * "메서드 이름 문법"으로 표현한다는 점이 다르다. 
 */

public interface BetRepository extends JpaRepository<Bet, Long> {
    /**
     * 전체 베팅 기록을, betDate(베팅 날짜) 기준으로 오래된 것 부터 최신 순서로,
     * 날짜가 같으면 id가 작은 것부터 순서로 가져온다. 
     * 
     * 메서드 이름을 뜯어보면: findAllBy(전체를 찾는다) + OrderBy(정렬한다) + 
     * BetDateAsc(betDate 오름차순으로) + IdAsc(그 다음 id도 오름차순)
     * 이 순서가 중요한 이유는, BetCalculationService가 잔고,수익,연승연패를
     * "옛날 베팅부터 순서대로 하나씩 누적 계산"하기 때문이다. 순서가 뒤바뀌면 
     * 계산 결과가 완전히 달라짐.
     */
    List<Bet> findAllByOrderByBetDateAscIdAsc();
}
