package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    Optional<Member> findByLoginId(String loginId);
}
