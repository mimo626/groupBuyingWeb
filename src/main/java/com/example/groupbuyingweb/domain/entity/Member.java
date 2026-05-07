package com.example.groupbuyingweb.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table
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
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer point;
    @Column(nullable = false)
    private Integer acornExp;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.radius = this.radius == null ? 1000 : this.radius;
        this.point = this.point == null ? 0 : this.point;
        this.acornExp = this.acornExp == null ? 0 : this.acornExp;
    }
}