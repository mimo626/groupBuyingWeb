package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNearbyAddressRepository extends JpaRepository<UserNearbyAddress, Long> {
    List<UserNearbyAddress> findAllByMemberId(String memberId);

    @Query("SELECT u.neighborhoodName FROM UserNearbyAddress u WHERE u.member.id = :memberId")
    List<String> findNeighborhoodNameByMemberId(@Param("memberId") String memberId);

    void deleteAllByMemberId(String memberId);
}