package com.example.groupbuyingweb.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 프로토콜 설정 클래스
 *
 * WebSocket: 서버-클라이언트 간 지속적인 양방향 통신 연결
 * STOMP: WebSocket 위에서 동작하는 메시지 프로토콜 (목적지 기반 발행/구독)
 *
 * 전체 흐름:
 * 1. 클라이언트가 /ws-chat 에 연결 요청
 * 2. SessionHandshakeInterceptor -> HTTP 세션에서 member_id 추출
 * 3. MemberPrincipalHandshakeHandler -> member_id로 Principal 생성
 * 4. 연결 완료 후, 클라이언트는 /topic/chat/room/{id} 구독
 * 5. 메시지 전송: 클라이언트 -> /app/chat/room/{id} → ChatStompController
 * 6. 처리 결과를 /topic/chat/room/{id} 구독자 전체에게 브로드캐스트
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final MemberPrincipalHandshakeHandler handshakeHandler;
    private final SessionHandshakeInterceptor handshakeInterceptor;

    /**
     * 메시지 브로커 설정
     * 브로커: 메시지를 중계하는 역할. 발신자 -> 브로커 -> 구독자들에게 전달.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 때 사용하는 prefix
        // ex) /topic/chat/room/1 구독 -> 해당 채팅방 메시지 수신
        // /topic: 1:N 브로드캐스트 (채팅방 전체)
        // /queue: 1:1 개인 메시지 (향후 알림 등에 활용)
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 prefix
        // ex) /app/chat/room/1 -> ChatStompController의 @MessageMapping("/chat/room/{roomId}")로 라우팅
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * WebSocket 연결 엔드포인트 등록
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")  // 클라이언트 접속 URL
                .setHandshakeHandler(handshakeHandler)  // member_id -> Principal 변환
                .addInterceptors(handshakeInterceptor)  // HTTP 세션에서 member_id 추출
                .setAllowedOriginPatterns("*")   // 모든 도메인 허용
                .withSockJS();  // 웹소켓이 동작하지 않을 때 사용
    }
}
