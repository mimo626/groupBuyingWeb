package com.example.groupbuyingweb.domain.dto.request;

public class ChatMessageRequest {
    // 텍스트 메시지 전송
    public record Send(
            String content
            // roomId -> URL에서 추출
            // senderId -> 인증 정보에서 추출
    ) {}
}
