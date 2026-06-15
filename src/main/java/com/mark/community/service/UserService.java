package com.mark.community.service;

import com.mark.community.dto.EditUserRequest;
import com.mark.community.dto.RegisterRequest;
import com.mark.community.dto.RegisterResponse;
import com.mark.community.entity.User;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FileService fileService;

    public UserService(UserRepository userRepository,
                       FileService fileService) {
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public RegisterResponse registerUser(RegisterRequest request, MultipartFile profileImage) {

        if(!StringUtils.hasText(request.getEmail()) ||
                !StringUtils.hasText(request.getNickname()) ||
                !StringUtils.hasText(request.getPassword())){
            throw new CustomException(ApiResponseErrorMessage.MISSING_REQUIRED_PARAMETER);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ApiResponseErrorMessage.DUPLICATE_EMAIL);
        }

        String fileId = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            fileId = fileService.upload(profileImage);
        }

        User user;
        if(fileId != null){
            user = new User(request.getEmail(), request.getPassword(), request.getNickname(), fileId);
        } else {
            user = new User(request.getEmail(), request.getPassword(), request.getNickname());
        }

        User registerUser = userRepository.save(user);

        return new RegisterResponse(registerUser.getUserId());

    }

    public void editUser(@RequestPart("request") EditUserRequest request, MultipartFile image , String userId){
        User user = userRepository.findById(userId).
                orElseThrow(() -> new CustomException(ApiResponseErrorMessage.USER_NOT_FOUND));

        String fileId = null;
        if(image != null){
            fileId = fileService.upload(image);
        }

        if(request.getNickname() == null && request.getPassword() == null){
            throw new CustomException(ApiResponseErrorMessage.INVALID_REQUEST);
        }


        if(request.getPassword() == null && !request.getNickname().isBlank()){
            if(fileId != null && !fileId.isBlank()){
                user.setProfileImage(fileId);
            }
            user.setNickname(request.getNickname());
            userRepository.save(user);
        } else if(request.getNickname() == null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
            userRepository.save(user);
        } else {
            throw new CustomException(ApiResponseErrorMessage.INVALID_REQUEST);
        }
    }

    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.USER_NOT_FOUND));
        user.setDeleted(true);
        userRepository.save(user);
    }

    public boolean existUser(String userId){
        return userRepository.existByUser(userId);

    }
}
