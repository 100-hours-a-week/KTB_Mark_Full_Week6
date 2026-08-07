package com.mark.community.service;

import com.mark.community.dto.*;
import com.mark.community.entity.*;
import com.mark.community.entity.key.PostLikeId;
import com.mark.community.entity.key.PostReportId;
import com.mark.community.entity.key.PostViewId;
import com.mark.community.enums.PostCategory;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostImageRepository postImageRepository;
    private final LikeRepository likeRepository;
    private final ViewRepository viewRepository;
    private final CommentRepository commentRepository;
    private final PostReportRepository postReportRepository;
    private final FileService fileService;
    private final UserService userService;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       PostImageRepository postImageRepository,
                       LikeRepository likeRepository,
                       ViewRepository viewRepository,
                       CommentRepository commentRepository,
                       PostReportRepository postReportRepository,
                       FileService fileService,
                       UserService userService,
                       NotificationService notificationService){
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postImageRepository = postImageRepository;
        this.likeRepository = likeRepository;
        this.viewRepository = viewRepository;
        this.commentRepository = commentRepository;
        this.postReportRepository = postReportRepository;
        this.fileService = fileService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public Post postTemp(PostTempRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.USER_NOT_FOUND));
        Post post = new Post(request.getTitle(), request.getBody(), user);
        return postRepository.save(post);
    }


    public PostTempResponse postAutoTemp(Long postId, PostTempRequest request, MultipartFile[] images, CustomUserDetails userDetails) {
        List<Long> imageList = new ArrayList<>();

        if(images != null){
            for(MultipartFile file : images){
                UploadFile uploadFile = fileService.upload(file);
                postImageRepository.save(new PostImage(uploadFile.getId(),postId));
                imageList.add(uploadFile.getId());
            }
        }

        Post post = postRepository.findById(postId).
                orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        if(!post.getUser().getId().equals(userDetails.getId())){
            throw new CustomException(ApiResponseErrorMessage.FORBIDDEN);
        }

        post.setTitle(request.getTitle());
        post.setBody(request.getBody());
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            post.setCategory(resolveCategory(request.getCategory()));
        }


        return new PostTempResponse(post.getId(), imageList);
    }

    private PostCategory resolveCategory(String category) {
        try {
            return PostCategory.checkCategory(category);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ApiResponseErrorMessage.INVALID_REQUEST);
        }
    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        if(!(post.getUser().getId().equals(userId))){
            throw new CustomException(ApiResponseErrorMessage.FORBIDDEN);
        }

        post.setDeleted(true);
    }

    public PostResponse getPost(Long postId, Long userId) {
        Post post = postRepository.findPost(postId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        List<PostImage> tempList = postImageRepository.findByIdPostId(postId);

        String userNickname = post.getUser().getNickname();
        boolean permission = post.getUser().getId().equals(userId);

        long likeCount = likeRepository.countByIdPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);

        Counts counts = new Counts(likeCount, commentCount, post.getViews());
        if(!userService.existsUser(post.getUser().getId())){
            userNickname = "알 수 없음";
        }

        List<Long> imageList = tempList.stream()
                .map(postImage -> postImage.getId().getFileId())
                .toList();

        Long thumbnailId = post.getUser().getProfileFile() != null
                ? post.getUser().getProfileFile().getId()
                : null;

        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        PostResponse postResponse = new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                thumbnailId,
                userNickname,
                post.getUser().getId(),
                counts,
                imageList,
                post.isEdited(),
                permission,
                sd.format(post.getPostTime()),
                post.getCategory() != null ? post.getCategory().name() : null
        );


        increasePostViews(post, userId);
        return postResponse;
    }

    public void increasePostViews(Post post, Long userId){
        Optional<View> checkView = viewRepository.findById(new PostViewId(userId, post.getId()));

        if(checkView.isEmpty()){
            viewRepository.save(new View(userId, post.getId()));
            post.setViews(post.getViews() + 1);
        } else {
            View view = checkView.get();
            boolean isExpired = view.getViewTime().getTime() < System.currentTimeMillis() - (24 * 60 * 60 * 1000L);
            if(isExpired){
                view.updateTime();
                post.setViews(post.getViews() + 1);
            }
        }
    }

    public Post savePost(Long postId, PostRequest postRequest, MultipartFile[] images, CustomUserDetails userDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        if(!post.getUser().getId().equals(userDetails.getId())){
            throw new CustomException(ApiResponseErrorMessage.FORBIDDEN);
        }

        if(postRequest.getTitle() == null || postRequest.getTitle().isBlank()
                || postRequest.getBody() == null || postRequest.getBody().isBlank()
                || postRequest.getCategory() == null || postRequest.getCategory().isBlank()){
            throw new CustomException(ApiResponseErrorMessage.MISSING_REQUIRED_PARAMETER);
        }

        Date oneMinuteAgo = new Date(System.currentTimeMillis() - 60 * 1000L);
        long recentPostCount = postRepository.countRecentPostsByUser(userDetails.getId(), oneMinuteAgo);
        if(recentPostCount >= 10){
            throw new CustomException(ApiResponseErrorMessage.POST_RATE_LIMIT_EXCEEDED);
        }

        if(images != null){
            for(MultipartFile file : images){
                UploadFile uploadfile = fileService.upload(file);
                postImageRepository.save(new PostImage(uploadfile.getId(), postId));
            }
        }

        post.setTitle(postRequest.getTitle());
        post.setBody(postRequest.getBody());
        post.setCategory(resolveCategory(postRequest.getCategory()));
        post.setTemp(false);
        post.setPostTime(new Date());

        return post;
    }

    @Transactional(readOnly = true)
    public PostListResponse getPosts(int size, Long lastPostId, String category) {
        Pageable pageable = PageRequest.of(0, size);
        PostCategory postCategory = (category == null || category.isBlank()) ? null : resolveCategory(category);

        List<Post> posts = (lastPostId == null)
                ? postRepository.findPosts(postCategory, pageable)
                : postRepository.findPosts(lastPostId, postCategory, pageable);

        PostListResponse postListResponse = new PostListResponse();
        List<PostResponse> tempList = new ArrayList<>();

        List<Long> postIds = new ArrayList<>();
        for (Post post : posts) {
            postIds.add(post.getId());
        }

        Map<Long, Long> likeCounts = new HashMap<>();
        for (PostCount row : likeRepository.countGroupByPostIds(postIds)) {
            likeCounts.put(row.getPostId(), row.getCount());
        }

        Map<Long, Long> commentCounts = new HashMap<>();
        for (PostCount row : commentRepository.countGroupedByPostIds(postIds)) {
            commentCounts.put(row.getPostId(), row.getCount());
        }

        for(Post post : posts){
            String userNickname = (post.getUser().isDeleted()) ? "알 수 없음" : post.getUser().getNickname();

            long likeCount = likeCounts.getOrDefault(post.getId(), 0L);
            long commentCount = commentCounts.getOrDefault(post.getId(), 0L);
            Long thumbnailId = post.getUser().getProfileFile() != null
                    ? post.getUser().getProfileFile().getId()
                    : null;

            Counts counts = new Counts(likeCount, commentCount, post.getViews());

            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            PostResponse postResponse =  new PostResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getBody(),
                    thumbnailId,
                    userNickname,
                    post.getUser().getId(),
                    counts,
                    sd.format(post.getPostTime()),
                    post.isDeleted(),
                    post.getReports() >= 5,
                    post.getCategory() != null ? post.getCategory().name() : null
            );

            tempList.add(postResponse);
        }

        postListResponse.setTotal(posts.size());
        postListResponse.setList(tempList);
       return postListResponse;
    }


    public long addLike(Long postId, Long userId) {
        Optional<Like> checkLike = likeRepository.findById(new PostLikeId(userId, postId));
        if(checkLike.isPresent()) {
            throw new CustomException(ApiResponseErrorMessage.ALREADY_LIKED);
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        likeRepository.save(new Like(userId, postId));
        notificationService.notifyPostLiked(post.getUser().getId(), userId, postId);
        return likeRepository.countByIdPostId(postId);
    }

    public long deleteLike(Long postId, Long userId) {
        Optional<Like> checkLike = likeRepository.findById(new PostLikeId(userId, postId));
        if(checkLike.isPresent()) {
            likeRepository.delete(new Like(userId, postId));
        }
        return likeRepository.countByIdPostId(postId);
    }

    public Post editPost(Long postId, PostTempRequest request, MultipartFile[] images, Long userId) {
        if(images != null){
            for(MultipartFile file : images){
                UploadFile uploadFile = fileService.upload(file);
                postImageRepository.save(new PostImage(uploadFile.getId(), postId));
            }
        }
        Post post = postRepository.findById(postId).
                orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        if(!post.getUser().getId().equals(userId)){
            throw new CustomException(ApiResponseErrorMessage.FORBIDDEN);
        }

        post.setEdited(true);
        if(request.getTitle() != null && !request.getTitle().isBlank()) post.setTitle(request.getTitle());
        if(request.getBody() != null && !request.getBody().isBlank()) post.setBody(request.getBody());
        if(request.getCategory() != null && !request.getCategory().isBlank()) post.setCategory(resolveCategory(request.getCategory()));


        return post;
    }

    public void addReports(Long postId, Long userId) {
        Optional<Report> checkReport =  postReportRepository.findById(new PostReportId(userId, postId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        if(checkReport.isEmpty()){
            postReportRepository.save(new Report(userId, postId));
            post.setReports(post.getReports() + 1);
        }
    }

    @Transactional(readOnly = true)
    public PostTempResponse getTempPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));

        List<PostImage> tempList = postImageRepository.findByIdPostId(postId);

        List<Long> imageList = tempList.stream()
                .map(postImage -> postImage.getId().getFileId())
                .toList();

        return new PostTempResponse(postId, post.getTitle(), post.getBody(), imageList,
                post.getCategory() != null ? post.getCategory().name() : null);
    }
}
