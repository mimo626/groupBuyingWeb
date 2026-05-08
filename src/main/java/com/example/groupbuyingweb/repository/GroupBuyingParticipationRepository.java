package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBuyingParticipationRepository extends JpaRepository<GroupBuyingParticipation, Long> {
}
