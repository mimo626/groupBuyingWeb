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
    private final H2TransactionHelper h2TransactionHelper; // 💡 트랜잭션 분리를 위한 헬퍼 컴포넌트 주입

    // 💡 여기에 있던 @Transactional("h2TransactionManager")을 과감히 제거합니다!
    @Override
    public void run(ApplicationArguments args) {
        log.info("=========================================================");
        log.info(">> [H2 인메모리] 런타임 시작: MySQL 멤버 주소 스캔 및 카카오 API 기반 H2 데이터 주입 시작");
        log.info("=========================================================");

        // 1. MySQL에서 현재 가입된 모든 회원 정보 스캔 (트랜잭션 없이 단순 조회)
        List<Member> allMembers = memberRepository.findAll();

        if (allMembers.isEmpty()) {
            log.info(">> MySQL에 가입된 회원이 없어 마이그레이션을 건너뜁니다.");
            return;
        }

        List<UserNearbyAddress> nearbyAddressesToH2 = new ArrayList<>();

        // 2. 각 회원 정보를 순회하며 카카오 API 호출 및 데이터 가공 (트랜잭션이 없으므로 지연 우려 없음)
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
                // 카카오 API 호출 도중 타임아웃이나 에러가 나더라도 다음 유저로 넘어가도록 방어적 코딩
                log.error(">> [마이그레이션 건너뛰기] 유저(ID: {}) 주변 주소 변환 중 예외 발생: {}", member.getId(), e.getMessage());
            }
        }

        // 3. 가공이 '완전히 끝난' 데이터 리스트를 H2 인메모리에 '짧은 트랜잭션'으로 한 번에 저장
        if (!nearbyAddressesToH2.isEmpty()) {
            h2TransactionHelper.saveAllToH2(nearbyAddressesToH2);
            log.info(">> [마이그레이션 성공] 총 {}명의 유저 기반 {}개의 주변 주소를 H2에 로드 완료했습니다.",
                    allMembers.size(), nearbyAddressesToH2.size());
        }
        log.info("=========================================================");
    }
}

// 💡 내부 클래스 혹은 별도 파일로 분리하여 H2 트랜잭션 프록시가 정상 작동하도록 유도합니다.
@Component
@RequiredArgsConstructor
class H2TransactionHelper {
    private final UserNearbyAddressRepository userNearbyAddressRepository;

    @Transactional("h2TransactionManager") // 딱 이 메서드가 실행되는 순간에만 H2 커넥션을 획득하고 즉시 반납합니다.
    public void saveAllToH2(List<UserNearbyAddress> list) {
        userNearbyAddressRepository.saveAll(list);
    }
}