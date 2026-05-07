package com.example.groupbuyingweb.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_nearby_address")
public class UserNearbyAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 어떤 사용자의 주변 주소인지 연결
    // ERD 기준 user_nearby_address.user_id -> member.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private Member member;

    // 시/도 이름
    @Column(name = "city_name", nullable = false, length = 50)
    private String cityName;

    // 구 이름
    @Column(name = "district_name", nullable = false, length = 50)
    private String districtName;

    // 동 이름
    @Column(name = "neighborhood_name", nullable = false, length = 50)
    private String neighborhoodName;

    // 주변 주소 위도
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    // 주변 주소 경도
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // 생성 날짜
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserNearbyAddress(
            Member member,
            String cityName,
            String districtName,
            String neighborhoodName,
            Double latitude,
            Double longitude
    ) {
        this.member = member;
        this.cityName = cityName;
        this.districtName = districtName;
        this.neighborhoodName = neighborhoodName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}