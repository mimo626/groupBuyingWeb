package com.example.groupbuyingweb.domain.entity;

import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class GroupBuyingParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //FK 사용자id
    @ManyToOne(FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member memberId;

    //FK 공구id
    @ManyToOne(FetchType.LAZY)
    @JoinColumn(name="group_buying_id")
    private GroupBuying groupBuyingId;

    // 참여자 / 주최자 역할
    private Enum<UserRole> role;

    private int applyQuantity;

    private Enum<PaymentStatus> paymentStatus;

    private double paidPoint;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

}
