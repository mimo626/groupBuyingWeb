package com.example.groupbuyingweb.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(500,"서버 내부 오류입니다."),
    // 회원가입 / 인증
    DUPLICATED_LOGIN_ID(400, "이미 사용 중인 로그인 아이디입니다."),
    DUPLICATED_NICKNAME(400, "이미 사용 중인 닉네임입니다."),
    PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다."),
    TERMS_NOT_AGREED(400, "필수 약관에 동의해야 합니다."),

    UNAUTHORIZED(400, "로그인이 필요합니다"),

    NOT_EXIST_MEMBER(400, "존재하지 않는 회원 ID입니다."),

    // 주소 / 카카오 API
    ADDRESS_REGION_NOT_FOUND(400, "좌표의 행정동 정보를 찾을 수 없습니다."),
    KAKAO_LOCAL_API_ERROR(500, "카카오 로컬 API 호출 중 오류가 발생했습니다."),

    // 로그인
    LOGIN_FAILED(401, "로그인 아이디 또는 비밀번호가 올바르지 않습니다."),
    LOGIN_REQUIRED(401, "로그인이 필요한 요청입니다."),

    // 포인트
    INSUFFICIENT_POINT(400, "포인트 잔액이 부족합니다."),
    INVALID_POINT(400, "유효하지 않은 결제 요청입니다."),
    KAKAO_PAY_API_ERROR(502,"카카오페이 API 호출에 실패했습니다." ),

    // 공동구매 생성
    NOT_EXIST_GROUP_BUYING(400, "존재하지 않는 공동구매 게시글입니다."),

    // 공동구매 참여
    EXCEED_TARGET_QUANTITY(400, "모집 수량을 초과하여 신청할 수 없습니다."),
    NOT_EXIST_GROUP_BUYING_PARTICIPANT(400, "존재하지 않는 공동구매 참여입니다."),


    // 채팅
    NOT_EXIST_CHAT_ROOM(404, "존재하지 않는 채팅방입니다."),
    NOT_CHAT_PARTICIPANT(403, "채팅방 참여자가 아닙니다.");

    private final int status;
    private final String message;

}
