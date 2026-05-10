package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.service.AddressService.RegionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoLocalApiClient {

    private final RestClient restClient;

    @Value("${kakao.local.rest-api-key}")
    private String kakaoRestApiKey;

    public RegionInfo convertCoordinateToRegion(
            Double longitude,
            Double latitude
    ) {
        KakaoRegionCodeResponse response;

        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("dapi.kakao.com")
                            .path("/v2/local/geo/coord2regioncode.json")
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("input_coord", "WGS84")
                            .build())
                    .header("Authorization", "KakaoAK " + kakaoRestApiKey)
                    .retrieve()
                    .body(KakaoRegionCodeResponse.class);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_LOCAL_API_ERROR);
        }

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            throw new BusinessException(ErrorCode.ADDRESS_REGION_NOT_FOUND);
        }

        KakaoRegionDocument document = response.documents().stream()
                .filter(item -> "H".equals(item.region_type()))
                .findFirst()
                .orElse(response.documents().get(0));

        return new RegionInfo(
                document.region_1depth_name(),
                document.region_2depth_name(),
                document.region_3depth_name(),
                longitude,
                latitude
        );
    }

    private record KakaoRegionCodeResponse(
            List<KakaoRegionDocument> documents
    ) {
    }

    private record KakaoRegionDocument(
            String region_type,
            String region_1depth_name,
            String region_2depth_name,
            String region_3depth_name
    ) {
    }
}