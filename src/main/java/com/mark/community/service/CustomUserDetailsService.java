package com.mark.community.service;

import com.mark.community.dto.CustomUserDetails;
import com.mark.community.entity.User;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedFalse(username)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.USER_NOT_FOUND));


        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().getValue()));

        Long profileFileId = user.getProfileFile() != null ? user.getProfileFile().getId() : null;

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                profileFileId,
                authorities
        );
    }
}
