package com.mark.community.service;

import com.mark.community.dto.EditUserRequest;
import com.mark.community.dto.RegisterRequest;
import com.mark.community.dto.RegisterResponse;
import com.mark.community.entity.UploadFile;
import com.mark.community.entity.User;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
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

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ApiResponseErrorMessage.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ApiResponseErrorMessage.DUPLICATE_NICKNAME);
        }


        UploadFile uploadFile = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            uploadFile = fileService.upload(profileImage);
        }

        User user;
        if(uploadFile != null){
            user = new User(request.getEmail(), request.getPassword(), request.getNickname(), uploadFile);
        } else {
            user = new User(request.getEmail(), request.getPassword(), request.getNickname());
        }

        User registerUser = userRepository.save(user);

        return new RegisterResponse(registerUser.getId());

    }

    public void editUser(EditUserRequest request, MultipartFile image , Long userId){
        User user = userRepository.findById(userId).
                orElseThrow(() -> new CustomException(ApiResponseErrorMessage.USER_NOT_FOUND));

        UploadFile uploadFile = null;
        if(image != null){
            uploadFile = fileService.upload(image);

        }

        if(request.getNickname() == null && request.getPassword() == null){
            throw new CustomException(ApiResponseErrorMessage.INVALID_REQUEST);
        }


        if(request.getPassword() == null && !request.getNickname().isBlank()){
            if(uploadFile != null){
                user.setFile(uploadFile);
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

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.USER_NOT_FOUND));
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean existsUser(Long userId){
        return userRepository.existsByIdAndDeletedFalse(userId);

    }
}
