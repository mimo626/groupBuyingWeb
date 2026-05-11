package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.response.ChatMessageResponse;
import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.domain.entity.ChatMessage;
import com.example.groupbuyingweb.domain.entity.ChatRoom;
import com.example.groupbuyingweb.domain.entity.ChatRoomParticipant;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.MessageType;
import com.example.groupbuyingweb.repository.ChatMessageRepository;
import com.example.groupbuyingweb.repository.ChatRoomParticipantRepository;
import com.example.groupbuyingweb.repository.ChatRoomRepository;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
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
    private final GroupBuyingParticipationRepository groupBuyingParticipationRepository;

    /**
     * 채팅방 생성 및 공구 참여자 전원 입장 처리
     *
     * 목표 수량 달성 시 GroupBuyingService에서 호출됨
     * 기존 참여자(주최자 포함) 전원을 채팅방에 자동 입장시킴
     *
     * @param groupBuying: 채팅방을 생성할 공구 엔티티
     */
    @Transactional
    public void createChatRoom(GroupBuying groupBuying) {
        ChatRoom chatRoom = ChatRoom.builder()
                .groupBuying(groupBuying)
                .build();
        chatRoomRepository.save(chatRoom);

        // 공동구매 참여자 목록 조회
        List<GroupBuyingParticipation> participations =
                groupBuyingParticipationRepository.findAllByGroupBuyingId(groupBuying.getId());

        // 입장 처리
        participations.forEach(participation -> {
            ChatRoomParticipant participant = ChatRoomParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(participation.getMember())
                    .build();
            participantRepository.save(participant);
        });
    }

    /**
     * 시스템 메시지 전송
     *
     * 공구 상태 변경 시 사용
     */
    @Transactional
    public void sendSystemMessage(Long groupBuyingId, GroupBuyingStatus status) {
        ChatRoom chatRoom = chatRoomRepository.findByGroupBuyingId(groupBuyingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        Member host = chatRoom.getGroupBuying().getMember();

        String content = switch (status) {
            case START -> "채팅방이 개설되었습니다. 주최자는 물품 구매를 완료해주세요.";
            case PURCHASED -> "주최자가 물품 구매를 완료했습니다. 주최자는 운송장 번호를 입력해주세요.";
            case SHIPPING -> "물품이 배송 중입니다. 주최자는 만남 일정을 등록해주세요.";
            case MEETING_SCHEDULED -> "만남 일정이 확정되었습니다.";
            case SETTLING -> "정산이 시작되었습니다. 참여자는 정산 완료 버튼을 눌러주세요.";
            case CLOSED -> "공구가 종료되었습니다. 이용해 주셔서 감사합니다!";
            default -> "[공구 상태 변경] " + status.getDescription();
        };

        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(host)
                .messageType(MessageType.SYSTEM)
                .content(content)
                .build();

        chatMessageRepository.save(systemMessage);
    }

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
     * 각 채팅방의 마지막 메시지와 안 읽은 메시지 수를 함께 반환
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
                            .findTopByChatRoomIdOrderByCreateAtDescIdDesc(chatRoom.getId())
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

    /**
     * 특정 채팅방의 안 읽은 메시지 수만 조회
     *
     * 채팅방 목록 화면에서 배지 숫자를 실시간으로 갱신할 때 사용
     * 전체 목록이나 상세를 다시 불러오지 않고 카운트만 반환
     *
     * @param chatRoomId: 조회할 채팅방 ID
     * @param memberId: 조회를 요청한 회원 ID (세션에서 추출)
     */
    @Transactional(readOnly = true)
    public ChatMessageResponse.UnreadCount getUnreadCount(Long chatRoomId, String memberId) {
        // 참여자인지 먼저 검증
        participantService.validateParticipant(chatRoomId, memberId);

        // 현재 사용자의 lastReadMessageId 조회
        ChatRoomParticipant participant = participantRepository
                .findByChatRoomIdAndUserId(chatRoomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 참여자가 아닙니다."));

        Long lastReadMessageId = participant.getLastReadMessageId();

        // lastReadMessageId가 null이면 한 번도 읽지 않은 것 -> 전체 메시지 수가 unreadCount
        // null이 아니면 마지막으로 읽은 메시지 이후의 메시지 수만 카운트
        long count = (lastReadMessageId == null)
                ? chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, 0L)
                : chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, lastReadMessageId);

        return new ChatMessageResponse.UnreadCount((int) count);
    }
}
