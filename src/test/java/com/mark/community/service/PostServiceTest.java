package com.mark.community.service;


import com.mark.community.dto.*;
import com.mark.community.entity.*;
import com.mark.community.entity.key.PostLikeId;
import com.mark.community.entity.key.PostReportId;
import com.mark.community.enums.FileType;
import com.mark.community.enums.UserRole;
import com.mark.community.exception.CustomException;
import com.mark.community.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostImageRepository postImageRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private ViewRepository viewRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostReportRepository postReportRepository;
    @Mock
    private FileService fileService;
    @Mock
    private UserService userService;

    @InjectMocks
    private PostService postService;

    @Test
    void 임시글작성시_유저_조회에_실패하면_예외를_던진다(){
        PostTempRequest request = new PostTempRequest("제목", "내용");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> postService.postTemp(request, 1L));
    }

    @Test
    void 임시글작성시_정상적으로_저장된다(){
        PostTempRequest request = new PostTempRequest("제목", "내용");
        User user = new User("test@gmail.com", "zkxpqn1", "juseung");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.postTemp(request, 1L);

        assertEquals("제목", result.getTitle());
        assertEquals("내용", result.getBody());
    }

    @Test
    void 임시글자동저장시_게시글_조회에_실패하면_예외를_던진다(){
        PostTempRequest request = new PostTempRequest("제목", "내용");
        CustomUserDetails userDetails = createUserDetails();
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> postService.postAutoTemp(1L, request, null, userDetails));
    }

    @Test
    void 임시글자동저장시_이미지가_없으면_업로드하지_않고_제목과_내용만_수정된다(){
        PostTempRequest request = new PostTempRequest("새제목", "새내용");
        Post post = new Post("기존제목", "기존내용");
        User writer = createUser();
        writer.setId(1L);
        post.setUser(writer);
        CustomUserDetails userDetails = createUserDetails();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostTempResponse response = postService.postAutoTemp(1L, request, null, userDetails);

        assertEquals("새제목", post.getTitle());
        assertEquals("새내용", post.getBody());
        assertTrue(response.getImages().isEmpty());
        verify(fileService, never()).upload(any());
    }

    @Test
    void 임시글자동저장시_이미지가_있으면_각각_업로드되고_이미지목록이_반환된다(){
        PostTempRequest request = new PostTempRequest("새제목", "새내용");
        Post post = new Post("기존제목", "기존내용");
        User writer = createUser();
        writer.setId(1L);
        post.setUser(writer);
        CustomUserDetails userDetails = createUserDetails();

        MultipartFile[] images = new MultipartFile[]{
                new MockMultipartFile("images", "photo1.png", "image/png", new byte[1]),
                new MockMultipartFile("images", "photo1.png", "image/png", new byte[1])
        };

        UploadFile uploadFile1 = new UploadFile("photo1.png", "/path1", FileType.IMAGE, 1L);
        uploadFile1.setId(1L);
        UploadFile uploadFile2 = new UploadFile("photo2.png", "/path2", FileType.IMAGE, 2L);
        uploadFile2.setId(2L);

        when(fileService.upload(any(MultipartFile.class))).thenReturn(uploadFile1, uploadFile2);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostTempResponse response = postService.postAutoTemp(1L, request, images, userDetails);

        assertEquals(2, response.getImages().size());
        assertTrue(response.getImages().containsAll(List.of(1L, 2L)));
        verify(postImageRepository, times(2)).save(any(PostImage.class));
    }

    @Test
    void 게시글삭제시_게시글_조회에_실패하면_예외를_던진다(){
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> postService.deletePost(1L, 1L));
    }

    @Test
    void 게시글삭제시_작성자가_아니면_예외를_던진다(){
        User writer = createUser();
        writer.setId(100L);
        Post post = new Post("제목", "내용", writer);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        assertThrows(CustomException.class, () -> postService.deletePost(1L, 1L));
    }

    @Test
    void 게시글삭제시_작성자가_맞으면_deleted가_true로_변경된다(){
        User writer = createUser();
        writer.setId(100L);
        Post post = new Post("제목", "내용", writer);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L, 100L);

        assertTrue(post.isDeleted());
    }

    @Test
    void 게시글조회시_게시글_조회에_실패하면_예외를_던진다(){
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> postService.getPost(1L, 1L));
    }

    @Test
    void 게시글조회시_탈퇴한_작성자면_닉네임이_알수없음으로_표시된다(){
        User writer = createUser();
        writer.setId(100L);
        Post post = new Post("제목", "내용", writer);
        post.setPostTime(new Date());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageRepository.findByIdPostId(1L)).thenReturn(List.of());
        when(userService.existsUser(100L)).thenReturn(false);
        when(viewRepository.findById(any())).thenReturn(Optional.empty());

        PostResponse response = postService.getPost(1L, 999L);

        assertEquals("알 수 없음", response.getNickname());
    }

    @Test
    void 게시글조회시_프로필이미지가_없으면_thumbnailId가_null이다(){
        User writer = createUser();
        writer.setId(100L);
        Post post = new Post("제목", "내용", writer);
        post.setPostTime(new Date());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageRepository.findByIdPostId(1L)).thenReturn(List.of());
        when(userService.existsUser(100L)).thenReturn(true);
        when(viewRepository.findById(any())).thenReturn(Optional.empty());

        PostResponse response = postService.getPost(1L, 999L);

        assertNull(response.getThumbnailId());
    }

    @Test
    void 게시글조회시_본인글이면_permission이_true다(){
        User writer = createUser();
        writer.setId(100L);
        Post post = new Post("제목", "내용", writer);
        post.setPostTime(new Date());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageRepository.findByIdPostId(1L)).thenReturn(List.of());
        when(userService.existsUser(100L)).thenReturn(true);
        when(viewRepository.findById(any())).thenReturn(Optional.empty());

        PostResponse response = postService.getPost(1L, 100L);

        assertTrue(response.isPermission());
    }

    @Test
    void 조회수증가시_24시간이내_최조조회면_조회수가_1_증가한다(){
        Post post = new Post("제목", "내용");
        post.setViews(0);
        when(viewRepository.findById(any())).thenReturn(Optional.empty());

        postService.increasePostViews(post, 1L);

        assertEquals(1, post.getViews());
        verify(viewRepository).save(any(View.class));
    }

    @Test
    void 조회수증가시_24시간이내_재조회면_조회수가_증가하지_않는다(){
        Post post = new Post("제목", "내용");
        post.setViews(5);
        View recentView = new View(1L, null);
        when(viewRepository.findById(any())).thenReturn(Optional.of(recentView));

        postService.increasePostViews(post, 1L);

        assertEquals(5, post.getViews());
        verify(viewRepository, never()).save(any());
    }

    @Test
    void 게시글저장시_게시글_조회에_실패하면_예외를_던진다(){
        PostRequest request = new PostRequest("제목", "내용", null);
        CustomUserDetails userDetails = createUserDetails();
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> postService.savePost(1L, request, null, userDetails));
    }

    @Test
    void 게시글저장시_제목이나_내용이_비어있으면_예외를_던진다(){
        Post post = new Post("기존제목", "기존내용");
        User writer = createUser();
        writer.setId(1L);
        post.setUser(writer);
        PostRequest request = new PostRequest("", "내용", null);
        CustomUserDetails userDetails = createUserDetails();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(CustomException.class, () -> postService.savePost(1L, request, null, userDetails));
    }

    @Test
    void 게시글저장시_정상적으로_저장되고_temp가_false로_변경된다(){
        Post post = new Post("기존제목", "기존내용");
        User writer = createUser();
        writer.setId(1L);
        post.setUser(writer);
        PostRequest request = new PostRequest("새제목", "새내용", null);
        CustomUserDetails userDetails = createUserDetails();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.savePost(1L, request, null, userDetails);

        assertEquals("새제목", result.getTitle());
        assertEquals("새내용", result.getBody());
        assertFalse(result.isTemp());
        assertNotNull(result.getPostTime());
    }

    @Test
    void 게시글저장시_최근1분간_10개이상_작성했으면_예외를_던진다(){
        Post post = new Post("기존제목", "기존내용");
        User writer = createUser();
        writer.setId(1L);
        post.setUser(writer);
        PostRequest request = new PostRequest("새제목", "새내용", null);
        CustomUserDetails userDetails = createUserDetails();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.countRecentPostsByUser(eq(1L), any(Date.class))).thenReturn(10L);

        assertThrows(CustomException.class, () -> postService.savePost(1L, request, null, userDetails));
    }

    @Test
    void 게시글목록조회시_lastPostId가_없으면_findPosts_pageable만_호출된다(){
        User writer = createUser();
        Post post = new Post("제목", "내용", writer);
        post.setPostTime(new Date());
        when(postRepository.findPosts(any(Pageable.class))).thenReturn(List.of(post));
        when(likeRepository.countGroupByPostIds(any())).thenReturn(List.of());
        when(commentRepository.countGroupedByPostIds(any())).thenReturn(List.of());

        PostListResponse response = postService.getPosts(10, null);

        assertEquals(1, response.getTotal());
        verify(postRepository).findPosts(any(Pageable.class));
        verify(postRepository, never()).findPosts(any(Long.class), any(Pageable.class));
    }

    @Test
    void 게시글목록조회시_lastPostId가_있으면_해당_id기준_findPosts가_호출된다(){
        User writer = createUser();
        Post post = new Post("제목", "내용", writer);
        post.setPostTime(new Date());
        when(postRepository.findPosts(eq(5L), any(Pageable.class))).thenReturn(List.of(post));
        when(likeRepository.countGroupByPostIds(any())).thenReturn(List.of());
        when(commentRepository.countGroupedByPostIds(any())).thenReturn(List.of());

        PostListResponse response = postService.getPosts(10, 5L);

        assertEquals(1, response.getTotal());
        verify(postRepository).findPosts(eq(5L), any(Pageable.class));
    }

    @Test
    void 게시글목록조회시_탈퇴한_작성자면_닉네임이_알수없음으로_표시된다(){
        User writer = createUser();
        writer.setDeleted(true);
        Post post = new Post("제목", "내용", writer);
        post.setPostTime(new Date());
        when(postRepository.findPosts(any(Pageable.class))).thenReturn(List.of(post));
        when(likeRepository.countGroupByPostIds(any())).thenReturn(List.of());
        when(commentRepository.countGroupedByPostIds(any())).thenReturn(List.of());

        PostListResponse response = postService.getPosts(10, null);

        assertEquals("알 수 없음", response.getList().get(0).getNickname());
    }

    @Test
    void 게시글목록조회시_신고가_5회이상이면_blind가_true로_표시된다(){
        User writer = createUser();
        Post post = new Post("제목", "내용", writer);
        post.setPostTime(new Date());
        post.setReports(5);
        when(postRepository.findPosts(any(Pageable.class))).thenReturn(List.of(post));
        when(likeRepository.countGroupByPostIds(any())).thenReturn(List.of());
        when(commentRepository.countGroupedByPostIds(any())).thenReturn(List.of());

        PostListResponse response = postService.getPosts(10, null);

        assertTrue(response.getList().get(0).isBlind());
    }

    @Test
    void 좋아요추가시_이미_좋아요한_상태면_예외를_던진다(){
        when(likeRepository.findById(any(PostLikeId.class))).thenReturn(Optional.of(new Like(1L, 1L)));

        assertThrows(CustomException.class, () -> postService.addLike(1L, 1L));

        verify(likeRepository, never()).save(any());
    }

    @Test
    void 좋아요추가시_좋아요한적_없으면_저장된다(){
        when(likeRepository.findById(any(PostLikeId.class))).thenReturn(Optional.empty());

        postService.addLike(1L, 1L);

        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void 좋아요삭제시_좋아요한_상태면_삭제된다(){
        when(likeRepository.findById(any(PostLikeId.class))).thenReturn(Optional.of(new Like(1L, 1L)));

        postService.deleteLike(1L, 1L);

        verify(likeRepository).delete(any(Like.class));
    }

    @Test
    void 좋아요삭제시_좋아요한적_없으면_삭제하지_않는다(){
        when(likeRepository.findById(any(PostLikeId.class))).thenReturn(Optional.empty());

        postService.deleteLike(1L, 1L);

        verify(likeRepository, never()).delete(any());
    }

    @Test
    void 게시글수정시_게시글_조회에_실패하면_예외를_던진다(){
        PostTempRequest request = new PostTempRequest("제목", "내용");
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> postService.editPost(1L, request, null, 1L));
    }

    @Test
    void 게시글수정시_작성자가_아니면_예외를_던진다(){
        PostTempRequest request = new PostTempRequest("제목", "내용");
        User writer = createUser();
        writer.setId(1L);
        Post post = new Post("기존제목", "기존내용", writer);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(CustomException.class, () -> postService.editPost(1L, request, null, 999L));
    }

    @Test
    void 게시글수정시_작성자가_맞으면_edited가_true로_변경되고_제목과_내용이_수정된다(){
        PostTempRequest request = new PostTempRequest("새제목", "새내용");
        User writer = createUser();
        writer.setId(100L);
        Post post = new Post("기존제목", "기존내용", writer);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.editPost(1L, request, null, 100L);

        assertTrue(result.isEdited());
        assertEquals("새제목", result.getTitle());
        assertEquals("새내용", result.getBody());
    }

    @Test
    void 게시글수정시_이미지가_여러개면_전부_저장된다(){
        PostTempRequest request = new PostTempRequest("새제목", "새내용");

        User writer = createUser();

        writer.setId(1L);
        MultipartFile[] images = new MultipartFile[]{
                 new MockMultipartFile("images1", "photo1.png", "image/png", new byte[1]),
                 new MockMultipartFile("images2", "photo2.png", "image/png", new byte[1]),
                 new MockMultipartFile("images3", "photo3.png", "image/png", new byte[1])
        };
        UploadFile uploadFile1 = new UploadFile("photo1.png", "/path1", FileType.IMAGE, 1L);
        UploadFile uploadFile2 = new UploadFile("photo2.png", "/path2", FileType.IMAGE, 1L);
        UploadFile uploadFile3 = new UploadFile("photo3.png", "/path3", FileType.IMAGE, 1L);

        Post post = new Post("제목", "본문", writer);

        uploadFile1.setId(1L);
        uploadFile2.setId(2L);
        uploadFile3.setId(3L);

        when(fileService.upload(any(MultipartFile.class))).thenReturn(uploadFile1, uploadFile2, uploadFile3);
        when(postImageRepository.save(any(PostImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        postService.editPost(1L, request, images, 1L);

        verify(postImageRepository, times(3)).save(any(PostImage.class));

    }

    @Test
    void 게시글신고시_게시글_조회에_실패하면_예외를_던진다(){
        when(postReportRepository.findById(any(PostReportId.class))).thenReturn(Optional.empty());
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> postService.addReports(1L, 1L));
    }

    @Test
    void 게시글신고시_이미_신고한_상태면_신고수가_증가하지_않는다(){
        Post post = new Post("제목", "내용");
        post.setReports(3);
        when(postReportRepository.findById(any(PostReportId.class))).thenReturn(Optional.of(new Report(1L, 1L)));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.addReports(1L, 1L);

        assertEquals(3, post.getReports());
        verify(postReportRepository, never()).save(any());
    }

    @Test
    void 게시글신고시_처음_신고하면_신고수가_1_증가한다(){
        Post post = new Post("제목", "내용");
        post.setReports(0);
        when(postReportRepository.findById(any(PostReportId.class))).thenReturn(Optional.empty());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.addReports(1L, 1L);

        assertEquals(1, post.getReports());
        verify(postReportRepository).save(any(Report.class));
    }

    @Test
    void 임시글조회시_게시글_조회에_실패하면_예외를_던진다(){
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> postService.getTempPost(1L));
    }

    @Test
    void 임시글조회시_정상적으로_조회된다(){
        Post post = new Post("제목", "내용");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageRepository.findByIdPostId(1L)).thenReturn(List.of());

        PostTempResponse response = postService.getTempPost(1L);

        assertEquals("제목", response.getTitle());
        assertEquals("내용", response.getBody());
    }

    @Test
    void 게시글_저장시_작성자가_아니면_예외를_던진다(){
        PostRequest request = new PostRequest("제목", "내용", List.of("F1"));
        Post post = new Post("제목", "내용");
        post.setId(1L);
        User writer = createUser();
        CustomUserDetails userDetails = createUserDetails();
        writer.setId(2L);
        post.setUser(writer);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(CustomException.class,() -> postService.savePost(1L, request, null, userDetails));
    }

    @Test
    void 게시글_자동임시저장시_작성자가_아니면_예외를_던진다(){
        PostTempRequest request = new PostTempRequest("제목", "내용");
        Post post = new Post("제목", "내용");
        post.setId(1L);
        User writer = createUser();
        CustomUserDetails userDetails = createUserDetails();
        writer.setId(2L);
        post.setUser(writer);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(CustomException.class,() -> postService.postAutoTemp(1L, request, null, userDetails));
    }

    private User createUser(){
        return new User("test@gmail.com", "zkxpqn1", "test1");
    }

    private CustomUserDetails createUserDetails(){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(UserRole.ROLE_AUTH_USER.getValue()));
        return new CustomUserDetails(1L, "test@gmail.com", "juseung", 1L, authorities);
    }


}
