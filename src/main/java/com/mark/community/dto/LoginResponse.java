package com.mark.community.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private Long profileFileId;

    public LoginResponse(Long profileFileId){
        this.profileFileId = profileFileId;
    }
}
