# Kotlin Commerce MSA

Spring Boot 4, Kotlin, Gradle, Spring Cloud 2025.1, Spring Cloud Kubernetes, Istio 기반 커머스 MSA 예제입니다.

## 모듈

- `api-gateway`: 외부 API Gateway
- `product-service`: 상품 카탈로그
- `inventory-service`: 재고 관리
- `payment-service`: 결제 승인
- `order-service`: 주문 처리

## 로컬 빌드

```bash
./gradlew build
```

## 전체 로컬 실행

### 1. bootRun으로 실행

터미널을 5개 열고 아래 명령을 각각 실행합니다.

```bash
./gradlew :product-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :order-service:bootRun
./gradlew :api-gateway:bootRun
```

로컬 포트:

- `api-gateway`: `8080`
- `product-service`: `8081`
- `inventory-service`: `8082`
- `payment-service`: `8083`
- `order-service`: `8084`

### 2. JAR로 실행

```bash
./gradlew build
```

터미널을 5개 열고 아래 명령을 각각 실행합니다.

```bash
java -jar product-service/build/libs/product-service-0.1.0.jar
java -jar inventory-service/build/libs/inventory-service-0.1.0.jar
java -jar payment-service/build/libs/payment-service-0.1.0.jar
java -jar order-service/build/libs/order-service-0.1.0.jar
java -jar api-gateway/build/libs/api-gateway-0.1.0.jar
```

### 3. 로컬 상태 확인

```bash
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/api/products
curl http://localhost:8080/api/inventories/SKU-001
```

주문 생성:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "CUST-001",
    "items": [
      {
        "sku": "SKU-001",
        "quantity": 1,
        "unitPrice": 129000
      }
    ],
    "paymentMethod": "CARD"
  }'
```

성공 응답은 `status`가 `CREATED`이고 `paymentId`가 채워집니다.

## 테스트 방법

### 1. 전체 Cucumber BDD 테스트

```bash
./gradlew test
```

각 서비스는 Cucumber + Gherkin 기반 BDD 테스트를 포함합니다.

- `api-gateway/src/test/resources/features/api-gateway.feature`
- `product-service/src/test/resources/features/product.feature`
- `inventory-service/src/test/resources/features/inventory.feature`
- `payment-service/src/test/resources/features/payment.feature`
- `order-service/src/test/resources/features/order.feature`

Step Definitions는 각 모듈의 `src/test/kotlin` 아래에 있습니다.

### 2. 전체 빌드 테스트

```bash
./gradlew clean build
```

위 명령은 Cucumber BDD 테스트, 전체 모듈 컴파일, 리소스 처리, JAR 패키징을 함께 검증합니다.

### 3. Gateway 기준 API 테스트

전체 서비스를 실행한 뒤 아래 API를 호출합니다.

```bash
curl http://localhost:8080/api/products
curl http://localhost:8080/api/inventories/SKU-001
```

주문 성공 케이스:

```json
{
  "customerId": "CUST-001",
  "items": [
    {
      "sku": "SKU-001",
      "quantity": 1,
      "unitPrice": 129000
    }
  ],
  "paymentMethod": "CARD"
}
```

주문 실패 케이스도 확인할 수 있습니다. 결제 서비스는 단일 결제 금액이 `1,000,000`을 초과하면 거절하도록 구현되어 있습니다.

```bash
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "CUST-001",
    "items": [
      {
        "sku": "SKU-001",
        "quantity": 1,
        "unitPrice": 1000001
      }
    ],
    "paymentMethod": "CARD"
  }'
```

### 4. HTTP 파일로 테스트

IntelliJ IDEA, VS Code REST Client 등을 사용한다면 [docs/local-requests.http](docs/local-requests.http)를 실행하면 됩니다.

### 5. 컨테이너 이미지 생성 테스트

Docker 데몬 없이 tar 이미지 생성:

```bash
./gradlew jibBuildTar
```

로컬 Docker 데몬에 이미지 적재:

```bash
./gradlew jibDockerBuild
docker images 'commerce/*'
```

## Kubernetes/Istio

로컬 Kubernetes 배포는 스크립트 사용을 기본으로 합니다.

필요 도구:

- `kubectl`
- 로컬 클러스터 중 하나: Docker Desktop Kubernetes, minikube, kind
- 이미지 빌드용 Docker 데몬 또는 minikube Docker daemon

로컬 배포 흐름:

1. Jib이 각 Spring Boot 서비스를 컨테이너 이미지로 빌드한다.
2. `k8s/base/commerce.yaml`이 `commerce` 네임스페이스, ServiceAccount, RBAC, ConfigMap, Deployment, Service를 생성한다.
3. 각 Pod는 클러스터 내부에서 `8080` 포트로 실행된다.
4. `api-gateway`가 외부 진입점 역할을 하고, 내부 Service DNS로 상품/재고/결제/주문 서비스에 라우팅한다.
5. 로컬에서는 `kubectl port-forward svc/api-gateway 18080:8080`으로 Gateway에 접근한다.

### 1. 스크립트로 로컬 배포

```bash
scripts/k8s-local-deploy.sh
```

현재 `kubectl config current-context`를 기준으로 `docker-desktop`, `minikube`, `kind`를 자동 감지합니다. 명시하고 싶으면 첫 번째 인자로 전달합니다.

```bash
scripts/k8s-local-deploy.sh docker-desktop
scripts/k8s-local-deploy.sh minikube
scripts/k8s-local-deploy.sh kind
```

대상별 동작:

- `docker-desktop`: 로컬 Docker 데몬에 이미지를 빌드한다. Docker Desktop이 kind 기반 클러스터로 동작하는 환경에서는 스크립트가 `kind load docker-image --name desktop`으로 이미지를 클러스터에 로드한다.
- `minikube`: `eval $(minikube docker-env)`를 적용한 뒤 minikube 내부 Docker daemon에 이미지를 빌드한다.
- `kind`: 로컬 Docker 데몬에 이미지를 빌드한 뒤 `kind load docker-image`로 kind 클러스터에 이미지를 로드한다.

Istio CRD가 설치된 로컬 클러스터에 Istio 리소스까지 적용하려면 `--with-istio`를 붙입니다.

```bash
scripts/k8s-local-deploy.sh minikube --with-istio
```

이미지를 이미 빌드해둔 경우에는 `--skip-build`를 사용할 수 있습니다.

```bash
scripts/k8s-local-deploy.sh kind --skip-build
```

스크립트가 생성/사용하는 기본 이미지명:

- `commerce/api-gateway:0.1.0`
- `commerce/product-service:0.1.0`
- `commerce/inventory-service:0.1.0`
- `commerce/payment-service:0.1.0`
- `commerce/order-service:0.1.0`

Jib은 현재 머신 아키텍처에 맞춰 이미지를 빌드합니다. Apple Silicon에서는 기본값이 `linux/arm64`입니다. 다른 아키텍처로 빌드하려면 `IMAGE_ARCH`를 지정합니다.

```bash
IMAGE_ARCH=amd64 scripts/k8s-local-deploy.sh docker-desktop
IMAGE_ARCH=arm64 scripts/k8s-local-deploy.sh docker-desktop
```

배포 후 실행 상태 확인:

```bash
kubectl -n commerce get deploy,svc,pods
kubectl -n commerce get endpoints
```

정상 상태 기준:

- Deployment `READY`가 `2/2`로 표시된다.
- Pod 상태가 `Running`이다.
- `api-gateway` Service가 `ClusterIP`로 생성되어 있다.

### 2. 로컬에서 배포된 서비스 실행 확인

배포가 끝나면 Gateway Service를 포트포워딩하고 주요 API를 검증합니다.

```bash
scripts/k8s-local-test.sh
```

검증 항목:

- `GET /actuator/health/readiness`
- `GET /api/products`
- `GET /api/inventories/SKU-001`
- `POST /api/orders` 성공 케이스
- `POST /api/orders` 결제 실패 케이스

기본 포트는 `18080`입니다. 변경하려면 환경변수를 사용합니다.

```bash
LOCAL_PORT=18081 scripts/k8s-local-test.sh
```

수동으로 확인하고 싶다면 아래처럼 포트포워딩을 직접 실행합니다.

```bash
kubectl -n commerce port-forward svc/api-gateway 18080:8080
```

다른 터미널에서 호출합니다.

```bash
curl http://localhost:18080/actuator/health/readiness
curl http://localhost:18080/api/products
curl http://localhost:18080/api/inventories/SKU-001
```

주문 생성:

```bash
curl -X POST http://localhost:18080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "CUST-001",
    "items": [
      {
        "sku": "SKU-001",
        "quantity": 1,
        "unitPrice": 129000
      }
    ],
    "paymentMethod": "CARD"
  }'
```

### 3. 로그 확인

```bash
kubectl -n commerce logs deploy/api-gateway
kubectl -n commerce logs deploy/product-service
kubectl -n commerce logs deploy/inventory-service
kubectl -n commerce logs deploy/payment-service
kubectl -n commerce logs deploy/order-service
```

특정 Pod 문제를 볼 때:

```bash
kubectl -n commerce describe pod <pod-name>
kubectl -n commerce get events --sort-by=.metadata.creationTimestamp
```

### 4. 스크립트로 삭제

```bash
scripts/k8s-local-cleanup.sh
```

### 5. 수동 배포 명령

Jib으로 Dockerfile 없이 서비스 이미지를 빌드할 수 있습니다.

```bash
./gradlew jibDockerBuild
```

Docker Desktop Kubernetes는 로컬 Docker 이미지 저장소를 같이 사용하므로 기본 이미지명 그대로 배포할 수 있습니다.

```bash
kubectl apply -f k8s/base
kubectl -n commerce get pods
```

minikube는 minikube Docker daemon에 이미지를 빌드해야 합니다.

```bash
eval $(minikube docker-env)
./gradlew jibDockerBuild
kubectl apply -f k8s/base
kubectl -n commerce get pods
```

kind는 로컬 Docker 이미지 빌드 후 클러스터에 이미지를 로드합니다.

```bash
./gradlew jibDockerBuild
kind load docker-image commerce/api-gateway:0.1.0
kind load docker-image commerce/product-service:0.1.0
kind load docker-image commerce/inventory-service:0.1.0
kind load docker-image commerce/payment-service:0.1.0
kind load docker-image commerce/order-service:0.1.0

kubectl apply -f k8s/base
kubectl -n commerce get pods
```

Istio를 수동 적용하려면 Istio CRD가 먼저 설치되어 있어야 합니다.

```bash
kubectl apply -f istio
```

### 6. 원격 레지스트리 배포

레지스트리 prefix를 환경변수로 지정하면 Jib 이미지명이 함께 바뀝니다.

```bash
export IMAGE_REGISTRY=ghcr.io/your-org/commerce
./gradlew jib
```

이 경우 Kubernetes 매니페스트의 이미지도 같은 prefix로 변경해야 합니다.

```bash
sed -i.bak 's#commerce/#ghcr.io/your-org/commerce/#g' k8s/base/commerce.yaml
kubectl apply -f k8s/base
kubectl apply -f istio
```

### 7. 배포 상태 확인

```bash
kubectl -n commerce get deploy,svc,pods
kubectl -n commerce rollout status deploy/api-gateway
kubectl -n commerce rollout status deploy/product-service
kubectl -n commerce rollout status deploy/inventory-service
kubectl -n commerce rollout status deploy/payment-service
kubectl -n commerce rollout status deploy/order-service
```

### 8. 클러스터 내부 API 테스트

Istio Ingress 설정 전에도 포트포워딩으로 Gateway를 테스트할 수 있습니다.

```bash
kubectl -n commerce port-forward svc/api-gateway 8080:8080
curl http://localhost:8080/api/products
curl http://localhost:8080/api/inventories/SKU-001
```

Istio Ingress Gateway를 사용하는 경우 `commerce.local`을 Ingress 주소로 매핑한 뒤 호출합니다.

```bash
curl -H 'Host: commerce.local' http://<ISTIO_INGRESS_HOST>/api/products
```

### 9. 자주 보는 문제

`ImagePullBackOff`:

- Docker Desktop: Docker Desktop이 kind 기반이면 `kind get clusters`에 `desktop`이 있는지 확인한다. 스크립트는 기본으로 `desktop` 클러스터에 이미지를 로드한다.
- minikube: `eval $(minikube docker-env)` 실행 후 다시 `./gradlew jibDockerBuild`를 실행한다.
- kind: `kind load docker-image commerce/<service>:0.1.0`를 실행했는지 확인한다.

아키텍처 불일치:

- Apple Silicon 로컬 클러스터에서는 `arm64` 이미지가 필요하다.
- `docker image inspect commerce/api-gateway:0.1.0 --format '{{.Architecture}}/{{.Os}}'`로 확인한다.
- 필요하면 `IMAGE_ARCH=arm64 scripts/k8s-local-deploy.sh docker-desktop`로 다시 빌드/배포한다.

`CrashLoopBackOff`:

```bash
kubectl -n commerce logs <pod-name>
kubectl -n commerce describe pod <pod-name>
```

`port-forward` 포트 충돌:

```bash
LOCAL_PORT=18081 scripts/k8s-local-test.sh
```

Istio 리소스 적용 실패:

- 로컬 클러스터에 Istio가 설치되어 있지 않으면 `--with-istio` 없이 배포한다.
- 기본 로컬 테스트는 Istio 없이 `api-gateway` Service 포트포워딩으로 충분하다.

### 10. 삭제

```bash
scripts/k8s-local-cleanup.sh
```
