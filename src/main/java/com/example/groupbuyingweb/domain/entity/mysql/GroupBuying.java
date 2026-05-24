package com.example.groupbuyingweb.domain.entity.mysql;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 제한
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 생성을 위해 추가
public class GroupBuying {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private GroupBuyingCategory category;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private Double totalPrice;
    @Column(nullable = false)
    private Integer targetQuantity;

    @Column(nullable = false, length = 500)
    private String productUrl;
    @Column(length = 500)
    private String productContent;

    @Column(nullable = false)
    private String meetingAddress;
    @Column(nullable = false)
    private String neighborhoodName;
    @Column(nullable = false)
    private String meetingPlace;
    @Column(name = "ent_x", nullable = false)
    private Double entX;
    @Column(name = "ent_y", nullable = false)
    private Double entY;


    @Column(nullable = false)
    private Integer viewCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private GroupBuyingStatus status;

    @Column(length = 50)
    private String trackingNumber;
    private LocalDateTime meetingAt;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Builder.Default
    @OneToMany(mappedBy = "groupBuying", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyingImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "groupBuying", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyingParticipation> participations = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.status = this.status == null ? GroupBuyingStatus.RECRUITING : this.status;
        this.viewCount = this.viewCount == null ? 0 : this.viewCount;
    }

    // 엔티티 내부 정보 업데이트 메서드
    public void update(String title, String productName, GroupBuyingCategory category,
                       String productContent, double totalPrice, int targetQuantity,
                       double entX, double entY, String meetingPlace, String meetingAddress,
                       String productUrl, LocalDateTime deadline, String neighborhoodName) {
        this.title = title;
        this.productName = productName;
        this.category = category;
        this.productContent = productContent;
        this.totalPrice = totalPrice;
        this.targetQuantity = targetQuantity;
        this.entX = entX;
        this.entY = entY;
        this.meetingPlace = meetingPlace;
        this.meetingAddress = meetingAddress;
        this.productUrl = productUrl;
        this.deadline = deadline;
        this.neighborhoodName = neighborhoodName;
    }

    // 기존 이미지를 싹 지우는 메서드
    public void clearImages() {
        this.images.clear();
    }

    // 상태 변경
    public void updateStatus(GroupBuyingStatus newStatus) {
        this.status = newStatus;
    }

    // 운송장 번호 등록/수정
    public void updateTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    // 만남 시간 설정/수정
    public void updateMeetingAt(LocalDateTime meetingAt) {
        this.meetingAt = meetingAt;
    }

    // 이미지 추가
    public void addImage(GroupBuyingImage image) {
        this.images.add(image);
        image.updateGroupBuying(this);
    }
}