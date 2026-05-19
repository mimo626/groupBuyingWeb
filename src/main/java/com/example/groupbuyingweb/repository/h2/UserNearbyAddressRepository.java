package com.example.groupbuyingweb.repository.h2;

import com.example.groupbuyingweb.domain.entity.h2.UserNearbyAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNearbyAddressRepository extends JpaRepository<UserNearbyAddress, Long> {
    List<UserNearbyAddress> findAllByMemberId(String memberId);

    @Query("SELECT u.neighborhoodName FROM UserNearbyAddress u WHERE u.memberId = :memberId")
    List<String> findNeighborhoodNameByMemberId(@Param("memberId") String memberId);

    void deleteAllByMemberId(String memberId);
}