package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.repository.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository participantRepository;

    public void validateParticipant(Long chatRoomId, String memberId) {
        if (!participantRepository.existsByChatRoomIdAndUserId(chatRoomId, memberId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
    }
}
