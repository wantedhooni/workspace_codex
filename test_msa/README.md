# MSA Commerce Blueprint (Admin / Application 공유 구조)

이 레포는 **커머스 도메인** 기준으로 MSA 아키텍처를 구성하고, **Admin / Application**이 공통 서비스(인증/권한/테넌트/메타/검색 등)를 어떻게 공유하는지 보여주는 샘플입니다.

## 구조
- `frontend/application`: 커머스 스토어프론트(UI)
- `frontend/admin`: 통합 어드민(UI)
- `backend`: Spring Boot 멀티모듈 (게이트웨이 + 서비스)
- `docs`: 아키텍처/운영 문서
- `infra`: 추후 인프라 스크립트 (docker-compose, k8s 등)

## 핵심 포인트
- Admin과 Application은 **서로 다른 게이트웨이**를 사용하지만, **공통 서비스들을 공유**합니다.
- 공통 서비스 예: `auth`, `tenant`, `metadata`, `search`, `notification`
- 각 도메인 서비스는 **자기 DB**를 소유하며, 공유가 필요한 값은 **API 또는 이벤트**로 동기화합니다.

## 실행(예정)
추후 `infra`에 docker-compose, Spring Cloud Gateway, 각 서비스 설정을 추가합니다.
