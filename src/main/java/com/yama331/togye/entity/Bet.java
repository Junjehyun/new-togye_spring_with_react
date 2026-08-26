package com.yama331.togye.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 베팅 기록 하나를 표현하는 클래스다. 이걸 엔티티(Entity)라고 부른다.
 * DB에 있는 bets 테이블의 한 줄(행, row)이 곧 이 클래스의 객체 하나와 정확히 대응된다.
 * 라라벨의 Eloquent Model({@code class bet extends model})과 역할이 일치.
 * 
 * 이 클래스에는 "저장해야 하는 값"만 있다. 적중금액/수익/잔고/합계/연승연패 등등 
 * "계산해서 나오는 값은" 필드로 두지 않고, 그런 값들은 BetCalculationService(주방장)
 * 가 조회할 때마다 매번 새로 계산한다. 왜 그렇게 하는지는 Design.md 02번 섹션과 
 * GUIDE.md 5.5장에 자세히 서술
 * =======================================================================
 * @Entity -> 이 클래스는 DB 테이블 하나랑 연결된다라는 표시, 라라벨의 Model과 같은 역할
 * @Table(name = "bets") // 연결될 테이블 이름이 정확히 "best"라는 뜻이다.
 */

@Entity
@Table(name = "bets")
public class Bet {
    /**
     * @Id 
     * -> 이 필드가 이 테이블의 기본키(PK)로 쓴다는 표시. 
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * -> DB측에서 새 행이 생길때마다 자동으로 늘려가며 만들어준다는것 = AUTO-INCREMENT
     */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 아래부터 나오는 private 필드들은 이 베팅이 실제로 저장되는 데이터 들이다.
     * 자바에서는 이렇게 필드를 private(바깥에서 함부로 못 건드리게 숨김)로 두고,
     * 바깥에서는 반드시 아래쪽의 get○○○() 메서드를 통해서만 값을 읽게 만드는게 관례다.
     * 라라벨에서는 $bet->betDate처럼 속성에 바로 접근하는게 자연스럽지만, 
     * 자바는 이렇게 한 단계 감싸서 "허락된 방법으로만" 접근하게 하는걸 선호한다.
     * 
     * @Column(nullable = false) -> NULL 허용X
     */
    @Column(nullable = false)
    private LocalDate betDate; // 베팅날짜

    /**
     * @Column(nullable = false, length = 100) -> 최대 100글자까지 저장 가능.
     */
    @Column(nullable = false, length = 100) 
    private String description; // 베팅 내역

    /**
     * @Column(nullable = false, precision = 10, scale = 4)
     * -> ↑ precision = 10, scale = 4 는 "전체 10자리 숫자 중, 소수점 아래는 4자리까지
     * 정확하게 저장한다"는 뜻. (예: 4.3617)
     * 
     * BigDecimal 돈/배당 계산에서 미세한 소수점 오차도 허용X
     * double, float은 소수점 계산에는 부정확의 위험이..
     */
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal odds; // 배당

    @Column(nullable = false)
    private Long stake; // 베팅금액(원단위)

    @Column(nullable = false)
    private Integer foldCount; // 폴더 수

    /**
     * @Enumerated(EnumType.STRING)
     * ↑ BetResult는 enum(정해진 몇 개 값 중 하나)인데, 이걸 DB에 저장할때,
     * 숫자가 아니라 미리 설정해둔 ("WIN", "LOST", "PUSH")등등 글자 그대로 저장하라는 뜻.
     * 숫자로 저장하면 DB로 열여봤을때 "2가 뭐였더라??" 헷갈리게 되니까,
     * 글자로 직접 저장해서 사람이 봐도 알 수 있게 하는것. 
     * enum 파일은 따로 내가 설정해서 만들어야함. (BetResult.java)
     * 설정은 PENDING(대기중)/WIN(적중)/LOSE(낙첨)/PUSH(적특) 중 하나.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BetResult result; 

    /**
     * JPA(=자바에서 DB를 다루게 하는 기술, Hibernate가 이걸 실제로 구현함)가
     * DB에서 데이터를 읽어와서 이 클래스의 객체로 만들 때, 내부적으로
     * 이 "아무것도 안 하는 빈 생성자"를 사용한다. 직접 호출할 일은 없지만, JPA가 
     * 쓸 수 있어야 하므로 반드시 있어야한다. (없으면 DB에서 값을 못 읽어온다.)
     * protected로 만들어서 "완전히 숨기지는 않지만, 아무나 함부로 쓰지 못하게"
     * 살짝 잠가 놓은것.
     */
    protected Bet() {
        // JPA 전용, 몸통은 비워둔다.
    }

    /**
     * 새 베팅 기록을 만들 때 쓰는, 우리가 실제로 사용하는 생성자다.
     * "새 베팅 하나를 만들려면 이 6개 값을 전부 줘야 한다"는 뜻이다.
     * PHP로 비유하면 {@code $this->betDate = $betDate;}를 6번 쓴것과 동일하다.
     */
    public Bet(
                LocalDate betDate, 
                String description, 
                BigDecimal odds, 
                Long stake, 
                Integer foldCount, 
                BetResult result
            ) {
        this.betDate = betDate;
        this.description = description;
        this.odds = odds;
        this.stake = stake;
        this.foldCount = foldCount;
        this.result = result;
    }

    /**
     * 이미 있는 베팅 기록의 내용을 통째로 새 값으로 바꿔치기하는 메서드 (수정)
     * (베팅결과가 나와서 PENDING -> WIN으로 바꿀 때도 이 메서드를 쓴다.)
     * 필드를 하나씩 따로 고치는 setter를 안쓰고 이렇게 "한꺼번에 다 바꾸는"
     * 메서드만 둔 이유는, REST API의 PUT 요청이 "일부만 수정"이 아니라, 
     * "전체를 통째로 교체"하는 방식이기 때문이다. (GUIDE.md 6장의 "PUT"설명 참조)
     */
    public void update(
        LocalDate betDate,
        String description,
        BigDecimal odds,
        Long stake,
        Integer foldCount,
        BetResult result
    ) {
     this.betDate = betDate;
     this.description = description;
     this.odds = odds;
     this.stake = stake;
     this.foldCount = foldCount;
     this.result = result;   
    }

    /**
     * 아래는 전부 getter
     * 이름이 get으로 시작하고, 하는 일은 전부 "그 필드 값을 그대로 돌려주기"뿐.
     * 라라벨이면 그냥 $bet->betDate라고 쓰는데, 자바는 관례상 그 메서드를 하나 만들어서,
     * bet.getBetDate() 라고 부른다. 필드가 private이라 바깥에서는 이 메서드를 거치지 
     * 않으면, 값을 아예 읽을 수가 없다. 
     */
    public Long getId() {
        return id;
    }
    public LocalDate getBetDate() {
        return betDate;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public Long getStake() {
        return stake;
    }

    public Integer getFoldCount() {
        return foldCount;
    }

    public BetResult getResult() {
        return result;
    }
}
