package com.mark.community.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private Long profileFileId;
    private Long userId;

    public LoginResponse(Long profileFileId, Long userId){
        this.profileFileId = profileFileId;
        this.userId = userId;
    }
}
