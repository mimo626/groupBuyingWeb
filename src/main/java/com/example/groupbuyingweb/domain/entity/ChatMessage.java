package com.example.groupbuyingweb.domain.entity;

import com.example.groupbuyingweb.domain.enums.MessageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "sender_id", nullable = false)
    //private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Column(nullable = false)
    private String content;

    private LocalDateTime createAt;

    //@Builder
    //public ChatMessage(ChatRoom chatRoom, User sender, MessageType messageType, String content) {
    //    this.chatRoom = chatRoom;
    //    this.sender = sender;
    //    this.messageType = messageType;
    //    this.content = content;
    //    this.createAt = LocalDateTime.now();
    //}
}