package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AddressService {

    private static final double DEFAULT_RADIUS_METER = 1000.0;
    private static final double SEARCH_INTERVAL_METER = 250.0;
    private static final double METER_PER_LATITUDE = 111_320.0;

    private final KakaoLocalApiClient kakaoLocalApiClient;

    public List<UserNearbyAddress> createNearbyAddresses(
            Member member,
            Double baseEntX,
            Double baseEntY
    ) {
        // 1. 프론트에서 받은 기준 좌표를 Coordinate 객체로 만든다.
        Coordinate baseCoordinate = new Coordinate(baseEntX, baseEntY);

        // 2. 기준 좌표 주변 1000m 안의 탐색 좌표를 만든다.
        List<Coordinate> searchCoordinates = createSearchCoordinates(baseCoordinate);

        // 3. 각 탐색 좌표를 행정동 정보로 변환하고, 같은 동네는 중복 제거한다.
        Map<String, RegionInfo> regionMap = new LinkedHashMap<>();

        for (Coordinate coordinate : searchCoordinates) {
            RegionInfo regionInfo = kakaoLocalApiClient.convertCoordinateToRegion(
                    coordinate.longitude(),
                    coordinate.latitude()
            );

            String key = regionInfo.cityName()
                    + "_"
                    + regionInfo.districtName()
                    + "_"
                    + regionInfo.neighborhoodName();

            regionMap.putIfAbsent(key, regionInfo);
        }

        // 4. 중복 제거된 행정동 정보를 UserNearbyAddress 객체로 만든다.
        return regionMap.values().stream()
                .map(region -> UserNearbyAddress.builder()
                        .member(member)
                        .cityName(region.cityName())
                        .districtName(region.districtName())
                        .neighborhoodName(region.neighborhoodName())
                        .entX(region.longitude())
                        .entY(region.latitude())
                        .build())
                .toList();
    }

    private List<Coordinate> createSearchCoordinates(Coordinate baseCoordinate) {
        List<Coordinate> coordinates = new ArrayList<>();

        for (double eastMeter = -DEFAULT_RADIUS_METER;
             eastMeter <= DEFAULT_RADIUS_METER;
             eastMeter += SEARCH_INTERVAL_METER) {

            for (double northMeter = -DEFAULT_RADIUS_METER;
                 northMeter <= DEFAULT_RADIUS_METER;
                 northMeter += SEARCH_INTERVAL_METER) {

                double distance = Math.sqrt(
                        Math.pow(eastMeter, 2)
                                + Math.pow(northMeter, 2)
                );

                if (distance <= DEFAULT_RADIUS_METER) {
                    coordinates.add(moveByMeter(baseCoordinate, eastMeter, northMeter));
                }
            }
        }

        return coordinates;
    }

    private Coordinate moveByMeter(
            Coordinate baseCoordinate,
            double eastMeter,
            double northMeter
    ) {
        double latitudeDelta = northMeter / METER_PER_LATITUDE;
        double longitudeDelta = eastMeter / (
                METER_PER_LATITUDE
                        * Math.cos(Math.toRadians(baseCoordinate.latitude()))
        );

        return new Coordinate(
                baseCoordinate.longitude() + longitudeDelta,
                baseCoordinate.latitude() + latitudeDelta
        );
    }

    public record Coordinate(
            Double longitude,
            Double latitude
    ) {
    }

    public record RegionInfo(
            String cityName,
            String districtName,
            String neighborhoodName,
            Double longitude,
            Double latitude
    ) {
    }
}