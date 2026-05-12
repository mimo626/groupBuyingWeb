package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyingParticipationRepository extends JpaRepository<GroupBuyingParticipation, Long> {

    @Query("SELECT SUM(g.paidPoint) FROM GroupBuyingParticipation g WHERE g.groupBuying.id = :gbpId")
    Double sumSettlePay(@Param("gbpId") long groupBuyingId);

    @Query("SELECT g.member FROM GroupBuyingParticipation g WHERE g.groupBuying.id = :groupBuyingId AND g.role = :role")
    Member findMemberByGroupBuyingIdAndRole(long groupBuyingId, UserRole role);

    GroupBuyingParticipation findByGroupBuyingIdAndMemberId(long groupBuyingId, String memberId);

    List<GroupBuyingParticipation> findAllByGroupBuyingId(long groupBuyingId);

    @Query("SELECT SUM(g.applyQuantity) FROM GroupBuyingParticipation g WHERE g.groupBuying.id = :gbpId")
    Integer sumQuantity(@Param("gbpId") long groupBuyingId);
}
