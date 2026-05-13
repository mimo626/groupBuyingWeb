package com.example.groupbuyingweb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MyPageViewController {

    @Value("${kakao.local.javascript-key}")
    private String kakaoJavaScriptKey;

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

    @GetMapping("/charge")
    public String chargePage() {
        return "mypage/pointCharge";
    }

    @GetMapping("/gb-detail/{participationId}")
    public String participationDetail(@PathVariable Long participationId) {
        return "mypage/participationDetail";
    }

    @GetMapping("/mypage")
    public String myPage(Model model) {
        model.addAttribute("kakaoJavaScriptKey", kakaoJavaScriptKey);

        return "mypage/myPage";
    }
}