package com.example.groupbuyingweb.domain.dto.response;

import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomResponse {
    // 채팅방 자동 입장
    public record Enter(
            Long chatRoomId,
            LocalDateTime createdAt
    ) {}

    // 채팅방 리스트 조회
    public record ListResponse(
            List<ListItem> chatRooms
    ) {}

    public record ListItem(
            Long chatRoomId,
            Long groupPurchaseId,
            String groupPurchaseTitle,
            String lastMessage,
            GroupBuyingStatus status,
            LocalDateTime lastMessageAt,
            int unreadCount
    ) {}

    // 채팅방 상세 조회
    public record Detail(
            Long chatRoomId,
            Long groupPurchaseId,
            String groupPurchaseTitle,
            GroupBuyingStatus status,
            String trackingNumber,
            LocalDateTime meetingAt,
            String meetingLocation,
            Long lastReadMessageId,
            int unreadCount,
            List<Message> messages
    ) {}

    public record Message(
            Long messageId,
            Long userId,       // Nullable
            String messageType,         // TEXT/SYSTEM
            String content,
            LocalDateTime sentAt
    ) {}
}
