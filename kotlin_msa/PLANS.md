# PLANS.md

## 목표

커머스 도메인을 주제로 Java 21, Kotlin, Gradle Kotlin DSL, Spring Boot 4, Spring Cloud 2025.1, Spring Cloud Kubernetes, Istio 기반 MSA 예제 프로젝트를 구성한다.

## 아키텍처

- `api-gateway`: 외부 요청 진입점. Spring Cloud Gateway WebFlux 기반 라우팅 담당.
- `product-service`: 상품 카탈로그 조회 및 등록 담당.
- `inventory-service`: 재고 조회, 예약, 해제 담당.
- `payment-service`: 결제 승인 시뮬레이션 담당.
- `order-service`: 주문 생성과 주문 조회 담당. 재고와 결제 서비스 HTTP API를 호출한다.

## 구현 범위

- 멀티 모듈 Gradle Kotlin DSL 구성
- 서비스별 Spring Boot 애플리케이션과 REST API
- 서비스별 인메모리 저장소 및 비즈니스 서비스
- Spring Cloud OpenFeign 기반 서비스 간 HTTP 호출
- Spring Cloud Kubernetes Fabric8 연동 의존성
- Kubernetes Deployment, Service, ConfigMap, ServiceAccount, RBAC
- Istio Gateway, VirtualService, DestinationRule
- 로컬 및 클러스터 실행 안내 문서

## 검증 계획

- Gradle Wrapper 생성
- `./gradlew build`로 전체 모듈 컴파일 및 테스트 수행
- 빌드 실패 시 Boot 4 및 Cloud 2025.1의 변경 사항에 맞춰 의존성/코드를 조정

