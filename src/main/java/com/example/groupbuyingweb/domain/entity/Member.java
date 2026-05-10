package com.example.groupbuyingweb.domain.entity;

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
    @Column(nullable = false)
    private Double entX;
    @Column(nullable = false)
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
        if (this.point < totalPay){
            //throw new Exception();
        }
        this.point -= totalPay;
    }

    public void chargePoint(Double charge) {
        this.point += charge;
    }
}