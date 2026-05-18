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

//    @GetMapping("/refund")
//    public ApiResponse<?> refundPoint(int 사용자id ,int 공구참여id, int 포인트){
//
//    }
//
    //@PostMapping("/api/point/charge")
    @PatchMapping("/api/members/me/point") //멤버리소스/마이페이지/포인트리소스 업데이트
    @ResponseBody
    public ApiResponse<?> chargePoint(
            // 성공 : 200 OK
            // 메시지 : 포인트 충전되었습니다.
            // 실패 : 500 서버 내부 오류
            // 실패 : 401 로그인 요청
            @RequestBody GroupBuyingParticipationRequest.Charge request,HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        double charge = request.point();
        GroupBuyingParticipationResponse.UserResult dto = pointService.chargePoint(memberId, charge);
        return ApiResponse.success(dto);
    }

    //@PostMapping("/api/point/settle")
    @PostMapping("/api/group-buyings/{groupBuyingId}/settlements/me") // 복수의 공구리소스 /중 하나의/(추상적)정산 리소스/마이페이지
    @ResponseBody
    public ApiResponse<?> sendPointToOrganizer(
            // 성공 : 200 ok
            // 메시지 : 정산 요청 되었습니다
            // dto : 정산된 공구 id, 전체 정산 여부 True/False
            // 실패 : 500 서버 내부 오류
            // 실패 : 401 로그인 오류
            @RequestBody GroupBuyingParticipationRequest.Send request,
            @PathVariable Long groupBuyingId,
            HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        GroupBuyingParticipationResponse.SettleResult dto = pointService.settlePoint(groupBuyingId, memberId);
        return ApiResponse.success(dto); // 공구 참여자 전체 정산 완료 : true / 아니면 false
    }
}
