package com.example.websample.config;

import com.example.websample.domain.post.Post;
import com.example.websample.domain.post.PostRepository;
import com.example.websample.domain.user.User;
import com.example.websample.domain.user.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 로컬 실행과 데모 테스트에 필요한 기본 데이터를 준비합니다. */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final String USER_DEMO_EMAIL = "user@example.com";
    private static final String USER_DEMO_PASSWORD = "User1234!";
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            PostRepository postRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 데모 사용자와 예시 게시글이 없을 때 초기 데이터를 생성합니다. */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findByEmail(USER_DEMO_EMAIL)
                .orElseGet(() -> userRepository.save(User.create(
                        USER_DEMO_EMAIL,
                        passwordEncoder.encode(USER_DEMO_PASSWORD),
                        "일반 사용자"
                )));
        createSamplePostIfNeeded(user);
    }

    private void createSamplePostIfNeeded(User user) {
        if (postRepository.count() > 0) {
            return;
        }
        postRepository.save(Post.create("첫 번째 게시글", "JWT 인증 후 게시글을 작성, 수정, 삭제할 수 있습니다.", user));
    }
}
