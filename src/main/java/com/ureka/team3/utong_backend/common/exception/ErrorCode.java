package com.ureka.team3.utong_backend.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

    CONCURRENT_ACCESS_ERROR(HttpStatus.CONFLICT, "C409", "동시 접근 오류가 발생했습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 내부 오류가 발생했습니다"),

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "A404", "존재하지 않는 계정입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U404", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다"),
    ROULETTE_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "R404", "룰렛 이벤트를 찾을 수 없습니다"),
    ROULETTE_EVENT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "R400", "활성화된 룰렛 이벤트가 없습니다"),
    ROULETTE_ALREADY_PARTICIPATED(HttpStatus.BAD_REQUEST, "R409", "이미 참여한 룰렛 이벤트입니다"),
    ROULETTE_WINNERS_FULL(HttpStatus.BAD_REQUEST, "R410", "당첨자가 모두 마감되었습니다"),
    LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "LINE_NOT_FOUND", "회선 정보를 찾을 수 없습니다."),

    GIFTICON_NOT_FOUND(HttpStatus.NOT_FOUND, "G404", "기프티콘 정보를 찾을 수 없습니다."),

  COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "사용자의 쿠폰이 존재하지 않습니다."),
  COUPON_EXPIRED(HttpStatus.BAD_REQUEST,"C002", "쿠폰이 만료되었습니다."),
  INVALID_COUPON_STATUS(HttpStatus.BAD_REQUEST,"C003", "쿠폰 상태가 유효하지 않습니다."),
  NOT_FEE_WAIVE_COUPON(HttpStatus.BAD_REQUEST,"C004", "해당 쿠폰은 수수료 면제 쿠폰이 아닙니다."),



    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다"),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED", "로그인이 필요합니다"),

    PRICE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRICE_NOT_FOUND", "가격 정보를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다."),
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTRACT_NOT_FOUND", "오늘 만료되는 계약이 없습니다."),
    NOT_MY_REVIEW(HttpStatus.FORBIDDEN, "NOT_MY_REVIEW", "본인의 리뷰만 수정 또는 삭제할 수 있습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "올바르지 않은 입력값입니다."),

    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "F400", "지원하지 않는 파일 형식입니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "F413", "파일 크기가 너무 큽니다."),
    FILE_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F500", "파일 처리 중 오류가 발생했습니다."),

    AI_MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI500", "AI 서버 데이터 전송 실패"),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다"),
    ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "ALREADY_REVIEWED", "이미 해당 요금제에 대한 리뷰를 작성하셨습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "INSUFFICIENT_POINT", "포인트가 부족합니다"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_ORDER", "해당 주문이 존재하지 않습니다"),
    INSUFFICIENT_DATA(HttpStatus.BAD_REQUEST,"INSUFFICIENT_DATA", "데이터가 부족합니다");
    private final HttpStatus status;
    private final String code;
    private final String message;


    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}