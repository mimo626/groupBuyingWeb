package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.request.MyPageRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.dto.response.MyPageResponse;
import com.example.groupbuyingweb.service.MyPageService;
import com.example.groupbuyingweb.service.PointService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/members")
@Controller
public class MemberController {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private LoginSessionManager loginSessionManager;

    @Autowired
    private PointService pointService;

    //@GetMapping("/api/mypage")
    // 성공 : 200 ok
    // 실패 : 500 내부 서버 오류
    // 실패 : 401 로그인 오류 -> 로그인 페이지 이동
    @ResponseBody
    @GetMapping("/me")
    public ApiResponse<?> getProfile(HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Profile dto = myPageService.getProfile(memberId);
        return ApiResponse.success(dto);
    }

    //@GetMapping("/api/mypage/neighborhood")
    // 성공 : 200 ok
    // 실패 : 500 내부 서버 에러
    @ResponseBody
    @GetMapping("/me/neighborhoods")
    public ApiResponse<?> getNeighborhood(HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Neighborhood dto = myPageService.getNeighborhood(memberId);
        return ApiResponse.success(dto);
    }

    //@PatchMapping("/api/mypage/neighborhood")
    // 성공 : 200 ok : 수정 성공 시 수정한 data 반환
    // 실패 : 500 내부 서버 에러
    @ResponseBody
    @PatchMapping("/me/neighborhoods")
    public ApiResponse<MyPageResponse.Neighborhood> patchNeighborhood(
            @RequestBody MyPageRequest.UpdateNeighborhood request,
            HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.Neighborhood dto = myPageService.patchNeighborhood(memberId, request);
        return ApiResponse.success(dto);
    }

    // 성공 시 200 OK
    // 실패(공구 진행 상태 값이 잘못된 경우) 시 400 Bad Request
    // 실패(인증되지 않은 사용자) 시 401 Unauthorized
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @GetMapping("/me/group-buyings")
    public ApiResponse<List<MyPageResponse.MyGroupBuyingListItem>> getHostedGroupBuyings(
            @Valid @ModelAttribute MyPageRequest.MyGroupBuyingSearchCondition request,
            HttpSession session
    ) {
        String memberId = loginSessionManager.requireLoginUserId(session);

        List<MyPageResponse.MyGroupBuyingListItem> dto =
                myPageService.getHostedGroupBuyings(memberId, request.status());

        return ApiResponse.success(dto);
    }

    // 추가: 성공/실패 상태 코드 기준 주석 정리
    // 성공 시 200 OK
    // 실패(공구 진행 상태 값이 잘못된 경우) 시 400 Bad Request
    // 실패(인증되지 않은 사용자) 시 401 Unauthorized
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @GetMapping("/me/participations") // 수정: 내가 참여한 공구 목록 API URL을 현재 로그인한 회원 기준으로 정리
    public ApiResponse<List<MyPageResponse.MyParticipationListItem>> getParticipatedGroupBuyings(
            @Valid @ModelAttribute MyPageRequest.MyGroupBuyingSearchCondition request,
            HttpSession session
    ) {
        String memberId = loginSessionManager.requireLoginUserId(session);

        List<MyPageResponse.MyParticipationListItem> dto =
                myPageService.getParticipatedGroupBuyings(memberId, request.status());

        return ApiResponse.success(dto);
    }


    // 성공 : 200 ok
    // 실패 : 400 공구or공구 참여 기록 없음
    // 실패 : 500 내부 서버 오류
    @ResponseBody
    //@GetMapping("/api/mypage/participations/{participationId}")
    @GetMapping("/me/group-buying/participations/{participationId}")
    public ApiResponse<MyPageResponse.MyParticipationDetail> getParticipationDetail(
            @PathVariable("participationId") Long participationId,
            HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        MyPageResponse.MyParticipationDetail dto =
                myPageService.getParticipationDetail(participationId);
        return ApiResponse.success(dto);
    }

    @PatchMapping("/me/point") //멤버리소스/마이페이지/포인트리소스 업데이트
    @ResponseBody
    public ApiResponse<?> chargePoint(
            // 성공 : 200 OK
            // 메시지 : 포인트 충전되었습니다.
            // 실패 : 500 서버 내부 오류
            // 실패 : 401 로그인 요청
            @RequestBody GroupBuyingParticipationRequest.Charge request, HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        double charge = request.point();
        GroupBuyingParticipationResponse.UserResult dto = pointService.chargePoint(memberId, charge);
        return ApiResponse.success(dto);
    }


}
