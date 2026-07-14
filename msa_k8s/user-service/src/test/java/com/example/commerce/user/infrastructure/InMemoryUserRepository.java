package com.example.commerce.user.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.example.commerce.user.domain.DuplicateEmailException;
import com.example.commerce.user.domain.User;
import com.example.commerce.user.domain.UserRepository;

/**
 * 회원 서비스 단위 테스트에서 사용하는 인메모리 저장소다.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, UUID> userIdsByEmail = new ConcurrentHashMap<>();

    /**
     * 비어 있는 테스트 저장소를 생성한다.
     */
    public InMemoryUserRepository() {
    }

    /**
     * 이메일 중복을 검사한 뒤 회원을 저장한다.
     *
     * @param user 저장할 회원
     * @return 저장된 회원
     */
    @Override
    public synchronized User save(User user) {
        if (userIdsByEmail.containsKey(user.email())) {
            throw new DuplicateEmailException(user.email());
        }
        usersById.put(user.id(), user);
        userIdsByEmail.put(user.email(), user.id());
        return user;
    }

    /**
     * 식별자로 회원을 조회한다.
     *
     * @param id 회원 식별자
     * @return 존재할 경우 회원
     */
    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(usersById.get(id));
    }

    /**
     * 이메일로 회원을 조회한다.
     *
     * @param email 정규화된 이메일
     * @return 존재할 경우 회원
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userIdsByEmail.get(email)).flatMap(this::findById);
    }

    /**
     * 가입 시각과 식별자 순서로 전체 회원을 반환한다.
     *
     * @return 정렬된 회원 목록
     */
    @Override
    public List<User> findAll() {
        return usersById.values().stream()
                .sorted(Comparator.comparing(User::createdAt).thenComparing(User::id))
                .toList();
    }
}
