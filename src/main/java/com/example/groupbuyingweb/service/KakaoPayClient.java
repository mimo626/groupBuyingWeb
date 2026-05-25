package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoPayClient {

    private final RestClient restClient;

    @Value("${kakao.pay.secret-key}")
    private String secretKey;

    @Value("${kakao.pay.cid}")
    private String cid;

    @Value("${kakao.pay.host}")
    private String host;

    private static final String KAKAO_PAY_HOST = "open-api.kakaopay.com";

    public record ReadyResponse(
            String tid,

            @JsonProperty("next_redirect_pc_url")
            String nextRedirectPcUrl,

            @JsonProperty("next_redirect_mobile_url")
            String nextRedirectMobileUrl
    ) {}

    public record ApproveResponse(
            String tid,

            @JsonProperty("partner_order_id")
            String partnerOrderId,

            @JsonProperty("partner_user_id")
            String partnerUserId,

            Amount amount
    ) {
        public record Amount(int total) {}
    }

    public ReadyResponse ready(String memberId, int point, String orderId) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(KAKAO_PAY_HOST)
                            .path("/online/v1/payment/ready")
                            .build())
                    .header("Authorization", "SECRET_KEY " + secretKey)
                    .body(Map.of(
                            "cid",              cid,
                            "partner_order_id", orderId,
                            "partner_user_id",  memberId,
                            "item_name",        "도토리 포인트 " + point + "P",
                            "quantity",         1,
                            "total_amount",     point,
                            "tax_free_amount",  0,
                            "approval_url", host + "/mypage/charge/popup-success",
                            "cancel_url",   host + "/mypage/charge",
                            "fail_url",     host + "/mypage/charge"
                    ))
                    .retrieve()
                    .body(ReadyResponse.class);

        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_PAY_API_ERROR);
        }
    }

    public ApproveResponse approve(String tid, String pgToken,
                                   String memberId, String orderId) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(KAKAO_PAY_HOST)
                            .path("/online/v1/payment/approve")
                            .build())
                    .header("Authorization", "SECRET_KEY " + secretKey)
                    .body(Map.of(
                            "cid",              cid,
                            "tid",              tid,
                            "partner_order_id", orderId,
                            "partner_user_id",  memberId,
                            "pg_token",         pgToken
                    ))
                    .retrieve()
                    .body(ApproveResponse.class);

        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_PAY_API_ERROR);
        }
    }

    public String makeOrderId(String memberId) {
        return "order_" + memberId + "_" + System.currentTimeMillis();
    }
}