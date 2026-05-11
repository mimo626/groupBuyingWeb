package com.example.groupbuyingweb.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 핸드셰이크 시 Principal(인증된 사용자 정보)을 생성하는 핸들러
 *
 * Spring Security 없이 순수 세션 기반 인증을 WebSocket에 연동할 때 사용
 * SessionHandshakeInterceptor가 먼저 실행되어 attributes에 member_id를 저장하고,
 * 이 클래스가 그 값을 꺼내 Principal 객체로 만듦
 *
 * 생성된 Principal은 WebSocket 연결 동안 유지되며,
 * STOMP 컨트롤러 메서드에서 Principal 파라미터로 자동 주입
 */
@Component
public class MemberPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    /**
     * WebSocket 세션에 연결될 Principal을 결정
     * SessionHandshakeInterceptor -> 이 메서드 순서로 실행
     *
     * @param attributes: SessionHandshakeInterceptor에서 저장한 값이 들어있음
     */
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String memberId = (String) attributes.get("member_id");
        // Principal은 getName() 하나만 가진 함수형 인터페이스이므로 람다로 구현 가능
        // principal.getName() 호출 시 memberId(UUID) 반환
        return () -> memberId;
    }
}
