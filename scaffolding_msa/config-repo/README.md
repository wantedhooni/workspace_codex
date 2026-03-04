# config-repo

`config-server`가 읽는 기본 설정 저장소다. 현재는 로컬 파일 기반 `native` 저장소로 두고 있으며, Vault 값과 함께 합쳐서 제공된다.

## 파일 규칙
- `application.yml`: 모든 서비스 공통 기본값
- `<service-name>.yml`: 서비스별 설정
- 파일명은 `spring.application.name`과 맞춰야 한다.
- 민감정보는 가능하면 여기에 두지 않고 Vault 경로로 분리한다.

## 예시 조회
```bash
curl http://localhost:8888/config/user-service/local
curl http://localhost:8888/config/order-service/local
```

## Vault와 함께 사용할 때
- 파일 기반 설정은 일반 구성값
- Vault는 비밀번호, 토큰, 키 같은 민감정보 저장용
- 기본 경로 예시는 `secret/application`, `secret/user-service`, `secret/order-service`다.

## 선택형 Config Client와의 관계
- 이 저장소의 값은 `SPRING_CONFIG_IMPORT=optional:configserver:`를 준 서비스만 읽는다.
- Config Server를 사용하지 않는 서비스는 각 모듈의 `application.yml`만 사용한다.
