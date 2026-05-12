package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
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
@RequestMapping("/group-buying")
public class GroupBuyingController {
    @Autowired
    private GroupBuyingService groupBuyingService;
    @Autowired
    private LoginSessionManager loginSessionManager;

    @Value("${kakao.local.javascript-key}")
    private String kakaoJsKey;

    @GetMapping("/create")
    public String createForm(Model model,
                HttpSession session) {
        String memberId = loginSessionManager.requireLoginUserId(session);
        String memberAddress = groupBuyingService.getMemberAddress(memberId);
        model.addAttribute("categories", GroupBuyingCategory.values());
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("memberAddress", memberAddress);
        return "groupbuying/create";
    }

    @PostMapping("/create")
    public String addGroupBuying(
            @Valid @ModelAttribute GroupBuyingRequest.Create request,
            @RequestParam("images") List<MultipartFile> images,
            HttpSession session) { // 폼의 name="images" 와 매핑
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        System.out.println(request.toString());
        GroupBuyingResponse.Create res = groupBuyingService.addGroupBuying(request, images, loggedInUserId);
        return "redirect:/group-buying/" + res.groupBuyingId();
    }
    @GetMapping("/{id}")
    public String getGroupBuyingById(@PathVariable("id") Long groupBuyingId, Model model, HttpSession session) {
        String loggedInUserId = loginSessionManager.requireLoginUserId(session);

        GroupBuyingResponse.Detail res = groupBuyingService.getGroupBuyingById(groupBuyingId, loggedInUserId);

        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("groupBuying", res);
        return "groupBuying/detail";
    }

    @PostMapping("/{id}/participate")
    public String participateGroupBuying(@PathVariable("id") Long groupBuyingId,
                                         @Valid GroupBuyingRequest.Participate groupBuyingRequest,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {

        String loggedInUserId = loginSessionManager.requireLoginUserId(session);
        GroupBuyingResponse.Participate res = groupBuyingService.participateGroupBuying(
                groupBuyingRequest.applyQuantity(),
                loggedInUserId,
                groupBuyingId);

        // 화면에서 성공 alert을 띄우기 위한 1회성 플래그 전달
        redirectAttributes.addFlashAttribute("participateSuccess", true);

        // 리다이렉트 경로
        return "redirect:/group-buying/" + groupBuyingId;
    }

    @GetMapping("/list")
    public String groupBuyingList(
            GroupBuyingRequest.SearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<GroupBuyingResponse.GroupBuyings> list = groupBuyingService.getGroupBuyings(condition, pageable);
        model.addAttribute("groupBuyings", list);
        return "groupbuying/list"; // 타임리프 템플릿 경로 (templates/groupbuying/list.html)
    }


    @PatchMapping("/{id}/status")
    public ApiResponse<GroupBuyingResponse.UpdateStatus> updateGroupBuyingStatus(
            @PathVariable("id") Long groupBuyingId,
            @RequestBody GroupBuyingRequest.UpdateStatus request
    ) {
        GroupBuyingResponse.UpdateStatus res = groupBuyingService.updateStatusFromRequest(groupBuyingId, request);
        return ApiResponse.success(res);
    }

}
