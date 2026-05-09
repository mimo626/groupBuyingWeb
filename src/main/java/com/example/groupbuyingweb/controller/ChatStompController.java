package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.domain.dto.request.ChatMessageRequest;
import com.example.groupbuyingweb.domain.dto.response.ChatRoomResponse;
import com.example.groupbuyingweb.service.ChatMessageService;
import com.example.groupbuyingweb.service.ChatRoomParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomParticipantService participantService;

    // 클라이언트 전송 경로: /app/chat/room/{roomId}
    // 구독 경로: /topic/chat/room/{roomId}
    @MessageMapping("/chat/room/{roomId}")
    @SendTo("/topic/chat/room/{roomId}")
    public ChatRoomResponse.Message send(
            @DestinationVariable Long roomId,
            ChatMessageRequest.Send request,
            Principal principal
    ) {
        // MemberPrincipalHandshakeHandler에서 세션의 member_id를 Principal로 변환해서 주입
        String memberId = principal.getName(); // NULL일때 예외처리 추가하면 좋을듯
        participantService.validateParticipant(roomId, memberId);
        return chatMessageService.sendMessage(roomId, memberId, request.content());
    }
}
