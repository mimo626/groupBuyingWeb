package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.request.MyPageRequest;
import com.example.groupbuyingweb.domain.dto.response.MyPageResponse;
import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.service.AddressService;
import com.example.groupbuyingweb.service.MyPageService;
import com.example.groupbuyingweb.service.PointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private LoginSessionManager loginSessionManager;




    @ResponseBody
    @GetMapping("/profile")
    public ApiResponse<?> getProfile(HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Profile dto = myPageService.getProfile(memberId);
        return ApiResponse.success(dto);
    }

    @ResponseBody
    @GetMapping("/neighborhood")
    public ApiResponse<?> getNeighborhood(HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Neighborhood dto = myPageService.getNeighborhood(memberId);
        return ApiResponse.success(dto);
    }

    @ResponseBody
    @PatchMapping("/neighborhood")
    public ApiResponse<MyPageResponse.Neighborhood> patchNeighborhood(
            @RequestBody MyPageRequest.UpdateNeighborhood request,
            HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Neighborhood dto = myPageService.patchNeighborhood(memberId, request);
        return ApiResponse.success(dto);
    }

    @ResponseBody
    @GetMapping("/group-buys")
    public ApiResponse<List<MyPageResponse.MyGroupBuyingListItem>> getHostedGroupBuyings(
            @RequestParam(required = false) MyPageRequest.MyGroupBuyingSearchCondition request,
            HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        List<MyPageResponse.MyGroupBuyingListItem> dto =
                myPageService.getHostedGroupBuyings(memberId, request.status());
        return ApiResponse.success(dto);
    }

    @ResponseBody
    @GetMapping("/participations/{participationId}")
    public ApiResponse<MyPageResponse.MyParticipationDetail> getParticipationDetail(
            @PathVariable("participationId") Long participationId,
            HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.MyParticipationDetail dto =
                myPageService.getParticipationDetail(memberId, participationId);
        return ApiResponse.success(dto);
    }


}
