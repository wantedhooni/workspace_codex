package com.example.marketsignal.user;

import com.example.marketsignal.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 프로필 관련 기능을 처리한다.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 현재 사용자 프로필을 조회한다.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(CurrentUser currentUser) {
        return UserProfileResponse.from(findUser(currentUser.id()));
    }

    /**
     * 현재 사용자 프로필을 수정한다.
     */
    @Transactional
    public UserProfileResponse updateMyProfile(CurrentUser currentUser, UpdateProfileRequest request) {
        User user = findUser(currentUser.id());
        user.updateProfile(request.name(), request.bio());
        return UserProfileResponse.from(user);
    }

    /**
     * 사용자 식별자로 엔티티를 조회한다.
     */
    @Transactional(readOnly = true)
    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
