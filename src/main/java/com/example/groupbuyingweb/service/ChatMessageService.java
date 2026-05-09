package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.domain.entity.ChatMessage;
import com.example.groupbuyingweb.domain.entity.ChatRoom;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.MessageType;
import com.example.groupbuyingweb.repository.ChatMessageRepository;
import com.example.groupbuyingweb.repository.ChatRoomRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoomResponse.Message sendMessage(Long chatRoomId, String memberId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .messageType(MessageType.TEXT)
                        .content(content)
                        .build()
        );

        return new ChatRoomResponse.Message(
                saved.getId(),
                saved.getSender().getId(),
                saved.getMessageType().name(),
                saved.getContent(),
                saved.getCreateAt()
        );
    }
}
