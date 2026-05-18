package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.service.PointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PointController {

    @Autowired
    private PointService pointService;

    @Autowired
    private LoginSessionManager loginSessionManager;

    @PostMapping("/api/group-buyings/{groupBuyingId}/settlements/me") // 복수의 공구리소스 /중 하나의/(추상적)정산 리소스/마이페이지
    @ResponseBody
    public ApiResponse<?> sendPointToOrganizer(
            // 성공 : 200 ok
            // 메시지 : 정산 요청 되었습니다
            // dto : 정산된 공구 id, 전체 정산 여부 True/False
            // 실패 : 500 서버 내부 오류
            // 실패 : 401 로그인 오류
            @PathVariable Long groupBuyingId,
            HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        GroupBuyingParticipationResponse.SettleResult dto = pointService.settlePoint(groupBuyingId, memberId);
        return ApiResponse.success(dto); // 공구 참여자 전체 정산 완료 : true / 아니면 false
    }
}
