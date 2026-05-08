package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBuyingRepository extends JpaRepository<GroupBuying, Long> {
}
