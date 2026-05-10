package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNearbyAddressRepository extends JpaRepository<UserNearbyAddress, Long> {
}
