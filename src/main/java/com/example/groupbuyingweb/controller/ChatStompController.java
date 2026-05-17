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

/**
 * WebSocket(STOMP) 메시지를 처리하는 컨트롤러
 *
 * REST 컨트롤러(@RestController)가 HTTP 요청을 처리하듯,
 * 이 컨트롤러는 WebSocket으로 들어오는 STOMP 메시지를 처리함
 */
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomParticipantService participantService;

    /**
     * 클라이언트가 채팅 메시지를 전송할 때 호출
     *
     * @MessageMapping: 클라이언트 전송 경로 /app/chat/room/{roomId} 와 매핑
     *   - WebSocketConfig에서 설정한 prefix(/app)가 자동으로 붙음
     *   - REST의 @PostMapping과 비슷한 역할
     *
     * @SendTo: 이 메서드의 반환값을 지정한 경로의 구독자 전체에게 브로드캐스트
     *   - 채팅방을 구독 중인 모든 클라이언트가 메시지 수신
     *   - REST의 @ResponseBody와 비슷한 역할 (대신 모든 구독자에게 전달)
     *
     * @DestinationVariable: URL 경로 변수 추출 (REST의 @PathVariable과 동일한 역할)
     *
     * @param principal MemberPrincipalHandshakeHandler에서 생성한 인증 객체.
     *                  principal.getName()이 세션의 member_id(UUID)를 반환함
     */
    // 성공 시 /topic/chat/room/{roomId} 구독자 전체에게 브로드캐스트
    // 실패(채팅방 참여자 아님) 시 403 Forbidden에 해당하는 BusinessException 발생
    // 실패(인증되지 않은 사용자) 시 WebSocket 핸드셰이크 단계에서 401 Unauthorized로 연결 거부
    @MessageMapping("/chat/room/{roomId}")
    @SendTo("/topic/chat/room/{roomId}")
    public ChatRoomResponse.Message send(
            @DestinationVariable Long roomId,
            ChatMessageRequest.Send request,
            Principal principal
    ) {
        // MemberPrincipalHandshakeHandler에서 세션의 member_id를 Principal로 변환해서 주입
        String memberId = principal.getName(); // + null일때 예외처리
        // 비참여자의 메시지 전송 차단
        participantService.validateParticipant(roomId, memberId);
        // DB 저장 후 응답 DTO 반환 -> @SendTo가 구독자 전체에게 브로드캐스트
        return chatMessageService.sendMessage(roomId, memberId, request.content());
    }
}
