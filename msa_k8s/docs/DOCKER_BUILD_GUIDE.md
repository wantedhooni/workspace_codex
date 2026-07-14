# Gradle 버전 관리 및 Docker 이미지 가이드

## 1. 목적

이 문서는 멀티모듈 프로젝트의 독립 버전 관리와 공통 `Dockerfile` 기반 이미지 빌드·업로드 방법을 정의한다.

지원 환경:

- `local`
- `dev`
- `staging`
- `production`

모든 실행 모듈은 저장소 루트의 동일한 멀티스테이지 `Dockerfile`을 사용한다. Docker Buildx가 Java 21 빌드 이미지에서 선택 모듈의 `bootJar`를 만들고, Java 21 JRE 런타임 이미지에는 실행 JAR만 복사한다.

## 2. 관련 파일

| 파일 | 역할 |
| --- | --- |
| `Dockerfile` | 모든 Spring Boot 실행 모듈의 공통 멀티스테이지 이미지 |
| `.dockerignore` | 빌드 컨텍스트에서 빌드 결과·문서·로컬 설정 제외 |
| `compose.yml` | PostgreSQL, Redis, 네 애플리케이션 빌드와 실행 |
| `gradle/docker.gradle` | 환경별 이미지 좌표, Buildx build/push 태스크 |
| `gradle.properties` | 모듈 버전, Registry, 기반 이미지 digest |
| `build.gradle` | 실행 모듈 집계 태스크 |

## 3. 공통 Dockerfile

필수 빌드 인자:

| 인자 | 예 | 설명 |
| --- | --- | --- |
| `MODULE` | `account-service` | 빌드할 실행 모듈 |
| `APP_PORT` | `8082` | 이미지에 기록할 애플리케이션 포트 |
| `APP_VERSION` | `0.1.0` | OCI 이미지 버전 label |
| `BUILD_IMAGE` | Java 21 JDK 이미지 | Gradle 빌드 단계 |
| `RUNTIME_IMAGE` | Java 21 JRE 이미지 | 최종 실행 단계 |

허용되는 `MODULE`:

```text
api-gateway
user-service
account-service
contents-service
```

이미지 보안 기본값:

- 빌드와 런타임 기반 이미지 digest 고정
- 최종 이미지에는 Gradle, JDK, 소스 코드 미포함
- UID/GID `1000:1000` 비루트 실행
- JVM 컨테이너 메모리 비율 `75%`
- OOM 발생 시 프로세스 종료
- Compose에서 read-only root filesystem, capability 제거, `no-new-privileges` 적용

## 4. 모듈 버전

`gradle.properties`에서 독립적으로 관리한다.

```properties
apiGatewayVersion=0.1.0
userServiceVersion=0.1.0
accountServiceVersion=0.1.0
contentsServiceVersion=0.1.0
commonMetricsVersion=0.1.0
```

확인:

```bash
./gradlew printVersions
```

`common-metrics`는 라이브러리이므로 테스트와 애플리케이션 이미지의 project dependency layer에는 포함되지만 독립 컨테이너 이미지는 만들지 않는다.

## 5. Docker Compose 전체 실행

환경 파일 준비:

```bash
cp .env.example .env
```

모든 이미지 빌드와 서비스 실행:

```bash
docker compose up -d --build --wait
docker compose ps
```

구동 서비스:

| 서비스 | 호스트 기본 포트 |
| --- | ---: |
| API Gateway | `8080` |
| User Service | `8081` |
| Account Service | `8082` |
| Contents Service | `8083` |
| PostgreSQL | `5432` |
| Redis | `6379` |
| Keycloak | `8090` |
| Keycloak management | `9000` |

인증이 기본 활성화되어 있으므로 API 테스트 전 Keycloak에서 토큰을 발급한다. 자세한 절차는 [Keycloak 인증/인가 가이드](AUTHENTICATION.md)를 따른다.

로그:

```bash
docker compose logs -f api-gateway user-service account-service contents-service
```

종료:

```bash
docker compose down
```

데이터 볼륨까지 삭제할 때만 실행:

```bash
docker compose down -v
```

PostgreSQL과 Redis만 실행해 IDE 또는 `bootRun` 애플리케이션과 연결하려면 서비스를 명시한다.

```bash
docker compose up -d --wait postgresql redis
```

## 6. Gradle Docker 태스크

이미지 좌표 확인:

```bash
./gradlew printImageCoordinatesAll -PtargetEnvironment=local
./gradlew printImageCoordinatesAll -PtargetEnvironment=dev
./gradlew printImageCoordinatesAll -PtargetEnvironment=staging
./gradlew printImageCoordinatesAll -PtargetEnvironment=production
```

로컬 Docker daemon에 네 이미지를 빌드:

```bash
./gradlew dockerBuildAll \
  -PtargetEnvironment=local \
  -PimageRegistry=commerce \
  -PimageTag=local
```

특정 모듈:

```bash
./gradlew :account-service:dockerBuild \
  -PtargetEnvironment=local \
  -PaccountServiceImageRegistry=commerce \
  -PaccountServiceImageTag=local
```

Docker Buildx 플랫폼은 호스트 아키텍처를 기본으로 사용한다. Kubernetes 노드용 이미지는 명시한다.

```bash
./gradlew dockerBuildAll \
  -PtargetEnvironment=staging \
  -PdockerArchitecture=amd64
```

## 7. 환경별 이미지 정책

| 환경 | 기본 Registry | 기본 태그 |
| --- | --- | --- |
| `local` | `commerce` | `{moduleVersion}-local` |
| `dev` | `commerce-dev` | `{moduleVersion}-dev` |
| `staging` | `commerce-staging` | `{moduleVersion}-rc` |
| `production` | `commerce-production` | `{moduleVersion}` |

태그 우선순위:

1. 모듈별 `apiGatewayImageTag`, `userServiceImageTag`, `accountServiceImageTag`, `contentsServiceImageTag`
2. 공통 `imageTag`
3. `IMAGE_TAG` 환경변수
4. 환경 기본 태그

Registry 우선순위:

1. 모듈별 `{modulePrefix}ImageRegistry`
2. 공통 `imageRegistry`
3. `{ENVIRONMENT}_IMAGE_REGISTRY` 환경변수
4. 환경 기본 Registry

## 8. Registry 업로드

로그인:

```bash
docker login registry.example.com
```

Dev:

```bash
./gradlew dockerPushAll \
  -PtargetEnvironment=dev \
  -PimageRegistry=registry.example.com/commerce-dev \
  -PimageTag=0.1.0-dev.abc123def456 \
  -PdockerArchitecture=amd64
```

Production:

```bash
./gradlew dockerPushAll \
  -PtargetEnvironment=production \
  -PimageRegistry=registry.example.com/commerce-production \
  -PapiGatewayImageTag=0.1.0 \
  -PuserServiceImageTag=0.1.0 \
  -PaccountServiceImageTag=0.1.0 \
  -PcontentsServiceImageTag=0.1.0 \
  -PdockerArchitecture=amd64
```

`dockerPush`는 Buildx `--push`, provenance, SBOM 생성을 사용한다. 업로드 태스크는 실수로 개발 기본 Registry에 push하지 않도록 명시적인 Registry 설정을 요구한다.

운영에서는:

- 동일 태그를 덮어쓰지 않는다.
- CI 전용 단기 Registry 자격증명을 사용한다.
- 이미지 취약점 검사와 서명을 수행한다.
- Kustomize에는 Registry가 반환한 digest를 반영한다.

## 9. CI 예시

```bash
API_VERSION="$(sed -n 's/^apiGatewayVersion=//p' gradle.properties)"
USER_VERSION="$(sed -n 's/^userServiceVersion=//p' gradle.properties)"
ACCOUNT_VERSION="$(sed -n 's/^accountServiceVersion=//p' gradle.properties)"
CONTENTS_VERSION="$(sed -n 's/^contentsServiceVersion=//p' gradle.properties)"
GIT_SHA="$(git rev-parse --short=12 HEAD)"

./gradlew clean test

./gradlew dockerPushAll \
  -PtargetEnvironment=dev \
  -PimageRegistry=registry.example.com/commerce-dev \
  -PapiGatewayImageTag="${API_VERSION}-dev.${GIT_SHA}" \
  -PuserServiceImageTag="${USER_VERSION}-dev.${GIT_SHA}" \
  -PaccountServiceImageTag="${ACCOUNT_VERSION}-dev.${GIT_SHA}" \
  -PcontentsServiceImageTag="${CONTENTS_VERSION}-dev.${GIT_SHA}" \
  -PdockerArchitecture=amd64
```

권장 순서:

1. `clean test`
2. Docker Buildx build/push
3. 취약점 검사
4. SBOM과 provenance 보관
5. 이미지 서명
6. Kustomize image digest 갱신
7. Kubernetes schema와 server-side dry-run
8. 승인 후 배포

## 10. Kustomize 반영

```yaml
images:
  - name: commerce/account-service
    newName: registry.example.com/commerce-production/account-service
    digest: sha256:IMAGE_DIGEST
```

검증:

```bash
kubectl kustomize deploy/k8s/overlays/production > /tmp/commerce-production.yml
kubectl apply --dry-run=server -f /tmp/commerce-production.yml
```

운영 overlay의 Registry, digest, OIDC issuer, 관리형 PostgreSQL·Redis endpoint를 실제 값으로 변경해야 한다.
