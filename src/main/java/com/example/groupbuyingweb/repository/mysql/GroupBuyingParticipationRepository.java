package com.example.groupbuyingweb.repository.mysql;

import com.example.groupbuyingweb.domain.entity.mysql.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.mysql.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyingParticipationRepository extends JpaRepository<GroupBuyingParticipation, Long> {

//    @Query("SELECT SUM(g.paidPoint) FROM GroupBuyingParticipation g WHERE g.groupBuying.id = :gbpId")
//    Double sumSettlePay(@Param("gbpId") long groupBuyingId);

    @Query("SELECT g.member FROM GroupBuyingParticipation g WHERE g.groupBuying.id = :groupBuyingId AND g.role = :role")
    Member findMemberByGroupBuyingIdAndRole(long groupBuyingId, UserRole role);

    GroupBuyingParticipation findByGroupBuyingIdAndMemberId(long groupBuyingId, String memberId);

    List<GroupBuyingParticipation> findAllByGroupBuyingId(long groupBuyingId);

    boolean existsByGroupBuyingIdAndRole(Long groupBuyingId, UserRole role);

    boolean existsByGroupBuyingIdAndMemberIdAndRole(Long groupBuyingId, String memberId, UserRole role);
    @Query("SELECT SUM(g.applyQuantity) FROM GroupBuyingParticipation g WHERE g.groupBuying.id = :gbpId")
    Integer sumQuantity(@Param("gbpId") long groupBuyingId);

    // 현재 로그인한 사용자가 참여한 공구 이력을 공구 진행 상태 기준으로 조회한다.
    // role=PARTICIPANT일 때 사용한다.
    // id 최신순으로 조회한다.
    List<GroupBuyingParticipation> findAllByMember_IdAndRoleAndGroupBuying_StatusOrderByIdDesc(
            String memberId,
            UserRole role,
            GroupBuyingStatus status
    );

    // 특정 공구의 현재 참여 수량 합계 계산
    // currentQuantity는 GroupBuying에 저장된 값이 아니므로 참여 이력의 applyQuantity 합계로 구한다.
    // applyQuantity는 Integer지만 SUM 결과는 Long으로 받는 것이 안전하다.
    @Query("""
            SELECT COALESCE(SUM(p.applyQuantity), 0)
            FROM GroupBuyingParticipation p
            WHERE p.groupBuying.id = :groupBuyingId
            """)
    Long sumApplyQuantityByGroupBuyingIdAndRole(
            @Param("groupBuyingId") Long groupBuyingId
    );

    // 공구에 InComplete 가 존재하는지 체크
    boolean existsByGroupBuyingIdAndPaymentStatus(long groupBuyingId, PaymentStatus paymentStatus);
}
