package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.core.security.SecurityContextLoginManager;
import com.example.groupbuyingweb.domain.dto.request.AuthRequest;
import com.example.groupbuyingweb.domain.dto.response.AuthResponse;
import com.example.groupbuyingweb.domain.entity.mysql.Member;
import com.example.groupbuyingweb.domain.entity.h2.UserNearbyAddress;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.repository.mysql.MemberRepository;
import com.example.groupbuyingweb.repository.h2.UserNearbyAddressRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.groupbuyingweb.core.session.LoginSessionManager;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final UserNearbyAddressRepository userNearbyAddressRepository;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionManager loginSessionManager;
    private final SecurityContextLoginManager securityContextLoginManager;

    public AuthResponse.DuplicateCheck checkLoginId(String loginId) {
        boolean exists = memberRepository.existsByLoginId(loginId);
        return new AuthResponse.DuplicateCheck(!exists);
    }

    public AuthResponse.DuplicateCheck checkNickname(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);
        return new AuthResponse.DuplicateCheck(!exists);
    }

    @Transactional
    public AuthResponse.SignupResult signup(AuthRequest.Signup request) {
        validatePasswordConfirm(request.password(), request.passwordConfirm());
        validateTermsAgreed(request.termsAgreed());
        validateDuplicateLoginId(request.loginId());
        validateDuplicateNickname(request.nickname());

        Member member = Member.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .address(request.address())
                .entX(request.entX())
                .entY(request.entY())
                .build();

        Member savedMember = memberRepository.save(member);

        List<UserNearbyAddress> nearbyAddresses =
                addressService.createNearbyAddresses(
                        savedMember,
                        request.entX(),
                        request.entY()
                );

        userNearbyAddressRepository.saveAll(nearbyAddresses);

        return new AuthResponse.SignupResult(savedMember.getId());
    }

    private void validatePasswordConfirm(
            String password,
            String passwordConfirm
    ) {
        if (!Objects.equals(password, passwordConfirm)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    private void validateTermsAgreed(Boolean termsAgreed) {
        if (!Boolean.TRUE.equals(termsAgreed)) {
            throw new BusinessException(ErrorCode.TERMS_NOT_AGREED);
        }
    }

    private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATED_LOGIN_ID);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
        }
    }

    public AuthResponse.LoginResult login(
            AuthRequest.Login request,
            HttpSession session,
            HttpServletRequest httpRequest, // 추가: SecurityContext 저장에 필요한 요청 객체이다.
            HttpServletResponse httpResponse // 추가: SecurityContext 저장에 필요한 응답 객체이다.
    ) {
        // 1. 로그인 아이디로 회원 조회
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        // 2. 입력 비밀번호와 저장된 해시 비밀번호 비교
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        // 3. 로그인 성공 시 세션에 Member.id 저장
        loginSessionManager.login(session, member.getId());
        // 추가: 로그인 성공 정보를 Spring Security의 SecurityContext에도 저장한다.
        securityContextLoginManager.login(member.getId(), httpRequest, httpResponse);
        // 4. 로그인 성공 응답 DTO 반환
        return new AuthResponse.LoginResult(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getAddress(),
                member.getRadius(),
                member.getEntX(),
                member.getEntY()
        );
    }


    public void logout(
            HttpSession session,
            HttpServletRequest httpRequest, // 추가: SecurityContext 정리에 필요한 요청 객체이다.
            HttpServletResponse httpResponse // 추가: SecurityContext 정리에 필요한 응답 객체이다.
    ) {
        loginSessionManager.requireLoginUserId(session);
        // 추가: Spring Security 인증 정보를 먼저 정리한다.
        securityContextLoginManager.logout(httpRequest, httpResponse);
        // 수정: Spring Security 인증 정보 정리 후 기존 세션을 무효화한다.
        loginSessionManager.logout(session);
    }

}