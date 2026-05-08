package com.example.groupbuyingweb;

import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@Commit
class MemberTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("멤버 생성 및 저장 테스트")
    void createMemberTest() {
        // 1. Given: 저장할 멤버 객체 생성
        Member member = Member.builder()
                .loginId("tester01")
                .password("password123")
                .nickname("도토리대장")
                .address("서울시 중랑구 상봉동")
                .latitude(37.595)
                .longitude(127.086)
                // point, acornExp, radius는 PrePersist에서 처리되므로 비워둠
                .build();

        // 2. When: 레포지토리를 통해 저장
        Member savedMember = memberRepository.saveAndFlush(member);

        // 3. Then: 검증
        // ID가 UUID 형식으로 잘 생성되었는지 확인
        assertNotNull(savedMember.getId());
        assertThat(savedMember.getLoginId()).isEqualTo("tester01");

        // @PrePersist 설정값들이 잘 들어갔는지 확인
        assertThat(savedMember.getRadius()).isEqualTo(1000); // 기본값 1000
        assertThat(savedMember.getPoint()).isEqualTo(0.0);    // 기본값 0.0

        // @CreatedDate(Auditing) 작동 확인
        assertNotNull(savedMember.getCreatedAt());

        System.out.println("생성된 멤버 ID: " + savedMember.getId());
    }
}