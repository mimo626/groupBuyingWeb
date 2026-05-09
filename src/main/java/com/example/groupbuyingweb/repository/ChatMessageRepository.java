package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByCreateAtAsc(Long chatRoomId);
    Optional<ChatMessage> findTopByChatRoomIdOrderByCreateAtDesc(Long chatRoomId);
    long countByChatRoomIdAndIdGreaterThan(Long chatRoomId, Long messageId);
}
