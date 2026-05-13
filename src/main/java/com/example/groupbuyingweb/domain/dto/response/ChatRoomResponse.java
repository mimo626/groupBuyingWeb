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
            String currentUserRole,
            List<Message> messages
    ) {}

    public record Message(
            Long messageId,
            String senderId,        // Nullable (SYSTEM 메시지의 경우 null), isMine 판별용
            String senderNickname,  // Nullable (SYSTEM 메시지의 경우 null), 화면 표시용
            String messageType,
            String content,
            LocalDateTime sentAt
    ) {}
}
