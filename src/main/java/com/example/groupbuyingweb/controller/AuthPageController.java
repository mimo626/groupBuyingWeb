package com.example.groupbuyingweb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @Value("${kakao.local.javascript-key}")
    private String kakaoJavaScriptKey;

    @GetMapping("/auth/login") // 추가: 로그인 화면을 컨트롤러 매핑 주소로 열기 위해 추가
    public String loginPage() {
        return "forward:/auth/login.html"; // 수정: login.html이 static에 있으므로 정적 파일로 forward
    }

    @GetMapping("/auth/signup")
    public String signupPage(Model model) { // 수정: 테스트용 이름 대신 회원가입 화면 반환 역할이 드러나도록 변경
        model.addAttribute("kakaoJavaScriptKey", kakaoJavaScriptKey);
        return "signup";
    }
}
