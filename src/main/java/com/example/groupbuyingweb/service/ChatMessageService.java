package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.domain.entity.mysql.ChatMessage;
import com.example.groupbuyingweb.domain.entity.mysql.ChatRoom;
import com.example.groupbuyingweb.domain.entity.mysql.Member;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.domain.enums.MessageType;
import com.example.groupbuyingweb.repository.mysql.ChatMessageRepository;
import com.example.groupbuyingweb.repository.mysql.ChatRoomRepository;
import com.example.groupbuyingweb.repository.mysql.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅 메시지 관련 로직을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    /**
     * 텍스트 메시지를 DB에 저장하고, 브로드캐스트할 응답 DTO 반환
     *
     * 반환된 DTO는 ChatStompController에서 @SendTo를 통해
     * 채팅방 구독자 전체에게 WebSocket으로 전달
     *
     * @param chatRoomId: 메시지를 보낼 채팅방 ID
     * @param memberId: 발신자 회원 ID (Principal.getName()으로부터 전달됨)
     * @param content: 메시지 내용
     */
    @Transactional
    public ChatRoomResponse.Message sendMessage(Long chatRoomId, String memberId, String content) {
        // 채팅방 존재 여부 확인 (없으면 예외)
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_CHAT_ROOM));

        // 발신자 존재 여부 확인 (없으면 예외)
        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_MEMBER));

        // 메시지 엔티티 생성 및 저장
        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .messageType(MessageType.TEXT)
                        .content(content)
                        .build()
        );

        // Entity -> DTO 변환 후 반환
        // 일반 채팅 삽입이지만 다른 구독자에게 전달할 전체 메시지 정보가 필요하므로 ID만 아닌 전체 DTO 반환
        return new ChatRoomResponse.Message(
                saved.getId(),
                saved.getSender().getId(),
                saved.getSender().getNickname(),
                saved.getMessageType().name(),
                saved.getContent(),
                saved.getCreateAt()
        );
    }
}
