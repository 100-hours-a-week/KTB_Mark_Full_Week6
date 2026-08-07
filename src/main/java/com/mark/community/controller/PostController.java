package com.mark.community.controller;

import com.mark.community.dto.*;
import com.mark.community.entity.Post;
import com.mark.community.messages.ApiResponseMessage;
import com.mark.community.response.ApiResponse;
import com.mark.community.service.PostService;
import com.mark.community.utils.IdempotencyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@Log4j2
public class PostController {
    private final PostService postService;

    public PostController(PostService postService){
        this.postService = postService;
    }

    @PostMapping("/temp")
    public ResponseEntity<?> postTemp(@RequestBody PostTempRequest request,
                                      HttpServletRequest httpRequest,
                                      @AuthenticationPrincipal CustomUserDetails userDetails){

        String idempotencyKey = httpRequest.getHeader("Idempotency-Key");
        ResponseEntity<?> idemResponseEntity = IdempotencyUtil.getResponse(idempotencyKey);

        if(idemResponseEntity != null) return idemResponseEntity;

        Post post =  postService.postTemp(request, userDetails.getId());

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
                                          @RequestPart(value = "images", required = false) MultipartFile[] images,
                                          @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        PostTempResponse postTempResponse = postService.postAutoTemp(postId, request, images, userDetails);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_POST_TEMP.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_POST_TEMP, postTempResponse));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable("postId") Long postId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails){


        postService.deletePost(postId, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_DELETE_POST.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_POST));
    }

    @GetMapping("/temp")
    public ResponseEntity<?> getTempPost(@RequestParam("postId") Long postId){

        PostTempResponse postTempResponse = postService.getTempPost(postId);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_POST_TEMP.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_POST_TEMP, postTempResponse));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){

        PostResponse postResponse = postService.getPost(postId, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_POST.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_POST, postResponse));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> savePost(@PathVariable("postId") Long postId,
                                      @RequestPart("request") PostRequest request,
                                      @RequestPart(value = "images", required = false) MultipartFile[] images,
                                      @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        Post post = postService.savePost(postId, request, images, userDetails);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_POST_SAVE.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_POST_SAVE,
                        new PostResponse(post.getId())));
    }

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam(value = "size", defaultValue = "10") int size,
                                      @RequestParam(value = "lastPostId", required = false) Long lastPostId,
                                      @RequestParam(value = "category", required = false) String category)
    {

        PostListResponse postListResponse = postService.getPosts(size, lastPostId, category);


        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_POSTS.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_POSTS, postListResponse));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> addLike(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){


        long likeCount = postService.addLike(postId, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_ADD_LIKE.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_ADD_LIKE, new LikeResponse(likeCount)));
    }


    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> deleteLike(@PathVariable("postId") Long postId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails){

        long likeCount = postService.deleteLike(postId, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_DELETE_LIKE.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_LIKE, new LikeResponse(likeCount)));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> editPost(@PathVariable("postId") Long postId,
                                      @RequestPart("request") PostTempRequest request,
                                      @RequestPart(value = "images", required = false) MultipartFile[] images,
                                      @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Post post = postService.editPost(postId, request, images, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_UPDATE_POST.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_UPDATE_POST));
    }

    @PostMapping("/{postId}/reports")
    public ResponseEntity<?> addReports(@PathVariable("postId") Long postId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails){

        postService.addReports(postId, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_ADD_REPORT.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_ADD_REPORT));

    }


}
