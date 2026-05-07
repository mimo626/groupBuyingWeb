package com.example.groupbuyingweb.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createAt;

    //@OneToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "buying_id", nullable = false, unique = true)
    //private GroupBuying groupBuying;

    //@Builder
    //public ChatRoom(GroupBuying groupBuying) {
    //    this.groupBuying = groupBuying;
    //    this.createAt = LocalDateTime.now();
    //}
}
