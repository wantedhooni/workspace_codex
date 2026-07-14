# Keycloak 인증/인가 가이드

## 구성 요약

- Keycloak realm: `commerce`
- Gateway OAuth2 Client: `commerce-gateway`
- Resource Server client id: `commerce-api`
- 로컬 토큰 발급 client: `commerce-cli`
- 공통 보안 모듈: `common-security`

Gateway는 두 인증 방식을 모두 지원한다.

- Bearer JWT 요청: API 클라이언트가 전달한 `Authorization: Bearer ...`를 검증하고 다운스트림으로 그대로 전달한다.
- OAuth2 Login 요청: Gateway가 Keycloak authorization code 로그인을 처리하고, WebSession의 access token을 다운스트림 서비스에 전달한다.

User, Account, Contents Service는 Resource Server로 동작하며 Keycloak JWT의 `realm_access.roles`, `resource_access[commerce-api].roles`, `scope`를 Spring Security 권한으로 변환한다.

## 권한 정책

| API | 요구 role |
| --- | --- |
| `/api/v1/users/**` | `commerce-user` 또는 `commerce-admin` |
| `/api/v1/accounts/**` | `commerce-account` 또는 `commerce-admin` |
| `GET /api/v1/contents/**` | `commerce-contents`, `commerce-contents-editor`, `commerce-admin` 중 하나 |
| Contents 쓰기 API | `commerce-contents-editor` 또는 `commerce-admin` |
| `/actuator/health/**`, `/actuator/info`, `/actuator/prometheus` | 공개. NetworkPolicy와 운영망 통제로 보호 |

현재 Account/Contents 요청의 `ownerId`, `authorId`는 API 입력값을 사용한다. 운영 수준의 소유권 검증은 JWT `sub`와 도메인 리소스 소유자 비교 또는 별도 권한 정책 서비스로 확장해야 한다.

## 로컬 Docker Compose

전체 스택은 Keycloak을 포함해 인증이 켜진 상태로 실행된다.

```bash
cp .env.example .env
docker compose up -d --build --wait
docker compose ps
```

Keycloak Admin Console:

- URL: `http://localhost:8090/admin`
- 기본 계정: `.env`의 `KEYCLOAK_ADMIN_USERNAME`, `KEYCLOAK_ADMIN_PASSWORD`
- 기본 realm import 파일: `deploy/k8s/auth/commerce-realm.json`

로컬 CLI용 access token 발급:

```bash
ACCESS_TOKEN=$(
  curl -fsS -X POST 'http://localhost:8090/realms/commerce/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=password' \
    --data-urlencode 'client_id=commerce-cli' \
    --data-urlencode 'username=alice' \
    --data-urlencode 'password=alice-password' |
  sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
)
```

API 호출:

```bash
curl -i http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

로컬 Compose는 issuer와 JWKS 조회 주소를 분리한다.

- JWT issuer: `http://localhost:8090/realms/commerce`
- 컨테이너 내부 JWKS: `http://keycloak:8080/realms/commerce/protocol/openid-connect/certs`

이 구성이 필요한 이유는 브라우저와 로컬 CLI가 `localhost:8090`으로 토큰을 발급받지만, 컨테이너 내부 애플리케이션은 `keycloak:8080`으로 Keycloak에 접근해야 하기 때문이다.

## Kubernetes local/dev

`local`과 `dev` overlay는 Keycloak Deployment를 함께 배포한다.

```bash
kubectl apply -k deploy/k8s/overlays/local
kubectl -n commerce rollout status deployment/keycloak --timeout=5m
kubectl -n commerce port-forward service/keycloak 8090:8080
```

토큰 발급은 Compose와 동일하게 `http://localhost:8090/realms/commerce`를 사용한다. Gateway는 다음 설정으로 OAuth2 Client와 Resource Server를 동시에 활성화한다.

- `OAUTH2_CLIENT_ENABLED=true`
- `OAUTH2_ENABLED=true`
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:8090/realms/commerce`
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=http://keycloak:8080/realms/commerce/protocol/openid-connect/certs`

## Staging/Production

운영 계열 overlay는 Keycloak을 배포하지 않는다. 외부 IdP 또는 별도 Keycloak 클러스터를 전제로 한다.

필수 Secret:

```text
commerce-auth-credentials
```

필수 key:

```text
OAUTH2_GATEWAY_CLIENT_SECRET
```

운영에서 확인할 항목:

- `commerce-gateway` client의 redirect URI가 실제 Gateway URL과 일치
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`가 외부 issuer와 일치
- Gateway Pod가 Keycloak token/userinfo/JWKS endpoint에 접근 가능
- 서비스 Pod가 JWKS endpoint에 접근 가능
- TLS 인증서와 issuer hostname이 토큰의 `iss` claim과 일치
- client secret은 External Secrets Operator, Secrets Store CSI, 클라우드 Secret Manager 등으로 주입

## 운영 보안 기준

- password grant를 사용하는 `commerce-cli`는 로컬 개발용이다. 운영 클라이언트에는 authorization code + PKCE 또는 client credentials를 사용한다.
- Keycloak Admin bootstrap 계정은 초기화 후 영구 관리자 계정과 break-glass 절차로 대체한다.
- realm export 파일에 운영 client secret과 사용자 비밀번호를 저장하지 않는다.
- role은 업무 권한 단위로 관리하고, 사용자·계좌·콘텐츠 ID 같은 고카디널리티 값을 메트릭 태그나 role로 사용하지 않는다.
