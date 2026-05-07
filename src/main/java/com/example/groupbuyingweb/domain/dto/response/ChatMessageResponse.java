package com.example.groupbuyingweb.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ChatMessageResponse {
    // 안 읽은 메시지 수 조회
    public record UnreadCount(
            int unreadCount
    ) {}
}
