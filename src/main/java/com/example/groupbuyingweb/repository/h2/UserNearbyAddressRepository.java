package com.example.groupbuyingweb.repository.h2;

import com.example.groupbuyingweb.domain.entity.h2.UserNearbyAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional("h2TransactionManager")
public interface UserNearbyAddressRepository extends JpaRepository<UserNearbyAddress, Long> {

    @Transactional(transactionManager = "h2TransactionManager", readOnly = true)
    List<UserNearbyAddress> findAllByMemberId(String memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByMemberId(String memberId);

//    @Query("SELECT u.neighborhoodName FROM UserNearbyAddress u WHERE u.memberId = :memberId")
//    List<String> findNeighborhoodNameByMemberId(@Param("memberId") String memberId);
}
