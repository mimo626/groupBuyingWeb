package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.service.PointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/point")
public class PointController {

    @Autowired
    private PointService pointService;


//    @GetMapping("/pay")
//    public ApiResponse<?> payPoint(GroupBuyingParticipationRequest.Send request, HttpSession session){
//        String userId = (String) session.getAttribute("user_id");
//        GroupBuyingParticipationResponse.UserResult dto =  pointService.payPoint(userId, request);
//        return ApiResponse.success(dto); // null -> errorHandler
//    }

//    @GetMapping("/refund")
//    public ApiResponse<?> refundPoint(int 사용자id ,int 공구참여id, int 포인트){
//
//    }
//
    @PostMapping("/charge")
    @ResponseBody
    public ApiResponse<?> chargePoint(GroupBuyingParticipationRequest.Charge request,HttpSession session){
        String memberId = (String) session.getAttribute("member_id");
        double charge = request.point();
        GroupBuyingParticipationResponse.UserResult dto = pointService.chargePoint(memberId, charge);
        return ApiResponse.success(dto); // null -> errorHandler
    }

    @GetMapping("/testapi/login")
    public String pointTestPage(HttpSession session) {
        // 테스트 : member_id 세션에 강제 주입, 도토리대장거
        session.setAttribute("member_id", "f698dfe8-cc7e-453a-946d-9a39f8e8574c");

        return "chargeTest"; // templates/chargeTest.html
    }


//
//    @GetMapping("/settlement")
//    public ApiResponse<?> sendPointToOrganizer(공구 참여 테이블 엔티티 order?){
//
//    }
}
