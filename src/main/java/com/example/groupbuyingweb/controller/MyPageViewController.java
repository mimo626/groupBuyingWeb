package com.example.groupbuyingweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MyPageViewController {

    // 내가 개설한 공구 목록 화면
    @GetMapping("/group-buys")
    public String hostedGroupBuyingsPage() {
        return "mypage/hostedGroupBuyings";
    }

    // 내가 참여한 공구 이력 목록 화면
    @GetMapping("/participations")
    public String participatedGroupBuyingsPage() {
        return "mypage/participatedGroupBuyings";
    }
}