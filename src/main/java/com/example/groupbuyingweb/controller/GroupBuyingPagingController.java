package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingSort;
import com.example.groupbuyingweb.service.GroupBuyingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/group-buyings")
public class GroupBuyingPagingController {
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

    // 수정 폼 화면 요청
    @GetMapping("/{id}/edit")
    public String editGroupBuyingForm(@PathVariable("id") Long groupBuyingId, Model model, HttpSession session) {
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        // (이때 서비스 단에서 loggedInUserId가 주최자가 맞는지 권한 체크 로직이 들어가면 좋습니다)
        GroupBuyingResponse.Detail res = groupBuyingService.getGroupBuyingById(groupBuyingId, loggedInUserId);
        model.addAttribute("categories", GroupBuyingCategory.values());
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("groupBuying", res);

        return "groupBuying/edit";
    }



    // 성공 시 200 OK
    // 실패(파라미터 타입 불일치 등) 시 400 Bad Request
    @GetMapping()
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
}