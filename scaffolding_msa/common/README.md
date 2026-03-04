# common

공통 모듈 디렉터리다. 현재는 `core` 하나만 두고 있으며, 여러 서비스가 함께 사용하는 응답 포맷, 예외 처리, 공통 유틸을 배치하는 기준점이다.

## 포함 모듈
- `core`: `ApiResponse`, `ApiError`, `BusinessException`, `GlobalExceptionHandler` 제공

## 운영 원칙
- 서비스별 비즈니스 규칙은 넣지 않는다.
- 재사용성이 명확한 코드만 이동한다.
- 공통 모듈 변경은 모든 서비스에 영향을 주므로 응답 계약 변경은 신중히 관리한다.

