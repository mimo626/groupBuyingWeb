package com.example.groupbuyingweb.repository.mysql;

import com.example.groupbuyingweb.domain.entity.mysql.GroupBuyingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupBuyingImageRepository extends JpaRepository<GroupBuyingImage, Long> {
    Optional<GroupBuyingImage> findByGroupBuyingIdAndIsThumbnailTrue(Long groupBuyingId);
    void deleteAllByIdIn(List<Long> groupBuyingIds);
}
