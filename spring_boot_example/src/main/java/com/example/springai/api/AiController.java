package com.example.springai.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
public class AiController {

	private final ChatClient chatClient;

	public AiController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@GetMapping("/ask")
	public AskResponse ask(@RequestParam("q") String question) {
		String normalizedQuestion = requireNotBlank(question, "q");

		String answer = chatClient.prompt()
				.system("당신은 Spring Boot 백엔드 개발을 돕는 시니어 코파일럿이다. 답변은 한국어로 간결하게 제공한다.")
				.user(normalizedQuestion)
				.call()
				.content();

		return new AskResponse(answer);
	}

	@PostMapping("/summarize")
	public SummaryResponse summarize(@RequestBody SummaryRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		String text = requireNotBlank(request.text(), "text");
		int maxSentences = clamp(request.maxSentences(), 3, 1, 10);

		String summary = chatClient.prompt()
				.system("당신은 백엔드 코드/문서 요약 전문가다. 핵심만 짧게 정리한다.")
				.user("""
					아래 내용을 %d문장 이하로 요약해 주세요.
					반드시 한국어로 답변하세요.
					
					--- 원문 시작 ---
					%s
					--- 원문 끝 ---
					""".formatted(maxSentences, text))
				.call()
				.content();

		return new SummaryResponse(summary);
	}

	@PostMapping("/rbac-draft")
	public RbacDraftResponse rbacDraft(@RequestBody RbacDraftRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		String serviceName = requireNotBlank(request.serviceName(), "serviceName");
		List<String> roles = sanitizeList(request.roles(), "roles");
		List<String> features = sanitizeList(request.features(), "features");

		String draftJson = chatClient.prompt()
				.system("당신은 관리자 백오피스 RBAC 설계 전문가다. 지시된 JSON 포맷만 정확히 반환한다.")
				.user("""
					서비스명: %s
					역할 목록: %s
					기능 목록: %s
					
					아래 요구사항을 만족하는 RBAC 초안을 JSON으로 생성해 주세요.
					- 코드블록(```) 없이 JSON 본문만 반환
					- permissions.code는 `도메인.행위` 형식 (예: menu.read, content.publish)
					- menuItems.path는 `/admin/...` 형태
					- roleBindings에 전달된 모든 role이 반드시 포함
					- 설명(description)은 한국어
					
					출력 JSON 스키마:
					{
					  "serviceName": "string",
					  "menuItems": [
					    { "name": "string", "path": "string", "requiredPermissions": ["string"] }
					  ],
					  "permissions": [
					    { "code": "string", "description": "string" }
					  ],
					  "roleBindings": [
					    { "role": "string", "permissions": ["string"] }
					  ]
					}
					""".formatted(
						serviceName,
						String.join(", ", roles),
						String.join(", ", features)))
				.call()
				.content();

		return new RbacDraftResponse(draftJson);
	}

	private String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw badRequest(fieldName + " 값은 비어 있을 수 없습니다.");
		}
		return value.trim();
	}

	private List<String> sanitizeList(List<String> values, String fieldName) {
		if (values == null) {
			throw badRequest(fieldName + " 값은 비어 있을 수 없습니다.");
		}

		List<String> normalized = values.stream()
				.filter(value -> value != null && !value.isBlank())
				.map(String::trim)
				.collect(Collectors.toList());

		if (normalized.isEmpty()) {
			throw badRequest(fieldName + " 값은 최소 1개 이상 필요합니다.");
		}
		return normalized;
	}

	private int clamp(Integer value, int defaultValue, int min, int max) {
		int normalized = (value == null) ? defaultValue : value;
		if (normalized < min) {
			return min;
		}
		return Math.min(normalized, max);
	}

	private ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	public record AskResponse(String answer) {
	}

	public record SummaryRequest(String text, Integer maxSentences) {
	}

	public record SummaryResponse(String summary) {
	}

	public record RbacDraftRequest(String serviceName, List<String> roles, List<String> features) {
	}

	public record RbacDraftResponse(String draftJson) {
	}
}
