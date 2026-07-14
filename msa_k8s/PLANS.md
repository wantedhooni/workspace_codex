# PLANS.md

## 목표

Spring Boot 3.x, Java 21, Spring Cloud Kubernetes를 사용하는 커머스·금융 MSA 프로젝트를 실무형 멀티모듈 구조로 구축한다. API Gateway, User, Account, Contents Service를 Kubernetes에 배포하고 LGTM 스택으로 로그, 메트릭, 트레이스를 관측할 수 있어야 한다.

## 구현 계획

1. 프로젝트 기반 구성
   - Gradle Wrapper와 Groovy DSL 멀티모듈 설정
   - API Gateway와 각 업무 서비스의 독립 버전 관리
   - 공통 Java 21, 테스트, 의존성 관리 규칙 구성
   - 로컬 및 Kubernetes 실행 프로파일 분리

2. API Gateway
   - Spring Cloud Gateway 기반 라우팅
   - Keycloak OAuth2 Client 로그인과 Bearer JWT Resource Server 구성
   - Kubernetes 서비스 디스커버리 기반 User Service 연동
   - 상관관계 ID 전파, 오류 처리, Actuator 및 관측성 구성

3. User Service
   - 사용자 등록, 단건 조회, 목록 조회 API
   - 요청 검증, 중복 검사, 일관된 오류 응답
   - PostgreSQL JPA 저장소와 Flyway migration
   - Redis 회원 조회 캐시
   - 서비스 계층 단위 테스트와 API 통합 테스트

4. 금융 서비스
   - Account Service 계좌 개설, 멱등 입출금, 불변 거래 원장
   - 계좌 행 잠금과 DB 제약을 이용한 잔액 정합성
   - Contents Service 공지사항·게시물 상태 전이와 낙관적 잠금
   - 복합 키 커서 페이지네이션과 PostgreSQL 인덱스

5. 컨테이너 및 Kubernetes
   - 공통 멀티스테이지 Dockerfile과 Docker Buildx 기반 이미지 빌드
   - Docker Compose 기반 전체 애플리케이션·데이터 인프라 실행
   - 로컬·개발 Keycloak realm import와 staging/production 외부 IdP Secret 연동
   - local, dev, staging, production 환경별 이미지 빌드 및 Registry 업로드
   - Namespace, ServiceAccount, RBAC, ConfigMap, Deployment, Service, Ingress
   - readiness/liveness probe, 리소스 요청/제한, 보안 컨텍스트
   - Kustomize 기반 base와 local overlay

6. LGTM 관측성
   - OpenTelemetry Collector를 통한 OTLP 로그, 메트릭, 트레이스 수집
   - Loki, Grafana, Tempo, Mimir 로컬 배포 구성
   - Grafana 데이터 소스 프로비저닝과 기본 대시보드 제공
   - 공통 메트릭 모듈과 업무 성공·실패·처리시간 표준화

7. 검증 및 문서
   - Gradle 빌드와 테스트
   - Docker/Kubernetes 설정 정적 검증
   - 로컬 빌드, 이미지 생성, 클러스터 배포, API 호출, 관측성 확인 절차 문서화

## 완료 조건

- `./gradlew clean test`와 `./gradlew clean build`가 성공한다.
- API Gateway를 통해 User, Account, Contents Service API를 호출할 수 있다.
- Kubernetes 리소스가 Kustomize로 정상 렌더링된다.
- 애플리케이션 로그, 메트릭, 트레이스가 LGTM 각 백엔드로 전달되도록 구성된다.
- README만으로 개발자가 로컬 또는 Kubernetes 환경을 재현할 수 있다.

## 진행 상태

- [x] Gradle Groovy DSL 멀티모듈 및 Java 21 구성
- [x] 모듈별 버전 및 환경별 Docker 이미지 정책
- [x] API Gateway 라우팅과 상관관계 ID 전파
- [x] User Service 회원 등록·조회와 오류 응답
- [x] Account Service 계좌·멱등 입출금·거래 원장
- [x] Contents Service 상태 전이·버전 충돌·커서 조회
- [x] PostgreSQL 영속화, Flyway, Redis 캐시, Testcontainers
- [x] 개발용 PostgreSQL/Redis StatefulSet과 PVC
- [x] PDB, topology spread, NetworkPolicy, Pod Security
- [x] 공통 Dockerfile과 Gradle Docker Buildx 태스크
- [x] Docker Compose 전체 서비스 빌드·기동
- [x] Keycloak 기반 OAuth2 Client/Resource Server와 공통 보안 모듈
- [x] Kubernetes base/local overlay, RBAC, 프로브, HPA
- [x] OpenTelemetry Collector와 LGTM 개발 스택
- [x] Grafana 데이터 소스 및 Commerce 기본 대시보드
- [x] Common Metrics 자동 구성, 공통 태그, 업무 지표와 카디널리티 제한
- [x] 최종 전체 빌드, 이미지 빌드, 매니페스트 검증
