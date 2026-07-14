# Commerce MSA Kubernetes 운영 가이드

## 1. 문서 목적

이 문서는 Commerce MSA를 Kubernetes에 배포하고 점검·변경·복구하기 위한 운영 런북이다. 대상 리소스는 다음과 같다.

| 구분 | Namespace | 리소스 |
| --- | --- | --- |
| 애플리케이션 | `commerce` | API Gateway, User, Account, Contents Service, Ingress, ConfigMap, RBAC, HPA |
| 관측성 | `observability` | OpenTelemetry Collector, Loki, Tempo, Mimir, Grafana |

모든 명령은 저장소 루트에서 실행한다.

> **중요:** Local/Dev 데이터 매니페스트는 개발 클러스터 실행용이다. 아래의 운영 투입 차단 조건을 해결하지 않은 상태로 실제 회원·계좌 데이터를 처리하면 안 된다.

## 2. 운영 투입 차단 조건

운영 환경 배포 승인 전에 다음 항목을 모두 해결해야 한다.

| 항목 | 현재 상태 | 운영 전 조치 |
| --- | --- | --- |
| 회원·계좌·콘텐츠 데이터 | 개발용 PostgreSQL StatefulSet | 서비스별 Managed PostgreSQL, 계좌 원장 PITR·대사·복구 훈련 구성 |
| Redis | 개발용 단일 StatefulSet | 운영 Managed Redis와 HA 구성 |
| LGTM 데이터 | 모든 백엔드가 `emptyDir` 사용 | PVC 또는 객체 스토리지, 보존 정책, 백업 구성 |
| Grafana 접근 | 익명 Viewer 활성화 | SSO 또는 인증 활성화, 익명 접근 차단 |
| 외부 통신 | 운영 overlay에 TLS/OAuth2 placeholder | 실제 인증서와 OIDC issuer 구성 |
| Secret | Local/Dev만 Kustomize Secret | 운영 External Secret Manager 구성 |
| 계획된 중단 | PDB와 hostname 분산 적용 | 운영 zone 분산 정책 추가 |
| 이미지 공급망 | 로컬 태그와 일반 이미지 참조 | 사설 Registry, 불변 태그 또는 digest, 스캔·서명 적용 |
| NetworkPolicy | Ingress 정책 적용 | 운영 endpoint 기준 Egress 정책 추가 |

Kubernetes Secret의 Base64 값은 암호화가 아니며, 기본적으로 etcd에 평문 저장될 수 있다. 운영 클러스터는 etcd 저장 암호화와 최소 권한 RBAC를 적용한다.

## 3. 운영 환경과 권한

### 3.1 필수 도구

- `kubectl`: 클러스터 버전과 지원되는 버전 차이 이내
- JDK 21과 Gradle Wrapper
- 로컬 Docker daemon 적재 시 `docker`
- `kustomize` 또는 `kubectl kustomize`
- 클러스터 접근용 kubeconfig
- 이미지 Registry push 권한
- 운영에서는 승인된 GitOps 도구 권장

### 3.2 셸 변수

명령 실행 전에 대상 환경을 명시한다.

```bash
export KUBE_CONTEXT="your-production-context"
export APP_NAMESPACE="commerce"
export OBS_NAMESPACE="observability"
export REGISTRY="registry.example.com/commerce"
export VERSION="$(git rev-parse --short=12 HEAD)"
```

### 3.3 대상 클러스터 확인

컨텍스트를 확인하지 않은 상태에서 변경 명령을 실행하지 않는다.

```bash
kubectl config use-context "${KUBE_CONTEXT}"
kubectl config current-context
kubectl cluster-info
kubectl get nodes -o wide
kubectl auth can-i get deployments -n "${APP_NAMESPACE}"
kubectl auth can-i patch deployments -n "${APP_NAMESPACE}"
```

운영 변경 전 노드와 시스템 Pod 상태를 확인한다.

```bash
kubectl get nodes
kubectl get pods -A \
  --field-selector='status.phase!=Running,status.phase!=Succeeded'
kubectl top nodes
```

`kubectl top`과 HPA는 Metrics API가 필요하다. `kubectl top nodes`가 실패하면 Metrics Server부터 복구한다.

## 4. 빌드와 이미지 배포

모듈별 버전, 환경별 태그, Registry 설정의 상세 기준은 [Gradle 버전 관리 및 Docker 이미지 가이드](DOCKER_BUILD_GUIDE.md)를 따른다.

### 4.1 테스트와 아티팩트 빌드

```bash
./gradlew clean test
./gradlew clean build
```

### 4.2 운영 이미지 빌드와 전송

이미지는 공통 멀티스테이지 Dockerfile과 Docker Buildx로 빌드한다. 동일 태그를 덮어쓰지 않고 Git commit SHA 같은 불변 버전을 사용한다.

```bash
./gradlew dockerPushAll \
  -PtargetEnvironment=production \
  -PimageRegistry="${REGISTRY}" \
  -PapiGatewayImageTag="0.1.0" \
  -PuserServiceImageTag="0.1.0" \
  -PaccountServiceImageTag="0.1.0" \
  -PcontentsServiceImageTag="0.1.0" \
  -PdockerArchitecture=amd64
```

Java 21 JDK/JRE 이미지는 `gradle.properties`와 공통 Dockerfile에서 digest로 고정되어 있다. 기반 이미지 보안 패치 시 두 기본값을 함께 변경하고 전체 이미지를 다시 검증한다.

```bash
./gradlew dockerPushAll \
  -PtargetEnvironment=production \
  -PimageRegistry="${REGISTRY}" \
  -PapiGatewayImageTag="0.1.0" \
  -PuserServiceImageTag="0.1.0" \
  -PaccountServiceImageTag="0.1.0" \
  -PcontentsServiceImageTag="0.1.0" \
  -PdockerArchitecture=amd64 \
  -PdockerBuildImage="eclipse-temurin:21-jdk-alpine@sha256:NEW_DIGEST" \
  -PdockerRuntimeImage="eclipse-temurin:21-jre-alpine@sha256:NEW_DIGEST"
```

`dockerArchitecture`는 실제 Kubernetes 노드의 `kubernetes.io/arch`와 일치해야 한다.

```bash
kubectl get nodes \
  -L kubernetes.io/arch \
  -o wide
```

노드 아키텍처가 혼합되어 있으면 아키텍처별 이미지를 만들고 OCI Image Index로 묶는 별도 CI 단계를 사용한다.

운영 권장사항:

- 운영과 개발에서 동일한 이미지 digest를 사용한다.
- `latest`와 재사용 가능한 태그를 사용하지 않는다.
- Docker credential helper 또는 CI 전용 단기 Registry 자격증명을 구성한다.
- Registry에서 취약점 스캔을 수행한다.
- 가능하면 SBOM을 생성하고 Cosign 등으로 이미지에 서명한다.
- Deployment에는 태그보다 `image@sha256:...` digest 고정을 권장한다.

## 5. 매니페스트 관리

### 5.1 현재 디렉터리 역할

```text
deploy/k8s/base                 공통 애플리케이션 리소스
deploy/k8s/overlays/local       로컬 이미지와 단일 복제본 설정
deploy/observability            개발용 단일 인스턴스 LGTM
```

`overlays/local`, `overlays/dev`와 현재 `deploy/observability`는 운영에 사용하지 않는다. `overlays/staging`과 `overlays/production`의 placeholder를 실제 환경 값으로 변경한다.

- Registry와 이미지 digest
- Ingress host, class, TLS
- 복제본, HPA, PDB
- 리소스 request/limit
- 외부 Secret 참조
- 관측성 OTLP endpoint와 trace sampling
- 운영 ConfigMap

긴급 변경을 제외하고 운영 리소스를 `kubectl edit`로 직접 수정하지 않는다. Git의 선언 상태를 변경하고 리뷰·승인 후 GitOps 또는 CI/CD로 적용한다.

### 5.2 렌더링과 스키마 검증

로컬 오버레이 검증:

```bash
kubectl kustomize deploy/k8s/overlays/local > /tmp/commerce-app.yml
kubectl kustomize deploy/observability > /tmp/commerce-observability.yml
```

클러스터 연결이 가능한 환경에서는 서버 스키마로 검증한다.

```bash
kubectl apply --dry-run=server -f /tmp/commerce-app.yml
kubectl apply --dry-run=server -f /tmp/commerce-observability.yml
```

클러스터 연결 없이 검증할 때:

```bash
docker run --rm \
  -v /tmp:/manifests \
  ghcr.io/yannh/kubeconform:latest \
  -strict -summary \
  /manifests/commerce-app.yml \
  /manifests/commerce-observability.yml
```

운영 CI에서는 검증 도구 버전을 `latest`가 아닌 고정 버전으로 사용한다.

## 6. 최초 배포

### 6.1 개발 클러스터 배포

공통 Dockerfile로 이미지를 빌드해 로컬 Docker daemon에 적재한다.

```bash
./gradlew dockerBuildAll \
  -PtargetEnvironment=local \
  -PimageRegistry=commerce \
  -PimageTag=local
```

kind 사용 시 이미지 적재:

```bash
kind load docker-image \
  commerce/api-gateway:local \
  commerce/user-service:local \
  commerce/account-service:local \
  commerce/contents-service:local
```

관측성을 먼저 배포한다. 애플리케이션의 trace endpoint가 `otel-collector.observability`를 참조하기 때문이다.

```bash
kubectl apply --server-side -k deploy/observability

kubectl -n observability rollout status deployment/loki --timeout=5m
kubectl -n observability rollout status deployment/tempo --timeout=5m
kubectl -n observability rollout status deployment/mimir --timeout=5m
kubectl -n observability rollout status deployment/grafana --timeout=5m
kubectl -n observability rollout status daemonset/otel-collector --timeout=5m
```

애플리케이션 배포:

```bash
kubectl apply --server-side -k deploy/k8s/overlays/local

kubectl -n commerce rollout status statefulset/postgresql --timeout=5m
kubectl -n commerce rollout status statefulset/redis --timeout=5m
kubectl -n commerce rollout status deployment/user-service --timeout=5m
kubectl -n commerce rollout status deployment/account-service --timeout=5m
kubectl -n commerce rollout status deployment/contents-service --timeout=5m
kubectl -n commerce rollout status deployment/api-gateway --timeout=5m
```

### 6.2 배포 직후 확인

```bash
kubectl get all -n commerce
kubectl get ingress,configmap,serviceaccount,role,rolebinding -n commerce
kubectl get all -n observability
kubectl get endpointslice -n commerce
```

필수 상태:

- API Gateway와 User/Account/Contents Service의 `READY`가 기대 Pod 수와 같다.
- API Gateway Service의 EndpointSlice에 Gateway Pod IP가 존재한다.
- 각 업무 Service의 EndpointSlice에 해당 Pod IP가 존재한다.
- PostgreSQL과 Redis StatefulSet이 Ready 상태이고 PVC가 Bound 상태다.
- API Gateway HPA가 `TARGETS`를 표시한다.
- 노드마다 OpenTelemetry Collector Pod가 1개 실행된다.

RBAC 확인:

```bash
kubectl auth can-i list services \
  --as=system:serviceaccount:commerce:commerce-app \
  -n commerce

kubectl auth can-i list endpointslices.discovery.k8s.io \
  --as=system:serviceaccount:commerce:commerce-app \
  -n commerce
```

### 6.3 API 스모크 테스트

Ingress가 준비되지 않았다면 포트 포워딩을 사용한다.

```bash
kubectl -n commerce port-forward service/api-gateway 8080:80
kubectl -n commerce port-forward service/keycloak 8090:8080
```

다른 터미널에서:

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

curl -fsS http://localhost:8080/actuator/health

curl -i -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: deploy-smoke-001' \
  -d '{"email":"smoke@example.com","name":"배포 점검"}'

curl -fsS http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"

curl -i -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"ownerId":"00000000-0000-0000-0000-000000000001","currency":"KRW"}'

curl -i -X POST http://localhost:8080/api/v1/contents \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{
    "type":"NOTICE",
    "title":"배포 점검 공지",
    "body":"Contents Service 배포 점검",
    "authorId":"00000000-0000-0000-0000-000000000001"
  }'
```

검증 기준:

- health 응답이 `UP`이다.
- 회원 생성 응답은 `201 Created`다.
- 계좌 개설과 콘텐츠 초안 생성 응답은 `201 Created`다.
- 응답의 `X-Correlation-Id`가 요청값과 같다.
- 목록 조회에서 생성한 회원이 확인된다.
- `ACCESS_TOKEN` 사용자 `alice`는 `commerce-user`, `commerce-account`, `commerce-contents-editor` role을 가진다.
- 스모크 테스트 데이터는 PostgreSQL에 남으므로 테스트 후 삭제 정책 또는 전용 테스트 계정을 사용한다.

## 7. 일상 점검

### 7.1 기본 상태 점검

```bash
kubectl get deployment,pod,service,hpa,ingress -n commerce -o wide
kubectl get deployment,pod,daemonset,service -n observability -o wide
kubectl get events -n commerce --sort-by=.metadata.creationTimestamp | tail -30
kubectl get events -n observability --sort-by=.metadata.creationTimestamp | tail -30
kubectl top pod -n commerce
kubectl top pod -n observability
```

### 7.2 프로브 점검

```bash
kubectl -n commerce port-forward deployment/api-gateway 18080:8080
curl -fsS http://localhost:18080/actuator/health/liveness
curl -fsS http://localhost:18080/actuator/health/readiness
```

- liveness 실패: 프로세스가 정상 동작하지 않아 kubelet 재시작 대상이다.
- readiness 실패: Service 트래픽에서 제외되며 원인을 확인해야 한다.
- 프로브 장애를 해결하려고 초기 대응으로 프로브를 제거하지 않는다.

### 7.3 로그 점검

최근 Gateway 로그:

```bash
kubectl logs -n commerce deployment/api-gateway \
  --all-pods=true \
  --since=15m \
  --prefix
```

이전 컨테이너 로그:

```bash
POD="$(kubectl get pod -n commerce \
  -l app.kubernetes.io/name=api-gateway \
  -o jsonpath='{.items[0].metadata.name}')"

kubectl logs -n commerce "${POD}" --previous
```

OpenTelemetry Collector 로그:

```bash
kubectl logs -n observability daemonset/otel-collector \
  --since=15m \
  --prefix
```

### 7.4 Grafana 확인

```bash
kubectl -n observability port-forward service/grafana 3000:3000
```

`http://localhost:3000`의 `Commerce MSA Overview`에서 요청률, 응답시간, 로그를 확인한다.

주요 PromQL 예시:

```promql
sum by (application) (rate(http_server_requests_seconds_count[5m]))
```

```promql
sum by (application, status) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
```

```promql
histogram_quantile(
  0.95,
  sum by (le, application) (
    rate(http_server_requests_seconds_bucket[5m])
  )
)
```

업무 작업 처리율:

```promql
sum by (environment, application, operation) (
  rate(commerce_business_operation_duration_seconds_count[5m])
)
```

업무 작업 실패율:

```promql
sum by (environment, application, operation) (
  rate(commerce_business_operation_duration_seconds_count{outcome="failure"}[5m])
)
/
clamp_min(
  sum by (environment, application, operation) (
    rate(commerce_business_operation_duration_seconds_count[5m])
  ),
  0.000001
)
```

공통 태그와 업무 메트릭 규칙은 [공통 메트릭 운영 가이드](METRICS.md)를 따른다. 각 환경 overlay는 `COMMERCE_OBSERVABILITY_METRICS_ENVIRONMENT`를 주입하며, 값이 대상 환경과 일치하는지 배포 전에 렌더링 결과에서 확인한다.

Loki에서 상관관계 ID 검색:

```logql
{service_name=~"api-gateway|user-service|account-service|contents-service"} |= "deploy-smoke-001"
```

운영 알림 최소 기준:

- 5분 HTTP 5xx 비율 임계치 초과
- p95 응답시간 임계치 초과
- Ready Pod 수 부족
- 재시작 횟수 급증 또는 `CrashLoopBackOff`
- CPU throttling, 메모리 사용량 또는 OOM 발생
- HPA가 최대 복제본에 지속 도달
- OTel Collector export 실패 또는 queue 적체
- Loki, Tempo, Mimir 저장공간과 오류율
- 인증서 만료 예정

임계치는 실제 SLO와 부하 테스트 결과로 확정한다.

## 8. 애플리케이션 변경 배포

### 8.1 표준 변경 절차

1. 테스트와 이미지 스캔을 통과한다.
2. 불변 이미지 digest를 생성한다.
3. 운영 overlay의 이미지 digest를 수정한다.
4. Kustomize 렌더링과 서버 dry-run을 수행한다.
5. 변경 리뷰와 승인을 받는다.
6. 트래픽이 낮은 승인된 시간에 배포한다.
7. rollout, 스모크 테스트, 핵심 지표를 확인한다.
8. 안정화 시간 동안 오류율과 지연시간을 감시한다.

배포 실행:

```bash
kubectl apply --server-side -k deploy/k8s/overlays/production
kubectl rollout status deployment/user-service -n commerce --timeout=5m
kubectl rollout status deployment/account-service -n commerce --timeout=5m
kubectl rollout status deployment/contents-service -n commerce --timeout=5m
kubectl rollout status deployment/api-gateway -n commerce --timeout=5m
```

운영 배포 전에 overlay의 Registry, digest, OIDC issuer, managed PostgreSQL·Redis endpoint와 외부 Secret을 실제 값으로 변경해야 한다.

### 8.2 긴급 이미지 교체

GitOps 경로를 사용할 수 없는 긴급 상황에서만 직접 변경한다.

```bash
kubectl set image deployment/api-gateway \
  api-gateway="${REGISTRY}/api-gateway:${VERSION}" \
  -n commerce

kubectl rollout status deployment/api-gateway \
  -n commerce \
  --timeout=5m
```

긴급 변경 후 즉시 Git 선언 상태에도 같은 digest를 반영해 drift를 제거한다.

### 8.3 ConfigMap 변경

Spring Cloud Kubernetes reload가 비활성화되어 있으므로 ConfigMap 변경 후 명시적으로 재시작한다.

```bash
kubectl apply --server-side -k deploy/k8s/overlays/production
kubectl rollout restart deployment/api-gateway -n commerce
kubectl rollout restart deployment/user-service -n commerce
kubectl rollout status deployment/api-gateway -n commerce --timeout=5m
kubectl rollout status deployment/user-service -n commerce --timeout=5m
```

ConfigMap 변경 전후 값을 비교하되 Secret 값은 터미널이나 CI 로그에 출력하지 않는다.

## 9. 롤백

### 9.1 롤백 판단 기준

다음 중 하나가 발생하고 즉시 수정이 불가능하면 롤백한다.

- rollout timeout 또는 Ready Pod 수 부족
- 배포 직후 5xx 비율이나 지연시간의 유의미한 증가
- 핵심 회원 API 스모크 테스트 실패
- OOM, 반복 재시작, 심각한 로그 오류
- 서비스 디스커버리 또는 설정 로드 실패

### 9.2 Deployment 롤백

이력 확인:

```bash
kubectl rollout history deployment/api-gateway -n commerce
kubectl rollout history deployment/user-service -n commerce
```

직전 Revision으로 롤백:

```bash
kubectl rollout undo deployment/api-gateway -n commerce
kubectl rollout status deployment/api-gateway -n commerce --timeout=5m
```

특정 Revision으로 롤백:

```bash
kubectl rollout undo deployment/api-gateway \
  -n commerce \
  --to-revision=2
```

롤백 후 스모크 테스트와 관측성 지표를 확인한다. GitOps 환경에서는 Git의 이미지 digest와 설정도 이전 승인 버전으로 되돌린다.

> 데이터베이스 스키마 변경은 Deployment 롤백만으로 복원되지 않는다. 운영 DB 도입 시 backward-compatible migration과 별도의 DB 복구 절차가 필요하다.

## 10. 스케일링

### 10.1 API Gateway

현재 HPA 설정:

- 최소 2개, 최대 6개 Pod
- CPU request 대비 평균 70%
- scale-down 안정화 시간 300초

상태 확인:

```bash
kubectl get hpa api-gateway -n commerce
kubectl describe hpa api-gateway -n commerce
kubectl top pod -n commerce \
  -l app.kubernetes.io/name=api-gateway
```

HPA가 `<unknown>`이면 다음을 확인한다.

1. Metrics Server와 Metrics API 상태
2. Pod의 CPU request 설정
3. 새 Pod의 readiness 상태
4. 노드 가용 CPU와 스케줄링 이벤트

### 10.2 User Service

User Service는 PostgreSQL 영속화와 Redis 공유 캐시를 사용하며 기본 2개, 최대 6개 Pod로 HPA가 구성되어 있다. managed PostgreSQL connection limit과 Hikari pool 총합을 확인한 뒤 최대 복제본을 조정한다. `kubectl scale`은 장애 대응용 임시 조치이며 Git 선언 상태를 함께 수정한다.

### 10.3 Account/Contents Service

두 서비스도 기본 2개, 최대 6개 Pod다. 각 Pod의 Hikari 기본 최대 pool은 10이므로 서비스별 최대 연결 수는 `최대 Pod 수 × DB_MAX_POOL_SIZE`에 Flyway와 운영 연결을 더해 계산한다. Account Service는 계좌별 행 잠금을 사용하므로 CPU보다 DB lock wait, 거래 지연, deadlock, connection pool 대기를 함께 관측한다.

## 11. 장애 대응

### 11.1 공통 초기 대응

```bash
kubectl get pod -n commerce -o wide
kubectl get events -n commerce --sort-by=.lastTimestamp | tail -50
kubectl describe deployment api-gateway -n commerce
kubectl describe deployment user-service -n commerce
kubectl get rs -n commerce
```

장애 대응 시 기록할 정보:

- 시작 시각과 탐지 경로
- 영향 API, 사용자 범위, 오류율
- 직전 이미지·ConfigMap·클러스터 변경
- 관련 Pod, 노드, Revision
- 실행한 명령과 결과
- 완화 및 복구 시각

### 11.2 `Pending`

대상 Pod 이름을 먼저 지정한다.

```bash
export POD_NAME="pending-pod-name"
```

```bash
kubectl describe pod -n commerce "${POD_NAME}"
kubectl get events -n commerce \
  --field-selector="involvedObject.name=${POD_NAME}"
kubectl top nodes
```

주요 원인:

- CPU 또는 메모리 부족
- taint/toleration 불일치
- 이미지 pull Secret 누락
- PVC 바인딩 실패
- 노드 selector 또는 affinity 불일치

### 11.3 `CrashLoopBackOff`

```bash
export POD_NAME="crash-loop-pod-name"

kubectl logs -n commerce "${POD_NAME}" --previous
kubectl describe pod -n commerce "${POD_NAME}"
kubectl get pod -n commerce "${POD_NAME}" \
  -o jsonpath='{.status.containerStatuses[*].lastState}'
```

주요 원인:

- 잘못된 환경 변수 또는 ConfigMap
- Kubernetes API/RBAC 접근 실패
- JVM OOM
- 애플리케이션 시작 예외
- liveness probe 반복 실패

### 11.4 Ready 실패

```bash
export POD_NAME="unready-pod-name"

kubectl describe pod -n commerce "${POD_NAME}"
kubectl logs -n commerce "${POD_NAME}" --since=15m
kubectl port-forward -n commerce "pod/${POD_NAME}" 18080:8080
curl -i http://localhost:18080/actuator/health/readiness
```

User Service는 컨테이너 포트가 `8081`이므로 해당 Pod를 직접 포워딩할 때 `18081:8081`을 사용한다.

### 11.5 Gateway 502/503

```bash
kubectl get service,endpointslice -n commerce
kubectl describe service user-service -n commerce
kubectl logs -n commerce deployment/api-gateway \
  --since=15m \
  --prefix
```

클러스터 내부 DNS와 Service 통신 확인:

```bash
kubectl run network-debug \
  -n commerce \
  --rm -it \
  --restart=Never \
  --image=curlimages/curl \
  -- sh
```

디버그 Pod 안에서:

```sh
nslookup user-service.commerce.svc.cluster.local
curl -i http://user-service/actuator/health/readiness
curl -i http://user-service/api/v1/users
```

확인 항목:

- User Service Pod readiness
- 인증 헤더 없이 `/api/v1/users`가 `401` 또는 `403`이면 네트워크는 연결되었고 Resource Server 정책이 동작하는 상태다.
- Service selector와 Pod label 일치
- EndpointSlice 존재
- `USER_SERVICE_URI=lb://user-service`
- Spring Cloud Kubernetes RBAC
- NetworkPolicy 또는 DNS 장애

### 11.6 관측 데이터 미수집

```bash
kubectl get pod -n observability -o wide
kubectl logs -n observability daemonset/otel-collector \
  --since=15m \
  --prefix
kubectl get service -n observability
kubectl get endpointslice -n observability
```

신호별 확인:

- 로그: Collector의 `/var/log/pods` mount와 filelog receiver
- 메트릭: Pod의 `prometheus.io/*` annotation과 `/actuator/prometheus`
- 트레이스: `TRACING_ENABLED=true`, OTLP endpoint, Tempo readiness
- Loki: `/ready`, structured metadata 설정
- Mimir: `/ready`, remote write 오류
- Tempo: `/ready`, OTLP 4317/4318 포트

### 11.7 OOMKilled

```bash
export POD_NAME="oom-killed-pod-name"

kubectl describe pod -n commerce "${POD_NAME}"
kubectl get pod -n commerce "${POD_NAME}" \
  -o jsonpath='{.status.containerStatuses[*].lastState.terminated.reason}'
kubectl top pod -n commerce "${POD_NAME}" --containers
```

임시로 limit만 올리기 전에 메모리 추세, heap 사용량, 트래픽, 최근 코드 변경을 확인한다. JVM은 컨테이너 메모리의 최대 75%를 사용하도록 구성되어 있으므로 나머지 메모리는 metaspace, thread stack, direct buffer 같은 비힙 영역에 필요하다.

## 12. 노드 유지보수

API Gateway와 User/Account/Contents Service에는 PDB와 hostname topology spread가 적용되어 있다. 운영 클러스터에서는 zone topology spread도 환경에 맞게 추가한다.

노드 유지보수:

```bash
export NODE_NAME="worker-node-name"

kubectl cordon "${NODE_NAME}"
kubectl get pod -A -o wide \
  --field-selector="spec.nodeName=${NODE_NAME}"
kubectl drain "${NODE_NAME}" \
  --ignore-daemonsets \
  --delete-emptydir-data \
  --grace-period=60 \
  --timeout=15m
```

`--delete-emptydir-data`는 해당 노드의 `emptyDir` 데이터를 삭제한다. 현재 LGTM 백엔드가 `emptyDir`를 사용하므로 데이터 손실을 감수할 수 있는 개발 환경에서만 사용한다.

유지보수 완료 후:

```bash
kubectl uncordon "${NODE_NAME}"
kubectl get nodes
kubectl get pod -A -o wide
```

## 13. 백업과 복구

### 13.1 현재 구성

- Local/Dev 회원·계좌·콘텐츠 데이터: PostgreSQL PVC, 자동 백업 없음
- Loki/Tempo/Mimir/Grafana 데이터: `emptyDir`, Pod 재생성 시 유실
- Kubernetes 선언 상태: Git 저장소가 원본

따라서 현재 구성은 재해 복구 목표를 제공하지 않는다.

### 13.2 운영 구성의 백업 대상

- PostgreSQL 전체 및 시점 복구용 WAL
- Loki/Tempo/Mimir 객체 스토리지와 설정
- Grafana 대시보드, 알림 규칙, 데이터소스 선언
- 외부 Secret Manager의 Secret과 복구 정책
- Ingress/TLS 및 운영 overlay
- 자체 관리 클러스터라면 etcd snapshot

Managed Kubernetes는 control plane과 etcd 백업 책임 범위를 공급자 문서로 확인한다. 자체 관리 클러스터는 정기 etcd snapshot과 별도 위치 보관, 실제 복구 훈련이 필요하다.

최소 복구 훈련:

1. 격리된 클러스터를 준비한다.
2. Git의 승인된 선언 상태를 배포한다.
3. DB와 객체 스토리지 백업을 복원한다.
4. Secret을 복원한다.
5. API와 관측성 스모크 테스트를 수행한다.
6. RTO와 RPO 실측값을 기록한다.

## 14. 보안 운영

인증/인가 운영:

- `common-security`가 Keycloak realm role과 client role을 Spring Security 권한으로 변환한다.
- Gateway는 OAuth2 Client 로그인과 Bearer JWT Resource Server를 동시에 지원한다.
- 업무 서비스는 Bearer JWT만 수용하는 Resource Server다.
- Local/Dev overlay는 개발용 Keycloak을 배포하지만 Staging/Production은 외부 IdP 또는 별도 Keycloak 클러스터를 사용한다.
- Staging/Production은 `commerce-auth-credentials` Secret의 `OAUTH2_GATEWAY_CLIENT_SECRET` key가 필요하다.
- 자세한 설정과 토큰 발급 절차는 [Keycloak 인증/인가 가이드](AUTHENTICATION.md)를 따른다.

정기 점검:

```bash
kubectl auth can-i --list \
  --as=system:serviceaccount:commerce:commerce-app \
  -n commerce

kubectl get role,rolebinding,serviceaccount -n commerce -o yaml
kubectl get clusterrole,clusterrolebinding | grep commerce
```

운영 원칙:

- 운영 Secret 값을 Git, 채팅, 티켓, CI 로그에 기록하지 않는다.
- Secret 조회와 변경을 감사 로그에 남긴다.
- ServiceAccount별 최소 권한을 적용한다.
- 컨테이너는 비루트, privilege escalation 금지, capability 제거를 유지한다.
- 외부와 내부 통신에 TLS를 적용한다.
- NetworkPolicy로 namespace 간 통신을 제한한다.
- 이미지 digest allowlist와 서명 검증 정책을 적용한다.
- 정기적으로 기반 이미지와 애플리케이션 의존성을 패치한다.
- 디버그 Pod는 사용 직후 삭제한다.

현재 `commerce-app` Role은 Spring Cloud Kubernetes 연동을 위해 ConfigMap, Service, Pod, EndpointSlice만 읽으며 Secret 조회 권한은 없다. 애플리케이션 Secret은 Pod의 `secretKeyRef`로만 주입한다.

## 15. 제거 절차

제거는 데이터 유실을 전제로 하므로 개발 환경에서만 다음 명령을 사용한다.

```bash
kubectl delete -k deploy/k8s/overlays/local --ignore-not-found
kubectl delete -k deploy/observability --ignore-not-found
```

운영 환경에서는 다음 순서를 따른다.

1. 신규 트래픽 유입을 차단한다.
2. 데이터와 감사 로그를 백업한다.
3. 보존 정책과 규제 요구사항을 확인한다.
4. 애플리케이션 리소스를 제거한다.
5. 관측성 수집 중단을 확인한 후 백엔드를 제거한다.
6. Secret, 인증서, Registry 권한을 폐기한다.
7. DNS와 외부 Load Balancer를 정리한다.
8. 제거 결과와 데이터 폐기 증적을 기록한다.

## 16. 운영 체크리스트

### 배포 전

- [ ] 올바른 kube context와 namespace 확인
- [ ] 테스트·빌드·이미지 스캔 성공
- [ ] 불변 이미지 digest 사용
- [ ] Kustomize 렌더링과 server dry-run 성공
- [ ] DB migration의 하위 호환성 확인
- [ ] 리소스 여유와 HPA 상태 확인
- [ ] 변경 승인과 롤백 버전 확보

### 배포 후

- [ ] 모든 rollout 성공
- [ ] Ready Pod와 EndpointSlice 정상
- [ ] API 스모크 테스트 성공
- [ ] 5xx, p95, 재시작, CPU·메모리 정상
- [ ] 로그·메트릭·트레이스 수집 정상
- [ ] Git 선언 상태와 클러스터 상태 일치

### 장애 종료 후

- [ ] 사용자 영향과 장애 시간 기록
- [ ] 임시 변경을 Git 선언 상태에 반영 또는 제거
- [ ] 근본 원인과 재발 방지 작업 등록
- [ ] 알림·대시보드·런북 개선

## 17. 공식 참고 문서

- [Kubernetes 클러스터 운영](https://kubernetes.io/docs/tasks/administer-cluster/)
- [Kubernetes 설정 모범 사례](https://kubernetes.io/docs/concepts/configuration/overview/)
- [Deployment 운영과 롤백](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [Horizontal Pod Autoscaling](https://kubernetes.io/docs/concepts/workloads/autoscaling/horizontal-pod-autoscale/)
- [Pod 중단과 PodDisruptionBudget](https://kubernetes.io/docs/concepts/workloads/pods/disruptions/)
- [Kubernetes Secret 모범 사례](https://kubernetes.io/docs/concepts/security/secrets-good-practices/)
- [etcd 운영과 백업](https://kubernetes.io/docs/tasks/administer-cluster/configure-upgrade-etcd/)
- [Grafana Loki Kubernetes 설치](https://grafana.com/docs/loki/latest/setup/install/helm/)
