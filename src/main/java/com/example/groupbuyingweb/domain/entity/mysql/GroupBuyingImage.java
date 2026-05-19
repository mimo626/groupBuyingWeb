package com.example.groupbuyingweb.domain.entity.mysql;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
public class GroupBuyingImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buying_id")
    private GroupBuying groupBuying;

    private String originalFilename; // 사용자가 올린 원본 이름
    private String storedFilename;   // 서버에 저장된 UUID 이름
    private String imageUrl;         // 브라우저에서 접근할 경로 (예: /uploads/abc.jpg)

    private boolean isThumbnail;     // 대표 이미지 여부 (첫 번째 사진을 true로)

    public void updateGroupBuying(GroupBuying groupBuying) {
        this.groupBuying = groupBuying;
    }
}