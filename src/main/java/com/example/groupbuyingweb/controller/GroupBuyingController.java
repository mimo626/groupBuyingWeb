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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/group-buying")
public class GroupBuyingController {
    @Autowired
    private GroupBuyingService groupBuyingService;
    @Autowired
    private LoginSessionManager loginSessionManager;

    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("categories", GroupBuyingCategory.values());

        return "groupbuying/create";
    }

    @PostMapping("/create")
    public String addGroupBuying(
            @Valid @ModelAttribute GroupBuyingRequest.Create request,
            @RequestParam("images") List<MultipartFile> images,
            HttpSession session) { // 폼의 name="images" 와 매핑

        String memberId = loginSessionManager.requireLoginUserId(session);

        GroupBuyingResponse.Create res = groupBuyingService.addGroupBuying(request, images, memberId);
        return "redirect:/group-buying/" + res.groupBuyingId();
    }
    @GetMapping("/{id}")
    public String getGroupBuyingById(@PathVariable("id") Long groupBuyingId, Model model) {
        GroupBuyingResponse.Detail res = groupBuyingService.getGroupBuyingById(groupBuyingId);
        model.addAttribute("groupBuying", res);return "groupBuying/detail";
    }

    @PostMapping("/{id}/participate")
    public ApiResponse<GroupBuyingResponse.Participate> participateGroupBuying(@PathVariable("id") Long groupBuyingId,
                                                                               @Valid GroupBuyingRequest.Participate groupBuyingRequest) {
        String memberId = "";
        GroupBuyingResponse.Participate res = groupBuyingService.participateGroupBuying(
                groupBuyingRequest.applyQuantity(),
                memberId,
                groupBuyingId);
        return ApiResponse.success(res);
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
