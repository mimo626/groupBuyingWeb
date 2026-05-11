package com.example.groupbuyingweb;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
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

    private Member testMember;
    private GroupBuying testGroupBuying;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 1명, 공구 1개 세팅
        Member member = Member.builder()
                .loginId("testUser")
                .password("1234")
                .nickname("테스터")
                .address("상봉동")
                .entX(37.0).entY(127.0)
                .build();
        testMember = memberRepository.saveAndFlush(member);

        GroupBuying groupBuying = GroupBuying.builder()
                .member(testMember)
                .title("상봉동 사과 공구")
                .productName("사과")
                .category(GroupBuyingCategory.FOOD)
                .totalPrice(10000.0)
                .targetQuantity(10)
                .productUrl("url")
                .meetingPlace("상봉역")
                .neighborhoodName("상봉동")
                .entX(37.0).entY(127.0)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();
        testGroupBuying = groupBuyingRepository.saveAndFlush(groupBuying);
    }

    @Test
    @DisplayName("1. 공구 생성 테스트 (Form Data 전송)")
    @Order(1)
    void createGroupBuyingTest() throws Exception {
        // 컨트롤러에 @RequestBody가 없으므로 Form 전송 방식으로 테스트합니다.
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists()) // 응답 data가 있는지 확인
                .andDo(print());
    }

    @Test
    @DisplayName("2. 공구 상세 조회 테스트 (Path Variable)")
    @Order(2)
    void getGroupBuyingDetailTest() throws Exception {
        // @RequestMapping("/group-buying")이 있으므로 경로를 맞춰줍니다.
        mockMvc.perform(get("/group-buying/" + testGroupBuying.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("상봉동 사과 공구"))
                .andDo(print());
    }

    @Test
    @DisplayName("3. 공구 참여 테스트 (Form Data 전송)")
    @Order(3)
    void participateGroupBuyingTest() throws Exception {
        mockMvc.perform(post("/group-buying/" + testGroupBuying.getId() + "/participate")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        // HTML 폼에서 <input name="applyQuantity" value="2"> 로 보낸 것과 똑같은 효과!
                        .param("applyQuantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupBuyingId").value(testGroupBuying.getId()))
                .andDo(print());
    }

    @Test
    @DisplayName("4. 공구 목록 조회 테스트 (Query Parameter)")
    @Order(4)
    void getGroupBuyingsListTest() throws Exception {
        // GET 요청에서 param()은 URL 뒤에 붙는 쿼리스트링(?category=FOOD&page=0)이 됩니다.
        mockMvc.perform(get("/group-buying/list")
                        .param("category", "FOOD")
                        .param("keyword", "사과") // DTO에 keyword 필드가 있다면 테스트
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                // 검색 결과 첫 번째 항목의 제목 확인
                .andExpect(jsonPath("$.data.content[0].title").value("상봉동 사과 공구"))
                .andDo(print());
    }

    @Test
    @DisplayName("5. 자동 상태 변경 테스트: 목표 수량 달성 시 공구 상태가 START로 변경되는지 확인")
    @Order(5)
    void autoUpdateStatusToStartWhenTargetQuantityMetTest() throws Exception {
        // given: testGroupBuying의 목표 수량은 10개입니다. (setUp 메서드 기준)
        int applyQuantity = 10;

        // when: 목표 수량인 10개를 꽉 채워서 참여 요청을 보냅니다.
        mockMvc.perform(post("/group-buying/" + testGroupBuying.getId() + "/participate")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("applyQuantity", String.valueOf(applyQuantity)))
                .andExpect(status().isOk())
                .andDo(print());

        // then: DB에서 해당 공구를 다시 조회하여 상태가 START로 자동 변경되었는지 확인합니다.
        GroupBuying updatedGroupBuying = groupBuyingRepository.findById(testGroupBuying.getId()).orElseThrow();
        assertEquals(GroupBuyingStatus.START, updatedGroupBuying.getStatus(), "목표 수량 달성 시 상태가 START로 변경되어야 합니다.");
    }

    @Test
    @DisplayName("6. 수동 상태 변경 테스트: 상태 업데이트 API 호출 시 상태와 부가 데이터가 저장되는지 확인")
    @Order(6)
    void updateGroupBuyingStatusManuallyTest() throws Exception {
        // given: 상태를 SHIPPING으로 변경하고 운송장 번호를 함께 넘기는 JSON 요청 생성
        // (직전 질문에서 작성했던 컨트롤러가 @RequestBody를 사용하므로 JSON 형식으로 테스트합니다)
        String requestJson = """
            {
                "status": "SHIPPING",
                "trackingNumber": "1234-5678-9012"
            }
            """;

        // when: PATCH 메서드로 상태 변경 API 호출
        mockMvc.perform(patch("/group-buying/" + testGroupBuying.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(print());

        // then: DB에서 다시 조회하여 상태와 운송장 번호가 올바르게 업데이트되었는지 검증
        GroupBuying updatedGroupBuying = groupBuyingRepository.findById(testGroupBuying.getId()).orElseThrow();
        assertEquals(GroupBuyingStatus.SHIPPING, updatedGroupBuying.getStatus());
        assertEquals("1234-5678-9012", updatedGroupBuying.getTrackingNumber(), "운송장 번호가 정상적으로 저장되어야 합니다.");
    }
}