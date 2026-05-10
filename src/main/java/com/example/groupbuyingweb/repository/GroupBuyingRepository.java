package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface GroupBuyingRepository extends JpaRepository<GroupBuying, Long> {
    Integer findTargetQuantityById(long groupBuyingId);
    @Query("SELECT g FROM GroupBuying g WHERE " +
            "(:category IS NULL OR g.category = :category) AND " +
            "(:keyword IS NULL OR :keyword = '' OR g.title LIKE %:keyword% OR g.productName LIKE %:keyword%)")
    Page<GroupBuying> searchGroupBuyings(
            @Param("category") GroupBuyingCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );}
