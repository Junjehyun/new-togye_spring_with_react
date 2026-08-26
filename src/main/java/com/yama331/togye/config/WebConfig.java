package com.yama331.togye.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 허용 설정을 담당하는 문지기
 * 
 * 브라우저는 보안상 "다른 포트/도메인에서 온 웹페이지가, 지금 보고 있는 페이지와
 * 다른 포트/도메인의 서버에 함부로 요청을 보내지 못하게" 기본적으로 막아둔다.
 * 이걸 CORS(Cross-Origin Resource Sharing) 문제라고 부른다. 
 * 
 * 이 프로젝트는 프론트엔드(5173포트)과 백엔드(8080포트)가 완전히 분리된 서버라서,
 * 백엔드가 "5173번 포트에서 오는 요청은 괜찮다"고 명시적으로 허락해줘야 한다.
 * 이 파일이 그 허락 도징인 셈인데, 라라벨을 쓸 때는 프론트/백엔드가 보통 한 서버 안에 있어서
 * 이런 걸 신경 쓸 일이 거의 없다. 여기서는 REST API 방식이라 MVC 패턴과는 다르니, 
 * 둘로 나뉘어 있어서 반드시 필요하다.
 * 
 */
@Configuration // 이 클래스는 스프링의 설정(환경설정) 코드를 담고 있다는 표시.
public class WebConfig implements WebMvcConfigurer{
    /**
     * implements WebMvcConfigurer : "나는 스프링 웹 설정을 커스터 마이징 할 것이다."
     * 라는 약속이다. 이 인터페이스가 제공하는 addCorsMappings 같은 메서드를 오버라이드해서
     * 원하는 설정을 끼워 넣을 수 있다.
     */

    // application.properties에 적혀있는 "허용할 주소"를 담아둘 곳.
    private final String allowedOrigin;

    /**
     * @Value("${app.cors.allowed-origin}")는 application.properties 파일에
     * 적어둔 {@code app.cors.allowed-origin=http://localhost:5173} 값을 읽어와서 
     * 이 생성자의 매개변수로 그대로 넣어달라는 뜻이다. 라라벨의 .env + config() 조합과 비슷
     * 코드에 주소를 직접 하드코딩 하지 않고 설정
     * 파일로 빼두면, 나중에 프론트엔드 포트가 바뀌어도 코드를 안 고치고 설정
     * 파일 한 줄만 바꾸면 된다.
     */
    public WebConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    /**
     * @Override: "이 메서드는 부모(WebMvcConfigurer)가 이미 정의해둔 메서드를
     * 내가 원하는 내용으로 다시 재정의 한것"이라는 표시.
     * 스프링이 서버를 켤 때 이 메서드를 자동으로 호출해서, 우리가 적어둔 CORS규칙을 적용. 
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // ↑ "/api/로 시작하는 모든 주소에 대해서" 라는 뜻. **는 "그 아래
                // 몇 단계든 다 포함"이라는 와일드카드
                .allowedOrigins(allowedOrigin)
                // ↑ 허락해줄 상대방 주소. 여기서는 http://localhost:5173 하나만 허락합니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // ↑ 허락해줄 HTTP 메서드 목록. (OPTIONS는 브라우저가 실제 요청 전에 
                // "이거 보내도 되는지" 미리 물어보는 사전 확인 요청이다.)
                .allowedHeaders("*");
                // ↑ "요청에 어떤 헤더를 붙여도 다 괜찮다"는 뜻. *는 "전부"라는 와일드카드.
    }
    
}
