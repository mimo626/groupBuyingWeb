package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.service.PointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/point")
public class PointController {

    @Autowired
    private PointService pointService;


//    public ApiResponse<Void> participate(@PathVariable Long id) {
//        groupBuyingService.participate(id);
//        return ApiResponse.success("참여가 완료되었습니다.");
//    }

    @GetMapping("/pay")
    public ApiResponse<?> payPoint(Long gbpId, HttpSession session){
        String userId = (String) session.getAttribute("user_id");
        GroupBuyingParticipationResponse.UserResult dto =  pointService.payPoint(new GroupBuyingParticipationRequest.Send(userId,gbpId));
        return ApiResponse.success(dto); // null -> errorHandler
    }

//    @GetMapping("/refund")
//    public ApiResponse<?> refundPoint(int 사용자id ,int 공구참여id, int 포인트){
//
//    }
//
//    @GetMapping("/charge")
//    public ApiResponse<?> chargePoint(int 사용자id, int 포인트){
//
//    }
//
//    @GetMapping("/settlement")
//    public ApiResponse<?> sendPointToOrganizer(공구 참여 테이블 엔티티 order?){
//
//    }
}
