package com.example.groupbuyingweb.repository;

import com.example.groupbuyingweb.domain.entity.GroupBuyingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupBuyingImageRepository extends JpaRepository<GroupBuyingImage, Long> {
    Optional<GroupBuyingImage> findByGroupBuyingIdAndIsThumbnailTrue(Long groupBuyingId);}
