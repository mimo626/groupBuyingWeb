package com.example.groupbuyingweb.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    private Long lastReadMessageId;

    @Builder
    public ChatRoomParticipant(ChatRoom chatRoom, Member user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    /**
     * 마지막으로 읽은 메시지 ID 갱신 (읽음 처리)
     *
     * 채팅방 상세 조회 시 호출되어, 이 값이 갱신된 이후에 도착한 메시지만
     * 안 읽은 메시지로 계산됨
     * (unreadCount = 메시지 id > lastReadMessageId 인 메시지 수)
     */
    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }
}