package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingSort;
import com.example.groupbuyingweb.service.GroupBuyingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/group-buyings")
@RequiredArgsConstructor
public class GroupBuyingPagingController {

    private final GroupBuyingService groupBuyingService;

    @Value("${kakao.local.javascript-key}")
    private String kakaoJsKey;

    // 공구 생성 폼 (로그인한 사용자만)
    // 성공 시 200 OK
    // 실패(인증되지 않은 사용자) 시 401 Unauthorized
    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createForm(Model model, Authentication authentication) {
        String memberId = authentication.getName();
        String memberAddress = groupBuyingService.getMemberAddress(memberId);

        model.addAttribute("categories", GroupBuyingCategory.values());
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("memberAddress", memberAddress);
        return "groupbuying/create";
    }

    // 공구 상세 조회 (로그인한 사용자만)
    // 성공 시 200 OK
    // 실패(해당 ID의 공구가 없을 때) 시 404 Not Found
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public String getGroupBuyingById(
            @PathVariable("id") Long groupBuyingId,
            Model model,
            Authentication authentication
    ) {
        String loggedInUserId = authentication.getName();

        System.out.println("getGroupBuyingById loggedInUserId: " + loggedInUserId);

        GroupBuyingResponse.Detail res = groupBuyingService.getGroupBuyingById(groupBuyingId, loggedInUserId);

        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("groupBuying", res);

        return "groupBuying/detail";
    }

    // 수정 폼 화면 요청 (주최자 & 모집중 상태일 때만)
    @GetMapping("/{id}/edit")
    @PreAuthorize("@groupBuyingSecurity.canModifyGroupBuying(authentication, #groupBuyingId)")
    public String editGroupBuyingForm(
            @PathVariable("id") Long groupBuyingId,
            Model model,
            Authentication authentication
    ) {
        String loggedInUserId = authentication.getName();

        GroupBuyingResponse.Detail res = groupBuyingService.getGroupBuyingById(groupBuyingId, loggedInUserId);

        model.addAttribute("categories", GroupBuyingCategory.values());
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("groupBuying", res);

        return "groupBuying/edit";
    }

    // 공구 목록 조회 (로그인한 사용자만)
    // 성공 시 200 OK
    // 실패(파라미터 타입 불일치 등) 시 400 Bad Request
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public String groupBuyingList(
            GroupBuyingRequest.SearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication,
            Model model
    ) {
        String loggedInUserId = authentication.getName();
        System.out.println("groupBuyingList loggedInUserId: " + loggedInUserId);

        Page<GroupBuyingResponse.GroupBuyings> list = groupBuyingService.getGroupBuyings(condition, pageable, loggedInUserId);
        String categoryName = (condition.category() != null) ? condition.category().name() : null;

        model.addAttribute("category", GroupBuyingCategory.values());
        model.addAttribute("selectedCategory", categoryName);
        model.addAttribute("sorts", GroupBuyingSort.values());
        model.addAttribute("groupBuyings", list);

        return "main"; // 템플릿 경로가 "main"이 맞는지 확인 (보통 "groupBuying/list" 등으로 사용)
    }
}