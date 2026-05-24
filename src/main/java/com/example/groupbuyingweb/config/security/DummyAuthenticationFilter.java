package com.example.groupbuyingweb.config.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

// 개발 테스트용 임시 필터 (로그인 구현 완료하면 삭제 예정)
public class DummyAuthenticationFilter extends OncePerRequestFilter{
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException{

        // 가짜 유저 객체 생성 (일반 사용자 상태)
        UserDetails dummyUser = User.builder()
                // 임시 사용자: 아이디가 minju일 때의 memberId
                .username("70698642-f42d-4aba-841e-0f4cac4a1eb1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(dummyUser, null, dummyUser.getAuthorities());

        // 시큐리티 컨텍스트에 강제 주입
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}