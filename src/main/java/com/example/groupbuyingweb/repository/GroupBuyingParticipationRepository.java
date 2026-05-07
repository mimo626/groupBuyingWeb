package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBuyingParticipationRepository extends JpaRepository<GroupBuyingParticipation, Long> {
    @Modifying(clearAutomatically = true) // 삽입 삭제 수정 시 메모리를 청소하고 db에서 새로 가져온다
    @Query("UPDATE GroupBuyingParticipation p SET p.paidPoint = p.paidPoint + :amount WHERE p.id = :id")
    void increasePaidPoint(@Param("id") Long id, @Param("amount") Double amount);

}
