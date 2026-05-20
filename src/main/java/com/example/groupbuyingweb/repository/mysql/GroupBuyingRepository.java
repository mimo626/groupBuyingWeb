package com.example.groupbuyingweb.repository.mysql;

import com.example.groupbuyingweb.domain.entity.mysql.GroupBuying;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface GroupBuyingRepository extends JpaRepository<GroupBuying, Long> {
    Integer findTargetQuantityById(long groupBuyingId);
    @Query("SELECT g FROM GroupBuying g WHERE " +
            "(g.status = GroupBuyingStatus.RECRUITING) AND " +
            "deadline > :currentTime AND " +
            "(:category IS NULL OR g.category = :category) AND " +
            "(:keyword IS NULL OR :keyword = '' OR g.title LIKE %:keyword% OR g.productName LIKE %:keyword%) AND " +
            "(g.neighborhoodName IN :neighborhoods)") // 추가된 부분
    Page<GroupBuying>  searchGroupBuyings(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("category") GroupBuyingCategory category,
            @Param("keyword") String keyword,
            @Param("neighborhoods") List<String> neighborhoods, // 파라미터 추가
            Pageable pageable
    );

    // 현재 로그인한 사용자가 개설한 공구 목록을 진행 상태 기준으로 조회한다.
    // role=ORGANIZER일 때 사용한다.
    // id 최신순으로 조회한다.
    List<GroupBuying> findAllByMember_IdAndStatusOrderByIdDesc(
            String memberId,
            GroupBuyingStatus status
    );

    // Status가 일치하고 Deadline이 지정된 시간보다 이전(Before)인 데이터 조회
    List<GroupBuying> findAllByStatusAndDeadlineBefore(GroupBuyingStatus status, LocalDateTime now);

    // DB에서 직접 조회수를 +1 올리는 쿼리
    @Modifying
    @Query("UPDATE GroupBuying g SET g.viewCount = g.viewCount + 1 WHERE g.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
