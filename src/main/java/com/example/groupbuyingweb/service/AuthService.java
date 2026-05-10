package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.dto.request.AuthRequest;
import com.example.groupbuyingweb.domain.dto.response.AuthResponse;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.repository.MemberRepository;
import com.example.groupbuyingweb.repository.UserNearbyAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}