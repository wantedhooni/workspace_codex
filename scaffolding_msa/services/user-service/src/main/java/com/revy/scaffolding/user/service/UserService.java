package com.revy.scaffolding.user.service;

import com.revy.scaffolding.core.exception.BusinessException;
import com.revy.scaffolding.user.domain.User;
import com.revy.scaffolding.user.domain.UserStatus;
import com.revy.scaffolding.user.domain.repository.UserRepository;
import com.revy.scaffolding.user.dto.UserCreateRequest;
import com.revy.scaffolding.user.dto.UserDetailResponse;
import com.revy.scaffolding.user.dto.UserLookupResponse;
import com.revy.scaffolding.user.dto.UserSummaryResponse;
import com.revy.scaffolding.user.exception.UserNotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDetailResponse create(UserCreateRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new BusinessException("USER_EMAIL_DUPLICATED", "이미 등록된 이메일입니다.", HttpStatus.CONFLICT);
        });
        User user = userRepository.save(new User(request.email(), request.name(), UserStatus.ACTIVE));
        return UserDetailResponse.from(user);
    }

    public UserDetailResponse get(Long userId) {
        return UserDetailResponse.from(load(userId));
    }

    public UserLookupResponse getLookup(Long userId) {
        return UserLookupResponse.from(load(userId));
    }

    public List<UserSummaryResponse> search(String keyword) {
        return userRepository.search(keyword);
    }

    private User load(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}

