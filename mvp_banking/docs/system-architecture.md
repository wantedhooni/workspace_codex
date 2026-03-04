# System Architecture

## 예상 완료 아키텍처

```mermaid
flowchart TB
    subgraph Clients["Client Channels"]
        AdminUser["Admin Operator / Auditor / Reviewer"]
        EndUser["End User / Investor / Customer"]
    end

    subgraph Frontend["Frontend Applications"]
        AdminPortal["Admin Portal\nReact + Refine"]
        UserWebApp["User Web Application\nReact"]
    end

    subgraph Edge["Edge Layer"]
        CDN["CDN / Static Hosting"]
        LB["Load Balancer / API Gateway"]
    end

    subgraph Backend["Banking Platform Backend\nJava 21 + Spring Boot"]
        subgraph API["Presentation Layer"]
            AdminAPI["Admin API\n/api/admin/**"]
            UserAPI["User API\n/api/user/**"]
            AuthAPI["Auth API\nJWT / Refresh Token"]
        end

        subgraph App["Application Layer"]
            AdminApp["Admin Application Services"]
            UserApp["User Application Services"]
            ApprovalApp["Approval Workflow"]
            AuditApp["Audit Logging"]
        end

        subgraph Domain["Domain Layer"]
            AdminDomain["Admin / RBAC"]
            CustomerDomain["Customer"]
            AccountDomain["Account / Balance"]
            ProductDomain["Product"]
            TransactionDomain["Transaction / Order / Deposit / Withdrawal"]
        end

        subgraph Infra["Infrastructure Layer"]
            JpaRepo["JPA Repositories"]
            QueryRepo["Querydsl / JSQL Query Repositories"]
            PgStore["PostgreSQL"]
            RedisStore["Redis\nRefresh Token / Cache / Session"]
            EventBus["Domain Events / Audit Events"]
            ExtAdapter["External Adapters"]
        end
    end

    subgraph Integrations["External Integration"]
        FileStore["Object Storage\nStatements / Attachments / Exports"]
        Notify["Notification Service\nEmail / SMS / Push"]
        Ops["External Ops / Banking / Brokerage Systems"]
    end

    AdminUser --> AdminPortal
    EndUser --> UserWebApp

    AdminPortal --> CDN
    UserWebApp --> CDN
    AdminPortal --> LB
    UserWebApp --> LB

    LB --> AdminAPI
    LB --> UserAPI
    LB --> AuthAPI

    AdminAPI --> AdminApp
    AdminAPI --> ApprovalApp
    AdminAPI --> AuditApp
    UserAPI --> UserApp
    UserAPI --> AuditApp
    AuthAPI --> AdminApp
    AuthAPI --> UserApp

    AdminApp --> AdminDomain
    AdminApp --> CustomerDomain
    AdminApp --> AccountDomain
    AdminApp --> TransactionDomain
    UserApp --> CustomerDomain
    UserApp --> AccountDomain
    UserApp --> ProductDomain
    UserApp --> TransactionDomain
    ApprovalApp --> TransactionDomain
    ApprovalApp --> AdminDomain
    AuditApp --> AdminDomain

    AdminDomain --> JpaRepo
    CustomerDomain --> JpaRepo
    AccountDomain --> JpaRepo
    ProductDomain --> JpaRepo
    TransactionDomain --> JpaRepo

    AdminApp --> QueryRepo
    UserApp --> QueryRepo
    ApprovalApp --> QueryRepo
    AdminApp --> RedisStore
    UserApp --> RedisStore
    AdminApp --> EventBus
    UserApp --> EventBus
    ApprovalApp --> EventBus
    AuditApp --> EventBus
    AuditApp --> ExtAdapter

    JpaRepo --> PgStore
    QueryRepo --> PgStore
    ExtAdapter --> FileStore
    ExtAdapter --> Notify
    ExtAdapter --> Ops
```

## 설명
- `admin portal`과 `user web application`은 별도 프론트엔드 앱으로 운영한다.
- 백엔드는 하나의 Spring Boot 서비스로 시작하되 `admin API`와 `user API` 경계를 분리한다.
- PostgreSQL은 계정, 계좌, 거래, 승인, 감사 로그의 주 데이터 저장소로 사용한다.
- Redis는 Refresh Token, 인증 세션성 데이터, 캐시 저장소로 사용한다.
- 관리자 기능은 RBAC, 승인 워크플로우, 감사 로그를 중심으로 설계한다.
- 사용자 기능은 본인 데이터 조회와 요청 흐름에 집중한다.
- 조회 성능이 중요한 리스트와 집계는 Querydsl / JSQL 전용 리포지토리로 분리한다.
- 외부 알림, 파일 저장, 뱅킹 / 증권 연계는 어댑터 계층에서 분리한다.

## 구현 시 참고 원칙
- 초기에는 모듈형 모놀리스로 구현하고, 이후 필요시 API 또는 모듈 단위 분리를 검토한다.
- `admin API`와 `user API`는 URL, 인증 필터, 응답 DTO, 권한 정책을 분리한다.
- PostgreSQL 트랜잭션 정합성과 Redis TTL 정책을 함께 설계한다.
- 모든 민감 관리자 액션은 Audit 이벤트로 남긴다.
- 고객 개인정보와 계좌 / 거래 식별자는 마스킹 정책을 기본 적용한다.
