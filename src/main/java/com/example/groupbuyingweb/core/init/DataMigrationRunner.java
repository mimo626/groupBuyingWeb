package com.example.groupbuyingweb.core.init;

import com.example.groupbuyingweb.domain.entity.h2.UserNearbyAddress;
import com.example.groupbuyingweb.domain.entity.mysql.Member;
import com.example.groupbuyingweb.repository.h2.UserNearbyAddressRepository;
import com.example.groupbuyingweb.repository.mysql.MemberRepository;
import com.example.groupbuyingweb.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final AddressService addressService;
    private final UserNearbyAddressRepository userNearbyAddressRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=========================================================");
        log.info(">> [H2 인메모리] 런타임 시작: MySQL 멤버 주소 스캔 및 카카오 API 기반 H2 데이터 주입 시작");
        log.info("=========================================================");

        // MySQL에서 회원 정보 스캔
        List<Member> allMembers = memberRepository.findAll();

        if (allMembers.isEmpty()) {
            log.info(">> MySQL에 가입된 회원이 없어 마이그레이션을 건너뜁니다.");
            return;
        }

        List<UserNearbyAddress> nearbyAddressesToH2 = new ArrayList<>();

        for (Member member : allMembers) {
            try {
                List<UserNearbyAddress> result = addressService.createNearbyAddresses(
                        member,
                        member.getEntX(),
                        member.getEntY()
                );

                if (result != null) {
                    nearbyAddressesToH2.addAll(result);
                }
            } catch (Exception e) {
                log.error(">> [마이그레이션 건너뛰기] 유저(ID: {}) 주변 주소 변환 중 예외 발생: {}", member.getId(), e.getMessage());
            }
        }

        // 가공된 주변 주소 데이터를 H2에 저장
        if (!nearbyAddressesToH2.isEmpty()) {
            userNearbyAddressRepository.saveAll(nearbyAddressesToH2);
            log.info(">> [마이그레이션 성공] 총 {}명의 유저 기반 {}개의 주변 주소를 H2에 로드 완료했습니다.",
                    allMembers.size(), nearbyAddressesToH2.size());
        }
        log.info("=========================================================");
    }
}