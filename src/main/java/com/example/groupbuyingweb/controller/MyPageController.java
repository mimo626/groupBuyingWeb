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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class MyPageController {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private LoginSessionManager loginSessionManager;



    //@GetMapping("/api/members/me")
    // 성공 : 200 ok
    // 실패 : 500 내부 서버 오류
    // 실패 : 401 로그인 오류 -> 로그인 페이지 이동
    @ResponseBody
    @GetMapping("/api/mypage/profile")
    public ApiResponse<?> getProfile(HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Profile dto = myPageService.getProfile(memberId);
        return ApiResponse.success(dto);
    }

    //GetMapping("api/members/me/neighborhoods")
    // 성공 : 200 ok
    // 실패 : 500 내부 서버 에러
    @ResponseBody
    @GetMapping("/api/mypage/neighborhood")
    public ApiResponse<?> getNeighborhood(HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Neighborhood dto = myPageService.getNeighborhood(memberId);
        return ApiResponse.success(dto);
    }

    //@PatchMapping("api/members/me/neighborhoods")
    // 성공 : 200 ok : 수정 성공 시 수정한 data 반환
    // 실패 : 500 내부 서버 에러
    @ResponseBody
    @PatchMapping("/api/mypage/neighborhood")
    public ApiResponse<MyPageResponse.Neighborhood> patchNeighborhood(
            @RequestBody MyPageRequest.UpdateNeighborhood request,
            HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Neighborhood dto = myPageService.patchNeighborhood(memberId, request);
        return ApiResponse.success(dto);
    }

    @ResponseBody
    @GetMapping("/api/mypage/group-buys")
    public ApiResponse<List<MyPageResponse.MyGroupBuyingListItem>> getHostedGroupBuyings(
            @Valid @ModelAttribute MyPageRequest.MyGroupBuyingSearchCondition request,
            HttpSession session
    ) {
        // 세션에서 현재 로그인한 사용자 ID를 꺼낸다.
        String memberId = loginSessionManager.requireLoginUserId(session);

        // Service에 로그인 사용자 ID와 진행 상태를 넘겨 내가 개설한 공구 목록을 조회한다.
        List<MyPageResponse.MyGroupBuyingListItem> dto =
                myPageService.getHostedGroupBuyings(memberId, request.status());

        return ApiResponse.success(dto);
    }

    @ResponseBody
    @GetMapping("/api/mypage/participations")
    public ApiResponse<List<MyPageResponse.MyParticipationListItem>> getParticipatedGroupBuyings(
            @Valid @ModelAttribute MyPageRequest.MyGroupBuyingSearchCondition request,
            HttpSession session
    ) {
        // 세션에서 현재 로그인한 사용자 ID를 꺼낸다.
        String memberId = loginSessionManager.requireLoginUserId(session);

        // Service에 로그인 사용자 ID와 진행 상태를 넘겨 내가 참여한 공구 이력 목록을 조회한다.
        List<MyPageResponse.MyParticipationListItem> dto =
                myPageService.getParticipatedGroupBuyings(memberId, request.status());

        return ApiResponse.success(dto);
    }

    //@GetMapping("/api/members/me/group-buying/participations/{participationId}")
    // 성공 : 200 ok
    // 실패 : 400 그런 공구or공구 참여 기록 없음
    // 실패 : 500 내부 서버 오류
    @ResponseBody
    @GetMapping("/api/mypage/participations/{participationId}")
    public ApiResponse<MyPageResponse.MyParticipationDetail> getParticipationDetail(
            @PathVariable("participationId") Long participationId,
            HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.MyParticipationDetail dto =
                myPageService.getParticipationDetail(participationId);
        return ApiResponse.success(dto);
    }


}
