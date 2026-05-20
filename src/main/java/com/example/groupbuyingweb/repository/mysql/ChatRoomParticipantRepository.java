package com.example.groupbuyingweb.repository.mysql;

import com.example.groupbuyingweb.domain.entity.mysql.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {
    boolean existsByChatRoomIdAndUserId(Long chatRoomId, String userId);
    Optional<ChatRoomParticipant> findByChatRoomIdAndUserId(Long chatRoomId, String userId);
    List<ChatRoomParticipant> findByUserId(String userId);
}
