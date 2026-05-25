package com.example.groupbuyingweb.core.security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import java.util.Collections;
@Component
public class SecurityContextLoginManager {
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    public void login(
            String memberId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 추가: 로그인 성공한 사용자의 Member.id를 기준으로 Authentication 객체를 만든다.
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                memberId,
                null,
                Collections.emptyList()
        );
        // 추가: 빈 SecurityContext를 만들고 Authentication을 저장한다.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        // 추가: 현재 요청 흐름에서 Authentication을 사용할 수 있도록 SecurityContextHolder에 저장한다.
        SecurityContextHolder.setContext(context);
        // 추가: 다음 요청에서도 인증 정보가 유지되도록 SecurityContext를 세션에 저장한다.
        securityContextRepository.saveContext(context, request, response);
    }
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 추가: 현재 요청 흐름에 남아 있는 SecurityContext를 비운다.
        SecurityContextHolder.clearContext();
        // 추가: 세션에 저장된 Spring Security 인증 정보도 제거한다.
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        }
        // 추가: 비어 있는 SecurityContext를 저장해 이후 요청에서 인증 정보가 남지 않도록 한다.
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        securityContextRepository.saveContext(emptyContext, request, response);
    }
}