package com.mark.community.service;

import com.mark.community.dto.CommentRequest;

import com.mark.community.dto.CommentResponse;
import com.mark.community.dto.CustomUserDetails;
import com.mark.community.entity.Comment;
import com.mark.community.entity.Post;
import com.mark.community.entity.User;
import com.mark.community.enums.UserRole;
import com.mark.community.exception.CustomException;
import com.mark.community.repository.CommentRepository;
import com.mark.community.repository.PostRepository;
import com.mark.community.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;


    @Test
    void 댓글작성시_유저_조회에_실패하면_예외를_던진다(){
        CommentRequest request = new CommentRequest("댓글내용", null);
        CustomUserDetails userDetails = createUserDetails();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> commentService.commentSave(1L, request, userDetails));
    }

    @Test
    void 댓글작성시_유저와_게시글이_있으면_작성된다(){
        CommentRequest request = new CommentRequest("작성내용", null);
        User user = createUser();
        Post post = new Post("제목", "본문");
        CustomUserDetails userDetails = createUserDetails();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postRepository.getReferenceById(anyLong())).thenReturn(post);
        CommentResponse commentResponse = commentService.commentSave(anyLong(), request, userDetails);
        assertEquals("작성내용", commentResponse.getComment());
    }

    @Test
    void 댓글수정시_댓글_조회에_실패하면_예외를_던진다(){
        CommentRequest request = new CommentRequest("수정내용", null);
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> commentService.editComment(1L, request, 1L));
    }

    @Test
    void 댓글수정시_작성자가_아니면_예외를_던진다(){
        CommentRequest request = new CommentRequest("수정내용", null);
        User writer = createUser();
        writer.setId(100L);
        Comment comment = new Comment(null, writer, "기존내용", new Date(), null);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(CustomException.class, () -> commentService.editComment(1L, request, 999L));
    }

    @Test
    void 댓글수정시_작성자가_맞으면_내용이_수정된다(){
        CommentRequest request = new CommentRequest("수정내용", null);
        User writer = createUser();
        writer.setId(100L);
        Comment comment = new Comment(null, writer, "기존내용", new Date(), null);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment result = commentService.editComment(1L, request, 100L);

        assertEquals("수정내용", result.getComment());
    }

    @Test
    void 댓글삭제시_댓글_조회에_실패하면_예외를_던진다(){
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> commentService.deleteComment(1L, 1L));
    }

    @Test
    void 댓글삭제시_작성자가_아니면_예외를_던진다(){
        User writer = createUser();
        writer.setId(100L);
        Comment comment = new Comment(null, writer, "내용", new Date(), null);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(CustomException.class, () -> commentService.deleteComment(1L, 999L));
    }

    @Test
    void 댓글삭제시_작성자가_맞으면_deleted가_true로_변경된다(){
        User writer = createUser();
        writer.setId(100L);
        Comment comment = new Comment(null, writer, "내용", new Date(), null);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment result = commentService.deleteComment(1L, 100L);

        assertTrue(result.isDeleted());
    }

    @Test
    void 댓글목록조회시_삭제된_댓글은_마스킹되어_표시된다(){
        User writer = createUser();
        Comment deletedComment = new Comment(null, writer, "삭제될 내용", new Date(), null);
        deletedComment.setDeleted(true);
        when(commentRepository.findByPostId(1L)).thenReturn(List.of(deletedComment));

        List<CommentResponse> responses = commentService.getComments(1L, 999L);

        assertEquals("삭제된 댓글입니다", responses.get(0).getComment());
    }

    @Test
    void 댓글목록조회시_정상_댓글은_내용_그대로_표시된다(){
        User writer = createUser();
        Comment comment = new Comment(null, writer, "정상 댓글", new Date(), null);
        when(commentRepository.findByPostId(1L)).thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getComments(1L, 999L);

        assertEquals("정상 댓글", responses.get(0).getComment());
    }

    @Test
    void 댓글3개_이상_작성시_유저가_등업된다(){
        CommentRequest request = new CommentRequest("작성내용", null);
        User user = createUser();
        Post post = new Post("제목", "본문");
        CustomUserDetails userDetails = createUserDetails();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postRepository.getReferenceById(anyLong())).thenReturn(post);
        when(commentRepository.countByUserId(1L)).thenReturn(3L);
        commentService.commentSave(anyLong(), request, userDetails);
        verify(userService, times(1)).changeAuthorization(1L);
        verify(authService, times(1)).changeSessionAuthorization(UserRole.ROLE_AUTH_USER);
    }

    private User createUser(){
        return new User("test@gmail.com", "zkxpqn1  ", "juseung", UserRole.ROLE_USER);
    }

    private CustomUserDetails createUserDetails(){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(UserRole.ROLE_AUTH_USER.getValue()));
        return new CustomUserDetails(1L, "test@gmail.com", "juseung", 1L, authorities);
    }

}
