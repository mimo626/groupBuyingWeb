package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.service.GroupBuyingService;
import com.example.groupbuyingweb.service.PointService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/group-buyings")
public class GroupBuyingController {
    @Autowired
    private GroupBuyingService groupBuyingService;
    @Autowired
    private LoginSessionManager loginSessionManager;
    @Autowired
    private PointService pointService;

    // 성공(자원 생성 완료) 시 201 Created
    // 실패(필수값 누락, 유효성 에러) 시 400 Bad Request
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @PostMapping()
    public String addGroupBuying(
            @Valid @ModelAttribute GroupBuyingRequest.Create request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) {
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        GroupBuyingResponse.Create res = groupBuyingService.addGroupBuying(request, images, loggedInUserId);

        return "redirect:/group-buyings/" + res.groupBuyingId();
    }

    // 실제 수정 처리 요청
    @PostMapping("/{id}/edit") // HTML Form은 기본적으로 GET/POST만 지원하므로 POST 사용 추천
    public String editGroupBuying(@PathVariable("id") Long groupBuyingId,
                                  @Valid @ModelAttribute GroupBuyingRequest.Create request, // 폼에서 넘어온 수정 데이터
                                  @RequestParam(value = "images", required = false) List<MultipartFile> images, // required=false 필수
                                  @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                                  HttpSession session) {
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        // 서비스에 수정 요청 (권한 체크 -> 기존 데이터 조회 -> 값 덮어쓰기 -> 저장)
        groupBuyingService.updateGroupBuying(groupBuyingId, request, images, deletedImageIds, loggedInUserId);

        // 수정이 완료되면 다시 상세 페이지로 리다이렉트
        return "redirect:/group-buyings/" + groupBuyingId;
    }

    // 성공(참여자 자원 생성 완료) 시 201 Created
    // 실패(참여 불가 상태, 모집 인원 초과 등 비즈니스 예외) 시 400 Bad Request
    // 실패(공구 정보 없음) 시 404 Not Found
    @PostMapping("/{id}/participants") // 행위(participate) 대신 '참여자(participants)'라는 하위 자원을 생성(POST)하는 것으로 변경
    public String participateGroupBuying(@PathVariable("id") Long groupBuyingId,
                                         @Valid GroupBuyingRequest.Participate groupBuyingRequest,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {

        String loggedInUserId = loginSessionManager.requireLoginUserId(session);
        GroupBuyingResponse.Participate res = groupBuyingService.participateGroupBuying(
                groupBuyingRequest.applyQuantity(),
                loggedInUserId,
                groupBuyingId);

        redirectAttributes.addFlashAttribute("participateSuccess", true);

        return "redirect:/group-buyings/" + groupBuyingId;
    }

    // 성공(상태 변경 완료) 시 200 OK
    // 실패(공구 정보 없음) 시 404 Not Found
    // 실패(상태 변경 권한 없음) 시 403 Forbidden
    @ResponseBody
    @PatchMapping("/{id}/status")
    public ApiResponse<GroupBuyingResponse.UpdateStatus> updateGroupBuyingStatus(
            @PathVariable("id") Long groupBuyingId,
            @RequestBody GroupBuyingRequest.UpdateStatus request
    ) {
        GroupBuyingResponse.UpdateStatus res = groupBuyingService.updateStatusFromRequest(groupBuyingId, request);
        return ApiResponse.success(res);
    }

    // 성공 : 200 ok
    // 메시지 : 정산 요청 되었습니다
    // dto : 정산된 공구 id, 전체 정산 여부 True/False
    // 실패 : 500 서버 내부 오류
    // 실패 : 401 로그인 오류
    @PostMapping("/{groupBuyingId}/settlements/me") // 복수의 공구리소스 /중 하나의/(추상적)정산 리소스/마이페이지
    @ResponseBody
    public ApiResponse<?> sendPointToOrganizer(
            @PathVariable Long groupBuyingId,
            HttpSession session){
        String memberId = loginSessionManager.requireLoginUserId(session);
        GroupBuyingParticipationResponse.SettleResult dto = pointService.settlePoint(groupBuyingId, memberId);
        return ApiResponse.success(dto); // 공구 참여자 전체 정산 완료 : true / 아니면 false
    }
}