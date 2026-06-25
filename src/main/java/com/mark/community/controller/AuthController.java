package com.mark.community.controller;


import com.mark.community.dto.LoginRequest;
import com.mark.community.dto.LoginResponse;
import com.mark.community.entity.User;
import com.mark.community.messages.ApiResponseMessage;
import com.mark.community.response.ApiResponse;
import com.mark.community.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("login")
    public ResponseEntity<?> loginUser(HttpSession session, @RequestBody LoginRequest request){
        User user = authService.loginUser(request);
        session.setAttribute("userId", user.getId());
        Long fileId = user.getProfileFile() != null ? user.getProfileFile().getId() : null;
        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_LOGIN.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_LOGIN, new LoginResponse(fileId)));
    }

}
