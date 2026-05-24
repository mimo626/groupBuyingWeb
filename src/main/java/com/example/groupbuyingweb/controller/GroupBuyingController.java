package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.service.GroupBuyingService;
import com.example.groupbuyingweb.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/group-buyings")
@RequiredArgsConstructor
public class GroupBuyingController {

    private final GroupBuyingService groupBuyingService;
    private final PointService pointService;

    // 공구 생성 (로그인한 사용자만)
    // 성공(자원 생성 완료) 시 201 Created
    // 실패(필수값 누락, 유효성 에러) 시 400 Bad Request
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @PostMapping()
    @PreAuthorize("isAuthenticated()") // 로그인 검증
    public String addGroupBuying(
            @Valid @ModelAttribute GroupBuyingRequest.Create request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {

        String loggedInUserId = authentication.getName();
        GroupBuyingResponse.Create res = groupBuyingService.addGroupBuying(request, images, loggedInUserId);
        return "redirect:/group-buyings/" + res.groupBuyingId();
    }

    // 공구 수정 (주최자만)
    @PostMapping("/{id}/edit")
    @PreAuthorize("@groupBuyingSecurity.canModifyGroupBuying(authentication, #groupBuyingId)")
    public String editGroupBuying(
            @PathVariable("id") Long groupBuyingId,
            @Valid @ModelAttribute GroupBuyingRequest.Create request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
            Authentication authentication) {

        String loggedInUserId = authentication.getName();
        groupBuyingService.updateGroupBuying(groupBuyingId, request, images, deletedImageIds, loggedInUserId);
        return "redirect:/group-buyings/" + groupBuyingId;
    }

    // 공구 참여 (주최자가 아닌 일반 사용자만)
    // 성공(참여자 자원 생성 완료) 시 201 Created
    // 실패(참여 불가 상태, 모집 인원 초과 등 비즈니스 예외) 시 400 Bad Request
    // 실패(공구 정보 없음) 시 404 Not Found
    @PostMapping("/{id}/participants")
    @PreAuthorize("@groupBuyingSecurity.canParticipate(authentication, #groupBuyingId)") // 참여 가능 여부 시큐리티 검증 추가!
    public String participateGroupBuying(
            @PathVariable("id") Long groupBuyingId,
            @Valid GroupBuyingRequest.Participate groupBuyingRequest,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String loggedInUserId = authentication.getName();
        GroupBuyingResponse.Participate res = groupBuyingService.participateGroupBuying(
                groupBuyingRequest.applyQuantity(),
                loggedInUserId,
                groupBuyingId);

        redirectAttributes.addFlashAttribute("participateSuccess", true);
        return "redirect:/group-buyings/" + groupBuyingId;
    }

    // 상태 변경 (로그인은 필수, 세부 조건은 서비스 로직에 위임)
    // 성공(상태 변경 완료) 시 200 OK
    // 실패(공구 정보 없음) 시 404 Not Found
    // 실패(상태 변경 권한 없음) 시 403 Forbidden
    @ResponseBody
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()") // 여기서 '상태'까지 검증하긴 복잡하므로 인증만 거치고, 서비스에서 권한/상태 예외 처리
    public ApiResponse<GroupBuyingResponse.UpdateStatus> updateGroupBuyingStatus(
            @PathVariable("id") Long groupBuyingId,
            @RequestBody GroupBuyingRequest.UpdateStatus request) {

        GroupBuyingResponse.UpdateStatus res = groupBuyingService.updateStatusFromRequest(groupBuyingId, request);
        return ApiResponse.success(res);
    }

    // 정산 처리 (로그인은 필수, 세부 정산 로직은 서비스에 위임)
    // 성공 : 200 ok
    // 메시지 : 정산 요청 되었습니다
    // dto : 정산된 공구 id, 전체 정산 여부 True/False
    // 실패 : 500 서버 내부 오류
    // 실패 : 401 로그인 오류
    @ResponseBody
    @PostMapping("/{groupBuyingId}/settlements/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> sendPointToOrganizer(
            @PathVariable Long groupBuyingId,
            Authentication authentication){

        String memberId = authentication.getName();
        GroupBuyingParticipationResponse.SettleResult dto = pointService.settlePoint(groupBuyingId, memberId);
        return ApiResponse.success(dto);
    }

    // 공구 삭제 (주최자만)
    // 수정 권한과 동일하게 주최자 + 참여자0명 조건 적용
    @ResponseBody
    @DeleteMapping("/{id}")
    @PreAuthorize("@groupBuyingSecurity.canModifyGroupBuying(authentication, #groupBuyingId)")
    public ResponseEntity<String> deleteGroupBuying(
            @PathVariable("id") Long groupBuyingId,
            Authentication authentication) {

        String loggedInUserId = authentication.getName();
        groupBuyingService.deleteGroupBuying(groupBuyingId, loggedInUserId);
        return ResponseEntity.ok("success");
    }
}