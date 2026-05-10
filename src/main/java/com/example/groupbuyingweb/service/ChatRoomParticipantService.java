package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.repository.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
    }
}
