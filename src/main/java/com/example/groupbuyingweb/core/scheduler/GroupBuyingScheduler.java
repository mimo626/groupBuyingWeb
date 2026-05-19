package com.example.groupbuyingweb.core.scheduler;

import com.example.groupbuyingweb.domain.entity.mysql.GroupBuying;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingRepository;
import com.example.groupbuyingweb.service.GroupBuyingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupBuyingScheduler {

    private final GroupBuyingRepository groupBuyingRepository;
    private final GroupBuyingService groupBuyingService; // processStatusChange가 있는 서비스

    /**
     * 매 1분마다 실행되며 마감 기한이 지난 공구의 상태를 CLOSED로 일괄 변경합니다.
     * 크론 표현식 설명: 초 분 시 일 월 요일 (여기서는 매 분 0초마다 실행)
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void closeExpiredGroupBuyings() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 아직 RECRUITING(모집중) 상태인데 마감 시간(deadline)이 현재 시간보다 과거인 공구들을 찾습니다.
        List<GroupBuying> expiredList = groupBuyingRepository.findAllByStatusAndDeadlineBefore(
                GroupBuyingStatus.RECRUITING, now
        );

        if (expiredList.isEmpty()) {
            return;
        }

        System.out.println("[스케줄러 실행] 마감된 공구 총 " + expiredList.size() + "건 발견. 순차적 CLOSED 처리 시작.");

        // 2. 발견된 마감 공구들을 순회하며 상태를 안전하게 변경합니다.
        for (GroupBuying gb : expiredList) {
            try {
                // 기존에 만들어둔 로직을 재활용하여 상태 변경 및 예외 처리가 적용되도록 합니다.
                groupBuyingService.processStatusChange(gb.getId(), GroupBuyingStatus.CLOSED, null, null);
            } catch (Exception e) {
                // 특정 공구 처리 중 에러가 나더라도 다음 공구는 계속 처리되도록 예외를 잡아줍니다.
                System.err.println("공구 ID " + gb.getId() + " 마감 처리 중 오류 발생: " + e.getMessage());
            }
        }
    }
}