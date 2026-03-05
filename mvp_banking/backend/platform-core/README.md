# Platform Core

Shared backend module used by `admin-api` and `user-api`.

Contains:
- domain models
- shared application services (admin/user 공통 유스케이스)
- security and JWT infrastructure
- JPA, Querydsl, Redis integration
- Flyway migrations
- Flyway 기반 데모 데이터 시드(`V28__seed_demo_data.sql`)
- shared API response models
- exchange fee / net settlement and stock fee / tax / net settlement domain logic

Channel split rule:
- admin 전용 서비스/컨트롤러는 `../admin-api`에 위치
- user 전용 서비스/컨트롤러는 `../user-api`에 위치
- `platform-core`는 도메인 엔티티/리포지토리/공통 서비스/인증 인프라/공통 응답 모델만 유지

Key paths:
- `src/main/java/com/revy/mvpbanking`
- `src/main/resources/application.yml`
- `src/main/resources/db/migration`

This module is not started directly. It is consumed by:
- `../admin-api`
- `../user-api`

Build:

```bash
cd /Users/revy/workspace_codex/mvp_banking/backend
./gradlew :platform-core:build
```
