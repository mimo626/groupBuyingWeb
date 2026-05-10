package com.example.groupbuyingweb;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import com.example.groupbuyingweb.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PointServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupBuyingParticipationRepository participationRepository;

    @Autowired
    private GroupBuyingRepository groupBuyingRepository;

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("포인트 충전 테스트")
    void chargePointTest() {
        // 1. test member 생성
        Member member = Member.builder()
                .loginId("charge_tester01")
                .password("password123")
                .nickname("charge")
                .address("서울시 중구 장충동")
                .entX(37.123)
                .entY(127.123)
                .point(5000.0) // 기존 포인트 5000
                .build();
        memberRepository.saveAndFlush(member);
        String memberId = member.getId();

        // 실행 : 3000 포인트 충전
        pointService.chargePoint(memberId, 3000.0);

        // 검증 : 5000 + 3000 = 8000
        assertThat(member.getPoint()).isEqualTo(8000.0);
    }

    @Test
    @DisplayName("포인트 결제 테스트")
    void payPointTest() {
        // 1.a test member 생성
        Member member = Member.builder()
                .loginId("charge_tester01")
                .password("password123")
                .nickname("charge")
                .address("서울시 중구 장충동")
                .entX(37.123)
                .entY(127.123)
                .point(10000.0) // 기존 포인트 5000
                .build();
        memberRepository.saveAndFlush(member);
        String memberId = member.getId();

        Member organizer = Member.builder()
                .loginId("charge_tester02")
                .password("password123")
                .nickname("org_테스터")
                .address("서울시 중구 장충동")
                .entX(37.124)
                .entY(127.124)
                .point(5000.0)
                .build();
        memberRepository.saveAndFlush(organizer);

        // 1.b test groupbuying 생성
        GroupBuying groupBuying = GroupBuying.builder()
                .member(organizer)
                .category(GroupBuyingCategory.FOOD)
                .title("테스트용 맛있는 사과 공구")
                .productName("충주 사과 10kg")
                .totalPrice(20000.0)
                .targetQuantity(10)
                .productUrl("httpsss://example.com/apple")
                .productContent("사과 10알이에요요용")
                .neighborhoodName("장충동")
                .meetingPlace("장충단공원 앞")
                .entX(37.560)
                .entY(127.000)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();

        groupBuyingRepository.saveAndFlush(groupBuying);
        long groupBuyingId = groupBuying.getId();
        // 1.c test GBP 생성
        GroupBuyingParticipation participation = GroupBuyingParticipation.builder()
                .role(UserRole.PARTICIPANT)
                .groupBuying(groupBuying)
                .member(member)
                .applyQuantity(2)
                .paidPoint(0.0)
                .paymentStatus(PaymentStatus.Incomplete)
                .build();
        participationRepository.saveAndFlush(participation);

        // 2. 목표 포인트 20000, 목표 수량 10개 (개당 2000) -> 2개 4000 지불
        pointService.payPoint(memberId, groupBuyingId, 20000.0, 10);

        // 3. 검증
        assertThat(member.getPoint()).isEqualTo(6000.0); // 10000 - 4000
        assertThat(participation.getPaidPoint()).isEqualTo(4000.0); // 4000 시스템 보관
    }

    @Test
    @DisplayName("주최자 정산 테스트")
    void settlePointTest() {

        Member member1 = Member.builder()
                .loginId("settle_participant_tester01")
                .password("password123")
                .nickname("settle11")
                .address("서울시 중구 장충동")
                .entX(37.123)
                .entY(127.123)
                .point(0.0)
                .build();

        Member member2 = Member.builder()
                .loginId("settle_participant_tester02")
                .password("password123")
                .nickname("settle22")
                .address("서울시 중구 장충동")
                .entX(37.125)
                .entY(127.123)
                .point(0.0)
                .build();

        Member organizerMember = Member.builder()
                .loginId("settle_organizer_tester03")
                .password("password123")
                .nickname("주최자입니다")
                .address("서울시 중구 장충동")
                .entX(37.121)
                .entY(127.123)
                .point(100.0) // 기존 포인트 100
                .build();
        memberRepository.saveAllAndFlush(List.of(member1,member2,organizerMember));

        GroupBuying groupBuying = GroupBuying.builder()
                .member(organizerMember)
                .category(GroupBuyingCategory.FOOD)
                .title("테스트용 맛있는 사과 공구")
                .productName("충주 사과 10kg")
                .totalPrice(20000.0)
                .targetQuantity(10)
                .productUrl("httpsss://example.com/apple")
                .productContent("사과 10알이에요요용")
                .neighborhoodName("장충동")
                .meetingPlace("장충단공원 앞")
                .entX(37.560)
                .entY(127.000)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();
        groupBuyingRepository.saveAndFlush(groupBuying);
        long groupBuyingId = groupBuying.getId();

        GroupBuyingParticipation organizerParticipation = GroupBuyingParticipation.builder()
                .paidPoint(10000.0)
                .member(organizerMember)
                .role(UserRole.ORGANIZER)
                .groupBuying(groupBuying)
                .applyQuantity(5)
                .paymentStatus(PaymentStatus.Incomplete)
                .build();

        GroupBuyingParticipation participation1 = GroupBuyingParticipation.builder()
                .paidPoint(4000.0)
                .member(organizerMember)
                .role(UserRole.PARTICIPANT)
                .groupBuying(groupBuying)
                .applyQuantity(2)
                .paymentStatus(PaymentStatus.Incomplete)
                .build();
        GroupBuyingParticipation participation2 = GroupBuyingParticipation.builder()
                .paidPoint(6000.0)
                .member(organizerMember)
                .role(UserRole.PARTICIPANT)
                .groupBuying(groupBuying)
                .applyQuantity(3)
                .paymentStatus(PaymentStatus.Incomplete)
                .build();
        participationRepository.saveAllAndFlush(List.of(participation1,participation2,organizerParticipation));

        pointService.settlePoint(groupBuyingId);

        // 검증
        assertThat(organizerMember.getPoint()).isEqualTo(20100.0); // 잔고 100 + 정산 20000 포인트
        assertThat(participation1.getPaidPoint()).isEqualTo(0.0);   // 시스템 보관금 초기화됨
        assertThat(participation2.getPaidPoint()).isEqualTo(0.0);
        assertThat(organizerParticipation.getPaidPoint()).isEqualTo(0.0);

        assertThat(organizerParticipation.getPaymentStatus()).isEqualTo(PaymentStatus.Complete);
    }
}
