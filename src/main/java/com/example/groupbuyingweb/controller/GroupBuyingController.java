package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.service.GroupBuyingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/group-buying")
public class GroupBuyingController {
    @Autowired
    private GroupBuyingService groupBuyingService;

    @PostMapping("/create")
    @ResponseBody
    public ApiResponse<GroupBuyingResponse.Create> addGroupBuying(@Valid GroupBuyingRequest.Create groupBuyingRequest) {
        // DB에 있는 첫 번째 유저의 ID를 가져오거나, 콘솔에 찍힌 ID를 직접 넣으세요.
        String memberId = "";
        GroupBuyingResponse.Create res = groupBuyingService.addGroupBuying(groupBuyingRequest, memberId);
        return ApiResponse.success(res);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ApiResponse<GroupBuyingResponse.Detail> getGroupBuyingById(@PathVariable("id") Long groupBuyingId) {
        GroupBuyingResponse.Detail res = groupBuyingService.getGroupBuyingById(groupBuyingId);
        return ApiResponse.success(res);
    }

    @PostMapping("/{id}/participate")
    @ResponseBody
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
    @ResponseBody
    public ApiResponse<Page<GroupBuyingResponse.List>> getGroupBuyings(
            GroupBuyingRequest.SearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(groupBuyingService.getGroupBuyings(condition, pageable));
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
