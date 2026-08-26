/**
 * 이 파일은 프론트엔드에서 백엔드로 요청을 보내는 "모등 통로가 지나가는 공통 관문"
 * api/bets.ts, api/transactions.ts가 전부 이 파일이 만들어 주는
 * get/post/put/delete 함수를 빌려서 쓴다. 
 */

// 백엔드 서버의 기본 주소다. 뒤에서 "/bets", "/transactions" 처럼 
// 각 API 경로를 이 뒤에 이어 붙여 쓴다. 
const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 실제로 fetch(브라우저 내장 HTTP 요청 함수)를 호출하고, 결과를 다루는 가장 안쪽의 공통함수다.
 * get/post/put/delete가 전부 이 함수를 통해서 실제 요청을 보낸다.
 * 
 * `async function`:"이 함수는 시간이 걸리는 일(네트워크 요청)을 한다."는 표시다.
 * 이런 함수를 부를 때는 앞에 await을 붙여 "끝낼 때까지 여기서 기다렸다가 다음줄로 넘어가라"
 * 라고 알려줘야 한다.
 * 
 * `<T>`: 타입스크립트의 "제네릭"이다. "이 함수는 어떤 타입이든 다룰 수 있는데, 정확히 어떤
 * 타입인지는 이 함수를 부르는 쪽에서 알려달라"는 뜻이다.
 * 예를 들어 `request<BetResponse[]>('/bets')`라고 부르면 T가 
 * `BetResponse[]'가 되어서, 결과값의 타입을 정확히 알 수 있다. 
 */
async function request<T>(path: string, options?: RequestInit): Promise<T> {
    // fetch(...): 브라우저에 기본으로 내장된, HTTP의 요청을 보내는 함수.
    const response = await fetch(`${API_BASE_URL}${path}`, {
        // ↑ 이 줄의 백틱(`)으로 감싼 부분은 "문자열 템플릿"
        // `${API_BASE_URL}${path}` 는 두 문자열을 이어 붙인 것과 같다.
        headers: { 'Content-Type': 'application/json' },
        // ↑ "내가 보내는 몸통(body)은 JSON 형식이다"라고 서버에게 알려주는 표시.
        ...options,
        // ↑ "..." 는 스프레드 연산자다. options 안에 들어있는 값들
        // (method, body 등)을 이 자리에 그대로 펼쳐 넣는다. get()을 부를 때는
        // options가 없어서 여기 아무것도 추가 안되고, post()를 부를 때는, 
        // { method: 'POST', body: ... }가 여기 펼쳐져 들어간다. 
    })

    // response.ok : 상태 코드가 200번대(성공)면 true, 400/500대면 false.
    if (!response.ok) {
        // 일단 기본 에러 메시지를 준비해둔다.
        let message = `요청이 실패했습니다 (${response.status})`
        try {
            // 백엔드가 에러 상황에서 JSON으로 자세한 이유를 같이 보내주는 경우가 많으므로,
            // 그 내용을 읽어서 더 자세한 메시지로 바꿔본다.
            const data = await response.json()
            message = data.detail ?? data.message ?? message
            // ↑ "??"는 "null 병합 연산자"다. "앞의 갚이 null/undefined면 다음 
            // 값을 쓴다"는 뜻. data.detail이 있으면 그걸 쓰고, 없으면 data.message,
            // 그것도 없으면 원래 기본 메세지를 쓴다는 뜻이다. 순서대로 오케이?
        } catch {
            // 응답 본문이 JSON이 아니면(파싱하다가 에러가 나면) 그냥 기본 메시지를 사용한다.
        }
        /**
         * throw new Error(...) : 자바스크립트에서 "에러를 하나 만들어서 던진다"는 뜻.
         * 이걸 호출한 쪽(예: BetsPage.tsx의 .catch(...))에서 붙잡아서 화면에 
         * 에러 메세지를 보여준다.
         */
        throw new Error(message)
    }
    // 204 No Content(성공했지만 돌려줄 내용이 없음, 예: 삭제 성공)인 경우엔
    // 몸통이 비어있으므로 JSON으로 읽으려고 하면 오히려 에러가 난다. 
    // 그래서 이 경우에는 그냥 undefined를 돌려준다. 
    if (response.status == 204) {
        return undefined as T
        /**
         * ↑ "as T" 는 "이건 사실 T 타입이라고 치자"라는 타입 단언
         * undefined가 진짜로는 어떤 타입도 아니지만, 호출하는 쪽에서 어차피
         * 신경 안 쓸 상황(삭제 성공)이라 이렇게 쳐준다.
         */
    }

    // 그 외의 성공 상황은 응답 몸통을 JSON으로 읽어서 돌려준다. 
    return (await response.json()) as T
}

/**
 * 다른 파일들(api/bets.ts, api/trasactions.ts)이 실제로 가져다 쓰는 
 * 짧은 이름의 함수 4개를 모두 모아둔 객체다. 이렇게 해두면 다른 파일에서
 * `api.post('/bets', {...})` 처럼 짧고 읽기 쉽게 쓸 수 있다.
 */
export const api = {
    get: <T>(path: string) => request<T>(path),
    post: <T>(path: string, body: unknown) =>
        // JSON.stringify(body) : 자바스크립트 객체를 JSON 문자열로 바꾼다.
        // (서버로 보낼 때는 실제 객체가 아니라 "글자로 된 JSON"을 보내야 한다.)
        // PHP의 json_encode($body)와 같은 역할이다. 
        request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
    put: <T>(path: string, body: unknown) =>
        request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (path: string) => request<void>(path, { method: 'DELETE' }),
}