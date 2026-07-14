package com.example.commerce.user.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.commerce.user.application.UserService;

/**
 * 커머스 회원 등록과 조회 HTTP API를 제공하는 컨트롤러다.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * 회원 애플리케이션 서비스를 주입받아 컨트롤러를 생성한다.
     *
     * @param userService 회원 애플리케이션 서비스
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 입력값 검증 후 신규 회원을 생성한다.
     *
     * @param request 회원 등록 요청
     * @return 생성 위치와 회원 정보
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = UserResponse.from(userService.createUser(request.email(), request.name()));
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    /**
     * 회원 식별자로 단일 회원을 조회한다.
     *
     * @param id 회원 식별자
     * @return 회원 정보
     */
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return UserResponse.from(userService.getUser(id));
    }

    /**
     * 전체 회원을 가입 순서로 조회한다.
     *
     * @return 회원 목록
     */
    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getUsers().stream().map(UserResponse::from).toList();
    }
}

