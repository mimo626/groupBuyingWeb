package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, String> {
    @Modifying
    @Query("UPDATE Member m SET m.point = m.point - :amount WHERE m.id = :id")
    void decreasePoint(@Param("id") String id, @Param("amount") Double amount);
}
