package com.example.groupbuyingweb;

import com.example.groupbuyingweb.domain.dto.response.ChatMessageResponse;
import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.domain.entity.mysql.*;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.mysql.*;
import com.example.groupbuyingweb.service.ChatMessageService;
import com.example.groupbuyingweb.service.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired private ChatRoomService chatRoomService;
    @Autowired private ChatMessageService chatMessageService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private GroupBuyingRepository groupBuyingRepository;
    @Autowired private GroupBuyingParticipationRepository participationRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatRoomParticipantRepository chatRoomParticipantRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;

    private Member organizer;
    private Member participant;
    private GroupBuying groupBuying;

    @BeforeEach
    void setUp() {
        // 이전 테스트 데이터 정리 (FK 순서 준수)
        chatMessageRepository.deleteAll();
        chatRoomParticipantRepository.deleteAll();
        chatRoomRepository.deleteAll();
        participationRepository.deleteAll();
        groupBuyingRepository.deleteAll();
        memberRepository.deleteAll();

        // 주최자 멤버 생성
        organizer = memberRepository.saveAndFlush(Member.builder()
                .loginId("chat_organizer")
                .password("password")
                .nickname("채팅주최자")
                .address("서울시 중구 장충동")
                .entX(37.0).entY(127.0)
                .build());

        // 참여자 멤버 생성
        participant = memberRepository.saveAndFlush(Member.builder()
                .loginId("chat_participant")
                .password("password")
                .nickname("채팅참여자")
                .address("서울시 중구 장충동")
                .entX(37.1).entY(127.1)
                .build());

        // 공구 생성
        groupBuying = groupBuyingRepository.saveAndFlush(GroupBuying.builder()
                .member(organizer)
                .title("테스트 사과 공구")
                .productName("사과")
                .category(GroupBuyingCategory.FOOD)
                .totalPrice(10000.0)
                .targetQuantity(5)
                .productUrl("http://example.com")
                .meetingPlace("상봉역 1번 출구")
                .neighborhoodName("상봉동")
                .entX(37.0).entY(127.0)
                .deadline(LocalDateTime.now().plusDays(3))
                .build());

        // 주최자 참여 정보
        participationRepository.saveAndFlush(GroupBuyingParticipation.builder()
                .member(organizer)
                .groupBuying(groupBuying)
                .role(UserRole.ORGANIZER)
                .applyQuantity(2)
                .build());

        // 참여자 참여 정보
        participationRepository.saveAndFlush(GroupBuyingParticipation.builder()
                .member(participant)
                .groupBuying(groupBuying)
                .role(UserRole.PARTICIPANT)
                .applyQuantity(3)
                .build());
    }

    // ──────────────────────────────────────────────
    // 채팅방 생성 관련 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("1. 채팅방 생성 시 공구 참여자 전원 자동 입장 테스트")
    void createChatRoomTest() {
        // 실행: 공구 완료 시 채팅방 생성
        chatRoomService.createChatRoom(groupBuying);

        // 채팅방이 DB에 생성됐는지 확인
        ChatRoom chatRoom = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow(() -> new AssertionError("채팅방이 생성되지 않았습니다."));
        assertThat(chatRoom.getGroupBuying().getId()).isEqualTo(groupBuying.getId());

        // 주최자와 참여자 둘 다 채팅방 참여자로 등록됐는지 확인
        List<ChatRoomParticipant> organizerRooms = chatRoomParticipantRepository.findByUserId(organizer.getId());
        List<ChatRoomParticipant> participantRooms = chatRoomParticipantRepository.findByUserId(participant.getId());

        assertThat(organizerRooms).hasSize(1);   // 주최자: 1개 채팅방 참여
        assertThat(participantRooms).hasSize(1); // 참여자: 1개 채팅방 참여
    }

    // ──────────────────────────────────────────────
    // 메시지 전송 관련 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("2. 메시지 전송 시 내용과 발신자 ID가 정확히 저장되는지 테스트")
    void sendMessageTest() {
        // 채팅방 먼저 생성
        chatRoomService.createChatRoom(groupBuying);
        Long chatRoomId = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow().getId();

        // 실행: 메시지 전송
        ChatRoomResponse.Message response = chatMessageService.sendMessage(
                chatRoomId, organizer.getId(), "안녕하세요!");

        // 검증
        assertThat(response.content()).isEqualTo("안녕하세요!");
        assertThat(response.senderId()).isEqualTo(organizer.getId());
        assertThat(response.messageType()).isEqualTo("TEXT");
        assertThat(response.messageId()).isNotNull();
    }

    // ──────────────────────────────────────────────
    // 채팅방 상세 조회 + 읽음 처리 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("3. 채팅방 상세 조회 시 메시지 목록이 시간 순으로 반환되는지 테스트")
    void getChatRoomDetailMessagesOrderTest() {
        chatRoomService.createChatRoom(groupBuying);
        Long chatRoomId = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow().getId();

        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "첫 번째 메시지");
        chatMessageService.sendMessage(chatRoomId, participant.getId(), "두 번째 메시지");

        // 실행
        ChatRoomResponse.Detail detail = chatRoomService.getChatRoomDetail(chatRoomId, organizer.getId());

        // 메시지 2개, 순서 확인
        assertThat(detail.messages()).hasSize(2);
        assertThat(detail.messages().get(0).content()).isEqualTo("첫 번째 메시지");
        assertThat(detail.messages().get(1).content()).isEqualTo("두 번째 메시지");
    }

    @Test
    @DisplayName("4. 첫 조회 시 unreadCount는 전체 메시지 수, 두 번째 조회 시 0이 되는지 테스트")
    void getChatRoomDetailUnreadCountTest() {
        chatRoomService.createChatRoom(groupBuying);
        Long chatRoomId = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow().getId();

        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "메시지1");
        chatMessageService.sendMessage(chatRoomId, participant.getId(), "메시지2");

        // 첫 번째 조회 → unreadCount 2 (아직 읽지 않음)
        ChatRoomResponse.Detail firstDetail = chatRoomService.getChatRoomDetail(chatRoomId, organizer.getId());
        assertThat(firstDetail.unreadCount()).isEqualTo(2);

        // 두 번째 조회 → 읽음 처리 되어 unreadCount 0
        ChatRoomResponse.Detail secondDetail = chatRoomService.getChatRoomDetail(chatRoomId, organizer.getId());
        assertThat(secondDetail.unreadCount()).isEqualTo(0);
    }

    // ──────────────────────────────────────────────
    // 채팅방 목록 조회 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("5. 채팅방 목록 조회 시 마지막 메시지와 제목이 올바르게 반환되는지 테스트")
    void getChatRoomListTest() {
        chatRoomService.createChatRoom(groupBuying);
        Long chatRoomId = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow().getId();

        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "첫 메시지");
        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "마지막 메시지입니다");

        // 실행
        ChatRoomResponse.ListResponse list = chatRoomService.getChatRoomList(organizer.getId());

        // 채팅방 1개, 마지막 메시지와 제목 확인
        assertThat(list.chatRooms()).hasSize(1);

        ChatRoomResponse.ListItem item = list.chatRooms().get(0);
        assertThat(item.lastMessage()).isEqualTo("마지막 메시지입니다");
        assertThat(item.groupPurchaseTitle()).isEqualTo("테스트 사과 공구");
    }

    @Test
    @DisplayName("6. 채팅방 목록의 unreadCount는 읽지 않은 메시지 수를 반영하는지 테스트")
    void getChatRoomListUnreadCountTest() {
        chatRoomService.createChatRoom(groupBuying);
        Long chatRoomId = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow().getId();

        // 주최자가 메시지 2개 전송 (참여자 입장에서는 2개 읽지 않음)
        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "메시지1");
        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "메시지2");

        // 참여자 목록 조회 → unreadCount 2
        ChatRoomResponse.ListResponse list = chatRoomService.getChatRoomList(participant.getId());
        assertThat(list.chatRooms().get(0).unreadCount()).isEqualTo(2);

        // 참여자가 상세 조회(읽음 처리) 후 목록 다시 조회 → unreadCount 0
        chatRoomService.getChatRoomDetail(chatRoomId, participant.getId());

        ChatRoomResponse.ListResponse listAfterRead = chatRoomService.getChatRoomList(participant.getId());
        assertThat(listAfterRead.chatRooms().get(0).unreadCount()).isEqualTo(0);
    }

    // ──────────────────────────────────────────────
    // 안 읽은 메시지 수 단독 조회 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("7. 안 읽은 메시지 수 조회 및 읽음 처리 후 0이 되는지 테스트")
    void getUnreadCountTest() {
        chatRoomService.createChatRoom(groupBuying);
        Long chatRoomId = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow().getId();

        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "1번");
        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "2번");
        chatMessageService.sendMessage(chatRoomId, organizer.getId(), "3번");

        // 읽기 전 → 3개
        ChatMessageResponse.UnreadCount before = chatRoomService.getUnreadCount(chatRoomId, participant.getId());
        assertThat(before.unreadCount()).isEqualTo(3);

        // 상세 조회로 읽음 처리
        chatRoomService.getChatRoomDetail(chatRoomId, participant.getId());

        // 읽은 후 → 0개
        ChatMessageResponse.UnreadCount after = chatRoomService.getUnreadCount(chatRoomId, participant.getId());
        assertThat(after.unreadCount()).isEqualTo(0);
    }

    // ──────────────────────────────────────────────
    // 예외 처리 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("8. 비참여자가 채팅방 조회 시 예외 발생 테스트")
    void nonParticipantAccessTest() {
        chatRoomService.createChatRoom(groupBuying);
        Long chatRoomId = chatRoomRepository.findByGroupBuyingId(groupBuying.getId())
                .orElseThrow().getId();

        // 공구와 전혀 관계없는 외부 멤버 생성
        Member stranger = memberRepository.saveAndFlush(Member.builder()
                .loginId("stranger_member")
                .password("password")
                .nickname("외부인")
                .address("서울시 중구 장충동")
                .entX(37.0).entY(127.0)
                .build());

        // 비참여자가 상세 조회 시도 → IllegalArgumentException 발생
        assertThatThrownBy(() -> chatRoomService.getChatRoomDetail(chatRoomId, stranger.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("채팅방 참여자가 아닙니다");
    }
}
