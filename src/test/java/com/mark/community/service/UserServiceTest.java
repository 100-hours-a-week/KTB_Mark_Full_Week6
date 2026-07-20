package com.mark.community.service;


import com.mark.community.dto.EditUserRequest;
import com.mark.community.dto.RegisterRequest;
import com.mark.community.dto.RegisterResponse;
import com.mark.community.dto.UserResponse;
import com.mark.community.entity.UploadFile;
import com.mark.community.entity.User;
import com.mark.community.enums.FileType;
import com.mark.community.exception.CustomException;
import com.mark.community.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileService fileService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;



    @ParameterizedTest
    @MethodSource("registerRequestData")
    void 회원가입시_Request_값이_비어있으면_예외를_발생시킨다(RegisterRequest request){
        assertThrows(CustomException.class, () -> userService.registerUser(request, null));
    }

    @Test
    void 회원가입시_이메일이_중복되면_예외를_던진다(){
        RegisterRequest request =
                new RegisterRequest("test@gmail.com", "zkxpqn!!11", "juseung");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

        assertThrows(CustomException.class, () -> userService.registerUser(request, null));
    }

    @Test
    void 회원가입시_닉네임이_중복되면_예외를_던진다(){
        RegisterRequest request = new RegisterRequest("test@gmail.com", "zkxpqn!!11", "juseung");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(userRepository.existsByNickname("juseung")).thenReturn(true);

        assertThrows(CustomException.class, () -> userService.registerUser(request, null));
    }

    @Test
    void 회원가입시_프로필이미지가_null이면_업로드하지_않고_회원가입에_성공한다(){
        RegisterRequest request = new RegisterRequest("test@gmail.com", "zkxpqn!!11", "juseung");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = userService.registerUser(request, null);

        assertNotNull(response);
        verify(fileService, never()).upload(any());
    }

    @Test
    void 회원가입시_프로필이미지가_있으면_업로드하고_회원가입에_성공한다(){
        RegisterRequest request = new RegisterRequest("test@gmail.com", "zkxpqn!!11", "juseung");
        MultipartFile realFile = new MockMultipartFile("profileImage", "photo.png", "image/png", new byte[1]);
        UploadFile uploadFile = new UploadFile("photo.png", "/path", FileType.IMAGE, 1L);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(fileService.upload(realFile)).thenReturn(uploadFile);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = userService.registerUser(request, realFile);

        assertNotNull(response);
        verify(fileService).upload(realFile);
    }

    @Test
    void 회원수정시_유저_조회에_실패하면_예외를_던진다(){
        EditUserRequest request = new EditUserRequest("test1", "zkxpqn1");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> userService.editUser(request, null, 1L));
    }

    @Test
    void 회원수정시_닉네임_변경만_요청하고_이미지가_없으면_파일_업로드는_호출되지_않고_닉네임만_변경된다(){
        EditUserRequest request = new EditUserRequest("test2", null);
        User user = createUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.editUser(request, null, 1L);

        assertEquals(request.getNickname(), user.getNickname());
        verify(fileService, never()).upload(any());
    }

    @Test
    void 회원수정시_닉네임과_비밀번호_둘다_값이_있으면_예외를_던진다(){
        EditUserRequest request = new EditUserRequest("test2", "zkxpqn1");
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(CustomException.class, () -> userService.editUser(request, null, 1L));
    }

    @Test
    void 회원수정시_닉네임과_이미지를_함께_요청하면_닉네임과_프로필이미지가_변경된다(){
        EditUserRequest request = new EditUserRequest("test2", null);
        User user = createUser();
        MultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", new byte[1]);
        UploadFile uploadFile = new UploadFile("photo.png", "/path", FileType.IMAGE, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileService.upload(image)).thenReturn(uploadFile);

        userService.editUser(request, image, 1L);

        assertEquals(request.getNickname(), user.getNickname());
        verify(fileService).upload(image);
    }

    @Test
    void 회원수정시_비밀번호만_요청하면_암호화되어_변경된다(){
        EditUserRequest request = new EditUserRequest(null, "newPassword1!");
        User user = createUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.encode("newPassword1!")).thenReturn("encodedNewPassword");

        userService.editUser(request, null, 1L);

        assertEquals("encodedNewPassword", user.getPassword());
        assertNotEquals(request.getPassword(), user.getPassword());
        verify(fileService, never()).upload(any());
    }

    @Test
    void 회원탈퇴시_유저_조회에_실패하면_예외를_던진다(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void 회원탈퇴시_deleted_상태가_true로_변경된다(){
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertTrue(user.isDeleted());
    }

    @Test
    void 유저조회시_존재하면_true를_반환한다(){
        when(userRepository.existsByIdAndDeletedFalse(1L)).thenReturn(true);

        boolean result = userService.existsUser(1L);

        assertTrue(result);
    }

    @Test
    void 유저조회시_존재하지_않으면_false를_반환한다(){
        when(userRepository.existsByIdAndDeletedFalse(1L)).thenReturn(false);

        boolean result = userService.existsUser(1L);

        assertFalse(result);
    }

    @Test
    void 유저조회시_조회에_실패하면_예외를_던진다(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> userService.getUser(1L));
    }

    @Test
    void 유저조회시_프로필이미지가_없으면_profileFileId가_null로_반환된다(){
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(1L);

        assertNull(response.getProfileFileId());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    void 유저조회시_프로필이미지가_있으면_profileFileId가_반환된다(){
        UploadFile uploadFile = new UploadFile("photo.png", "/path", FileType.IMAGE, 1L);
        User user = new User("test@gmail.com", "zkxpqn1", "test1", uploadFile);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(1L);

        assertEquals(uploadFile.getId(), response.getProfileFileId());
    }

    private static Stream<RegisterRequest> registerRequestData() {
        return Stream.of(
                new RegisterRequest("", "Zkxpqn!!11", "juseung"),
                new RegisterRequest("test", "", "juseung"),
                new RegisterRequest("test", "Zkxpqn!!11", "")
        );
    }

    private User createUser(){
        return new User("test@gmail.com", "zkxpqn1", "test1");
    }
}
