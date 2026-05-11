package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNearbyAddressRepository extends JpaRepository<UserNearbyAddress, Long> {
    List<UserNearbyAddress> findAllByMemberId(String memberId);

    void deleteAllByMemberId(String memberId);
}
