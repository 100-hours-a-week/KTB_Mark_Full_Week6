package com.mark.community.controller;

import com.mark.community.dto.*;
import com.mark.community.entity.Post;
import com.mark.community.entity.User;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.messages.ApiResponseMessage;
import com.mark.community.response.ApiResponse;
import com.mark.community.service.PostService;
import com.mark.community.utils.IdempotencyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService){
        this.postService = postService;
    }

    @PostMapping("/temp")
    public ResponseEntity<?> postTemp(@RequestBody PostTempRequest request,
                                                     HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }
        Long userId = (Long) session.getAttribute("userId");
        String idempotencyKey = httpRequest.getHeader("Idempotency-Key");
        ResponseEntity<?> idemResponseEntity = IdempotencyUtil.getResponse(idempotencyKey);

        if(idemResponseEntity != null) return idemResponseEntity;

        Post post =  postService.postTemp(request, userId);

        ResponseEntity<?> responseEntity = ResponseEntity
                .status(ApiResponseMessage.SUCCESS_POST_TEMP.getStatusCode())
                .header("Location", "/posts/" + post.getId())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_POST_TEMP, new PostTempResponse(post.getId())));

         IdempotencyUtil.setResponse(idempotencyKey, responseEntity);

        return responseEntity;
    }

    @PatchMapping("/{postId}/temp")
    public ResponseEntity<?> postAutoTemp(@PathVariable("postId") Long postId,
                                          @RequestPart("request") PostTempRequest request,
                                          @RequestPart("images") MultipartFile[] images,
                                          HttpServletRequest httpRequest
    ){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }
        PostTempResponse postTempResponse = postService.postAutoTemp(postId, request, images);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_POST_TEMP.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_POST_TEMP, postTempResponse));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable("postId") Long postId, HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        Long userId = (Long) session.getAttribute("userId");

        postService.deletePost(postId, userId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_DELETE_POST.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_POST));
    }

    @GetMapping("/temp")
    public ResponseEntity<?> getTempPost(@RequestParam("postId") Long postId, HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        PostTempResponse postTempResponse = postService.getTempPost(postId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_POST_TEMP.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_POST_TEMP, postTempResponse));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable("postId") Long postId, HttpServletRequest httpRequest){

        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        Long userId = (Long) session.getAttribute("userId");

        PostResponse postResponse = postService.getPost(postId, userId);


        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_POST.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_POST, postResponse));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> savePost(@PathVariable("postId") Long postId,
                                      @RequestPart("request") PostRequest request,
                                      @RequestPart("images") MultipartFile[] images,
                                      HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        Post post = postService.savePost(postId, request, images);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_POST_SAVE.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_POST_SAVE,
                        new PostResponse(post.getId())));
    }

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam(value = "size", defaultValue = "10") int size,
                                      @RequestParam(value = "lastPostId", required = false) Long lastPostId,
                                      HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        PostListResponse postListResponse = postService.getPosts(size, lastPostId);


        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_POSTS.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_POSTS, postListResponse));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> addLike(@PathVariable("postId") Long postId, HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        Long userId = (Long) session.getAttribute("userId");

        postService.addLike(postId, userId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_ADD_LIKE.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_ADD_LIKE));
    }


    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> deleteLike(@PathVariable("postId") Long postId, HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }

        Long userId = (Long) session.getAttribute("userId");

        postService.deleteLike(postId, userId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_DELETE_LIKE.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_LIKE));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> editPost(@PathVariable("postId") Long postId,
                                          @RequestPart("request") PostTempRequest request,
                                          @RequestPart("images") MultipartFile[] images,
                                          HttpServletRequest httpRequest
    ){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }
        Long userId = (Long) session.getAttribute("userId");


        Post post = postService.editPost(postId, request, images, userId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_UPDATE_POST.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_UPDATE_POST));
    }

    @PostMapping("/{postId}/reports")
    public ResponseEntity<?> addReports(@PathVariable("postId") Long postId, HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);

        if(session == null){
            throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
        }
        Long userId = (Long) session.getAttribute("userId");

        postService.addReports(postId, userId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_ADD_REPORT.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_ADD_REPORT));

    }


}
