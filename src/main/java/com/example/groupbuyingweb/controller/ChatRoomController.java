package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.response.ChatMessageResponse;
import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.service.ChatRoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 채팅방 조회 관련 REST API 컨트롤러
 *
 * WebSocket 메시지 처리는 ChatStompController가 담당하고,
 * 채팅방 목록/상세 조회처럼 HTTP 요청으로 처리할 수 있는 기능은 여기서 담당함
 */
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    @Autowired
    private LoginSessionManager loginSessionManager;

    // ──────────────── 페이지 렌더링 ────────────────

    @GetMapping("/rooms-test")
    public String chatRoomListPage() {
        return "chat/list";
    }

    @GetMapping("/rooms-test/{roomId}")
    public String chatRoomDetailPage(@PathVariable Long roomId) {
        return "chat/detail";
    }

    // ──────────────── REST API ────────────────

    /**
     * 내 채팅방 목록 조회
     *
     * GET /chat/api/rooms
     *
     * 세션에서 memberId를 꺼내 서비스에 전달
     * 반환값에는 각 채팅방의 마지막 메시지와 안 읽은 메시지 수 포함
     */
    @ResponseBody
    @GetMapping("/api/rooms")
    public ApiResponse<ChatRoomResponse.ListResponse> getChatRoomList(HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        ChatRoomResponse.ListResponse response = chatRoomService.getChatRoomList(memberId);
        return ApiResponse.success(response);
    }

    /**
     * 채팅방 상세 조회
     *
     * GET /chat/api/rooms/{roomId}
     *
     * 전체 메시지 목록, 안 읽은 수, 공동구매 상태 등 반환
     * 조회 시 자동으로 읽음 처리가 수행됨 (lastReadMessageId 갱신)
     *
     * @param: roomId 조회할 채팅방 ID (URL 경로에서 추출)
     */
    @ResponseBody
    @GetMapping("/api/rooms/{roomId}")
    public ApiResponse<ChatRoomResponse.Detail> getChatRoomDetail(
            @PathVariable Long roomId,
            HttpSession session) {
        String memberId = (String) session.getAttribute("member_id");
        ChatRoomResponse.Detail response = chatRoomService.getChatRoomDetail(roomId, memberId);
        return ApiResponse.success(response);
    }

    /**
     * 특정 채팅방의 안 읽은 메시지 수 조회
     *
     * GET /chat/api/rooms/{roomId}/unread
     *
     * 채팅방 목록 화면에서 배지 숫자를 실시간으로 갱신할 때 사용
     * 전체 목록/상세를 다시 불러오지 않고 카운트만 빠르게 반환
     *
     * @param: roomId 안 읽은 수를 조회할 채팅방 ID
     */
    @ResponseBody
    @GetMapping("/api/rooms/{roomId}/unread")
    public ApiResponse<ChatMessageResponse.UnreadCount> getUnreadCount(
            @PathVariable Long roomId,
            HttpSession session) {
        String memberId = (String) session.getAttribute("member_id");
        ChatMessageResponse.UnreadCount response = chatRoomService.getUnreadCount(roomId, memberId);
        return ApiResponse.success(response);
    }
}
