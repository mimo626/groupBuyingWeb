package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingSort;
import com.example.groupbuyingweb.service.GroupBuyingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/group-buyings")
public class GroupBuyingController {
    @Autowired
    private GroupBuyingService groupBuyingService;
    @Autowired
    private LoginSessionManager loginSessionManager;

    @Value("${kakao.local.javascript-key}")
    private String kakaoJsKey;

    // 성공 시 200 OK
    // 실패(인증되지 않은 사용자) 시 401 Unauthorized
    @GetMapping("/new")
    public String createForm(Model model,
                             HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        String memberAddress = groupBuyingService.getMemberAddress(memberId);
        model.addAttribute("categories", GroupBuyingCategory.values());
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("memberAddress", memberAddress);
        return "groupbuying/create";
    }

    // 성공(자원 생성 완료) 시 201 Created
    // 실패(필수값 누락, 유효성 에러) 시 400 Bad Request
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @PostMapping
    public String addGroupBuying(
            @Valid @ModelAttribute GroupBuyingRequest.Create request,
            @RequestParam("images") List<MultipartFile> images,
            HttpSession session) {
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        System.out.println(request.toString());
        GroupBuyingResponse.Create res = groupBuyingService.addGroupBuying(request, images, loggedInUserId);

        return "redirect:/group-buyings/" + res.groupBuyingId();
    }

    // 성공 시 200 OK
    // 실패(해당 ID의 공구가 없을 때) 시 404 Not Found
    @GetMapping("/{id}")
    public String getGroupBuyingById(@PathVariable("id") Long groupBuyingId, Model model, HttpSession session) {
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        GroupBuyingResponse.Detail res = groupBuyingService.getGroupBuyingById(groupBuyingId, loggedInUserId);

        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("groupBuying", res);
        return "groupBuying/detail";
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

    // 성공 시 200 OK
    // 실패(파라미터 타입 불일치 등) 시 400 Bad Request
    @GetMapping
    public String groupBuyingList(
            GroupBuyingRequest.SearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session,
            Model model) {
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        Page<GroupBuyingResponse.GroupBuyings> list = groupBuyingService.getGroupBuyings(condition, pageable, loggedInUserId);
        String categoryName = (condition.category() != null) ? condition.category().name() : null;

        model.addAttribute("category", GroupBuyingCategory.values());
        model.addAttribute("selectedCategory", categoryName);
        model.addAttribute("sorts", GroupBuyingSort.values());
        model.addAttribute("groupBuyings", list);

        return "main";
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
}