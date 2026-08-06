package com.mark.community.controller;


import com.mark.community.dto.*;
import com.mark.community.messages.ApiResponseMessage;
import com.mark.community.response.ApiResponse;
import com.mark.community.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                                           @RequestPart(value = "profileImage", required = false) MultipartFile profileImage){
        RegisterResponse registerResponse =  userService.registerUser(request, profileImage);

            return ResponseEntity
                    .status(ApiResponseMessage.SUCCESS_REGISTER.getStatusCode())
                    .header("Location", "/users/" + registerResponse.getUserId())
                    .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_REGISTER, registerResponse));
        }

        @PatchMapping
        public ResponseEntity<?> editUser(
                @RequestPart("request") EditUserRequest request,
                @RequestPart(value = "image", required = false) MultipartFile image,
                @AuthenticationPrincipal CustomUserDetails userDetails){

            userService.editUser(request, image, userDetails.getId());

            return ResponseEntity
                    .status(ApiResponseMessage.SUCCESS_UPDATE_USER.getStatusCode())
                    .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_UPDATE_USER));
        }

        @DeleteMapping
        public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails){
            userService.deleteUser(userDetails.getId());

            return ResponseEntity
                    .status(ApiResponseMessage.SUCCESS_DELETE_USER.getStatusCode())
                    .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_USER));
        }

    @GetMapping
    public ResponseEntity<?> getUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        UserResponse userResponse = userService.getUser(userDetails.getId());
        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_USER.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_USER, userResponse));
    }

    }
