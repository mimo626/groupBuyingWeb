package com.example.groupbuyingweb.domain.entity.mysql;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buying_id", nullable = false, unique = true)
    private GroupBuying groupBuying;

    @Builder
    public ChatRoom(GroupBuying groupBuying) {
        this.groupBuying = groupBuying;
        this.createAt = LocalDateTime.now();
    }
}
