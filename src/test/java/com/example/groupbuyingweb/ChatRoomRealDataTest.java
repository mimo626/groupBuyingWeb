package com.example.groupbuyingweb;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.service.ChatRoomService;
import com.example.groupbuyingweb.service.GroupBuyingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 DB 데이터를 대상으로 하는 통합 테스트
 * @Transactional 없음 → 변경사항이 실제 DB에 반영됨
 * 한 번만 실행할 것 (중복 실행 시 채팅방 중복 생성)
 */
@SpringBootTest
class ChatRoomRealDataTest {

    @Autowired private GroupBuyingService groupBuyingService;
    @Autowired private ChatRoomService chatRoomService;

    private static final String HOST_ID = "dabf4134-0b0f-4034-a1ff-c2889b9a95e1";

    @Test
    @DisplayName("공구 status → START 변경 후 채팅방 목록 조회")
    void statusStartAndGetChatRoomList() {
        GroupBuyingRequest.UpdateStatus request = new GroupBuyingRequest.UpdateStatus(
                GroupBuyingStatus.START, null, null
        );

        groupBuyingService.updateStatusFromRequest(1L, request);
        groupBuyingService.updateStatusFromRequest(2L, request);

        ChatRoomResponse.ListResponse list = chatRoomService.getChatRoomList(HOST_ID);

        System.out.println("=== 채팅방 목록 ===");
        list.chatRooms().forEach(item ->
                System.out.printf("id=%d | 제목=%s | 마지막메시지=%s | unread=%d%n",
                        item.chatRoomId(), item.groupPurchaseTitle(), item.lastMessage(), item.unreadCount())
        );

        assertThat(list.chatRooms()).isNotEmpty();
    }
}
