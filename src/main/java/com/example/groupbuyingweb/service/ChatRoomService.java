package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.domain.entity.ChatMessage;
import com.example.groupbuyingweb.domain.entity.ChatRoom;
import com.example.groupbuyingweb.domain.entity.ChatRoomParticipant;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.repository.ChatMessageRepository;
import com.example.groupbuyingweb.repository.ChatRoomParticipantRepository;
import com.example.groupbuyingweb.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 채팅방 조회 관련 비즈니스 로직을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final ChatRoomParticipantService participantService;

    /**
     * 채팅방 상세 정보 조회
     *
     * 조회와 동시에 읽음 처리 수행
     * 즉, 이 메서드를 호출하면 해당 채팅방의 모든 메시지를 읽은 것으로 간주함
     *
     * @param chatRoomId: 조회할 채팅방 ID
     * @param memberId: 조회를 요청한 회원 ID (세션에서 추출)
     */
    @Transactional
    public ChatRoomResponse.Detail getChatRoomDetail(Long chatRoomId, String memberId) {
        // 1. 비참여자 차단
        participantService.validateParticipant(chatRoomId, memberId);

        // 2. 채팅방 조회 (없으면 예외)
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // GroupBuying 정보는 ChatRoom의 연관 필드에서 접근
        GroupBuying groupBuying = chatRoom.getGroupBuying();

        // 3. 현재 사용자의 참여 정보 조회 (lastReadMessageId 확인용)
        ChatRoomParticipant participant = participantRepository
                .findByChatRoomIdAndUserId(chatRoomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 참여자가 아닙니다."));

        // 4. 채팅방의 전체 메시지 목록을 시간 오름차순으로 조회
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderByCreateAtAsc(chatRoomId);

        // 5. 안 읽은 메시지 수 계산
        // lastReadMessageId가 null이면 아직 한 번도 읽지 않은 것 -> 전체 메시지 수가 unreadCount
        Long lastReadMessageId = participant.getLastReadMessageId();
        long unreadCount = (lastReadMessageId == null)
                ? messages.size()
                : chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, lastReadMessageId);

        // 6. 읽음 처리 - 가장 최신 메시지 ID로 lastReadMessageId 갱신
        // messages가 비어있지 않을 때만 처리 (빈 채팅방이면 갱신할 ID가 없음)
        Long latestMessageId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
        participantService.markAsRead(chatRoomId, memberId, latestMessageId);

        // 7. ChatMessage 엔티티 리스트 -> ChatRoomResponse.Message DTO 리스트 변환
        List<ChatRoomResponse.Message> messageDtos = messages.stream()
                .map(m -> new ChatRoomResponse.Message(
                        m.getId(),
                        m.getSender().getId(),   // SYSTEM 메시지라도 sender는 항상 존재 (시스템 발송 회원)
                        m.getMessageType().name(),
                        m.getContent(),
                        m.getCreateAt()
                ))
                .toList();

        // 8. Detail DTO 조합 후 반환
        return new ChatRoomResponse.Detail(
                chatRoom.getId(),
                groupBuying.getId(),
                groupBuying.getTitle(),
                groupBuying.getStatus(),
                groupBuying.getTrackingNumber(),
                groupBuying.getMeetingAt(),
                groupBuying.getMeetingPlace(),
                latestMessageId, // 클라이언트가 다음 읽음 처리 기준으로 사용
                (int) unreadCount,
                messageDtos
        );
    }

    /**
     * 현재 사용자가 참여 중인 채팅방 목록 조회
     *
     * 각 채팅방의 마지막 메시지와 안 읽은 메시지 수를 함께 반환함
     *
     * @param memberId: 조회를 요청한 회원 ID (세션에서 추출)
     */
    @Transactional(readOnly = true)
    public ChatRoomResponse.ListResponse getChatRoomList(String memberId) {
        // 1. 현재 사용자가 참여 중인 모든 채팅방 참여 정보 조회
        List<ChatRoomParticipant> participants = participantRepository.findByUserId(memberId);

        // 2. 각 참여 항목을 ListItem DTO로 변환
        List<ChatRoomResponse.ListItem> items = participants.stream()
                .map(participant -> {
                    ChatRoom chatRoom = participant.getChatRoom();
                    GroupBuying groupBuying = chatRoom.getGroupBuying();

                    // 마지막 메시지 조회 (없으면 null)
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreateAtDesc(chatRoom.getId())
                            .orElse(null);

                    // 안 읽은 메시지 수 계산
                    Long lastReadId = participant.getLastReadMessageId();
                    long unreadCount = (lastReadId == null)
                            ? chatMessageRepository.findByChatRoomIdOrderByCreateAtAsc(chatRoom.getId()).size()
                            : chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoom.getId(), lastReadId);

                    return new ChatRoomResponse.ListItem(
                            chatRoom.getId(),
                            groupBuying.getId(),
                            groupBuying.getTitle(),
                            lastMessage != null ? lastMessage.getContent() : null,
                            groupBuying.getStatus(),
                            lastMessage != null ? lastMessage.getCreateAt() : null,
                            (int) unreadCount
                    );
                })
                .toList();

        return new ChatRoomResponse.ListResponse(items);
    }
}
