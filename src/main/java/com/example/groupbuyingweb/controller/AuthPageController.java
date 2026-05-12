package com.example.groupbuyingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @Value("${kakao.local.javascript-key}")
    private String kakaoJavaScriptKey;

    @GetMapping("/auth/signup-test")
    public String signupTest(Model model) {
        model.addAttribute("kakaoJavaScriptKey", kakaoJavaScriptKey);
        return "signupTest";
    }

    @GetMapping("/auth/mypage")
    public String myPageTest(Model model, HttpSession session) {
        model.addAttribute("kakaoJavaScriptKey", kakaoJavaScriptKey);
        String login = (String) session.getAttribute("member_id");
        System.out.println(login);
        return "myPageTest";
    }
}