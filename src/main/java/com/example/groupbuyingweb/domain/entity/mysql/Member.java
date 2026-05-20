package com.example.groupbuyingweb.domain.entity.mysql;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.dto.request.MyPageRequest;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "`member`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 50, unique = true, nullable = false)
    private String loginId;
    @Column(nullable = false)
    private String password;
    @Column(length = 50, unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private Integer radius;
    @Column(name = "ent_x", nullable = false)
    private Double entX;
    @Column(name = "ent_y", nullable = false)
    private Double entY;

    @Column(nullable = false)
    private Double point;
    @Column(nullable = false)
    private Integer acornExp;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.radius = this.radius == null ? 1000 : this.radius;
        this.point = this.point == null ? 0 : this.point;
        this.acornExp = this.acornExp == null ? 0 : this.acornExp;
    }

    public void decreasePoint(double totalPay) {
        if (this.point < totalPay){ // 결제할것이 보유 포인트보다 많으면
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        this.point -= totalPay;
    }

    public void chargePoint(Double charge) {
        if (charge <= 0.0){ // 충전금액 0 일 때 테스트
            throw new BusinessException(ErrorCode.TEST_ZERO_POINT);
        }
        this.point += charge;
    }

    public void patchAddress(MyPageRequest.UpdateNeighborhood request) {
        if (request.address() == null || request.entX() == null || request.entY() == null) {
            throw new BusinessException(ErrorCode.ADDRESS_REGION_NOT_FOUND);
        }
        this.address = request.address();
        this.entX = request.entX();
        this.entY = request.entY();
    }
}