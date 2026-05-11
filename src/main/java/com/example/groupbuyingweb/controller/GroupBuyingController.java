package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.service.GroupBuyingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/group-buying")
public class GroupBuyingController {
    @Autowired
    private GroupBuyingService groupBuyingService;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("categories", GroupBuyingCategory.values());

        return "groupbuying/create";
    }

    @PostMapping("/create")
    public String addGroupBuying(@Valid @ModelAttribute GroupBuyingRequest.Create groupBuyingRequest) {
        String memberId = "testUser"; // 테스트용 임시 ID

        System.out.println(groupBuyingRequest);

        GroupBuyingResponse.Create res = groupBuyingService.addGroupBuying(groupBuyingRequest, memberId);

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

        Page<GroupBuyingResponse.List> list = groupBuyingService.getGroupBuyings(condition, pageable);
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
