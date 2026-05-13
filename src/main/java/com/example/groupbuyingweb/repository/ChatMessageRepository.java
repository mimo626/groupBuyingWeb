package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByCreateAtAsc(Long chatRoomId);
    Optional<ChatMessage> findTopByChatRoomIdOrderByCreateAtDescIdDesc(Long chatRoomId);
    long countByChatRoomIdAndIdGreaterThan(Long chatRoomId, Long messageId);

    // 본인 메시지 제외 unread count (SYSTEM 메시지는 항상 포함)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.id > :lastReadId AND (m.sender IS NULL OR m.sender.id <> :memberId)")
    long countUnreadAfter(@Param("chatRoomId") Long chatRoomId, @Param("lastReadId") Long lastReadId, @Param("memberId") String memberId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND (m.sender IS NULL OR m.sender.id <> :memberId)")
    long countUnreadAll(@Param("chatRoomId") Long chatRoomId, @Param("memberId") String memberId);
}
