package com.example.groupbuyingweb.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final MemberPrincipalHandshakeHandler handshakeHandler;
    private final SessionHandshakeInterceptor handshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 메시지를 구독할 prefix, ex: /topic/chat/1
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지 보낼 때 사용할 prefix, ex: /app/chat/send
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setHandshakeHandler(handshakeHandler) // member_id -> Principal 변환
                .addInterceptors(handshakeInterceptor)  // HTTP 세션에서 member_id 추출
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
