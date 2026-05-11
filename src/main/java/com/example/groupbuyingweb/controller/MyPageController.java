package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.dto.request.MyPageRequest;
import com.example.groupbuyingweb.domain.dto.response.MyPageResponse;
import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.service.AddressService;
import com.example.groupbuyingweb.service.MyPageService;
import com.example.groupbuyingweb.service.PointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    private MyPageService myPageService;




    @ResponseBody
    @GetMapping("/profile")
    public ApiResponse<?> getProfile(HttpSession session){
        // 이대로 authService에 로그인 체크로 넣는것도 고려
        String memberId = (String) session.getAttribute("loginUserId");
        if (memberId == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        //
        MyPageResponse.Profile dto = myPageService.getProfile(memberId);
        return ApiResponse.success(dto);
    }

    @ResponseBody
    @GetMapping("/neighborhood")
    public ApiResponse<?> getNeighborhood(HttpSession session){
        String memberId = (String) session.getAttribute("loginUserId");
        if (memberId == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        MyPageResponse.Neighborhood dto = myPageService.getNeighborhood(memberId);
        return ApiResponse.success(dto);
    }

    @ResponseBody
    @PatchMapping("/neighborhood")
    public ApiResponse<MyPageResponse.Neighborhood> patchNeighborhood(MyPageRequest.UpdateNeighborhood request,HttpSession session){
        String memberId = (String) session.getAttribute("loginUserId");
        if (memberId == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        MyPageResponse.Neighborhood dto = myPageService.patchNeighborhood(memberId, request);
        return ApiResponse.success(dto); // 수정 결과에 수정한 주변 동 정보 데이터가 들어가야하는지는 고민 필요
    }





}
