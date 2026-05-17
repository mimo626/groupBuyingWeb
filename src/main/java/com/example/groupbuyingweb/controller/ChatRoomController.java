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
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    @Autowired
    private LoginSessionManager loginSessionManager;

    // ──────────────── 페이지 렌더링 ────────────────

    @GetMapping("/chat/rooms")
    public String chatRoomListPage() {
        return "chat/list";
    }

    @GetMapping("/chat/rooms/{roomId}")
    public String chatRoomDetailPage(@PathVariable Long roomId) {
        return "chat/detail";
    }

    // ──────────────── REST API ────────────────

    // GET /api/chat/rooms
    @ResponseBody
    @GetMapping("/api/chat/rooms")
    public ApiResponse<ChatRoomResponse.ListResponse> getChatRoomList(HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        ChatRoomResponse.ListResponse response = chatRoomService.getChatRoomList(memberId);
        return ApiResponse.success(response);
    }

    // GET /api/chat/rooms/{roomId}
    @ResponseBody
    @GetMapping("/api/chat/rooms/{roomId}")
    public ApiResponse<ChatRoomResponse.Detail> getChatRoomDetail(
            @PathVariable Long roomId,
            HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        ChatRoomResponse.Detail response = chatRoomService.getChatRoomDetail(roomId, memberId);
        return ApiResponse.success(response);
    }

    // GET /api/chat/rooms/{roomId}/unread
    @ResponseBody
    @GetMapping("/api/chat/rooms/{roomId}/unread")
    public ApiResponse<ChatMessageResponse.UnreadCount> getUnreadCount(
            @PathVariable Long roomId,
            HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        ChatMessageResponse.UnreadCount response = chatRoomService.getUnreadCount(roomId, memberId);
        return ApiResponse.success(response);
    }
}
