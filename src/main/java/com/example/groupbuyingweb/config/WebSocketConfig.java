package com.example.groupbuyingweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 메시지를 구독할 prefix, ex: /topic/chat/1
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지 보낼 때 사용할 prefix, ex: /app/chat/send
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws-chat") // 클라이언트가 접속할 URL
                .setAllowedOriginPatterns("*") // 모든 도메인 허용
                .withSockJS();  // 웹소켓이 동작하지 않을 때 사용
    }
}
