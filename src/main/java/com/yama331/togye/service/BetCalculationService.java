package com.yama331.togye.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.RootPaneUI;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.yama331.togye.dto.BetCreateRequest;
import com.yama331.togye.dto.BetResponse;
import com.yama331.togye.dto.BetStatsResponse;
import com.yama331.togye.dto.BetUpdateRequest;
import com.yama331.togye.entity.Bet;
import com.yama331.togye.entity.BetResult;
import com.yama331.togye.repository.BetRepository;

/**
 * BetCalculationService
 * 
 * 이 프로젝트의 "주방장" 역할. 실제 계산(요리)이 전부 이 클래스 안에서 일어난다.
 * Bet Entity(냉장고 속 재료)는 사용자가 직접 입력한 값만 저장한다.
 * 적중금액 / 수익 / 잔고 / 합계 / 연승연패는 여기서 날짜(오름차순) 전체를 매번 다시 훑어 계산한다.
 * DESIGN.md 02번 섹션의 설계 결정을 따른 것으로, 중간 기록을 고쳐도 그 아래 행들을
 * 다시 손으로 쓸 필요가 없게 한다. (3장 "요청 하나의 여정" 참고)
 */
@Service // "이 클래스는 서비스 계층이고, 스프링이 이객체를 하나 만들어서 관리해라"라는 표시.
public class BetCalculationService {
    /**
     * 창고 관리인(Repository)을 "필요한 재료"로 선언해둔다. 
     * final은 "한번 정해지면 다른걸로 바꿔치기 못한다"는 뜻이다.
     */
    private final BetRepository betRepository;

    /**
     * 생성자다. 스프링이 이 클래스의 객체를 만들 때, 자동으로 진짜 BetRepository 객체를 
     * 찾아서 여기 넣어준다. 이걸 "의존성 주입(DI)"이라고 부른다. 
     * 우리가 직접 {@code new BetRepository(...)} 라고 슬 필요가 없다.
     * 라라벨의 서비스 컨테이너가 생성자에 필요한 걸 자동으로 채워주는 것과 원리가 완전히 같다.
     */
    public BetCalculationService(BetRepository betRepository) {
        this.betRepository = betRepository;
    }

    /**
     * 조건(기간, 결과)에 맞는 베팅 기록 목록을 돌려준다. BetController의 
     * GET /api/bets 가 이 메서드를 호출한다.
     * 
     * 순서가 중요하다: 먼저 computeAll()로 "전체를" 다 계산한 다음에,
     * 그 결과 중에서 조건에 맞는 것만 걸러낸다.(필터링은 나중에!) 
     * 계산을 일부만 하고 필터링하면 잔고/스트릭 같은 누적값이 틀어지기 때문에
     * 항상 "전체 계산 -> 그 다음 걸러내기" 순서를 지킨다. 
     */
    public List<BetResponse> findAll(LocalDate from, LocalDate to, BetResult result) {
        return computeAll().stream() // .stream(): 리스트를 하나씩 가공할 수 있는 모드로 바꿔준다.
                // 라라벨 컬렉션의 필터/맵 체이닝과 비슷한 개념. 
                .filter(b -> from == null || !b.betDate().isBefore(from))
                // ↑ "from이 안 주어졌으면 무조건 통과, 주어졌으면 날짜가 from보다
                // 이전이 아닌 것만 통과" 라는 뜻. (b -> ...) 는 "람다식"이라고 부르는,
                // 이름 없는 짧은 함수다. PHP의 화살표 함수 fn($b) => ... 와 같다.
                .filter(b -> to == null || !b.betDate().isAfter(to))
                .filter(b -> result == null || b.result() == result)
                .toList(); // 가공이 끝난 스트림을 다시 평범한 리스트(List)로 되돌린다.
    }

    /**
     * id 하나로 베팅 기록 하나를 찾는다. 못 찾으면 404 에러 던짐.
     * (여기서도 "전체를 계산한 다음, 그중에서 id가 맞는 것 하나만 골라낸다"는
     * 같은 원칙을 쓴다. 계산은 항상 전체 맥락 안에서 해야 정확하기 때문이다.)
     */
    public BetResponse findOne(Long id) {
        return computeAll().stream()
            .filter(b -> b.id().equals(id))
            .findFirst()
            // ↑ 조건에 맞는 첫 번째 것을 찾되, "있을 수도 없을 수도 있는 값"
            // (Optional)으로 감싸서 돌려준다.
            .orElseThrow(() -> notFound(id));
            // ↑ "만약 못 찾았으면(값이 없으면), notFound(id)가 만든 에러를 던져라."
    }

    /**
     * 새 베팅 기록을 만든다. BetController의 POST /api/bets가 이 메서드를 호출한다. 
     */
    public BetResponse create(BetCreateRequest request) {
        // 1단계: 주문서(DTO)에 담긴 값으로, 실제 냉장고에 넣을 재료(Entity)를 만든다.
        Bet bet = new Bet(
            request.betDate(),
            request.description(),
            request.odds(),
            request.stake(),
            request.foldCount(),
            request.resultOrDefault()
            // ↑ result를 안 보냈으면 자동으로 PENDING이 되도록, DTO에 만들어둔 메서드를 사용.
        );
        // 2단계: 창고 관리인에게 "이거 냉장고에 저장해줘"라고 시킨다.
        Bet saved = betRepository.save(bet);
        // save가 끝나면 saved 안에는 DB가 자동으로 채워준 id 값도 들어있다.

        // 3단계: 저장은 했지만, saved(Bet엔티티)에는 아직 payoutAmount/profit같은 
        // "계산된 값"이 없다. 그래서 방금 저장한 id로 findOne()을 다시 호출해서,
        // 전체를 다시 계산한 뒤, "계산까지 끝난 영수증(BetResponse)"을 만들어 돌려준다.
        return findOne(saved.getId());
    }

    /**
     * 이미 있는 베팅 기록을 통째로 수정한다. (결과를 WIN/LOSE 등으로 확정하는 것도 여기서 일어남.)
     * BetController의 PUT /api/bets/{id}가 이 메서드를 호출한다.
     */
    public BetResponse update(Long id, BetUpdateRequest request) {
        // 1단계 : 창고에서 그 id의 재료를 찾아온다. 없으면 404
        Bet bet = betRepository.findById(id).orElseThrow(() -> notFound(id));
        // 2단계 : 찾아온 재료의 낸용을 새 값으로 통째로 바꿔치기한다.
        bet.update(
            request.betDate(),
            request.description(),
            request.odds(),
            request.stake(),
            request.foldCount(),
            request.result()
        );
        // 3단계 : 바뀐 내용을 다시 창고(DB)에 저장한다.
        betRepository.save(bet);
        // 4단계 : create()에서와 같은 이유로, 계산까지 끝난 최신 상태를 다시 조회해서 돌려준다.
        return findOne(id);
    }

    /**
     * 베팅 기록을 삭제한다.
     * BetController의 DELETE /api/bets/{id}가 이 메서드를 호출한다. 
     */
    public void delete(Long id) {
        if (!betRepository.existsById(id)) {
            // 없는 걸 지우려고 한다면 "없다"라고 알려주는게 맞으니 여기서도 404
            throw notFound(id);
        }
        betRepository.deleteById(id);
    }

    /**
     * 대시보드에 보여줄 전체 통계를 계산한다. BetController의 
     * GET /api/bets/stats가 이 메서드를 호출한다. 
     */
    public BetStatsResponse stats() {
        List<BetResponse> all = computeAll();

        // .count() : 조건에 맞는 것이 몇개인지 센다.
        long winCount = all.stream().filter(b -> b.result() == BetResult.WIN).count();
        long loseCount = all.stream().filter(b -> b.result() == BetResult.LOSE).count();
        long pushCount = all.stream().filter(b -> b.result() == BetResult.PUSH).count();
        long pendingCount = all.stream().filter(b -> b.result() == BetResult.PENDING).count();

        // .mapToLong(BetResponse::stake) : 각 베팅에서 stake(베팅금액) 숫자만 뽑아낸다.
        // BetResponse::stake는 "b -> b.stake()" 를 짧게 쓰는 방법이다. ("메서드 레퍼런스")
        // 그렇게 뽑은 숫자들을 .sum()으로 전부 더한다.
        long totalStake = all.stream().mapToLong(BetResponse::stake).sum();

        long cumulativeProfit = all.stream()
            .filter(b -> b.profit() != null)
            // PENDING은 베팅인 profit이 null(아직모름)이라서, 통계 계산에서는 
            // 뺴고 "결과가 확정된 것만 더한다."
            .mapToLong(BetResponse::profit)
            .sum();

        /**
         * 환수율 = (총 베팅액 + 누적수익) ÷ 총배팅액. 0으로 나누면 에러가 나므로,
         * 총베팅액이 0일 때는(베팅이 하나도 없을 때) null(모름)을 돌려준다.
         * "조건 ? A : B"는 삼항연산자이다. PHP와 완전히 같다.
         */
        BigDecimal returnRate = totalStake == 0
                ? null 
                : BigDecimal.valueOf(totalStake + cumulativeProfit)
                    .divide(BigDecimal.valueOf(totalStake), 4, RoundingMode.HALF_UP);
                    // ↑ .divide(나눌 값, 소수점 4자리까지, 반올림 방식)
                    // BigDecimal은 돈 계산처럼 정확한 소수 계산이 필요할 때 쓰는 타입

        // 적중률 = 적중 건수 ÷ (적중 건수 + 낙첨 건수)
        BigDecimal hitRate = (winCount + loseCount) == 0
                ? null 
                : BigDecimal.valueOf(winCount)
                        .divide(BigDecimal.valueOf(winCount + loseCount, 4), RoundingMode.HALF_UP);
        
        // 평균 배당률 = 모든 베팅의 배당률을 다 더한 뒤, 건수로 나눈다.
        BigDecimal avgOdds = all.isEmpty()
                ? null
                : all.stream().map(BetResponse::odds).reduce(BigDecimal.ZERO, BigDecimal::add)
                // ↑ .reduce(시작값, 합치는 방법) : 0부터 시작해서 하나씩 더해나간다.
                        .divide(BigDecimal.valueOf(all.size()), 4, RoundingMode.HALF_UP);

        // 연승만 있으면 최다연패는 0이어야 한다 (그 반대도 마찬가지) - 부호가 다른 스트릭만 걸러서 비교
        // (예전에는 이 필터가 없어서, 이긴 적만 있어도 "최다 연패 1패"로 나오는 버그가 있었다.
        // 실제로 테스트 하다가 발견해서 고친부분)
        int maxWinStreak = all.stream().mapToInt(BetResponse::streak).filter(s -> s >= 0).max().orElse(0);
        int maxLoseStreak = all.stream().mapToInt(BetResponse::streak).filter(s -> s <= 0).min().orElse(0);
        // ↑ .orElse(0) : "만약 조건에 맞는 게 하나도 없으면(예: 이긴 적이 한 번도
        // 없어서 s >= 0인 게 없으면) 기본값 0을 쓴다"는 뜻.

        return new BetStatsResponse(
            all.size(), winCount, loseCount, pushCount, pendingCount,
            totalStake, cumulativeProfit, returnRate, hitRate, avgOdds,
            maxWinStreak, maxLoseStreak
        );
    }

    /**
     * 이 프로젝트에서 가장 중요한 메서드다. 
     * 
     * 날짜 오름차순으로 전체 베팅을 한 번 훑으면서, 잔고(runningBalance),
     * 합계(cumulativeTotal), 연승연패(streak)를 옛날 베팅부터 하나씩 이어서 계산한다.
     * 엑셀에서 한 행을 고치면 그 아래 행들이 자동으로 다시 계산되던 것과 똑같은 효과를,
     * "매번 처음부터 다시 계산하기"로 흉내낸 것. 
     * 
     * PENDING(결과 대기)인 기록은 베팅금이 이미 잠긴 것으로 보고 잔고에서만 빼고,
     * 합계, 스트릭은 결과가 확정되기 전까지 직전 값을 그대로 들고 간다. 
     * 엑셀 P/Q열의 "결과 미확정시 잠정 차감" 동작을 그대로 따라한 것.
     * 
     * private로 되어 있어서 이 클래스 밖에서는 부를 수 없다. 
     * "주방 안에서만 쓰는 비밀 조리법"이다.
     */
    private List<BetResponse> computeAll() {
        // 창고에서 날짜 오름차순으로 전체 베팅을 가져온다.
        List<Bet> bets = betRepository.findAllByOrderByBetDateAscIdAsc();
        // 계산기 끝난 영수증들을 담을 빈 리스트를 준비한다.
        List<BetResponse> responses = new ArrayList<>(bets.size());

        // 누적 계산에 쓸 변수 3개를 0에서 시작한다.
        long balance = 0L; // 잔고 (이 순간 지금까지의 상태)
        int streak = 0; // 연승연패 스트릭 (양수=연승 중, 음수= 연패 중)
        long cumulativeTotal = 0L; // 합계 (확정된 수익만 누적)

        // for (Bet bet : bets) : 리스트 안의 항목을 하나씩 순서대로 꺼내며 돈다.
        // PHP의 foreach ($bets as $bet)와 완전히 같은 문법이다.
        for (Bet bet : bets) {
            long stake = bet.getStake();

            // 적중금액 = 배당률 * 베팅금액. 실제로 이겼는지와 무관하게 늘 계산해둠.
            // ("만약 이겼다면 얼마를 받았을지"를 미리 보여주는 값이라고 생각하면 된다.)
            long payoutAmount = bet.getOdds()
                    .multiply(BigDecimal.valueOf(stake))
                    .setScale(0, RoundingMode.HALF_UP)
                    // ↑ 소수점을 없애고(정수로), 반올림한다. 배당금은 원 단위로 딱 끊어서 쓴다.
                    .longValueExact();
                    // ↑ BigDecimal을 진짜 long 숫자로 바꾼다. (소수점이 이미 없으니 정확히 변환됨)

            // profit(수익)은 아직 모른다(null). 아래 switch에서 결과에 따라 채워진다.
            Long profit = null;

            // switch(bet.getResult()) : 결과값(WIN/LOSE/PUSH/PENDING)에 따라 
            // 완전이 다른 코드를 실행한다. 라라벨의 match($result) { ... } 와 개념이 같다.
            // 새 문법으로, 예전의 "case WIN: ... break;" 보다 짧게 쓴다.
            switch (bet.getResult()) {
                case WIN -> {
                    // 이긴 경우: 수익 = 적중금액 - 베팅금액 (순수익)
                    profit = payoutAmount - stake;
                    balance += profit;
                    cumulativeTotal += profit;
                    // 연승 스트릭 갱신: 지금까지 연승 중이었으면(streak이 양수) 1을 더 쌓고,
                    // 아니었으면 (연패 중이거나 0이었으면) 1로 새로 시작한다.
                    streak = streak > 0 ? streak + 1 : 1;
                }
                case LOSE -> {
                    // 진 경우: 수익 = -베팅금액 (건 돈을 그대로 잃음)
                    profit = -stake;
                    balance += profit;
                    cumulativeTotal += profit;
                    // 연패 스트릭 갱신: 위와 반대 방향으로 똑같은 로직
                    streak = streak < 0 ? streak - 1 : 1;
                }
                case PUSH -> {
                    // 적특 처리 수익 0
                    profit = 0L;
                    balance += profit; // 0을 더하는 것이므로 실제로는 그대로 유지됨
                    cumulativeTotal += profit; // 마찬가지로 그대로 유지됨 
                    // 본전이 나오면 지금까지의 연승/연패 흐름이 끊긴다고 보고 0으로 리셋
                    streak = 0;
                }
                case PENDING -> {
                    // 아직 결과를 모름. null
                    profit = null;
                    // 그런데 돈은 이미 걸어놓은 상태이므로, 잔고에서는 미리 빼준다. 
                    // (마치 계좌에서 돈이 잠깐 묶여있는 것처럼)
                    balance -= stake;
                    // 합계(cumulativeTotal)와 스트릭은 결과가 나오기 전까지 그대로 둠
                    // (코드를 아무것도 안 쓰면 자동으로 "그대로 유지"가 된다. 
                    // 바로 위 for문에서 다음 베팅으로 넘어갈 때도 cucumlativeTotal과 
                    // streak 변수는 값을 그대로 들고 간다).
                }
            }

            // 이번 베팅 하나에 대한 계산이 다 끝났으니, "영수증(BetResponse)"을 하나 만들어 
            // 결과 리스트에 추가한다. 
            responses.add(new BetResponse(
                bet.getId(),
                bet.getBetDate(),
                bet.getDescription(),
                bet.getOdds(),
                stake,
                bet.getFoldCount(),
                bet.getResult(),
                payoutAmount,
                profit,
                balance,          // 지금까지 누적된 잔고
                cumulativeTotal,  // 지금까지 누적된 합계
                streak,           // 지금까지의 연승연패 스트릭
                outcomeLabel(bet.getResult())
            ));
        }
        return responses;
    }


    /**
     * 결과(enum)을 사람이 읽기 좋은 한글 단어로 바꿔줌 !!
     * "switch (result) { case WIN -> ... }" 는 이번엔 "switch 문장"이 아니라 
     * "switch 표현식"이다. 각 case의 결과를 바로 값으로 돌려줄 수 있어서
     * return과 합쳐서 짧게 쓸수 있다. 
     */
    private String outcomeLabel(BetResult result) {
        return switch (result) {
            case WIN -> "적중";
            case LOSE -> "미적중";
            case PUSH -> "적특";
            case PENDING -> "결과대기중";
        };
    }

    /**
     * "id로 찾아봤는데 없었다."는 상황에서 던질 에러를 만들어주는 도우미
     * HttpStatus.NOT_FOUND는 HTTP 상태 코드 404를 뜻함.
     * (GUIDE.md 6장 참고) 이 예외를 던지면 스프링이 자동으로 "404 응답"을
     * 만들어서 프론트엔드로 보내준다. 
     */
    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "베팅 기록을 찾을 수 없습니다: " + id);
    }
}
