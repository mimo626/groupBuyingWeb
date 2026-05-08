package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.service.GroupBuyingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
