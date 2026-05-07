package com.example.groupbuyingweb.domain.entity;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 제한
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 생성을 위해 추가
public class GroupBuying {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    private String productImageUrl;
    @Column(length = 500)
    private String productContent;

    @Column(nullable = false)
    private String neighborhoodName;
    @Column(nullable = false)
    private String meetingPlace;
    @Column(nullable = false)
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;


    @Column(nullable = false)
    private Integer viewCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private GroupBuyingStatus status;

    @Column(length = 50)
    private String trackingNumber;
    private LocalDateTime meetingAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime deadline;

    @PrePersist
    public void prePersist() {
        this.viewCount = this.viewCount == null ? 0 : this.viewCount;
    }
    public void patch(GroupBuyingRequest.Update request) {
        if (request.title() != null) this.title = request.title();
        if (request.productName() != null) this.productName = request.productName();
        if (request.category() != null) this.category = request.category();
        if (request.productContent() != null) this.productContent = request.productContent();
        if (request.totalPrice() != null) this.totalPrice = request.totalPrice();
        if (request.targetQuantity() != null) this.targetQuantity = request.targetQuantity();
        if (request.latitude() != null) this.latitude = request.latitude();
        if (request.longitude() != null) this.longitude = request.longitude();
        if (request.meetingPlace() != null) this.meetingPlace = request.meetingPlace();
        if (request.productUrl() != null) this.productUrl = request.productUrl();
        if (request.productImageUrl() != null) this.productImageUrl = request.productImageUrl();
        if (request.deadline() != null) this.deadline = request.deadline();
    }
}