package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // DB에서 직접 경험치 +1 올리는 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.acornExp = m.acornExp + :exp WHERE m.id = :memberId")
    void incrementAcornExp(@Param("memberId") String memberId, @Param("exp") int exp);
}
