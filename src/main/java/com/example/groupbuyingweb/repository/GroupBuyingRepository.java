package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface GroupBuyingRepository extends JpaRepository<GroupBuying, Long> {
    Integer findTargetQuantityById(long groupBuyingId);
    @Query("SELECT g FROM GroupBuying g WHERE " +
            "(g.status = GroupBuyingStatus.RECRUITING) AND " +
            "(:category IS NULL OR g.category = :category) AND " +
            "(:keyword IS NULL OR :keyword = '' OR g.title LIKE %:keyword% OR g.productName LIKE %:keyword%)")
    Page<GroupBuying> searchGroupBuyings(
            @Param("category") GroupBuyingCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 현재 로그인한 사용자가 개설한 공구 목록을 진행 상태 기준으로 조회한다.
    // role=ORGANIZER일 때 사용한다.
    // id 최신순으로 조회한다.
    List<GroupBuying> findAllByMember_IdAndStatusOrderByIdDesc(
            String memberId,
            GroupBuyingStatus status
    );


}
