package com.mark.community.service;

import com.mark.community.dto.CommentRequest;
import com.mark.community.dto.CommentResponse;
import com.mark.community.dto.CustomUserDetails;
import com.mark.community.entity.Comment;
import com.mark.community.entity.Post;
import com.mark.community.entity.User;
import com.mark.community.enums.UserRole;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.repository.CommentRepository;
import com.mark.community.repository.PostRepository;
import com.mark.community.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AuthService authService;
    private final UserService userService;
    private final NotificationService notificationService;

    public CommentResponse commentSave(Long postId, CommentRequest request, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.POST_NOT_FOUND));


        Comment commentData = new Comment(
                post,
                user,
                request.getComment(),
                new Date(),
                request.getParentCommentId()
        );
        Comment comment = commentRepository.save(commentData);

        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            notificationService.notifyNewComment(post.getUser().getId(), userDetails.getId(), postId, comment.getId());
        } else {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.COMMENT_NOT_FOUND));
            notificationService.notifyNewReply(post.getUser().getId(), parentComment.getUser().getId(), userDetails.getId(), postId, comment.getId());
        }

        if (user.getRole() != UserRole.ROLE_AUTH_USER) {
            long commentCount = commentRepository.countByUserId(userDetails.getId());
            if (commentCount >= 3) {
                userService.changeAuthorization(userDetails.getId());
                authService.changeSessionAuthorization(UserRole.ROLE_AUTH_USER);
            }
        }
        CommentResponse commentResponse = new CommentResponse(comment.getId(), comment.getComment() ,user.getRole().getValue());

        return commentResponse;
    }

    public Comment editComment(Long commentId, CommentRequest commentRequest, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.COMMENT_NOT_FOUND));

        if((!comment.getUser().getId().equals(userId))){
            throw new CustomException(ApiResponseErrorMessage.FORBIDDEN);
        }

        comment.setComment(commentRequest.getComment());
        comment.setCommentTime(new Date());

        return comment;
    }

    public Comment deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.COMMENT_NOT_FOUND));

        if(!comment.getUser().getId().equals(userId)){
            throw new CustomException(ApiResponseErrorMessage.FORBIDDEN);
        }

        comment.setDeleted(true);

        return comment;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId, Long userId) {
        List<Comment> comments = commentRepository.findByPostId(postId);

        return comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getUser().getNickname(),
                        comment.isDeleted() ? "삭제된 댓글입니다" : comment.getComment(),
                        comment.getUser().getId(),
                        comment.getParentCommentId(),
                        comment.isDeleted(),
                        userId.equals(comment.getUser().getId())
                )).toList();

    }
}
