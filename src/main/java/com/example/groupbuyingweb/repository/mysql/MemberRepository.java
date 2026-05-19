package com.example.groupbuyingweb.repository.mysql;

import com.example.groupbuyingweb.domain.entity.mysql.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    Optional<Member> findByLoginId(String loginId);

    interface MemberLocationInfo {
        String getAddress();
        Integer getRadius();
        Double getEntX();
        Double getEntY();
    }

    Optional<MemberLocationInfo> findLocationById(String id);
}
