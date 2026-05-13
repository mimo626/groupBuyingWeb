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

//    @GetMapping("/refund")
//    public ApiResponse<?> refundPoint(int 사용자id ,int 공구참여id, int 포인트){
//
//    }
//
    @PostMapping("/charge")
    @ResponseBody
    public ApiResponse<?> chargePoint(GroupBuyingParticipationRequest.Charge request,HttpSession session){
        String memberId = (String) session.getAttribute("loginUserId");
        double charge = request.point();
        GroupBuyingParticipationResponse.UserResult dto = pointService.chargePoint(memberId, charge);
        return ApiResponse.success(dto); // null -> errorHandler
    }
//
//    @GetMapping("/settlement")
//    public ApiResponse<?> sendPointToOrganizer(공구 참여 테이블 엔티티 order?){
//
//    }
}
