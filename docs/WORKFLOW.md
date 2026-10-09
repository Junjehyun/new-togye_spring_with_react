# 개발 작업 순서도

> 이 프로젝트를 실제로 어떤 순서로 만들었는지 한눈에 보는 문서입니다.
> 자세한 설명은 `DESIGN.md`(설계 이유)와 `GUIDE.md`(코드 해설)를 참고하세요.

## 전체 흐름

```
                         ┌─────────────────────────────┐
                         │           백엔드              │
                         │  (Spring Boot, 8090번 포트)   │
                         └─────────────────────────────┘
   1) Entity        Bet.java, Transaction.java
        │           "저장할 데이터의 모양"부터 정한다
        ▼
   2) Enum           BetResult.java, TransactionType.java
        │           Entity 안에서 쓸 "정해진 몇 개 값" 정의
        ▼
   3) Repository     BetRepository.java, TransactionRepository.java
        │           DB에서 Entity를 가져오고 저장하는 통로
        ▼
   4) DTO            bet/dto/*.java, transaction/dto/*.java
        │           API로 주고받을 "겉모습"(요청/응답) 정의
        ▼
   5) Service        BetCalculationService.java, TransactionService.java
        │           실제 계산 · 비즈니스 로직 (이 프로젝트의 핵심)
        ▼
   6) Controller     BetController.java, TransactionController.java
        │           HTTP 요청을 받아 Service로 연결 (REST API 완성)
        ▼
   7) Config         WebConfig.java, application.properties
        │           CORS, 포트, DB 연결 같은 환경 설정
        ▼
   ── 여기까지 완성되면 curl / Postman으로 API가 이미 다 동작한다 ──
        │
        │             REST API (JSON, http://localhost:8090/api/...)
        ▼
                         ┌─────────────────────────────┐
                         │          프론트엔드            │
                         │   (React + TS, 5180번 포트)    │
                         └─────────────────────────────┘
   8) Types          types.ts
        │           백엔드 DTO와 모양을 맞춘 타입 정의
        ▼
   9) API client     api/client.ts → api/bets.ts, api/transactions.ts
        │           fetch 공통 래퍼 → 도메인별 API 호출 함수
        ▼
  10) 화면 조각       components/StatCard.tsx, format.ts
        │           여러 화면에서 재사용할 작은 블록 · 표시 형식
        ▼
  11) 페이지          pages/DashboardPage.tsx, BetsPage.tsx, TransactionsPage.tsx
        │           실제 화면 하나씩 (목록, 폼, 통계)
        ▼
  12) 라우팅          App.tsx, main.tsx
                     페이지들을 주소(URL)에 연결해서 앱을 완성
```

## 왜 이 순서인가

- **백엔드를 먼저, 프론트엔드를 나중에** — 백엔드가 먼저 완성되면 화면 없이도
  `curl`이나 브라우저 주소창, H2 콘솔로 API가 제대로 동작하는지 검증할 수
  있습니다. 화면과 API를 동시에 만들면 문제가 생겼을 때 "화면 버그인지 API
  버그인지" 구분하기 어려워집니다.
- **백엔드 안에서도 "안쪽 → 바깥쪽" 순서** — 데이터의 모양(Entity, DTO)을 먼저
  정하고, 그 데이터를 다루는 로직(Repository, Service)을 만들고, 마지막에
  그걸 세상에 노출하는 문(Controller)을 엽니다. 반대로 Controller부터 만들면
  "무슨 데이터를 주고받을지"가 아직 안 정해져서 자꾸 앞의 단계로 되돌아가게 됩니다.
- **프론트엔드도 같은 원리** — 타입(데이터 모양)을 먼저 정하고, 그 타입으로
  API를 호출하는 함수를 만들고, 마지막에 그 함수를 실제로 쓰는 화면을 그립니다.

## 한 단계씩 확인하는 법

| 단계 끝났을 때 | 이렇게 확인 |
|---|---|
| 1~4 (Entity, Enum, Repository, DTO) | `./gradlew compileJava` — 컴파일만 되면 OK (아직 동작 확인은 못함) |
| 5~7 (Service, Controller, Config) | `./gradlew bootRun` 으로 서버를 켜고, `curl`로 GET/POST/PUT/DELETE 직접 호출 |
| 8~9 (Types, API client) | `npx tsc -b` — 타입 에러 없이 통과하면 OK |
| 10~12 (컴포넌트, 페이지, 라우팅) | `npm run dev` 로 켜서 브라우저에서 직접 클릭해보기 |

이 표의 순서대로 "작게 만들고 → 확인하고 → 다음 단계로" 넘어가면, 한 번에
너무 많은 걸 고쳐서 뭐가 문제인지 못 찾는 상황을 피할 수 있습니다.
