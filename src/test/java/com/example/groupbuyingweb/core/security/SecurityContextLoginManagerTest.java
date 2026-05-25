package com.example.groupbuyingweb.core.security;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import static org.assertj.core.api.Assertions.assertThat;
class SecurityContextLoginManagerTest {
    private SecurityContextLoginManager securityContextLoginManager;
    @BeforeEach
    void setUp() {
        securityContextLoginManager = new SecurityContextLoginManager();
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    @Test
    @DisplayName("로그인 성공 시 Member.id가 Authentication.getName()에 저장된다")
    void loginSaveMemberIdToAuthenticationName() {
        // given: 실제 로그인 성공 후 전달될 Member.id 역할의 테스트 값이다.
        String memberId = "1111-aaaa";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        // when: SecurityContextLoginManager가 Member.id를 인증 정보로 저장한다.
        securityContextLoginManager.login(memberId, request, response);
        // then: 현재 요청에서 사용하는 SecurityContextHolder에 Authentication이 저장되어야 한다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(memberId);
        // then: 세션에도 SPRING_SECURITY_CONTEXT가 저장되어야 다음 요청에서 인증 정보를 사용할 수 있다.
        HttpSession session = request.getSession(false);
        assertThat(session).isNotNull();
        SecurityContext savedContext = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        assertThat(savedContext).isNotNull();
        assertThat(savedContext.getAuthentication()).isNotNull();
        assertThat(savedContext.getAuthentication().getName()).isEqualTo(memberId);
    }
    @Test
    @DisplayName("로그아웃 시 SecurityContext 인증 정보가 정리된다")
    void logoutClearSecurityContext() {
        // given: 먼저 로그인 상태를 만들어 SecurityContext에 인증 정보를 저장한다.
        String memberId = "1111-aaaa";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        securityContextLoginManager.login(memberId, request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        // when: 로그아웃을 실행한다.
        securityContextLoginManager.logout(request, response);
        // then: SecurityContextHolder의 Authentication이 비워져야 한다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        // then: 세션에 SecurityContext가 남아 있더라도 내부 Authentication은 없어야 한다.
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object savedContext = session.getAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
            );
            if (savedContext != null) {
                SecurityContext securityContext = (SecurityContext) savedContext;
                assertThat(securityContext.getAuthentication()).isNull();
            }
        }
    }
}