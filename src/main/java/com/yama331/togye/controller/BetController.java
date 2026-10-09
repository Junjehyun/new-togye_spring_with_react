package com.yama331.togye.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yama331.togye.dto.BetCreateRequest;
import com.yama331.togye.dto.BetResponse;
import com.yama331.togye.dto.BetStatsResponse;
import com.yama331.togye.dto.BetUpdateRequest;
import com.yama331.togye.entity.BetResult;
import com.yama331.togye.service.BetCalculationService;

import jakarta.validation.Valid;

/**
 * 베팅 관련 API의 웨이터다. 브라우저(손님)에게 온 HTTP 요청을 받아서,
 * 검증하고, 주방장(BetCalculationService)에게 그대로 전달하고, 주방장이 만들어준
 * 결과를 다시 손님에게 돌려주는 역할만 한다. 이 클래스 안에는 실제 계산 로직이 전혀 없다.
 * "웨이터는 요리하지 않는다."
 * 
 * @RestController -> JSON 데이터를 주고받는 API 창구다 
 * @RequestMapping("/api/bets") -> 이 클래스 안의 모든 메서드는 기본적으로 ("/api/bets")
 * 로 시작하는 주소를 처리 한다는 뜻.
 * 라라벨의 Route::prefix('api/bets')->group(...)과 같은역할이다.
 */
@RestController
@RequestMapping("/api/bets")
public class BetController {
    
    // 주방장을 "필요한 재료"로 선언. 실제 요리(계산)는 이 객체에게 전부 넘긴다.
    private final BetCalculationService betCalculationService;

    // 스프링이 이 컨트롤러를 만들 때, 자동으로 진짜 BetCalculationService를 찾아서 넣어줌.
    public BetController(BetCalculationService betCalculationService) {
        this.betCalculationService = betCalculationService;
    }

    /**
     * GET /api/bets - 베팅 목록 조회 
     * 예: GET /api/bets?from=2026-08-01&to=2026-08-31&result=WIN
     */
    @GetMapping
    // ↑ (클래스의 "/api/bets"와 합쳐져서) "GET 방식으로 /api/bets 요청이 오면 이 메서드가 처리한다."
    public List<BetResponse> findAll(
        // @RequestParam : 주소 끝에 붙는 "?key=value" 형태의 쿼리스트링을 읽음.
        // 라라벨의 $request->query('from')과 같다. required = false 라서
        // 안 보내도 에러가 안나고 그냥 null이 들어옴. 
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // "yyyy-mm-dd 형식의 글자를 날짜로 바꿔서 읽음."
        LocalDate from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,
        @RequestParam(required = false) BetResult result
    ) {
        // 컨트롤러는 그대로 서비스에게 넘기면 된다. 이게 이 메서드의 전부 
        return betCalculationService.findAll(from, to, result);
    }

    /**
     * GET /api/bets/stats - 대시보드용 통계 조회.
     * (아래 findOne()의 "/{id}" 경로보다 "stats"처럼 글자가 고정된 경로가 항상 먼저
     * 매칭되므로, 코드 순서와 무관하게 항상 올바르게 동작한다. 다만 사람이 읽을 때
     * 헷갈리지 않게 구체적인 경로를 위쪽에 적어두는 습관을 들이면 좋다.)
     */
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    @GetMapping("/stats")
    public BetStatsResponse stats() {
        return betCalculationService.stats();
    }

    /**
     * GET /api/bets/{id} - 베팅 하나를 id로 조회.
     */
    @GetMapping("/{id}")
    public BetResponse findOne(
        // @PathVariable : 주소스트링이 아니라 "주소 경로 자체에 박혀있는 값"을 읽는다.
        // 예를 들어 "/api/bets/7" 요청이 오면 {id}의 7을 이 변수에 넣어준다.
        @PathVariable Long id
    ) {
        return betCalculationService.findOne(id);
    }

    /**
     * POST /api/bets - 새 베팅 등록.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // ↑ 성공하면 200이 아니라 201(Created, "새로 만들어졌다")을 응답하라는 뜻.
    // REST 관례상 "새로 생성됨"은 201로 표시한다. (GUIDE.md 6장 참고)
    public BetResponse create(
        /**
         * @RequestBody: URL이 아니라 "HTTP 요청의 몸통(JSON)"을 자바 객체로
         * 바꿔서 받는다. 라라벨의 $request->all() 과 비슷한 역할.
         * @Valid : 이 데이터를 실제로 쓰기 전에, DTO(BetCreateRequest)에 붙어있던
         * @NotBlank, @Min(1) 같은 검증 규칙을 먼저 검사하는 뜻. 검증에 실패하면 
         * 이 메서드의 몸통은 실행조차 안 되고 스프링이 자동으로 400 에러를 응답한다.
         */
        @Valid @RequestBody BetCreateRequest request
    ){
        return betCalculationService.create(request);
    }

    /**
     * PUT /api/bets/{id} - 베팅 기록을 수정 (결과 확정 포함)
     */
    @PutMapping("/{id}")
    public BetResponse update(
        @PathVariable Long id,
        @Valid @RequestBody BetUpdateRequest request
    ){
        return betCalculationService.update(id, request);
    }

    /**
     * DELETE /api/bets/{id} - 베팅 기록 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // ↑ 204(No Content, "성공했지만 돌려줄 내용은 없다")를 응답하라는 뜻.
    public void delete(@PathVariable Long id) {
        // 반환 타입이 void인 이유: 삭제는 돌려줄 데이터가 없기 때문 
        // 즉 return; 이 없다.
        betCalculationService.delete(id);
    }


}
