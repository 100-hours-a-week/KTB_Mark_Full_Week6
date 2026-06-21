package com.mark.community.controller;

import com.mark.community.dto.CommentRequest;
import com.mark.community.dto.CommentResponse;
import com.mark.community.entity.Comment;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.messages.ApiResponseMessage;
import com.mark.community.response.ApiResponse;
import com.mark.community.service.CommentService;
import com.mark.community.utils.IdempotencyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> commentSave(
            @RequestBody CommentRequest request,
            @PathVariable("postId") Long postId,
            HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }
        Long userId = (Long) session.getAttribute("userId");
        String idempotencyKey = httpRequest.getHeader("Idempotency-Key");
        ResponseEntity<?> idemResponseEntity = IdempotencyUtil.getResponse(idempotencyKey);

        if(idemResponseEntity != null) return idemResponseEntity;

        Comment comment = commentService.commentSave(postId, request, userId);

        CommentResponse commentResponse = new CommentResponse(comment.getId());

        ResponseEntity<?> responseEntity = ResponseEntity
                .status(ApiResponseMessage.SUCCESS_COMMENT_SAVE.getStatusCode())
                .header("LOCATION", "/comment/" + comment.getId())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_COMMENT_SAVE, commentResponse));

        IdempotencyUtil.setResponse(idempotencyKey, responseEntity);

        return responseEntity;
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> editComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentRequest request,
            HttpServletRequest httpRequest
    ){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }
        Long userId = (Long) session.getAttribute("userId");

        Comment comment = commentService.editComment(commentId, request, userId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_UPDATE_COMMENT.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_UPDATE_COMMENT));

    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") Long commentId, HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }
        Long userId = (Long) session.getAttribute("userId");

        Comment comment = commentService.deleteComment(commentId, userId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_DELETE_COMMENT.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_COMMENT));
    }


    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable("postId") Long postId, HttpServletRequest httpRequest){

        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        List<CommentResponse> comments = commentService.getComments(postId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_COMMENTS.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_COMMENTS, comments));

    }

}
