package com.mark.community.messages;

import org.springframework.http.HttpStatus;

public enum ApiResponseErrorMessage {
    FAIL_LOGIN("사용자 정보가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),
    DUPLICATE_EMAIL("중복된 이메일 입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("중복된 닉네임 입니다.", HttpStatus.CONFLICT),
    MISSING_REQUIRED_PARAMETER("필수값이 누락됐습니다.", HttpStatus.CONFLICT),
    EXPIRED_SESSION("세션이 만료됐습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    SERVER_ERROR("서버에서 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    POST_NOT_FOUND("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    FORBIDDEN("권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus status;

    ApiResponseErrorMessage(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage(){
        return message;
    }

    public HttpStatus getStatusCode(){
        return status;
    }
}
