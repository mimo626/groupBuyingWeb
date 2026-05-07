package com.example.groupbuyingweb.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_nearby_address")
public class UserNearbyAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // user_nearby_address.user_id -> member.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private Member member;

    // 시/도 이름
    @Column(name = "city_name", nullable = false, length = 50)
    private String cityName;

    // 구/군 이름
    @Column(name = "district_name", nullable = false, length = 50)
    private String districtName;

    // 동 이름
    @Column(name = "neighborhood_name", nullable = false, length = 50)
    private String neighborhoodName;

    // 주변 주소의 위도
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    // 주변 주소의 경도
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // 생성 날짜
    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP"
    )
    private LocalDateTime createdAt;
}