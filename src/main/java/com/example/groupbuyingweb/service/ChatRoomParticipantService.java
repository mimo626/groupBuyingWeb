package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.entity.ChatRoomParticipant;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.repository.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅방 참여자 관련 로직을 담당하는 서비스
 *
 * ChatRoomService, ChatMessageService 양쪽에서 참여자 관련 처리가 필요하기 때문에
 * 별도 서비스로 분리하여 순환 의존성을 방지
 */
@Service
@RequiredArgsConstructor
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository participantRepository;

    /**
     * 해당 회원이 채팅방 참여자인지 검증
     * 참여자가 아니면 예외를 던져 이후 로직(메시지 저장, 브로드캐스트) 차단
     *
     * @param chatRoomId: 검증할 채팅방 ID
     * @param memberId: 검증할 회원 ID (UUID)
     */
    public void validateParticipant(Long chatRoomId, String memberId) {
        if (!participantRepository.existsByChatRoomIdAndUserId(chatRoomId, memberId)) {
            throw new BusinessException(ErrorCode.NOT_CHAT_PARTICIPANT);
        }
    }

    /**
     * 채팅방의 마지막 읽은 메시지 ID 갱신 (읽음 처리)
     *
     * 채팅방 상세 조회 시 자동으로 호출되어,
     * 이후 조회 시 안 읽은 메시지 수(unreadCount)가 0으로 초기화됨
     *
     * lastMessageId가 null이면 아직 채팅방에 메시지가 없는 것이므로 처리하지 않음
     *
     * @param chatRoomId: 읽음 처리할 채팅방 ID
     * @param memberId: 읽음 처리할 회원 ID
     * @param lastMessageId: 현재 채팅방의 가장 최신 메시지 ID
     */
    @Transactional
    public void markAsRead(Long chatRoomId, String memberId, Long lastMessageId) {
        if (lastMessageId == null) {
            return; // 메시지가 하나도 없는 채팅방이면 처리 불필요
        }

        // 참여자 정보 조회 - lastReadMessageId를 수정해야 하므로 엔티티를 직접 가져옴
        ChatRoomParticipant participant = participantRepository
                .findByChatRoomIdAndUserId(chatRoomId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CHAT_PARTICIPANT));

        // 엔티티의 updateLastReadMessageId() 호출
        participant.updateLastReadMessageId(lastMessageId);
    }
}
