package com.mark.community.controller;


import com.mark.community.dto.EditUserRequest;
import com.mark.community.dto.RegisterRequest;
import com.mark.community.dto.RegisterResponse;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.messages.ApiResponseMessage;
import com.mark.community.response.ApiResponse;
import com.mark.community.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }
        @PostMapping
        public ResponseEntity<?> registerUser(@RequestPart("request") RegisterRequest request,
                                           @RequestPart("profileImage") MultipartFile profileImage){
        RegisterResponse registerResponse =  userService.registerUser(request, profileImage);

            return ResponseEntity
                    .status(ApiResponseMessage.SUCCESS_REGISTER.getStatusCode())
                    .header("Location", "/users/" + registerResponse.getUserId())
                    .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_REGISTER, registerResponse));
        }

        @PatchMapping
        public ResponseEntity<?> editUser(
                @RequestPart("request") EditUserRequest request,
                @RequestPart("image") MultipartFile image,
                HttpServletRequest httpRequest){
            HttpSession session = httpRequest.getSession(false);
            if(session == null){
                throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
            }
            Long userId = (Long) session.getAttribute("userId");

            userService.editUser(request, image, userId);

            return ResponseEntity
                    .status(ApiResponseMessage.SUCCESS_UPDATE_USER.getStatusCode())
                    .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_UPDATE_USER));
        }

        @DeleteMapping
        public ResponseEntity<?> deleteUser(HttpServletRequest httpRequest){
            HttpSession session = httpRequest.getSession(false);
            if(session == null){
                throw new CustomException(ApiResponseErrorMessage.EXPIRED_SESSION);
            }
            Long userId = (Long) session.getAttribute("userId");
            userService.deleteUser(userId);

            return ResponseEntity
                    .status(ApiResponseMessage.SUCCESS_DELETE_USER.getStatusCode())
                    .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_USER));
        }
    }
