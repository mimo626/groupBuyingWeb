package com.example.groupbuyingweb.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 직전에 실행되는 인터셉터
 *
 * WebSocket은 처음 연결할 때 HTTP 업그레이드 요청 사용
 * 이 순간이 HTTP 세션에 접근할 수 있는 유일한 시점임
 * 이후 WebSocket 연결이 맺어지면 HTTP 세션에는 접근할 수 없으므로
 * 여기서 필요한 인증 정보를 WebSocket 속성(attributes)에 옮겨 저장
 */
@Component
public class SessionHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * 핸드셰이크 직전에 호출
     *
     * @param attributes: WebSocket 연결 전체 생애 동안 유지되는 저장소
     *                   여기에 저장한 값은 이후 HandshakeHandler에서 꺼내 쓸 수 있음
     * @return true  -> 핸드셰이크 계속 진행 (연결 허용)
     *         false -> 핸드셰이크 중단 (연결 거부)
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // ServerHttpRequest를 ServletServerHttpRequest로 캐스팅해야 HttpSession에 접근 가능
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // getSession(false): 세션이 없으면 새로 만들지 말고 null 반환
            HttpSession session = servletRequest.getServletRequest().getSession(false);
            if (session != null) {
                String memberId = (String) session.getAttribute("member_id");
                if (memberId != null) {
                    // WebSocket 속성에 저장 -> MemberPrincipalHandshakeHandler에서 꺼내 Principal로 변환
                    attributes.put("member_id", memberId);
                    return true;
                }
            }
        }
        // 로그인하지 않은 사용자는 WebSocket 연결 거부
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    // 핸드셰이크 완료 후 호출, 현재는 처리할 내용 없음
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
