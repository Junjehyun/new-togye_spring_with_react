package com.yama331.togye.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.yama331.togye.dto.BettingRecordRequest;
import com.yama331.togye.dto.BettingRecordResponse;
import com.yama331.togye.entity.BetResult;
import com.yama331.togye.entity.BettingRecord;
import com.yama331.togye.repository.BettingRecordRepository;

import org.springframework.transaction.annotation.Transactional;

/**
 * 배팅 장부의 "요리사".
 *
 * 손님이 준 주문서(Request)를 받아서
 *  1) 냉장고(DB, Repository)에 넣고
 *  2) 적중금액·수익·잔고를 계산하고
 *  3) 접시(Response)에 담아 돌려준다.
 *
 * 이 클래스 밖(Controller)은 list / get / create / update / delete 다섯 개만 부른다.
 * 나머지 private 메서드는 요리사 혼자 쓰는 도구다. 밖에서 부르면 안 된다.
 *
 * @Service = Spring이 이 요리사를 한 명만 만들어서, 필요할 때 자동으로 넣어 준다.
 *            우리가 new BettingRecordService() 하지 않는다.
 */
@Service
public class BettingRecordService {

    /**
     * 냉장고. DB에 넣고 꺼내는 창구.
     *
     * final = 한 번 받으면 다른 냉장고로 갈아끼우지 않는다.
     * 값은 생성자에서만 넣는다.
     */
    private final BettingRecordRepository bettingRecordRepository;

    /**
     * Spring이 앱을 켤 때 이 생성자를 호출한다.
     * 인자로 Repository를 넣어 준다. @Autowired를 필드에 안 적어도 된다.
     *
     * 매개변수 이름(repository)과 필드 이름(bettingRecordRepository)이 달라도 된다.
     * this.필드 = 매개변수  로 연결하면 된다.
     */
    public BettingRecordService(BettingRecordRepository repository) {
        this.bettingRecordRepository = repository;
    }

    /**
     * [밖에서 부름] 장부 전체를 보여 준다. 읽기만 한다. DB를 안 바꾼다.
     *
     * 1. 냉장고에서 번호(id) 작은 것부터 전부 꺼낸다. (엑셀처럼 위에서 아래로)
     * 2. stream() = 줄을 하나씩 흐르게 한다.
     * 3. map(from) = 각 도시락(Entity)을 접시(Response)로 바꾼다.
     * 4. toList() = 접시들을 목록으로 모은다.
     *
     * readOnly = true → "이번엔 읽기만 할 거야"라고 DB에 알려 준다.
     */
    @Transactional(readOnly = true)
    public List<BettingRecordResponse> list() {
        return bettingRecordRepository.findAllByOrderByIdAsc()
            .stream()
            .map(BettingRecordResponse::from)
            .toList();
    }

    /**
     * [밖에서 부름] 번호 하나짜리만 보여 준다. 읽기만 한다.
     *
     * id = 손님이 "3번 보여 줘"라고 말한 그 숫자.
     * 없으면 findOrFail이 404(없다)를 던진다.
     * 있으면 Entity를 접시(Response)로 바꿔 돌려준다.
     */
    @Transactional(readOnly = true)
    public BettingRecordResponse get(Long id) {
        return BettingRecordResponse.from(findOrFail(id));
    }

    /**
     * [밖에서 부름] 새 줄을 장부에 추가한다. DB를 바꾼다.
     *
     * 순서 (이 순서를 바꾸면 안 된다):
     *  1. 빈 도시락(Entity)을 만든다.
     *  2. applyInput     → 주문서 6칸을 베낀다. (날짜, 내역, 배당, 배팅금, 폴더, 결과)
     *  3. applyCalculated → 적중금액·수익을 계산한다. 잔고는 일단 0.
     *  4. save            → 냉장고에 넣는다. 이때 번호(id)가 생긴다.
     *  5. recalculateBalances → 장부 모든 줄의 잔고를 위에서 아래로 다시 더한다.
     *  6. 다시 꺼내서 접시에 담아 돌려준다. (5번 때문에 잔고가 바뀌었으니 옛 값을 주면 안 됨)
     *
     * @Transactional = 중간에 실패하면 1~5를 전부 취소한다.
     * 빼먹으면 "줄은 넣었는데 잔고는 안 고침" 같은 중간 상태가 남을 수 있다.
     */
    @Transactional
    public BettingRecordResponse create(BettingRecordRequest request) {
        BettingRecord entity = new BettingRecord();
        applyInput(entity, request);
        applyCalculated(entity, request);
        bettingRecordRepository.save(entity);
        recalculateBalances();

        return BettingRecordResponse.from(findOrFail(entity.getId()));
    }

    /**
     * [밖에서 부름] 이미 있는 줄을 고친다. DB를 바꾼다.
     *
     * create와 거의 같다. 다른 점은 하나:
     *  빈 도시락을 만들지 않고, 번호(id)로 이미 있는 도시락을 꺼낸다.
     *
     * 중간 줄을 고치면 그 아래 줄의 잔고도 달라져야 한다.
     * 그래서 저장한 뒤 반드시 recalculateBalances() 를 부른다.
     */
    @Transactional
    public BettingRecordResponse update(Long id, BettingRecordRequest request) {
        BettingRecord entity = findOrFail(id);
        applyInput(entity, request);
        applyCalculated(entity, request);
        bettingRecordRepository.save(entity);
        recalculateBalances();
        return BettingRecordResponse.from(findOrFail(id));
    }

    /**
     * [밖에서 부름] 한 줄을 버린다. DB를 바꾼다.
     *
     * 1. findOrFail → 없는 번호면 404. (이 확인 없이 deleteById만 하면,
     *    없는 번호를 지워도 "성공한 척"할 수 있다.)
     * 2. deleteById → 그 줄을 냉장고에서 뺀다.
     * 3. recalculateBalances → 지운 줄의 수익이 빠지도록, 남은 줄 잔고를 다시 더한다.
     *
     * 반환값이 없다(void). "지웠다"만 하면 된다. 접시를 줄 필요가 없다.
     */
    @Transactional
    public void delete(Long id) {
        findOrFail(id);
        bettingRecordRepository.deleteById(id);
        recalculateBalances();
    }

    /**
     * [안에서만 씀] 번호로 한 줄을 찾는다. 없으면 "없다"고 소리친다.
     *
     * findById는 두 가지 상자 중 하나를 준다.
     *  - 안에 도시락이 있음
     *  - 상자가 비어 있음
     *
     * orElseThrow = 비어 있으면 예외를 던진다.
     * HttpStatus.NOT_FOUND = 손님에게 404 페이지/JSON을 보여 주라는 뜻.
     *
     * get / update / delete 가 모두 이걸 먼저 부른다.
     * "찾기"를 세 곳에 복사하지 않으려고 한곳으로 모았다.
     */
    private BettingRecord findOrFail(Long id) {
        return bettingRecordRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "BettingRecord not found: " + id));
    }

    /**
     * [안에서만 씀] 주문서(Request) → 도시락(Entity) 베끼기.
     *
     * 손님이 적어도 되는 6칸만 옮긴다.
     *   날짜, 내역, 배당, 배팅금, 폴더 수, 결과
     *
     * 여기에는 산수가 없다.
     * 적중금액 / 수익 / 잔고는 손님이 적는 칸이 아니다. applyCalculated가 채운다.
     */
    private void applyInput(BettingRecord entity, BettingRecordRequest request) {
        entity.setBetDate(request.getBetDate());
        entity.setDescription(request.getDescription());
        entity.setOdds(request.getOdds());
        entity.setBetAmount(request.getBetAmount());
        entity.setFolderCount(request.getFolderCount());
        entity.setBetResult(request.getBetResult());
    }

    /**
     * [안에서만 씀] 한 줄의 산수. 적중금액과 수익을 도시락에 쓴다.
     *
     * winAmount = calculateWinAmount (아래 공식)
     * profit    = calculateProfit    (아래 공식)
     *
     * 잔고(balance)는 이 메서드가 "최종 값"을 정하지 않는다.
     * 잔고는 모든 줄을 더해야 해서 recalculateBalances가 담당한다.
     *
     * 다만 profit과 balance는 DB에서 빈칸을 싫어한다(NOT NULL).
     * 첫 저장 전에 잔고가 아직 없으면 0을 넣어  savе가 실패하지 않게 한다.
     * 진짜 잔고는 바로 다음 recalculateBalances가 덮어쓴다.
     */
    private void applyCalculated(BettingRecord entity, BettingRecordRequest request) {
        BigDecimal winAmount = calculateWinAmount(request);
        entity.setWinAmount(winAmount);
        entity.setProfit(calculateProfit(request, winAmount));
        if (entity.getBalance() == null) {
            entity.setBalance(BigDecimal.ZERO);
        }
    }

    /**
     * [안에서만 씀] 장부 모든 줄의 잔고를 처음부터 다시 더한다.
     *
     * 왜 한 줄만 보면 안 되나?
     *  1번 줄 수익 -10000, 2번 줄 수익 +10000 이면
     *  2번의 잔고는 "이전 잔고 + 자기 수익" = 0 이다.
     *  1번을 지우면 2번의 잔고가 10000으로 바뀌어야 한다.
     *
     * 방법:
     *  1. running(지금까지 더한 합)을 0으로 시작한다. 시작 잔고는 0.
     *  2. 번호 작은 줄부터 한 줄씩 본다. (날짜순이 아니다. 날짜가 빈 줄이 있다.)
     *  3. running = running + 그 줄의 수익
     *  4. 그 줄의 잔고 칸에 running을 쓴다.
     *  5. 다 더했으면 냉장고에 다시 저장한다.
     *
     * create / update / delete 가 끝날 때마다 이걸 부른다.
     */
    private void recalculateBalances() {
        BigDecimal running = BigDecimal.ZERO.setScale(2);
        List<BettingRecord> rows = bettingRecordRepository.findAllByOrderByIdAsc();
        for (BettingRecord row : rows) {
            BigDecimal profit = row.getProfit() == null
                ? BigDecimal.ZERO
                : row.getProfit();
            running = running.add(profit).setScale(2, RoundingMode.HALF_UP);
            row.setBalance(running);
        }
        bettingRecordRepository.saveAll(rows);
    }

    /**
     * [안에서만 씀] 적중금액(따면 얼마가 돌아오나).
     *
     * HIT     : 배당 × 배팅금
     *           예) 배당 2, 배팅금 10000 → 20000 이 돌아온다.
     * PENDING : 아직 경기가 안 끝남. 얼마인지 모름 → null (빈칸)
     * MISS    : 짐. 한 푼도 안 돌아옴 → 0
     * PUSH    : 본전(적특). 이 프로젝트 규칙에선 적중금액을 0으로 둔다.
     *
     * setScale(2, HALF_UP) = 원 단위 소수 둘째 자리, 반올림.
     * 돈 계산은 double(1.1 + 2.2)을 쓰면 숫자가 살짝 깨진다. 그래서 BigDecimal.
     */
    private BigDecimal calculateWinAmount(BettingRecordRequest req) {
        if (req.getBetResult() == BetResult.HIT) {
            return req.getOdds()
                .multiply(req.getBetAmount())
                .setScale(2, RoundingMode.HALF_UP);
        }
        if (req.getBetResult() == BetResult.PENDING) {
            return null;
        }
        return BigDecimal.ZERO.setScale(2);
    }

    /**
     * [안에서만 씀] 수익(내 주머니가 얼마나 늘거나 줄었나).
     *
     * HIT     : 돌아온 돈 − 내가 건 돈
     *           예) 20000 − 10000 = +10000 (이득)
     * MISS    : 내가 건 돈을 통째로 잃음 → −배팅금
     *           예) −10000
     *           negate() = 부호를 뒤집는다. 10000 → -10000
     * PUSH    : 본전. 늘지도 줄지도 않음 → 0
     * PENDING : 아직 모름. 잔고도 안 깎음 → 0
     *
     * switch = 결과가 뭐냐에 따라 갈림길을 고른다. if를 여러 번 쓰는 것과 같다.
     */
    private BigDecimal calculateProfit(BettingRecordRequest req, BigDecimal winAmount) {
        return switch (req.getBetResult()) {
            case HIT -> winAmount.subtract(req.getBetAmount())
                .setScale(2, RoundingMode.HALF_UP);
            case MISS -> req.getBetAmount().negate()
                .setScale(2, RoundingMode.HALF_UP);
            case PUSH, PENDING -> BigDecimal.ZERO.setScale(2);
        };
    }
}
