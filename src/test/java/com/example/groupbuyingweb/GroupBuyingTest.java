package com.example.groupbuyingweb;

import com.example.groupbuyingweb.domain.entity.mysql.GroupBuying;
import com.example.groupbuyingweb.domain.entity.mysql.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.mysql.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.mysql.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Transactional
class GroupBuyingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupBuyingRepository groupBuyingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupBuyingParticipationRepository groupBuyingParticipationRepository;

    // 전역 변수로 선언해두어야 @Test 메서드들에서 꺼내 쓸 수 있습니다.
    private Member testMember;
    private GroupBuying testGroupBuying;

    @BeforeEach
    void setUp() {
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
                .point(100.0)
                .build();
        memberRepository.saveAllAndFlush(List.of(member1, member2, organizerMember));

        // ✨ 전역 변수 testMember에 주최자를 할당해줍니다. (테스트에서 사용하기 위함)
        testMember = organizerMember;

        GroupBuying groupBuying = GroupBuying.builder()
                .member(organizerMember)
                .category(GroupBuyingCategory.FOOD)
                .title("테스트용 맛있는 사과 공구")
                .productName("충주 사과 10kg")
                .totalPrice(20000.0)
                .targetQuantity(10) // 목표 수량 10개
                .productUrl("httpsss://example.com/apple")
                .productContent("사과 10알이에요요용")
                .neighborhoodName("장충동")
                .meetingPlace("장충단공원 앞")
                .entX(37.560)
                .entY(127.000)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();
        groupBuyingRepository.saveAndFlush(groupBuying);

        // ✨ 전역 변수 testGroupBuying에 생성된 공구를 할당해줍니다.
        testGroupBuying = groupBuying;

        // ✨ 목표 달성 테스트를 위해 초기 신청 수량을 줄였습니다. (총합 6개, 잔여 4개)
        GroupBuyingParticipation organizerParticipation = GroupBuyingParticipation.builder()
                .paidPoint(4000.0)
                .member(organizerMember)
                .role(UserRole.ORGANIZER)
                .groupBuying(groupBuying)
                .applyQuantity(2) // 5 -> 2 로 수정
                .paymentStatus(PaymentStatus.Incomplete)
                .build();

        GroupBuyingParticipation participation1 = GroupBuyingParticipation.builder()
                .paidPoint(4000.0)
                .member(member1) // ✨ member1 로 수정
                .role(UserRole.PARTICIPANT)
                .groupBuying(groupBuying)
                .applyQuantity(2)
                .paymentStatus(PaymentStatus.Incomplete)
                .build();

        GroupBuyingParticipation participation2 = GroupBuyingParticipation.builder()
                .paidPoint(4000.0)
                .member(member2) // ✨ member2 로 수정
                .role(UserRole.PARTICIPANT)
                .groupBuying(groupBuying)
                .applyQuantity(2) // 3 -> 2 로 수정
                .paymentStatus(PaymentStatus.Incomplete)
                .build();

        groupBuyingParticipationRepository.saveAllAndFlush(List.of(participation1, participation2, organizerParticipation));
    }

    @Test
    @DisplayName("1. 공구 생성 테스트 (Form Data 전송)")
    @Order(1)
    void createGroupBuyingTest() throws Exception {
        mockMvc.perform(post("/group-buying/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "새로운 공구")
                        .param("productName", "배")
                        .param("category", "FOOD")
                        .param("totalPrice", "20000")
                        .param("targetQuantity", "5")
                        .param("organizerQuantity", "1")
                        .param("entX", "126.94621910280937")
                        .param("entY", "37.547261903037")
                        .param("meetingPlace", "우리집 앞")
                        .param("productUrl", "http://apple.com")
                        .param("deadline", LocalDateTime.now().plusDays(5).toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("2. 공구 상세 조회 테스트 (Path Variable)")
    @Order(2)
    void getGroupBuyingDetailTest() throws Exception {
        mockMvc.perform(get("/group-buying/" + testGroupBuying.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                // ✨ 검증 데이터 제목 수정
                .andExpect(jsonPath("$.data.title").value("테스트용 맛있는 사과 공구"));
    }

    @Test
    @DisplayName("3. 공구 참여 테스트 (Form Data 전송)")
    @Order(3)
    void participateGroupBuyingTest() throws Exception {
        // 기존 6개 + 추가 2개 = 총 8개 (목표 10개 미달성이므로 정상 참여됨)
        mockMvc.perform(post("/group-buying/" + testGroupBuying.getId() + "/participate")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("applyQuantity", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupBuyingId").value(testGroupBuying.getId()));
    }

    @Test
    @DisplayName("4. 공구 목록 조회 테스트 (Query Parameter)")
    @Order(4)
    void getGroupBuyingsListTest() throws Exception {
        mockMvc.perform(get("/group-buying/list")
                        .param("category", "FOOD")
                        .param("keyword", "사과")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andDo(print())
                .andExpect(status().isOk())
                // ✨ 검증 데이터 제목 수정
                .andExpect(jsonPath("$.data.content[0].title").value("테스트용 맛있는 사과 공구"));
    }

    @Test
    @DisplayName("5. 자동 상태 변경 테스트: 목표 수량 달성 시 공구 상태가 START로 변경되는지 확인")
    @Order(5)
    void autoUpdateStatusToStartWhenTargetQuantityMetTest() throws Exception {
        // given: testGroupBuying의 목표 수량은 10개. setUp에서 6개를 이미 채웠음.
        // when: 남은 4개를 마저 꽉 채워서 참여 요청을 보냄.
        int applyQuantity = 4;

        mockMvc.perform(post("/group-buying/" + testGroupBuying.getId() + "/participate")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("applyQuantity", String.valueOf(applyQuantity)))
                .andDo(print())
                .andExpect(status().isOk());

        // then: 상태가 START로 자동 변경되었는지 확인
        GroupBuying updatedGroupBuying = groupBuyingRepository.findById(testGroupBuying.getId()).orElseThrow();
        assertEquals(GroupBuyingStatus.START, updatedGroupBuying.getStatus(), "목표 수량 달성 시 상태가 START로 변경되어야 합니다.");
    }

    @Test
    @DisplayName("6. 수동 상태 변경 테스트: SETTLING으로 변경 (NullPointerException 해결 확인)")
    @Order(6)
    void updateGroupBuyingStatusManuallyTest() throws Exception {
        String requestJson = """
            {
                "status": "SETTLING",
                "trackingNumber": "1234-5678-9012"
            }
            """;

        mockMvc.perform(patch("/group-buying/" + testGroupBuying.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk());

        GroupBuying updatedGroupBuying = groupBuyingRepository.findById(testGroupBuying.getId()).orElseThrow();
        assertEquals(GroupBuyingStatus.SETTLING, updatedGroupBuying.getStatus());
    }
}