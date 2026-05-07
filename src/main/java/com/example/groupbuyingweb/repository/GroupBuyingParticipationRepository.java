package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBuyingParticipationRepository extends JpaRepository<GroupBuyingParticipation, Long> {
}
