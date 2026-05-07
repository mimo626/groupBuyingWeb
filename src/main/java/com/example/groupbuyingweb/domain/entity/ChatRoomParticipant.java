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
}