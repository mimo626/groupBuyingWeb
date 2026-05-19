package com.example.groupbuyingweb.domain.entity.mysql;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
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
    private Integer applyQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private Double paidPoint;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.paymentStatus = this.paymentStatus == null ? PaymentStatus.Incomplete : this.paymentStatus;
        this.paidPoint = this.paidPoint == null ? 0 : this.paidPoint;
    }
    public void increasePoint(double totalPay) {
        this.paidPoint += totalPay;
    }

    public void settlePoint(Double point) {
        if (this.paidPoint < point){
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        this.paymentStatus = PaymentStatus.Complete;
        this.paidPoint -= point;
    }
}
