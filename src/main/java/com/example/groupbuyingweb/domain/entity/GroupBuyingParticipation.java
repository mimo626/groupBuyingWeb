package com.example.groupbuyingweb.domain.entity;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class GroupBuyingParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //FK 사용자 id
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    //FK 공구 id
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="group_buying_id")
    private GroupBuying groupBuying;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private int applyQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Double paidPoint;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

}
