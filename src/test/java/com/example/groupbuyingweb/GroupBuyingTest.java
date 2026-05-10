package com.example.groupbuyingweb;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
//@Commit
class GroupBuyingTest {

    @Autowired
    private GroupBuyingRepository groupBuyingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("공동구매 게시글 생성 및 저장 테스트")
    void createGroupBuyingTest() {
        // 1. Given: 공구 작성자(Member) 먼저 생성 및 저장
        Member member = Member.builder()
                .loginId("host01")
                .password("password123")
                .nickname("사과킬러")
                .address("서울시 중랑구 상봉동")
                .entX(37.595)
                .entY(127.086)
                .build();
        Member savedMember = memberRepository.saveAndFlush(member);

        // 저장할 공구 객체 생성 (Member 객체 주입)
        GroupBuying groupBuying = GroupBuying.builder()
                .member(savedMember) // FK 연관관계 매핑
                .title("상봉동 꿀사과 10kg 같이 사요!")
                .productName("청송 꿀사과 10kg")
                .category(GroupBuyingCategory.FOOD)
                .totalPrice(50000.0)
                .targetQuantity(10)
                .productUrl("https://example.com/apple")
                .productImageUrl("https://example.com/apple.jpg")
                .productContent("진짜 맛있는 사과입니다. 1인당 1kg씩 나눠 가져요!")
                .neighborhoodName("상봉동")
                .meetingPlace("상봉역 3번 출구 앞")
                .entX(37.596) // 사용자 엔티티의 entX, entY와 맞춰서 작성
                .entY(127.087)
                .deadline(LocalDateTime.now().plusDays(3)) // 3일 뒤 마감 설정
                // viewCount, status는 @PrePersist에서 처리되므로 비워둠
                .build();

        // 2. When: 레포지토리를 통해 저장
        GroupBuying savedGroupBuying = groupBuyingRepository.saveAndFlush(groupBuying);

        // 3. Then: 검증
        // ID가 잘 생성되었는지 확인
        assertNotNull(savedGroupBuying.getId());
        assertThat(savedGroupBuying.getTitle()).isEqualTo("상봉동 꿀사과 10kg 같이 사요!");

        // FK 매핑 검증: 게시글의 작성자가 처음에 만든 멤버가 맞는지 확인
        assertThat(savedGroupBuying.getMember().getId()).isEqualTo(savedMember.getId());

        // @PrePersist 설정값들이 잘 들어갔는지 확인
        assertThat(savedGroupBuying.getStatus()).isEqualTo(GroupBuyingStatus.RECRUITING); // 기본값: RECRUITING
        assertThat(savedGroupBuying.getViewCount()).isEqualTo(0); // 기본값: 0

        // @CreationTimestamp(Auditing) 작동 확인
        assertNotNull(savedGroupBuying.getCreatedAt());

        System.out.println("생성된 공구 게시글 ID: " + savedGroupBuying.getId());
    }
}