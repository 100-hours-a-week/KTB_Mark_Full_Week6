package com.mark.community.messages;

public enum ErrorMessage {
    FAIL_UPLOAD("파일 업로드에 실패했습니다."),
    FAIL_CHECK_FILE_TYPE("지원하지 않는 파일입니다");

    private final String message;

    ErrorMessage(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
