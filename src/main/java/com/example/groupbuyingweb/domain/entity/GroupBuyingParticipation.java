package com.example.groupbuyingweb.domain.entity;

import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class GroupBuyingParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    //FK 사용자id
    //@ManyToOne(FetchType.LAZY)
    //@JoinColumn(name="id")
    //private User userId;

    //FK 공구id
    //@ManyToOne(FetchType.LAZY)
    //@JoinColumn(name="id")
    //private GroupBuying groupBuyingId;

    // 참여자 / 주최자 역할
    private Enum<UserRole> role;

    private Long applyQuantity;

    private Enum<PaymentStatus> paymentStatus;

    private Long paidPoint;

    private LocalDateTime createdAt;
}
