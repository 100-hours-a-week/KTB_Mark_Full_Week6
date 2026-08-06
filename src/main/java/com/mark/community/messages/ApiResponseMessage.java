package com.mark.community.messages;

import org.springframework.http.HttpStatus;

public enum ApiResponseMessage {
    SUCCESS_REGISTER("회원가입에 성공했습니다.", HttpStatus.CREATED),
    SUCCESS_LOGIN("로그인에 성공했습니다.", HttpStatus.OK),
    SUCCESS_UPDATE_USER("회원정보 변경에 성공했습니다", HttpStatus.OK),
    SUCCESS_DELETE_USER("회원삭제에 성공했습니다.", HttpStatus.OK),
    SUCCESS_POST_TEMP("게시글 임시저장에 성공했습니다.", HttpStatus.CREATED),
    SUCCESS_POST_SAVE("게시글 저장에 성공했습니다.", HttpStatus.OK),
    SUCCESS_DELETE_POST("게시글 삭제에 성공했습니다.", HttpStatus.OK),
    SUCCESS_GET_POST_TEMP("임시 게시글 조회에 성공했습니다.", HttpStatus.OK),
    SUCCESS_GET_POST("게시글 상세 조회에 성공했습니다.", HttpStatus.OK),
    SUCCESS_GET_POSTS("게시글 목록을 성공적으로 불러왔습니다.", HttpStatus.OK),
    SUCCESS_UPDATE_POST("게시글 수정에 성공했습니다.", HttpStatus.OK),
    SUCCESS_COMMENT_SAVE("댓글 생성에 성공했습니다.", HttpStatus.OK),
    SUCCESS_UPDATE_COMMENT("댓글 수정에 성공했습니다.", HttpStatus.OK),
    SUCCESS_DELETE_COMMENT("댓글 삭제에 성공했습니다.", HttpStatus.OK),
    SUCCESS_GET_COMMENTS("댓글을 불러오는데 성공했습니다.", HttpStatus.OK),
    SUCCESS_ADD_LIKE("좋아요에 성공했습니다.", HttpStatus.OK),
    SUCCESS_DELETE_LIKE("좋아요 삭제에 성공했습니다.", HttpStatus.OK),
    SUCCESS_GET_FILE("파일 조회에 성공했습니다.", HttpStatus.OK),
    SUCCESS_ADD_REPORT("신고에 성공했습니다.", HttpStatus.OK),
    SUCCESS_GET_USER("유저 정보를 불러왔습니다.", HttpStatus.OK),
    SUCCESS_CSRF_TOKEN("CSRF 토큰을 불러왔습니다.", HttpStatus.OK),
    SUCCESS_GET_NOTIFICATIONS("알림 목록을 불러왔습니다.", HttpStatus.OK),
    SUCCESS_UPDATE_NOTIFICATION("알림을 읽음 처리했습니다.", HttpStatus.OK),
    SUCCESS_UPDATE_ALL_NOTIFICATIONS("모든 알림을 읽음 처리했습니다.", HttpStatus.OK),
    SUCCESS_DELETE_NOTIFICATION("알림을 삭제했습니다.", HttpStatus.OK),
    SUCCESS_DELETE_ALL_NOTIFICATIONS("모든 알림을 삭제했습니다.", HttpStatus.OK);

    private final String message;
    private final HttpStatus status;

    ApiResponseMessage(String message, HttpStatus status) {
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
